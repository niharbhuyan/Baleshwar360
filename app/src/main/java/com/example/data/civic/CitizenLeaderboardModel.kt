package com.example.data.civic

/**
 * Tier levels recognizing citizen contributions based on reports submitted and resolved.
 */
enum class CivicTier(
    val titleEn: String,
    val titleOd: String,
    val emoji: String,
    val badgeColorHex: Long,
    val minPoints: Int,
    val minResolved: Int
) {
    BRONZE_SCOUT(
        titleEn = "Civic Scout",
        titleOd = "ନାଗରିକ ସ୍କାଉଟ୍",
        emoji = "🔍",
        badgeColorHex = 0xFFCD7F32,
        minPoints = 0,
        minResolved = 0
    ),
    SILVER_PATROL(
        titleEn = "Municipal Patrol",
        titleOd = "ପୌର ରକ୍ଷକ",
        emoji = "🛡️",
        badgeColorHex = 0xFF94A3B8,
        minPoints = 150,
        minResolved = 2
    ),
    GOLD_GUARDIAN(
        titleEn = "Balasore Guardian",
        titleOd = "ବାଲେଶ୍ୱର ସୁରକ୍ଷାକାରୀ",
        emoji = "🌟",
        badgeColorHex = 0xFFF59E0B,
        minPoints = 350,
        minResolved = 5
    ),
    PLATINUM_CHAMPION(
        titleEn = "Apex Civic Champion",
        titleOd = "ଶ୍ରେଷ୍ଠ ନାଗରିକ ଚାମ୍ପିଅନ୍",
        emoji = "👑",
        badgeColorHex = 0xFF0284C7,
        minPoints = 700,
        minResolved = 10
    );

    companion object {
        fun computeTier(points: Int, resolvedCount: Int): CivicTier {
            return when {
                points >= PLATINUM_CHAMPION.minPoints && resolvedCount >= PLATINUM_CHAMPION.minResolved -> PLATINUM_CHAMPION
                points >= GOLD_GUARDIAN.minPoints && resolvedCount >= GOLD_GUARDIAN.minResolved -> GOLD_GUARDIAN
                points >= SILVER_PATROL.minPoints && resolvedCount >= SILVER_PATROL.minResolved -> SILVER_PATROL
                else -> BRONZE_SCOUT
            }
        }
    }
}

/**
 * Represents a community contributor on the Citizen Civic Eye leaderboard.
 */
data class CivicContributor(
    val userId: String,
    val displayName: String,
    val odiaDisplayName: String,
    val wardName: String,
    val odiaWardName: String,
    val avatarEmoji: String,
    val reportsSubmitted: Int,
    val reportsResolved: Int,
    val reportsInProgress: Int,
    val upvotesReceived: Int,
    val civicPoints: Int,
    val tier: CivicTier,
    val rank: Int = 0,
    val isCurrentUser: Boolean = false,
    val specialtyCategory: String = "General Infrastructure",
    val recentAchievement: String = "Active Reporter"
)

/**
 * Recognition achievement badges for specific civic milestones.
 */
data class CivicAchievementBadge(
    val id: String,
    val title: String,
    val odiaTitle: String,
    val description: String,
    val odiaDescription: String,
    val iconEmoji: String,
    val isUnlocked: Boolean,
    val unlockedDate: String? = null,
    val progress: Float = 0f,
    val progressLabel: String = ""
)

/**
 * Overall municipal impact and resolution statistics for Balasore.
 */
data class MunicipalCivicImpactStats(
    val totalReportsLogged: Int,
    val totalReportsResolved: Int,
    val totalInProgress: Int,
    val resolutionRatePercentage: Int,
    val averageTurnaroundDays: Double,
    val potholesRepaired: Int,
    val streetlightsRestored: Int,
    val drainsDesilted: Int,
    val garbageDumpsCleared: Int,
    val waterPipelinesFixed: Int
)

enum class LeaderboardTimePeriod(val labelEn: String, val labelOd: String) {
    ALL_TIME("All-Time", "ସବୁ ସମୟ"),
    THIS_MONTH("This Month", "ଚଳିତ ମାସ"),
    THIS_WEEK("This Week", "ଏହି ସପ୍ତାହ")
}
