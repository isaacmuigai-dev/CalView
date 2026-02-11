package com.example.calview.core.data.repository

import android.util.Log
import com.example.calview.core.data.model.GroupDto
import com.example.calview.core.data.model.GroupMemberDto
import com.example.calview.core.data.model.GroupMessageDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class GroupsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val streakFreezeRepository: StreakFreezeRepository
) : GroupsRepository {

    private val groupsCollection = firestore.collection("groups")
    private val membersCollection = firestore.collection("group_members")
    private val TAG = "GroupsRepo"

    private var lastActionTime = 0L
    private var lastMessageTime = 0L

    override suspend fun createGroup(name: String, description: String, photoUrl: String): String {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastActionTime < 5000) return ""
        lastActionTime = currentTime

        val userId = auth.currentUser?.uid ?: return ""
        val groupId = UUID.randomUUID().toString()
        
        // 1. Generate a unique invite code with retry logic
        var inviteCode = ""
        var attempts = 0
        while (attempts < 5) {
            val potentialCode = generateInviteCode()
            val query = groupsCollection.whereEqualTo("inviteCode", potentialCode).get().await()
            if (query.isEmpty) {
                inviteCode = potentialCode
                break
            }
            attempts++
        }
        if (inviteCode.isEmpty()) inviteCode = generateInviteCode() // Fallback even if collision

        val group = GroupDto(
            id = groupId,
            name = name,
            description = description,
            photoUrl = photoUrl,
            creatorId = userId,
            inviteCode = inviteCode,
            memberCount = 1
        )

        try {
            // Fetch preferences before starting writes
            val firstName = userPreferencesRepository.groupsFirstName.first()
            val lastName = userPreferencesRepository.groupsLastName.first()
            val profilePhotoUrl = userPreferencesRepository.groupsProfilePhotoUrl.first()

            // Perform atomic creation using a Batch
            val batch = firestore.batch()
            
            // 1. Create the group document
            batch.set(groupsCollection.document(groupId), group)

            // 2. Add creator as owner
            val memberData = mapOf(
                "groupId" to groupId,
                "userId" to userId,
                "role" to "owner",
                "userName" to "$firstName $lastName".trim(),
                "userPhotoUrl" to profilePhotoUrl,
                "isOnline" to true, // Mark as online since they just created it
                "joinedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            batch.set(membersCollection.document("${groupId}_${userId}"), memberData)
            
            batch.commit().await()
            
            // Mark locally that user is in a group
            userPreferencesRepository.setGroupCreated(true)
            
            return groupId
        } catch (e: Exception) {
            Log.e(TAG, "Error creating group", e)
            throw e
        }
    }

    override suspend fun joinGroup(inviteCode: String): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastActionTime < 5000) return false
        lastActionTime = currentTime

        val userId = auth.currentUser?.uid ?: return false

        try {
            // 1. Find the group by invite code
            val querySnapshot = groupsCollection.whereEqualTo("inviteCode", inviteCode).get().await()
            if (querySnapshot.isEmpty) return false

            val groupDoc = querySnapshot.documents.first()
            val groupId = groupDoc.id

            // Fetch user info for membership
            val firstName = userPreferencesRepository.groupsFirstName.first()
            val lastName = userPreferencesRepository.groupsLastName.first()
            val profilePhotoUrl = userPreferencesRepository.groupsProfilePhotoUrl.first()

            // 2. Use a transaction to prevent duplicate memberships AND increment count
            firestore.runTransaction { transaction ->
                val memberRef = membersCollection.document("${groupId}_${userId}")
                val groupRef = groupsCollection.document(groupId)
                
                val snapshot = transaction.get(memberRef)
                
                if (!snapshot.exists()) {
                    // Use a Map to set serverTimestamp for joinedAt
                    val memberData = mapOf(
                        "groupId" to groupId,
                        "userId" to userId,
                        "role" to "member",
                        "userName" to "$firstName $lastName".trim(),
                        "userPhotoUrl" to profilePhotoUrl,
                        "isOnline" to false,
                        "joinedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                    transaction.set(memberRef, memberData)
                    
                    // Increment member count
                    transaction.update(groupRef, "memberCount", com.google.firebase.firestore.FieldValue.increment(1))
                }
            }.await()
            
            // Mark locally that user is in a group
            userPreferencesRepository.setGroupCreated(true)
            
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error joining group", e)
            return false
        }
    }

    override suspend fun leaveGroup(groupId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        try {
            // Check if user is the owner
            val memberDoc = membersCollection.document("${groupId}_${userId}").get().await()
            if (!memberDoc.exists()) return false
            
            val role = memberDoc.getString("role")
            
            if (role == "owner") {
                // Delete the entire group
                try {
                    // 1. Delete group document FIRST (while ownership rule still passes)
                    groupsCollection.document(groupId).delete().await()

                    // 2. Fetch all members to delete
                    val groupMembers = membersCollection.whereEqualTo("groupId", groupId).get().await()
                    
                    // 3. Batch delete members (Firestore limit 500 ops per batch)
                    val batches = groupMembers.documents.chunked(500)
                    for (batchDocs in batches) {
                        val batch = firestore.batch()
                        batchDocs.forEach { doc ->
                            batch.delete(doc.reference)
                        }
                        batch.commit().await()
                    }
                    
                    // 4. Keep typing status clean
                    firestore.collection("typing_status").document(groupId).delete().await()

                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting owned group $groupId", e)
                    return false
                }
            } else {
                // Member leaving: Use transaction to delete membership AND decrement count
                try {
                    firestore.runTransaction { transaction ->
                        transaction.delete(memberDoc.reference)
                        transaction.update(groupsCollection.document(groupId), "memberCount", com.google.firebase.firestore.FieldValue.increment(-1))
                    }.await()
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Error leaving group (member)", e)
                    return false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error leaving group", e)
            return false
        }
    }

    override suspend fun getGroupDetails(groupId: String): GroupDto? {
        return try {
            val doc = groupsCollection.document(groupId).get().await()
            doc.toObject(GroupDto::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting group details", e)
            null
        }
    }

    override fun observeGroupMembers(groupId: String): Flow<List<GroupMemberDto>> = callbackFlow {
        val listener = membersCollection.whereEqualTo("groupId", groupId)
            // .orderBy("joinedAt", Query.Direction.DESCENDING) // Removed to avoid composite index requirement involving joinedAt
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val members = snapshot?.toObjects(GroupMemberDto::class.java) ?: emptyList()
                trySend(members)
            }
        awaitClose { listener.remove() }
    }

    override fun observeUserGroups(): Flow<List<GroupDto>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var groupsListeners = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()

        val membershipListener = membersCollection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing memberships", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val memberships = snapshot?.toObjects(GroupMemberDto::class.java) ?: emptyList()
                if (memberships.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val groupIds = memberships.map { it.groupId }
                
                // Clear old groups listeners
                groupsListeners.forEach { it.remove() }
                groupsListeners.clear()

                // Firestore limitation: whereIn supports a maximum of 30 elements (historically 10).
                // We chunk the list to handle any number of groups robustly.
                val chunkedIds = groupIds.chunked(10)
                val allGroups = mutableListOf<GroupDto>()

                chunkedIds.forEach { chunk ->
                    val listener = firestore.collection("groups")
                        .whereIn("id", chunk)
                        .addSnapshotListener { groupSnapshot, groupError ->
                            if (groupError != null) {
                                Log.e(TAG, "Error observing group chunk", groupError)
                                return@addSnapshotListener
                            }
                            
                            val chunkGroups = groupSnapshot?.toObjects(GroupDto::class.java) ?: emptyList()
                            
                            synchronized(allGroups) {
                                // Update/Add these groups
                                chunkGroups.forEach { cg ->
                                    val index = allGroups.indexOfFirst { it.id == cg.id }
                                    if (index != -1) allGroups[index] = cg else allGroups.add(cg)
                                }
                                
                                // Final filter: Only include groups the user is currently a member of
                                val filteredGroups = allGroups.filter { it.id in groupIds }
                                trySend(filteredGroups.sortedBy { it.name })
                            }
                        }
                    groupsListeners.add(listener)
                }
            }

        awaitClose {
            membershipListener.remove()
            groupsListeners.forEach { it.remove() }
        }
    }

    override suspend fun restoreUserGroups(): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        try {
            val memberships = membersCollection.whereEqualTo("userId", userId).get().await()
            if (!memberships.isEmpty) {
                userPreferencesRepository.setGroupCreated(true)
                return true
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring user groups", e)
            return false
        }
    }

    override suspend fun sendMessage(
        groupId: String, 
        text: String, 
        imageUrl: String?,
        replyToId: String?
    ): String? {
        // Rate Limiting: Prevent spam (1 second cooldown)
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastMessageTime < 1000) {
            return null
        }
        lastMessageTime = currentTime

        if (!verifyMembership(groupId)) return null
        val userId = auth.currentUser?.uid ?: return null
        val messageId = UUID.randomUUID().toString()

        try {
            val firstName = userPreferencesRepository.groupsFirstName.first()
            val lastName = userPreferencesRepository.groupsLastName.first()
            val profilePhotoUrl = userPreferencesRepository.groupsProfilePhotoUrl.first()

            var replyToText: String? = null
            var replyToSenderName: String? = null

            // 1. Fetch reply context if needed (to embed for instant rendering)
            if (replyToId != null) {
                val originalMsgDoc = firestore.collection("group_messages").document(replyToId).get().await()
                if (originalMsgDoc.exists()) {
                    replyToText = originalMsgDoc.getString("text") ?: ""
                    replyToSenderName = originalMsgDoc.getString("senderName") ?: "Unknown"
                } else {
                    Log.w(TAG, "Reply target message $replyToId no longer exists")
                    // If the message was deleted, we still send the reply but without context
                    // Or we could return null to signal error. For now, we continue without context.
                }
            }

            // 2. Construct message data as a Map for server timestamp support
            val messageData = mutableMapOf<String, Any?>(
                "id" to messageId,
                "groupId" to groupId,
                "senderId" to userId,
                "senderName" to "$firstName $lastName".trim(),
                "senderPhotoUrl" to profilePhotoUrl,
                "text" to text,
                "imageUrl" to imageUrl,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "replyToId" to replyToId,
                "replyToText" to replyToText,
                "replyToSenderName" to replyToSenderName,
                "likeCount" to 0
            )

            firestore.collection("group_messages").document(messageId).set(messageData).await()
            return messageId
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            return null
        }
    }

    override suspend fun toggleLikeMessage(groupId: String, messageId: String): Boolean {
        if (!verifyMembership(groupId)) return false
        val userId = auth.currentUser?.uid ?: return false
        val messageRef = firestore.collection("group_messages").document(messageId)
        val likeRef = messageRef.collection("likes").document(userId)

        return try {
            firestore.runTransaction { transaction ->
                val likeDoc = transaction.get(likeRef)
                if (likeDoc.exists()) {
                    // Unlike: remove doc and decrement count
                    transaction.delete(likeRef)
                    transaction.update(messageRef, "likeCount", com.google.firebase.firestore.FieldValue.increment(-1))
                } else {
                    // Like: add doc and increment count
                    transaction.set(likeRef, mapOf(
                        "userId" to userId,
                        "groupId" to groupId,
                        "likedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    ))
                    transaction.update(messageRef, "likeCount", com.google.firebase.firestore.FieldValue.increment(1))
                }
            }.await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like", e)
            false
        }
    }

    override fun observeIsMessageLiked(messageId: String): Flow<Boolean> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(false)
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("group_messages")
            .document(messageId)
            .collection("likes")
            .document(userId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot != null && snapshot.exists())
            }
        awaitClose { listener.remove() }
    }

    override fun observeUserLikes(groupId: String): Flow<Set<String>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(emptySet())
            close()
            return@callbackFlow
        }

        // Use Collection Group query for hyperscale "my likes" discovery
        // Use Collection Group query for hyperscale "my likes" discovery
        // Note: We removed .whereEqualTo("groupId", groupId) to avoid requiring a composite index
        // on (userId + groupId) for collection group queries. We filter by groupId client-side instead.
        // This relies on the single-field index for 'userId' on 'likes' collection group, which is easier to support.
        val listener = firestore.collectionGroup("likes")
            .whereEqualTo("userId", userId)
            // .whereEqualTo("groupId", groupId) // Removed to reduce index complexity and potential permission issues
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing user likes group", error)
                    trySend(emptySet())
                    return@addSnapshotListener
                }
                
                // Client-side filter: Only include likes where the 'groupId' field matches the current group
                val likedMessageIds = snapshot?.documents?.mapNotNull { doc ->
                    // The like document itself contains "groupId" (set in toggleLikeMessage)
                    if (doc.getString("groupId") == groupId) {
                         // The parent's parent is the message document
                         doc.reference.parent.parent?.id
                    } else null
                }?.toSet() ?: emptySet()
                    
                trySend(likedMessageIds)
            }
        awaitClose { listener.remove() }
    }

    override fun observeGroupMessages(groupId: String, limit: Int): Flow<List<GroupMessageDto>> = callbackFlow {
        val listener = firestore.collection("group_messages")
            .whereEqualTo("groupId", groupId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing group messages", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(GroupMessageDto::class.java) ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getOlderMessages(groupId: String, beforeTimestamp: Date, limit: Int): List<GroupMessageDto> {
        return try {
            firestore.collection("group_messages")
                .whereEqualTo("groupId", groupId)
                .whereLessThan("timestamp", beforeTimestamp)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
                .toObjects(GroupMessageDto::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching older messages", e)
            emptyList()
        }
    }

    override suspend fun getGroupDetailsByInviteCode(inviteCode: String): GroupDto? {
        return try {
            val querySnapshot = groupsCollection.whereEqualTo("inviteCode", inviteCode).get().await()
            if (querySnapshot.isEmpty) null else querySnapshot.documents.first().toObject(GroupDto::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting group details by invite code", e)
            null
        }
    }

    override suspend fun syncProfileToGroups(): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        try {
            val firstName = userPreferencesRepository.groupsFirstName.first()
            val lastName = userPreferencesRepository.groupsLastName.first()
            val profilePhotoUrl = userPreferencesRepository.groupsProfilePhotoUrl.first()
            val fullName = "$firstName $lastName".trim()

            // Fetch current streak
            val currentMeals = firestore.collection("meals")
                .whereEqualTo("userId", userId)
                .whereEqualTo("analysisStatus", "COMPLETED")
                .get().await()
                .toObjects(com.example.calview.core.data.local.MealEntity::class.java)
            
            val mealDates = currentMeals.map { 
                java.time.Instant.ofEpochMilli(it.timestamp)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate() 
            }.distinct()
            
            val streak = streakFreezeRepository.getStreakData(mealDates).first()

            // 1. Find all memberships for this user
            val memberships = membersCollection.whereEqualTo("userId", userId).get().await()
            if (memberships.isEmpty) return true

            // 2. Update each membership in recursive chunks (Firestore batch limit is 500)
            val docs = memberships.documents
            val chunks = docs.chunked(500)
            
            for (chunk in chunks) {
                val batch = firestore.batch()
                for (doc in chunk) {
                    batch.update(doc.reference, mapOf(
                        "userName" to fullName,
                        "userPhotoUrl" to profilePhotoUrl,
                        "streak" to streak
                    ))
                }
                batch.commit().await()
            }
            
            Log.d(TAG, "Successfully synced profile to ${docs.size} memberships")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing profile to groups", e)
            return false
        }
    }

    override suspend fun updatePresence(groupId: String, isOnline: Boolean) {
        if (!verifyMembership(groupId)) return
        val userId = auth.currentUser?.uid ?: return
        try {
            membersCollection.document("${groupId}_${userId}")
                .update("isOnline", isOnline)
                .await()
        } catch (e: Exception) {
            // Silently fail for presence to avoid UX interruption
            Log.w(TAG, "Failed to update presence", e)
        }
    }

    override suspend fun setTypingStatus(groupId: String, isTyping: Boolean) {
        if (!verifyMembership(groupId)) return
        val userId = auth.currentUser?.uid ?: return
        try {
            val typingRef = firestore.collection("typing_status").document(groupId)
            if (isTyping) {
                // Update map with server timestamp
                typingRef.set(
                    mapOf("typingMap" to mapOf(userId to com.google.firebase.firestore.FieldValue.serverTimestamp())),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()
            } else {
                // Remove userId from the map
                typingRef.update("typingMap.$userId", com.google.firebase.firestore.FieldValue.delete()).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed typing status update", e)
        }
    }

    override fun observeTypingStatus(groupId: String): Flow<List<String>> = callbackFlow {
        val userId = auth.currentUser?.uid
        val listener = firestore.collection("typing_status").document(groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val typingMap = snapshot?.get("typingMap") as? Map<String, com.google.firebase.Timestamp> ?: emptyMap()
                val now = System.currentTimeMillis()
                
                // Filter users who updated their typing status in the last 15 seconds
                val activeTypingUsers = typingMap.filter { (id, timestamp) ->
                    id != userId && (now - timestamp.toDate().time) < 15000
                }.keys.toList()
                
                trySend(activeTypingUsers)
            }
        awaitClose { listener.remove() }
    }

    // ===== Notification Support Methods =====
    
    override suspend fun getRecentMessagesForNotification(sinceTimestamp: Long): List<GroupMessageDto> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        
        return try {
            // 1. Get user's group IDs
            val memberships = membersCollection.whereEqualTo("userId", userId).get().await()
            val groupIds = memberships.documents.mapNotNull { it.getString("groupId") }
            
            if (groupIds.isEmpty()) return emptyList()
            
            // Convert Long timestamp to Date for Firestore comparison
            val sinceDate = java.util.Date(sinceTimestamp)
            
            // 2. Query messages in those groups since timestamp, exclude own messages
            val allMessages = mutableListOf<GroupMessageDto>()
            
            // Firestore whereIn supports max 30 elements, chunk if needed
            // Note: Removed orderBy to avoid composite index requirement
            groupIds.chunked(10).forEach { chunk ->
                val messages = firestore.collection("group_messages")
                    .whereIn("groupId", chunk)
                    .whereGreaterThan("timestamp", sinceDate)
                    .orderBy("timestamp", Query.Direction.DESCENDING) // Added orderBy
                    .limit(50)
                    .get()
                    .await()
                    .toObjects(GroupMessageDto::class.java)
                    .filter { it.senderId != userId } // Exclude own messages
                
                allMessages.addAll(messages)
            }
            
            // Sort in Kotlin since we removed Firestore orderBy
            allMessages.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching recent messages for notification", e)
            emptyList()
        }
    }
    
    override suspend fun getRecentLikesOnUserMessages(sinceTimestamp: Long): List<Pair<String, String>> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        
        return try {
            // Convert Long timestamp to Date for Firestore comparison
            val sinceDate = java.util.Date(sinceTimestamp)
            
            // Query likes collection group for recent likes on user's messages
            val likes = firestore.collectionGroup("likes")
                .whereGreaterThan("likedAt", sinceDate)
                .get()
                .await()
            
            val results = mutableListOf<Pair<String, String>>()
            
            for (likeDoc in likes.documents) {
                val likerId = likeDoc.getString("userId") ?: continue
                if (likerId == userId) continue // Skip own likes
                
                // Get the parent message to check if it belongs to us
                val messageRef = likeDoc.reference.parent.parent ?: continue
                val messageDoc = messageRef.get().await()
                
                if (messageDoc.getString("senderId") == userId) {
                    val messageText = messageDoc.getString("text")?.take(30) ?: "your message"
                    
                    // Get liker's name from their membership
                    val groupId = messageDoc.getString("groupId") ?: continue
                    val likerMembership = membersCollection
                        .whereEqualTo("groupId", groupId)
                        .whereEqualTo("userId", likerId)
                        .get()
                        .await()
                        .documents
                        .firstOrNull()
                    
                    val likerName = likerMembership?.getString("userName") ?: "Someone"
                    results.add(Pair(messageText, likerName))
                }
            }
            
            results
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching recent likes for notification", e)
            emptyList()
        }
    }
    
    override suspend fun getRecentMemberJoins(sinceTimestamp: Long): List<Pair<String, String>> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        
        return try {
            // 1. Get user's group IDs
            val memberships = membersCollection.whereEqualTo("userId", userId).get().await()
            val groupIds = memberships.documents.mapNotNull { it.getString("groupId") }
            
            if (groupIds.isEmpty()) return emptyList()
            
            // Convert Long timestamp to Date for Firestore comparison
            val sinceDate = java.util.Date(sinceTimestamp)
            
            val results = mutableListOf<Pair<String, String>>()
            
            // 2. For each group, check for new members since timestamp
            groupIds.forEach { groupId ->
                val newMembers = membersCollection
                    .whereEqualTo("groupId", groupId)
                    .whereGreaterThan("joinedAt", sinceDate)
                    .get()
                    .await()
                    .documents
                    .filter { it.getString("userId") != userId } // Exclude self
                
                if (newMembers.isNotEmpty()) {
                    // Get group name
                    val groupDoc = groupsCollection.document(groupId).get().await()
                    val groupName = groupDoc.getString("name") ?: "your group"
                    
                    newMembers.forEach { memberDoc ->
                        val memberName = memberDoc.getString("userName") ?: "Someone"
                        results.add(Pair(groupName, memberName))
                    }
                }
            }
            
            results
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching recent member joins for notification", e)
            emptyList()
        }
    }
    
    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    private suspend fun verifyMembership(groupId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return try {
            membersCollection.document("${groupId}_${userId}").get().await().exists()
        } catch (e: Exception) {
            false
        }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    override suspend fun removeUserFromAllGroups(): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        try {
            // 1. Find all memberships
            val memberships = membersCollection.whereEqualTo("userId", userId).get().await()
            if (memberships.isEmpty) return true

            // 2. Process each membership
            for (doc in memberships) {
                val groupId = doc.getString("groupId") ?: continue
                val role = doc.getString("role")

                if (role == "owner") {
                    // Delete the entire group (Atomic Batch)
                    try {
                        // 1. Delete group document FIRST
                        groupsCollection.document(groupId).delete().await()
                        
                        // 2. Delete all members
                        val groupMembers = membersCollection.whereEqualTo("groupId", groupId).get().await()
                        val batches = groupMembers.documents.chunked(500)
                        for (batchDocs in batches) {
                            val batch = firestore.batch()
                            batchDocs.forEach { mDoc -> batch.delete(mDoc.reference) }
                            batch.commit().await()
                        }
                        
                        firestore.collection("typing_status").document(groupId).delete().await()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during global group cleanup $groupId", e)
                    }
                } else {
                    // Just leave the group (Atomic Transaction with decrement)
                    try {
                        firestore.runTransaction { transaction ->
                            transaction.delete(doc.reference)
                            transaction.update(groupsCollection.document(groupId), "memberCount", com.google.firebase.firestore.FieldValue.increment(-1))
                        }.await()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during global member cleanup for group $groupId", e)
                    }
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing user from all groups", e)
            return false
        }
    }

    override suspend fun updateGroup(groupId: String, name: String, description: String, photoUrl: String): Boolean {
        return try {
            groupsCollection.document(groupId).update(mapOf(
                "name" to name,
                "description" to description,
                "photoUrl" to photoUrl
            )).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating group $groupId", e)
            false
        }
    }

    override suspend fun clearLocalData() {
        userPreferencesRepository.setGroupCreated(false)
        userPreferencesRepository.setGroupsFirstName("")
        userPreferencesRepository.setGroupsLastName("")
        userPreferencesRepository.setGroupsProfilePhotoUrl("")
        userPreferencesRepository.setGroupsProfileComplete(false)
    }
}
