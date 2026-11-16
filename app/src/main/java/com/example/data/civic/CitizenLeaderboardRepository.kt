package com.example.data.civic

import android.content.Context
import com.example.data.firebase.FirestoreService
import com.example.data.local.CivicReportEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Repository calculating dynamic leaderboard standings, civic recognition tiers,
 * and municipal resolution statistics for the Balasore Citizen Civic Eye program.
 */
class CitizenLeaderboardRepository(private val context: Context) {

    private val firestoreService = FirestoreService(context)

    companion object {
        private val BASE_COMMUNITY_CONTRIBUTORS = listOf(
            CivicContributor(
                userId = "citizen_bls_001",
                displayName = "Soumya Ranjan Mohanty",
                odiaDisplayName = "ସୌମ୍ୟରଞ୍ଜନ ମହାନ୍ତି",
                wardName = "Ward 14 - OT Road",
                odiaWardName = "ୱାର୍ଡ ୧୪ - ଓଟି ରୋଡ୍",
                avatarEmoji = "👨‍💼",
                reportsSubmitted = 24,
                reportsResolved = 19,
                reportsInProgress = 4,
                upvotesReceived = 142,
                civicPoints = 2020, // 24*15 + 19*50 + 142*5 + 24*10
                tier = CivicTier.PLATINUM_CHAMPION,
                specialtyCategory = "Road Potholes & Traffic Safety",
                recentAchievement = "Resolved 5 critical OT Road craters"
            ),
            CivicContributor(
                userId = "citizen_bls_002",
                displayName = "Priyanka Priyadarshini Das",
                odiaDisplayName = "ପ୍ରିୟଙ୍କା ପ୍ରିୟଦର୍ଶିନୀ ଦାସ",
                wardName = "Ward 4 - Sahadevkhunta",
                odiaWardName = "ୱାର୍ଡ ୪ - ସହଦେବଖୁଣ୍ଟା",
                avatarEmoji = "👩‍⚕️",
                reportsSubmitted = 18,
                reportsResolved = 14,
                reportsInProgress = 3,
                upvotesReceived = 98,
                civicPoints = 1460,
                tier = CivicTier.PLATINUM_CHAMPION,
                specialtyCategory = "Sanitation & Waste Management",
                recentAchievement = "Organized Nuabazar Dumper Cleansing"
            ),
            CivicContributor(
                userId = "citizen_bls_003",
                displayName = "Debashis Rout",
                odiaDisplayName = "ଦେବାଶିଷ ରାଉତ",
                wardName = "Ward 18 - Station Square",
                odiaWardName = "ୱାର୍ଡ ୧୮ - ଷ୍ଟେସନ ଛକ",
                avatarEmoji = "👨‍🏫",
                reportsSubmitted = 15,
                reportsResolved = 11,
                reportsInProgress = 3,
                upvotesReceived = 84,
                civicPoints = 1195,
                tier = CivicTier.PLATINUM_CHAMPION,
                specialtyCategory = "Drainage & Stormwater Flow",
                recentAchievement = "Prevented monsoon station waterlogging"
            ),
            CivicContributor(
                userId = "citizen_bls_004",
                displayName = "Sunita Behera",
                odiaDisplayName = "ସୁନୀତା ବେହେରା",
                wardName = "Ward 7 - Motiganj",
                odiaWardName = "ୱାର୍ଡ ୭ - ମୋତିଗଞ୍ଜ",
                avatarEmoji = "👩‍🎓",
                reportsSubmitted = 12,
                reportsResolved = 8,
                reportsInProgress = 3,
                upvotesReceived = 56,
                civicPoints = 860,
                tier = CivicTier.GOLD_GUARDIAN,
                specialtyCategory = "Faulty Streetlights & Safety",
                recentAchievement = "12 Solar LED streetlights restored"
            ),
            CivicContributor(
                userId = "citizen_bls_005",
                displayName = "Subrat Kumar Nayak",
                odiaDisplayName = "ସୁବ୍ରତ କୁମାର ନାୟକ",
                wardName = "Ward 1 - Azimabad",
                odiaWardName = "ୱାର୍ଡ ୧ - ଆଜିମାବାଦ",
                avatarEmoji = "👨‍💻",
                reportsSubmitted = 9,
                reportsResolved = 6,
                reportsInProgress = 2,
                upvotesReceived = 45,
                civicPoints = 660,
                tier = CivicTier.GOLD_GUARDIAN,
                specialtyCategory = "Drinking Water Pipeline Leaks",
                recentAchievement = "Fixed WATCO main pipe burst"
            ),
            CivicContributor(
                userId = "citizen_bls_006",
                displayName = "Alok Patnaik",
                odiaDisplayName = "ଆଲୋକ ପଟ୍ଟନାୟକ",
                wardName = "Ward 12 - Cinema Chhak",
                odiaWardName = "ୱାର୍ଡ ୧୨ - ସିନେମା ଛକ",
                avatarEmoji = "👨‍🔧",
                reportsSubmitted = 7,
                reportsResolved = 4,
                reportsInProgress = 2,
                upvotesReceived = 38,
                civicPoints = 495,
                tier = CivicTier.GOLD_GUARDIAN,
                specialtyCategory = "Encroachment & Pavement Safety",
                recentAchievement = "Clear pedestrian passage on Cinema road"
            ),
            CivicContributor(
                userId = "citizen_bls_007",
                displayName = "Dr. Manas Ranjan Jena",
                odiaDisplayName = "ଡଃ ମାନସ ରଞ୍ଜନ ଜେନା",
                wardName = "Remuna NAC (Gopinath)",
                odiaWardName = "ରେମୁଣା ଏନଏସି",
                avatarEmoji = "👨‍🔬",
                reportsSubmitted = 5,
                reportsResolved = 3,
                reportsInProgress = 1,
                upvotesReceived = 29,
                civicPoints = 370,
                tier = CivicTier.GOLD_GUARDIAN,
                specialtyCategory = "Pilgrim Route Cleanliness",
                recentAchievement = "Bio-toilet & dustbin setup at temple"
            ),
            CivicContributor(
                userId = "citizen_bls_008",
                displayName = "Lipsa Ray",
                odiaDisplayName = "ଲିପ୍ସା ରାୟ",
                wardName = "Ward 22 - Gopalgaon",
                odiaWardName = "ୱାର୍ଡ ୨୨ - ଗୋପାଳଗାଁ",
                avatarEmoji = "👩‍🎨",
                reportsSubmitted = 4,
                reportsResolved = 2,
                reportsInProgress = 1,
                upvotesReceived = 18,
                civicPoints = 250,
                tier = CivicTier.SILVER_PATROL,
                specialtyCategory = "Park Playground Maintenance",
                recentAchievement = "Restored swing park at Gopalgaon"
            ),
            CivicContributor(
                userId = "citizen_bls_009",
                displayName = "Bikash Barik",
                odiaDisplayName = "ବିକାଶ ବାରିକ",
                wardName = "Soro Municipality",
                odiaWardName = "ସୋର ପୌରପାଳିକା",
                avatarEmoji = "👷",
                reportsSubmitted = 3,
                reportsResolved = 1,
                reportsInProgress = 1,
                upvotesReceived = 12,
                civicPoints = 155,
                tier = CivicTier.SILVER_PATROL,
                specialtyCategory = "Canal Embankment Vigilance",
                recentAchievement = "Prompt notice on Soro canal breach"
            )
        )
    }

