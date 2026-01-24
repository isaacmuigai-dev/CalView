package com.example.calview.core.data.repository

import com.example.calview.core.data.local.GamificationDao
import com.example.calview.core.data.local.ChallengeEntity
import com.example.calview.core.data.local.ChallengeType
import com.example.calview.core.data.local.BadgeEntity
import com.example.calview.core.data.local.BadgeTier
import com.example.calview.core.data.local.MealDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Calendar
import java.util.UUID

@Singleton
class GamificationRepository @Inject constructor(
    private val gamificationDao: GamificationDao,
    private val mealDao: MealDao
) {
    val activeChallenges: Flow<List<ChallengeEntity>> = gamificationDao.getActiveChallenges()
    val completedChallenges: Flow<List<ChallengeEntity>> = gamificationDao.getCompletedChallenges()
    val unlockedBadges: Flow<List<BadgeEntity>> = gamificationDao.getAllBadges()

    /**
     * initializes challenges if none exist.
     * Should be called on app start or dashboard load.
     */
    suspend fun initializeChallenges() {
        val currentChallenges = activeChallenges.first()
        if (currentChallenges.isEmpty()) {
            // Create default weekly challenges
            val now = System.currentTimeMillis()
            val oneWeekMs = 7 * 24 * 60 * 60 * 1000L
            
            val challenges = listOf(
                ChallengeEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Consistency King",
                    description = "Log at least 3 meals for 5 days this week.",
                    type = ChallengeType.LOG_MEALS,
                    targetValue = 15, // 3 meals * 5 days = 15 meals (simplified logic)
                    currentProgress = 0,
                    startDate = now,
                    endDate = now + oneWeekMs,
                    badgeRewardId = "consistency_badge"
                ),
                ChallengeEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Protein Power",
                    description = "Hit 100g of protein 3 times this week.",
                    type = ChallengeType.HIT_PROTEIN,
                    targetValue = 3,
                    currentProgress = 0,
                    startDate = now,
                    endDate = now + oneWeekMs,
                    badgeRewardId = "protein_badge"
                ),
                ChallengeEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Early Bird",
                    description = "Log a meal before 9 AM for 5 days.",
                    type = ChallengeType.EARLY_BIRD,
                    targetValue = 5,
                    currentProgress = 0,
                    startDate = now,
                    endDate = now + oneWeekMs,
                    badgeRewardId = "early_bird_badge"
                )
            )
            gamificationDao.insertChallenges(challenges)
        }
    }

    /**
     * Updates progress for challenges based on logs.
     * This is a simplified check. Real app would have more complex query logic.
     */
    suspend fun checkChallengeProgress() {
        val challenges = activeChallenges.first()
        
        challenges.forEach { challenge ->
            if (challenge.type == ChallengeType.LOG_MEALS) {
                // Simplified: Just counting total meals in the period
                // Ideally we'd group by day, but for MVP this is fine or we improve query.
                val meals = mealDao.getAllMeals().first()
                val validMeals = meals.count { it.timestamp >= challenge.startDate && it.timestamp <= challenge.endDate }
                
                if (validMeals != challenge.currentProgress) {
                    val updatedChallenge = challenge.copy(
                        currentProgress = validMeals,
                        isCompleted = validMeals >= challenge.targetValue
                    )
                    gamificationDao.updateChallenge(updatedChallenge)
                    
                    if (updatedChallenge.isCompleted && !challenge.isCompleted) {
                         awardBadge(challenge.badgeRewardId)
                    }
                }
            } else if (challenge.type == ChallengeType.HIT_PROTEIN) {
                // Check distinct days with > 100g protein
                val meals = mealDao.getAllMeals().first()
                val currentWeekMeals = meals.filter { it.timestamp >= challenge.startDate && it.timestamp <= challenge.endDate }
                
                val daysWithHighProtein = currentWeekMeals
                    .groupBy { 
                        // Group by day using calendar or simplified division
                        it.timestamp / (24 * 60 * 60 * 1000)
                    }
                    .count { (_, dayMeals) ->
                        dayMeals.sumOf { it.protein.toDouble() } >= 100.0
                    }
                
                 if (daysWithHighProtein != challenge.currentProgress) {
                    val updatedChallenge = challenge.copy(
                        currentProgress = daysWithHighProtein,
                        isCompleted = daysWithHighProtein >= challenge.targetValue
                    )
                    gamificationDao.updateChallenge(updatedChallenge)
                    if (updatedChallenge.isCompleted && !challenge.isCompleted) {
                         awardBadge(challenge.badgeRewardId)
                    }
                 }
            } else if (challenge.type == ChallengeType.EARLY_BIRD) {
                // Check meals before 9 AM
                 val meals = mealDao.getAllMeals().first()
                 val currentWeekMeals = meals.filter { it.timestamp >= challenge.startDate && it.timestamp <= challenge.endDate }
                 
                 val earlyBirdDays = currentWeekMeals
                    .groupBy { it.timestamp / (24 * 60 * 60 * 1000) }
                    .count { (_, dayMeals) ->
                        dayMeals.any { 
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = it.timestamp
                            calendar.get(Calendar.HOUR_OF_DAY) < 9
                        }
                    }
                   
                 if (earlyBirdDays != challenge.currentProgress) {
                    val updatedChallenge = challenge.copy(
                        currentProgress = earlyBirdDays,
                        isCompleted = earlyBirdDays >= challenge.targetValue
                    )
                    gamificationDao.updateChallenge(updatedChallenge)
                    if (updatedChallenge.isCompleted && !challenge.isCompleted) {
                         awardBadge(challenge.badgeRewardId)
                    }
                 }
            }
        }
    }

    
    private suspend fun awardBadge(badgeId: String?) {
        if (badgeId == null) return
        
        // Define badges map or fetch from somewhere
        val badge = when(badgeId) {
            "consistency_badge" -> BadgeEntity(
                id = UUID.randomUUID().toString(),
                name = "Consistency King",
                description = "Logged meals consistently for a week",
                iconResName = "ic_badge_consistency",
                dateUnlocked = System.currentTimeMillis(),
                tier = BadgeTier.GOLD
            )
            "protein_badge" -> BadgeEntity(
                id = UUID.randomUUID().toString(),
                name = "Protein Master",
                description = "Hit protein goals multiple times",
                iconResName = "ic_badge_protein",
                dateUnlocked = System.currentTimeMillis(),
                tier = BadgeTier.SILVER
            )
            "early_bird_badge" -> BadgeEntity(
                id = UUID.randomUUID().toString(),
                name = "Early Bird",
                description = "Consistent morning logger",
                iconResName = "ic_badge_morning",
                dateUnlocked = System.currentTimeMillis(),
                tier = BadgeTier.BRONZE
            )
            else -> null
        }
        
        badge?.let { gamificationDao.insertBadge(it) }
    }

    suspend fun clearAllData() {
        gamificationDao.clearAllChallenges()
        gamificationDao.clearAllBadges()
    }
}
