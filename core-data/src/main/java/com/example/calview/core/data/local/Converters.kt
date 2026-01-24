package com.example.calview.core.data.local

import androidx.room.*

import androidx.room.TypeConverter

/**
 * Type converters for Room to handle complex types like enums.
 */
class Converters {
    
    @TypeConverter
    fun fromAnalysisStatus(status: AnalysisStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toAnalysisStatus(status: String): AnalysisStatus {
        return try {
            AnalysisStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            AnalysisStatus.COMPLETED // Default fallback
        }
    }

    @TypeConverter
    fun fromSocialChallengeType(type: SocialChallengeType): String {
        return type.name
    }

    @TypeConverter
    fun toSocialChallengeType(type: String): SocialChallengeType {
        return try {
            SocialChallengeType.valueOf(type)
        } catch (e: Exception) {
            SocialChallengeType.LOGGING
        }
    }
}