    /**
     * Calculates the user's civic contribution metrics based on their local Room database reports.
     * Formula:
     * - 15 points per non-draft report submitted
     * - 50 points per report marked RESOLVED
     * - 10 points per photo evidence attached
     * - 5 points per upvote received
     */
    fun calculateUserCivicStats(
        userReports: List<CivicReportEntity>,
        userName: String? = null,
        userWard: String? = null
    ): CivicContributor {
        val nonDrafts = userReports.filterNot { it.status.equals("DRAFT", ignoreCase = true) }
        val resolvedCount = nonDrafts.count { it.status.equals("RESOLVED", ignoreCase = true) }
        val inProgressCount = nonDrafts.count { it.status.equals("IN_PROGRESS", ignoreCase = true) }
        val photosCount = nonDrafts.count { it.hasPhotoAttached || !it.photoUri.isNullOrBlank() }
        val upvotesCount = nonDrafts.sumOf { it.upvotesCount }

        val submittedPoints = nonDrafts.size * 15
        val resolvedPoints = resolvedCount * 50
        val photoPoints = photosCount * 10
        val upvotePoints = upvotesCount * 5
        val totalPoints = submittedPoints + resolvedPoints + photoPoints + upvotePoints

        val computedTier = CivicTier.computeTier(totalPoints, resolvedCount)

        val resolvedWard = userWard ?: nonDrafts.firstOrNull()?.wardLocation ?: "Balasore Central"
        val effectiveName = if (!userName.isNullOrBlank()) userName else "You (Civic Champion)"

        return CivicContributor(
            userId = "current_user",
            displayName = effectiveName,
            odiaDisplayName = if (effectiveName.contains("You", ignoreCase = true)) "ଆପଣ (ନାଗରିକ ଚାମ୍ପିଅନ୍)" else effectiveName,
            wardName = resolvedWard,
            odiaWardName = resolvedWard,
            avatarEmoji = when (computedTier) {
                CivicTier.PLATINUM_CHAMPION -> "👑"
                CivicTier.GOLD_GUARDIAN -> "🌟"
                CivicTier.SILVER_PATROL -> "🛡️"
                CivicTier.BRONZE_SCOUT -> "🙋"
            },
            reportsSubmitted = nonDrafts.size,
            reportsResolved = resolvedCount,
            reportsInProgress = inProgressCount,
            upvotesReceived = upvotesCount,
            civicPoints = totalPoints,
            tier = computedTier,
            isCurrentUser = true,
            specialtyCategory = when {
                nonDrafts.count { it.category.contains("Pothole", ignoreCase = true) } > 0 -> "Road Pothole Vigilance"
                nonDrafts.count { it.category.contains("Drain", ignoreCase = true) } > 0 -> "Drainage & Flow Taskforce"
                nonDrafts.count { it.category.contains("Streetlight", ignoreCase = true) } > 0 -> "Public Lighting Watch"
                else -> "Active Civic Reporter"
            },
            recentAchievement = when {
                resolvedCount >= 5 -> "Resolved $resolvedCount municipal issues in Balasore!"
                resolvedCount >= 1 -> "Successfully resolved $resolvedCount civic report!"
                nonDrafts.isNotEmpty() -> "Logged ${nonDrafts.size} active civic reports"
                else -> "Ready to submit first Civic Eye report"
            }
        )
    }

