package com.example.data.polling

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.data.firebase.FirestoreService
import com.example.data.local.AppDatabase
import com.example.data.local.CitizenVoteDao
import com.example.data.local.CitizenVoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

data class PollOption(
    val id: String,
    val label: String,
    val odiaLabel: String,
    val initialVotes: Int,
    val currentVotes: Int = initialVotes,
    val percentage: Float = 0f
)

data class CitizenPoll(
    val id: String,
    val title: String,
    val odiaTitle: String,
    val category: String, // "Park Improvements", "Event Scheduling", "Coastal Preservation", "Urban Infrastructure"
    val description: String,
    val odiaDescription: String,
    val options: List<PollOption>,
    val totalVotes: Int,
    val closingDate: String,
    val isActive: Boolean = true
)

/**
 * Repository that manages Citizen Voice Polling for Balasore 360.
 * Supports:
 * 1. 100% offline local voting via Room Database (CitizenVoteDao)
 * 2. Real-time vote percentage calculation
 * 3. Bidirectional cloud sync with Firestore 'citizen_votes' when online
 */
class CitizenVoicePollingRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val voteDao: CitizenVoteDao = db.citizenVoteDao()
    private val firestoreService = FirestoreService(context)

    companion object {
        val CIVIC_POLLS = listOf(
            CitizenPoll(
                id = "poll_gandhi_park_upgrade",
                title = "Gandhi Smriti Park Lighting & Walkway Upgrade",
                odiaTitle = "ଗାନ୍ଧୀ ସ୍ମୃତି ପାର୍କ ଆଲୋକୀକରଣ ଓ ଚଲାପଥ ଉନ୍ନୟନ",
                category = "Park Improvements",
                description = "Balasore Municipality has allocated civic development funds for Gandhi Smriti Park near Station Square. Vote for which amenity should receive first priority.",
                odiaDescription = "ଷ୍ଟେସନ ଛକ ନିକଟ ଗାନ୍ଧୀ ସ୍ମୃତି ପାର୍କର କେଉଁ ଉନ୍ନୟନ କାର୍ଯ୍ୟକୁ ପ୍ରାଥମିକତା ଦିଆଯିବା ଉଚିତ, ସେଥିପାଇଁ ଆପଣଙ୍କ ମତ ଦିଅନ୍ତୁ।",
                options = listOf(
                    PollOption("opt_solar_cctv", "Solar LED Walkway & 24x7 Security CCTV", "ସୌର ଏଲଇଡି ଚଲାପଥ ଓ ସିସିଟିଭି ସୁରକ୍ଷା", 342),
                    PollOption("opt_sensory_kids", "Inclusive Children's Playground & Toy Zone", "ଶିଶୁ ଖେଳ ପଡ଼ିଆ ଓ ସ୍ୱତନ୍ତ୍ର ଝୁଲା ପ୍ରାଙ୍ଗଣ", 289),
                    PollOption("opt_open_gym", "Open-Air Fitness Gym & Jogging Circuit", "ଖୋଲା ବ୍ୟାୟାମଶାଳା ଓ ଯୋଗ ପ୍ରାଙ୍ଗଣ", 194)
                ),
                totalVotes = 825,
                closingDate = "30 Nov 2026"
            ),
            CitizenPoll(
                id = "poll_balasore_heritage_mela_schedule",
                title = "Balasore District Heritage Mela 2026 Timing & Venue",
                odiaTitle = "ବାଲେଶ୍ୱର ଜିଲ୍ଲା ଐତିହ୍ୟ ମେଳା ୨୦୨୬ ସମୟ ଓ ସ୍ଥାନ",
                category = "Event Scheduling",
                description = "Help schedule the upcoming annual Baleswar Craft, Handloom & Cultural Expo for maximum citizen and tourist participation.",
                odiaDescription = "ବାର୍ଷିକ ବାଲେଶ୍ୱର ହସ୍ତତନ୍ତ, କଳା ଓ ସାଂସ୍କୃତିକ ମେଳା ଆୟୋଜନ ପାଇଁ ଉପଯୁକ୍ତ ସମୟ ଓ ସ୍ଥାନ ବାଛନ୍ତୁ।",
                options = listOf(
                    PollOption("opt_winter_jan", "Winter Expo (Jan 10–18, ITI Ground)", "ଶୀତକାଳୀନ ମହୋତ୍ସବ (ଜାନୁଆରୀ, ଆଇଟିଆଇ ପଡ଼ିଆ)", 512),
                    PollOption("opt_raja_june", "Raja Special Heritage Week (June 12–16, Remuna)", "ରଜ ସ୍ୱତନ୍ତ୍ର ସପ୍ତାହ (ଜୁନ, ରେମୁଣା)", 388),
                    PollOption("opt_autumn_oct", "Post-Puja Autumn Fair (Oct 24–30, Police Ground)", "ପୂଜା ପରବର୍ତ୍ତୀ ଶରତ ମେଳା (ଅକ୍ଟୋବର, ପୋଲିସ ଲାଇନ୍)", 245)
                ),
                totalVotes = 1145,
                closingDate = "15 Dec 2026"
            ),
            CitizenPoll(
                id = "poll_chandipur_coastal_cleanliness",
                title = "Chandipur Beach Eco-Protection & Solar Smart Bins",
                odiaTitle = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ସଂରକ୍ଷଣ ଓ ସୌର ବର୍ଜ୍ୟ ପରିଚାଳନା",
                category = "Coastal Preservation",
                description = "Protect the sensitive horseshoe crab breeding shoreline and vanishing seabed eco-system through citizen-backed sanitation infrastructure.",
                odiaDescription = "ଅଦୃଶ୍ୟ ବେଳାଭୂମି ଓ ରାଜକଙ୍କଡ଼ା ସଂରକ୍ଷଣ ପାଇଁ ଚାନ୍ଦିପୁର ଉପକୂଳରେ ପରିମଳ ବ୍ୟବସ୍ଥା ମଜବୁତ କରନ୍ତୁ।",
                options = listOf(
                    PollOption("opt_smart_bins", "Solar-Powered Compaction Eco Bins", "ସୌର ଚାଳିତ ସ୍ମାର୍ଟ ଡଷ୍ଟବିନ୍ ସ୍ଥାପନ", 478),
                    PollOption("opt_green_volunteers", "Community Eco-Guard Kiosks with Rewards", "ସ୍ୱେଚ୍ଛାସେବୀ ଇକୋ-ଗାର୍ଡ ଓ ପୁରସ୍କାର ବ୍ୟବସ୍ଥା", 310),
                    PollOption("opt_ev_trolleys", "Battery EV Sand Sweepers & Cleanup Trolleys", "ଇ-ଟ୍ରଲି ବାଲି ସଫେଇ ବାହିକା", 265)
                ),
                totalVotes = 1053,
                closingDate = "20 Dec 2026"
            ),
            CitizenPoll(
                id = "poll_remuna_pilgrim_amenities",
                title = "Remuna Gopinath Pilgrim Corridor & Transit Shelter",
                odiaTitle = "ରେମୁଣା ଗୋପୀନାଥ ଯାତ୍ରୀ କରିଡର ଓ ବିଶ୍ରାମାଗାର",
                category = "Urban Infrastructure",
                description = "Enhance devotee and visitor connectivity along the Balasore to Remuna corridor leading to Khirachora Gopinath Temple.",
                odiaDescription = "କ୍ଷୀରଚୋରା ଗୋପୀନାଥ ମନ୍ଦିର ଯାତ୍ରୀଙ୍କ ସୁବିଧା ପାଇଁ ପଦକ୍ଷେପ ଚୟନ କରନ୍ତୁ।",
                options = listOf(
                    PollOption("opt_ev_shuttle", "Dedicated 10-Min EV Mini-Bus Shuttle", "ବାଲେଶ୍ୱର ଷ୍ଟେସନରୁ ୧୦ ମିନିଟ୍ ଇଭି ସଟଲ", 615),
                    PollOption("opt_shaded_walkway", "Covered Pedestrian Walkway & Cold Water", "ଛାଇଯୁକ୍ତ ଚଲାପଥ ଓ ବିଶୁଦ୍ଧ ଶୀତଳ ଜଳ କିଓସ୍କ", 532),
                    PollOption("opt_artisan_complex", "Traditional Brass & Sweet Artisan Center", "ପାରମ୍ପରିକ କଂସା ବାସନ ଓ ମିଠା କେନ୍ଦ୍ର", 390)
                ),
                totalVotes = 1537,
                closingDate = "31 Dec 2026"
            )
        )
    }

    /**
     * Observes polls dynamically combined with local user voting state from Room.
     */
    fun observePollsWithUserVotes(): Flow<List<Pair<CitizenPoll, CitizenVoteEntity?>>> {
        return voteDao.getAllVotesFlow().combine(kotlinx.coroutines.flow.flowOf(CIVIC_POLLS)) { localVotes, staticPolls ->
            val voteMap = localVotes.associateBy { it.pollId }

            staticPolls.map { poll ->
                val userVote = voteMap[poll.id]
                val adjustedOptions = poll.options.map { option ->
                    val extraVote = if (userVote?.optionId == option.id) 1 else 0
                    val count = option.initialVotes + extraVote
                    option.copy(currentVotes = count)
                }

                val totalVotes = adjustedOptions.sumOf { it.currentVotes }
                val optionsWithPercentages = adjustedOptions.map { opt ->
                    val pct = if (totalVotes > 0) (opt.currentVotes.toFloat() / totalVotes.toFloat()) * 100f else 0f
                    opt.copy(percentage = pct)
                }

                Pair(poll.copy(options = optionsWithPercentages, totalVotes = totalVotes), userVote)
            }
        }
    }

    /**
     * Casts a vote on a civic initiative.
     * Instantly stores vote locally in Room and queues/attempts cloud sync to Firestore.
     */
    suspend fun castVote(
        pollId: String,
        optionId: String,
        optionLabel: String,
        initiativeCategory: String,
        userId: String? = null
    ): Result<CitizenVoteEntity> = withContext(Dispatchers.IO) {
        try {
            val vote = CitizenVoteEntity(
                pollId = pollId,
                optionId = optionId,
                optionLabel = optionLabel,
                votedAtTimestamp = System.currentTimeMillis(),
                syncedToCloud = false,
                userId = userId,
                initiativeCategory = initiativeCategory
            )

            // 1. Save locally to Room
            voteDao.insertVote(vote)

            // 2. If network is available, attempt immediate Firestore sync
            if (isNetworkAvailable()) {
                val syncResult = firestoreService.saveCitizenVote(vote)
                if (syncResult.isSuccess) {
                    voteDao.markAsSynced(pollId)
                    return@withContext Result.success(vote.copy(syncedToCloud = true))
                }
            }

            Result.success(vote)
        } catch (e: Exception) {
            Log.e("CitizenVoicePolling", "Error casting vote: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Synchronizes all unsynced local votes to Firestore.
     * Returns the count of successfully synced votes.
     */
    suspend fun syncPendingVotesToFirestore(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (!isNetworkAvailable()) {
                return@withContext Result.failure(Exception("Offline: Device is not connected to internet"))
            }

            val unsynced = voteDao.getUnsyncedVotes()
            if (unsynced.isEmpty()) {
                return@withContext Result.success(0)
            }

            var syncedCount = 0
            for (vote in unsynced) {
                val res = firestoreService.saveCitizenVote(vote)
                if (res.isSuccess) {
                    voteDao.markAsSynced(vote.pollId)
                    syncedCount++
                }
            }

            Result.success(syncedCount)
        } catch (e: Exception) {
            Log.e("CitizenVoicePolling", "Error during cloud sync: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Checks if device is currently online.
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun getUnsyncedCount(): Int = withContext(Dispatchers.IO) {
        voteDao.getUnsyncedVotes().size
    }
}