    /**
     * Builds the complete combined leaderboard including the current active user and top community citizens.
     */
    fun getCombinedLeaderboard(
        userReports: List<CivicReportEntity>,
        timePeriod: LeaderboardTimePeriod = LeaderboardTimePeriod.ALL_TIME,
        wardFilter: String = "All Wards",
        userName: String? = null
    ): List<CivicContributor> {
        val currentUserContributor = calculateUserCivicStats(userReports, userName)

        val periodMultiplier = when (timePeriod) {
            LeaderboardTimePeriod.ALL_TIME -> 1.0
            LeaderboardTimePeriod.THIS_MONTH -> 0.4
            LeaderboardTimePeriod.THIS_WEEK -> 0.15
        }

        val scaledCommunity = BASE_COMMUNITY_CONTRIBUTORS.map { c ->
            val pts = (c.civicPoints * periodMultiplier).toInt().coerceAtLeast(45)
            val sub = (c.reportsSubmitted * periodMultiplier).toInt().coerceAtLeast(1)
            val res = (c.reportsResolved * periodMultiplier).toInt().coerceAtLeast(0)
            c.copy(
                civicPoints = pts,
                reportsSubmitted = sub,
                reportsResolved = res,
                tier = CivicTier.computeTier(pts, res)
            )
        }

        val allList = (scaledCommunity + currentUserContributor)
            .filter { contributor ->
                wardFilter == "All Wards" ||
                contributor.wardName.contains(wardFilter, ignoreCase = true) ||
                (contributor.isCurrentUser) // Always retain current user
            }
            .sortedByDescending { it.civicPoints }

        return allList.mapIndexed { index, contributor ->
            contributor.copy(rank = index + 1)
        }
    }

    /**
     * Dynamically computes unlocked and in-progress achievement badges for the citizen.
     */
    fun evaluateBadges(userReports: List<CivicReportEntity>): List<CivicAchievementBadge> {
        val nonDrafts = userReports.filterNot { it.status.equals("DRAFT", ignoreCase = true) }
        val resolvedCount = nonDrafts.count { it.status.equals("RESOLVED", ignoreCase = true) }
        val photosCount = nonDrafts.count { it.hasPhotoAttached || !it.photoUri.isNullOrBlank() }
        val upvotesCount = nonDrafts.sumOf { it.upvotesCount }
        val potholeCount = nonDrafts.count { it.category.contains("Pothole", ignoreCase = true) }
        val drainCount = nonDrafts.count { it.category.contains("Drain", ignoreCase = true) }
        val streetlightCount = nonDrafts.count { it.category.contains("Streetlight", ignoreCase = true) }

        return listOf(
            CivicAchievementBadge(
                id = "badge_first_report",
                title = "Civic Initiator",
                odiaTitle = "ପ୍ରଥମ ନାଗରିକ ପ୍ରହରୀ",
                description = "Reported your very first civic infrastructure issue in Balasore.",
                odiaDescription = "ବାଲେଶ୍ୱର ପୌରପାଳିକାରେ ଆପଣଙ୍କର ପ୍ରଥମ ନାଗରିକ ଅଭିଯୋଗ ଦାଖଲ କରିଛନ୍ତି।",
                iconEmoji = "🌱",
                isUnlocked = nonDrafts.isNotEmpty(),
                unlockedDate = if (nonDrafts.isNotEmpty()) "Unlocked" else null,
                progress = (nonDrafts.size / 1f).coerceIn(0f, 1f),
                progressLabel = "${nonDrafts.size}/1 report"
            ),
            CivicAchievementBadge(
                id = "badge_first_resolved",
                title = "Verified Resolution Hero",
                odiaTitle = "ସଫଳ ସମାଧାନ ବୀର",
                description = "Had at least one reported issue successfully verified and resolved by the municipality.",
                odiaDescription = "ପୌରପାଳିକା ଦ୍ୱାରା ଅତିକମରେ ଗୋଟିଏ ଅଭିଯୋଗ ସଫଳତାର ସହ ସମାଧାନ ହୋଇଛି।",
                iconEmoji = "✅",
                isUnlocked = resolvedCount >= 1,
                unlockedDate = if (resolvedCount >= 1) "Verified" else null,
                progress = (resolvedCount / 1f).coerceIn(0f, 1f),
                progressLabel = "$resolvedCount/1 resolved"
            ),
            CivicAchievementBadge(
                id = "badge_pothole_crusader",
                title = "Pothole Patrol Master",
                odiaTitle = "ରାସ୍ତା ଖାଲଖମା ପ୍ରହରୀ",
                description = "Submitted 2 or more road asphalt crater or pothole hazard reports.",
                odiaDescription = "ଦୁଇଟି କିମ୍ବା ଅଧିକ ରାସ୍ତା ଖାଲଖମା ଅଭିଯୋଗ ଫଟୋ ସହ ଦାଖଲ କରିଛନ୍ତି।",
                iconEmoji = "🕳️",
                isUnlocked = potholeCount >= 2,
                unlockedDate = if (potholeCount >= 2) "Active" else null,
                progress = (potholeCount / 2f).coerceIn(0f, 1f),
                progressLabel = "$potholeCount/2 potholes"
            ),
            CivicAchievementBadge(
                id = "badge_drain_guardian",
                title = "Drainage & Silt Sentinel",
                odiaTitle = "ନିଷ୍କାସନ ନାଳା ରକ୍ଷକ",
                description = "Reported clogged storm drains or waterlogging risks to protect wards during rains.",
                odiaDescription = "ବର୍ଷା ଜଳବନ୍ଦୀ ଓ ଅବରୋଧ ଡ୍ରେନେଜ୍ ସଫେଇ ପାଇଁ ଅଭିଯୋଗ କରିଛନ୍ତି।",
                iconEmoji = "🌊",
                isUnlocked = drainCount >= 1,
                unlockedDate = if (drainCount >= 1) "Active" else null,
                progress = (drainCount / 1f).coerceIn(0f, 1f),
                progressLabel = "$drainCount/1 drain"
            ),
            CivicAchievementBadge(
                id = "badge_photo_evidence",
                title = "Photographic Evidence Pro",
                odiaTitle = "ପ୍ରମାଣିକ ଫଟୋଗ୍ରାଫର",
                description = "Captured geotagged photo evidence ensuring high-precision field municipal inspection.",
                odiaDescription = "ଜିଓଟ୍ୟାଗ୍ ଫଟୋ ପ୍ରମାଣ ସଂଲଗ୍ନ କରି ପୌରପାଳିକା ଯାଞ୍ଚକୁ ତ୍ୱରାନ୍ୱିତ କରିଛନ୍ତି।",
                iconEmoji = "📸",
                isUnlocked = photosCount >= 1,
                unlockedDate = if (photosCount >= 1) "Master" else null,
                progress = (photosCount / 1f).coerceIn(0f, 1f),
                progressLabel = "$photosCount/1 photo report"
            ),
            CivicAchievementBadge(
                id = "badge_community_voice",
                title = "Community Trust Favorite",
                odiaTitle = "ଲୋକପ୍ରିୟ ନାଗରିକ ସ୍ୱର",
                description = "Received 5 or more community citizen upvotes on your submitted reports.",
                odiaDescription = "ଆପଣଙ୍କ ଅଭିଯୋଗରେ ୫ରୁ ଅଧିକ ନାଗରିକଙ୍କ ସମର୍ଥନ ଓ ଅପଭୋଟ୍ ପାଇଛନ୍ତି।",
                iconEmoji = "👍",
                isUnlocked = upvotesCount >= 5,
                unlockedDate = if (upvotesCount >= 5) "Recognized" else null,
                progress = (upvotesCount / 5f).coerceIn(0f, 1f),
                progressLabel = "$upvotesCount/5 upvotes"
            ),
            CivicAchievementBadge(
                id = "badge_gold_tier",
                title = "Balasore Guardian Honor",
                odiaTitle = "ବାଲେଶ୍ୱର ସୁରକ୍ଷାକାରୀ ସମ୍ମାନ",
                description = "Achieved 350+ civic contribution points and 5+ verified resolutions.",
                odiaDescription = "୩୫୦ରୁ ଅଧିକ ପଏଣ୍ଟ ଓ ୫ଟି ସଫଳ ସମାଧାନ ସହ ଗୋଲ୍ଡ ଗାର୍ଡିଆନ୍ ଟାୟାର୍ ହାସଲ କରିଛନ୍ତି।",
                iconEmoji = "🌟",
                isUnlocked = resolvedCount >= 5,
                unlockedDate = if (resolvedCount >= 5) "Elite Honor" else null,
                progress = (resolvedCount / 5f).coerceIn(0f, 1f),
                progressLabel = "$resolvedCount/5 resolved"
            )
        )
    }

    /**
     * Overall municipal civic impact stats across all Balasore wards.
     */
    fun getMunicipalImpactStats(): MunicipalCivicImpactStats {
        return MunicipalCivicImpactStats(
            totalReportsLogged = 542,
            totalReportsResolved = 418,
            totalInProgress = 89,
            resolutionRatePercentage = 77,
            averageTurnaroundDays = 2.4,
            potholesRepaired = 184,
            streetlightsRestored = 129,
            drainsDesilted = 105,
            garbageDumpsCleared = 76,
            waterPipelinesFixed = 48
        )
    }

    /**
     * Syncs user civic leaderboard stats to Firestore when online.
     */
    suspend fun syncUserLeaderboardStats(
        userReports: List<CivicReportEntity>,
        userId: String?,
        userName: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (userId.isNullOrBlank()) return@withContext Result.success(Unit)
        try {
            val stats = calculateUserCivicStats(userReports, userName)
            // Save to Firestore users collection
            firestoreService.saveCitizenVote(
                com.example.data.local.CitizenVoteEntity(
                    pollId = "leaderboard_sync_$userId",
                    optionId = stats.tier.name,
                    optionLabel = "${stats.civicPoints} pts (${stats.reportsResolved} resolved)",
                    votedAtTimestamp = System.currentTimeMillis(),
                    syncedToCloud = true,
                    userId = userId,
                    initiativeCategory = "LEADERBOARD_POINTS"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
