package com.example.data.recommendation

import android.content.Context
import com.example.data.model.Hotspot
import com.example.data.model.NewsArticle
import com.example.data.model.UserWeatherPreferences
import com.example.data.repository.BalasoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class UserBehaviorProfile(
    val viewedArticleIds: Set<String> = emptySet(),
    val savedArticleIds: Set<String> = emptySet(),
    val viewedLocationIds: Set<String> = emptySet(),
    val savedLocationIds: Set<String> = emptySet(),
    val visitedLocationIds: Set<String> = emptySet(),
    val categoryAffinities: Map<String, Int> = emptyMap(),
    val localityAffinities: Map<String, Int> = emptyMap(),
    val totalInteractions: Int = 0
)

data class RecommendedHotspotItem(
    val hotspot: Hotspot,
    val matchScorePercent: Int,
    val reasonEn: String,
    val reasonOdia: String,
    val matchedTags: List<String>
)

data class RecommendedArticleItem(
    val article: NewsArticle,
    val matchScorePercent: Int,
    val reasonEn: String,
    val reasonOdia: String,
    val matchedCategory: String
)

data class RecommendedWeatherAlertItem(
    val id: String,
    val titleEn: String,
    val titleOdia: String,
    val advisoryEn: String,
    val advisoryOdia: String,
    val locality: String,
    val alertCategory: String,
    val severity: String, // "HIGH", "MODERATE", "ADVISORY"
    val matchScorePercent: Int,
    val reasonEn: String,
    val reasonOdia: String,
    val matchedFeatures: List<String>
)

/**
 * Content-Based Behavioral Recommendation Engine for the BALASORE 360 App.
 * Analyzes user behavior — including viewed articles, saved articles, bookmarked locations,
 * visited tourism hotspots, custom alert preferences, and category/locality feature vectors —
 * using content-based filtering to suggest personalized News, Weather Alerts, and Tourism Hotspots.
 */
object BalasoreRecommendationEngine {

    private const val PREFS_NAME = "balasore_recommendation_behavior"
    private const val KEY_VIEWED_ARTICLES = "viewed_article_ids"
    private const val KEY_SAVED_ARTICLES = "saved_article_ids"
    private const val KEY_VIEWED_LOCATIONS = "viewed_location_ids"
    private const val KEY_SAVED_LOCATIONS = "saved_location_ids"
    private const val KEY_CATEGORY_AFFINITY = "category_affinity_entries"
    private const val KEY_LOCALITY_AFFINITY = "locality_affinity_entries"

    private val _behaviorState = MutableStateFlow(UserBehaviorProfile())
    val behaviorState: StateFlow<UserBehaviorProfile> = _behaviorState.asStateFlow()

    fun initialize(context: Context) {
        _behaviorState.value = loadBehaviorProfile(context)
    }

    fun loadBehaviorProfile(context: Context): UserBehaviorProfile {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val viewedArticles = prefs.getStringSet(KEY_VIEWED_ARTICLES, null)?.toSet()
            ?: setOf("1", "2")
        val savedArticles = prefs.getStringSet(KEY_SAVED_ARTICLES, null)?.toSet()
            ?: BalasoreRepository.newsArticles.filter { it.isBookmarked }.map { it.id }.toSet()
        val viewedLocations = prefs.getStringSet(KEY_VIEWED_LOCATIONS, null)?.toSet()
            ?: setOf("1", "2", "3")
        val savedLocations = prefs.getStringSet(KEY_SAVED_LOCATIONS, null)?.toSet()
            ?: setOf("1", "3")

        val visitedPrefs = context.getSharedPreferences("balasore_visited_spots_prefs", Context.MODE_PRIVATE)
        val visitedLocations = visitedPrefs.getStringSet("visited_spot_ids", setOf("1", "3"))?.toSet()
            ?: setOf("1", "3")

        val rawCatSet = prefs.getStringSet(KEY_CATEGORY_AFFINITY, null)
        val categoryMap = mutableMapOf<String, Int>()
        if (rawCatSet != null) {
            rawCatSet.forEach { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    categoryMap[parts[0]] = parts[1].toIntOrNull() ?: 1
                }
            }
        } else {
            categoryMap["Beach"] = 4
            categoryMap["Temple"] = 3
            categoryMap["Heritage"] = 3
            categoryMap["Tourism"] = 4
            categoryMap["Weather"] = 3
            categoryMap["Civic"] = 2
        }

        val rawLocSet = prefs.getStringSet(KEY_LOCALITY_AFFINITY, null)
        val localityMap = mutableMapOf<String, Int>()
        if (rawLocSet != null) {
            rawLocSet.forEach { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    localityMap[parts[0]] = parts[1].toIntOrNull() ?: 1
                }
            }
        } else {
            localityMap["Chandipur"] = 4
            localityMap["Remuna"] = 3
            localityMap["Nilagiri"] = 3
            localityMap["Balasore Town"] = 3
        }

        val total = viewedArticles.size + savedArticles.size + viewedLocations.size + savedLocations.size +
            categoryMap.values.sum()

        return UserBehaviorProfile(
            viewedArticleIds = viewedArticles,
            savedArticleIds = savedArticles,
            viewedLocationIds = viewedLocations,
            savedLocationIds = savedLocations,
            visitedLocationIds = visitedLocations,
            categoryAffinities = categoryMap,
            localityAffinities = localityMap,
            totalInteractions = total
        )
    }

    private fun saveMaps(
        context: Context,
        viewedArticles: Set<String>,
        savedArticles: Set<String>,
        viewedLocations: Set<String>,
        savedLocations: Set<String>,
        categories: Map<String, Int>,
        localities: Map<String, Int>
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val catEncoded = categories.entries.map { "${it.key}:${it.value}" }.toSet()
        val locEncoded = localities.entries.map { "${it.key}:${it.value}" }.toSet()
        prefs.edit()
            .putStringSet(KEY_VIEWED_ARTICLES, viewedArticles)
            .putStringSet(KEY_SAVED_ARTICLES, savedArticles)
            .putStringSet(KEY_VIEWED_LOCATIONS, viewedLocations)
            .putStringSet(KEY_SAVED_LOCATIONS, savedLocations)
            .putStringSet(KEY_CATEGORY_AFFINITY, catEncoded)
            .putStringSet(KEY_LOCALITY_AFFINITY, locEncoded)
            .apply()
        _behaviorState.value = loadBehaviorProfile(context)
    }

    fun recordArticleViewed(context: Context, article: NewsArticle) {
        val current = loadBehaviorProfile(context)
        val updatedViewed = current.viewedArticleIds + article.id
        val updatedCats = current.categoryAffinities.toMutableMap()
        val catKey = article.category.ifBlank { "Local News" }
        updatedCats[catKey] = (updatedCats[catKey] ?: 0) + 2

        val updatedLocs = current.localityAffinities.toMutableMap()
        detectLocalityFromText("${article.title} ${article.snippet}").forEach { loc ->
            updatedLocs[loc] = (updatedLocs[loc] ?: 0) + 1
        }

        saveMaps(
            context = context,
            viewedArticles = updatedViewed,
            savedArticles = current.savedArticleIds,
            viewedLocations = current.viewedLocationIds,
            savedLocations = current.savedLocationIds,
            categories = updatedCats,
            localities = updatedLocs
        )
    }

    fun recordArticleSaved(context: Context, articleId: String, title: String, category: String, isSaved: Boolean = true) {
        val current = loadBehaviorProfile(context)
        val updatedSaved = if (isSaved) current.savedArticleIds + articleId else current.savedArticleIds - articleId
        val updatedCats = current.categoryAffinities.toMutableMap()
        if (isSaved && category.isNotBlank()) {
            updatedCats[category] = (updatedCats[category] ?: 0) + 3
        }
        val updatedLocs = current.localityAffinities.toMutableMap()
        if (isSaved) {
            detectLocalityFromText(title).forEach { loc ->
                updatedLocs[loc] = (updatedLocs[loc] ?: 0) + 2
            }
        }
        saveMaps(
            context = context,
            viewedArticles = current.viewedArticleIds,
            savedArticles = updatedSaved,
            viewedLocations = current.viewedLocationIds,
            savedLocations = current.savedLocationIds,
            categories = updatedCats,
            localities = updatedLocs
        )
    }

    fun recordLocationViewed(context: Context, spot: Hotspot) {
        val current = loadBehaviorProfile(context)
        val updatedViewed = current.viewedLocationIds + spot.id
        val updatedCats = current.categoryAffinities.toMutableMap()
        updatedCats[spot.category] = (updatedCats[spot.category] ?: 0) + 2

        val updatedLocs = current.localityAffinities.toMutableMap()
        val locKey = spot.location.substringBefore(",").trim().ifBlank { "Balasore" }
        updatedLocs[locKey] = (updatedLocs[locKey] ?: 0) + 2

        saveMaps(
            context = context,
            viewedArticles = current.viewedArticleIds,
            savedArticles = current.savedArticleIds,
            viewedLocations = updatedViewed,
            savedLocations = current.savedLocationIds,
            categories = updatedCats,
            localities = updatedLocs
        )
    }

    fun recordLocationSaved(
        context: Context,
        spotId: String,
        spotName: String,
        category: String,
        location: String,
        isSaved: Boolean = true
    ) {
        val current = loadBehaviorProfile(context)
        val updatedSaved = if (isSaved) current.savedLocationIds + spotId else current.savedLocationIds - spotId
        val updatedCats = current.categoryAffinities.toMutableMap()
        if (isSaved && category.isNotBlank()) {
            updatedCats[category] = (updatedCats[category] ?: 0) + 4
        }
        val updatedLocs = current.localityAffinities.toMutableMap()
        val locKey = location.substringBefore(",").trim().ifBlank { spotName.substringBefore(" ") }
        if (isSaved && locKey.isNotBlank()) {
            updatedLocs[locKey] = (updatedLocs[locKey] ?: 0) + 3
        }
        saveMaps(
            context = context,
            viewedArticles = current.viewedArticleIds,
            savedArticles = current.savedArticleIds,
            viewedLocations = current.viewedLocationIds,
            savedLocations = updatedSaved,
            categories = updatedCats,
            localities = updatedLocs
        )
    }

    /**
     * Pure Content-Based Filtering similarity scorer (Cosine + Weighted TF Overlap)
     * comparing a user's behavioral feature vector against an item's category/locality/keyword features.
     */
    fun computeContentBasedSimilarity(
        profile: UserBehaviorProfile,
        itemCategories: List<String>,
        itemLocalities: List<String>,
        itemKeywords: List<String> = emptyList()
    ): Int {
        val userVector = mutableMapOf<String, Double>()
        profile.categoryAffinities.forEach { (k, v) ->
            userVector[k.lowercase()] = (userVector[k.lowercase()] ?: 0.0) + v.toDouble() * 1.5
        }
        profile.localityAffinities.forEach { (k, v) ->
            userVector[k.lowercase()] = (userVector[k.lowercase()] ?: 0.0) + v.toDouble() * 1.3
        }

        if (userVector.isEmpty()) return 80

        val itemVector = mutableMapOf<String, Double>()
        itemCategories.forEach { c -> itemVector[c.lowercase()] = 2.5 }
        itemLocalities.forEach { l -> itemVector[l.lowercase()] = 2.2 }
        itemKeywords.forEach { kw -> itemVector[kw.lowercase()] = 1.2 }

        var dotProduct = 0.0
        itemVector.forEach { (feature, weight) ->
            val matchedUserWeight = userVector.entries
                .firstOrNull { (uKey, _) -> uKey.contains(feature) || feature.contains(uKey) }
                ?.value ?: 0.0
            dotProduct += weight * matchedUserWeight
        }

        val userNorm = sqrt(userVector.values.sumOf { it * it }).coerceAtLeast(1.0)
        val itemNorm = sqrt(itemVector.values.sumOf { it * it }).coerceAtLeast(1.0)
        val cosine = (dotProduct / (userNorm * itemNorm)).coerceIn(0.0, 1.0)

        return (68.0 + (cosine * 31.0)).roundToInt().coerceIn(72, 99)
    }

    private fun detectLocalityFromText(text: String): List<String> {
        val knownLocalities = listOf(
            "Chandipur", "Remuna", "Panchalingeswar", "Nilagiri",
            "Kuldiha", "Talasari", "Bhograi", "Jaleswar", "Soro", "Balaramgadi", "Balasore"
        )
        return knownLocalities.filter { text.contains(it, ignoreCase = true) }
    }

    /**
     * Generates personalized Tourism Hotspot recommendations based on saved locations,
     * visited spots, viewed articles, and content-based category/locality affinities.
     */
    fun getRecommendedHotspots(
        context: Context? = null,
        allHotspots: List<Hotspot> = BalasoreRepository.hotspots,
        externalSavedIds: Set<String> = emptySet(),
        externalVisitedIds: Set<String> = emptySet(),
        limit: Int = 4,
        profileOverride: UserBehaviorProfile? = null
    ): List<RecommendedHotspotItem> {
        val profile = profileOverride ?: context?.let { loadBehaviorProfile(it) } ?: _behaviorState.value
        val allSavedIds = profile.savedLocationIds + externalSavedIds
        val allVisitedIds = profile.visitedLocationIds + externalVisitedIds

        val savedSpots = allHotspots.filter { allSavedIds.contains(it.id) }
        val savedCategories = savedSpots.map { it.category }.toSet()
        val topCategory = profile.categoryAffinities.maxByOrNull { it.value }?.key ?: "Beach"

        return allHotspots.map { spot ->
            val locToken = spot.location.substringBefore(",").trim()
            val contentSim = computeContentBasedSimilarity(
                profile = profile,
                itemCategories = listOf(spot.category, "Tourism"),
                itemLocalities = listOf(locToken),
                itemKeywords = listOf(spot.name.substringBefore(" "))
            )
            var rawScore = contentSim.toDouble()
            val matchedTags = mutableListOf<String>()
            var reasonEn = "Popular among Balasore explorers"
            var reasonOdia = "ବାଲେଶ୍ୱର ପର୍ଯ୍ୟଟକଙ୍କ ମଧ୍ୟରେ ଲୋକପ୍ରିୟ"

            val catWeight = profile.categoryAffinities[spot.category] ?: 0
            if (catWeight > 0) {
                rawScore += (catWeight * 2.5).coerceAtMost(12.0)
                matchedTags.add(spot.category)
            }

            if (savedCategories.contains(spot.category)) {
                val similarSaved = savedSpots.firstOrNull { it.category == spot.category && it.id != spot.id }
                rawScore += 6.0
                if (similarSaved != null) {
                    reasonEn = "Content match with your saved ${similarSaved.name} (${spot.category})"
                    reasonOdia = "ଆପଣ ${similarSaved.odiaName} ସେଭ୍ କରିଥିବାରୁ (${spot.category})"
                } else {
                    reasonEn = "Matches your saved ${spot.category} locations"
                    reasonOdia = "ଆପଣଙ୍କ ପସନ୍ଦର ${spot.category} ସ୍ଥାନ ସହିତ ମେଳ ଖାଉଛି"
                }
            }

            val viewedNewsObjects = BalasoreRepository.newsArticles.filter {
                profile.viewedArticleIds.contains(it.id) || profile.savedArticleIds.contains(it.id)
            }
            val mentionedInViewedNews = viewedNewsObjects.firstOrNull {
                it.title.contains(spot.name.substringBefore(" "), ignoreCase = true) ||
                    it.snippet.contains(locToken, ignoreCase = true)
            }
            if (mentionedInViewedNews != null) {
                rawScore += 7.0
                matchedTags.add("In Your News")
                reasonEn = "Featured in articles you read: \"${mentionedInViewedNews.title.take(32)}...\""
                reasonOdia = "ଆପଣ ପଢ଼ିଥିବା ଖବର ଆଧାରରେ: ${spot.odiaName}"
            }

            if (!allVisitedIds.contains(spot.id)) {
                rawScore += 4.0
                matchedTags.add("New to Visit")
            } else {
                matchedTags.add("Visited Favorite")
            }

            rawScore += (spot.rating - 4.0).coerceAtLeast(0.0) * 4.0

            if (reasonEn == "Popular among Balasore explorers" && catWeight > 0) {
                reasonEn = "Content-based match for your #1 interest in $topCategory & ${spot.location}"
                reasonOdia = "ଆପଣଙ୍କ $topCategory ଆଗ୍ରହ ଆଧାରରେ ବିଶେଷ ପରାମର୍ଶ"
            }

            val finalPercent = rawScore.roundToInt().coerceIn(78, 99)
            RecommendedHotspotItem(
                hotspot = spot,
                matchScorePercent = finalPercent,
                reasonEn = reasonEn,
                reasonOdia = reasonOdia,
                matchedTags = matchedTags.distinct().take(3)
            )
        }
            .sortedWith(
                compareByDescending<RecommendedHotspotItem> { !allVisitedIds.contains(it.hotspot.id) }
                    .thenByDescending { it.matchScorePercent }
            )
            .take(limit)
    }

    /**
     * Generates personalized News Article recommendations using Content-Based Filtering
     * across viewed articles, saved articles, and saved tourism locations.
     */
    fun getRecommendedArticles(
        context: Context? = null,
        allArticles: List<NewsArticle> = BalasoreRepository.newsArticles,
        externalSavedNewsIds: Set<String> = emptySet(),
        limit: Int = 4,
        profileOverride: UserBehaviorProfile? = null
    ): List<RecommendedArticleItem> {
        val profile = profileOverride ?: context?.let { loadBehaviorProfile(it) } ?: _behaviorState.value
        val allSavedNews = profile.savedArticleIds + externalSavedNewsIds
        val savedSpots = BalasoreRepository.hotspots.filter { profile.savedLocationIds.contains(it.id) }
        val viewedArticlesList = allArticles.filter { profile.viewedArticleIds.contains(it.id) || allSavedNews.contains(it.id) }
        val preferredNewsCategories = viewedArticlesList.map { it.category }.groupingBy { it }.eachCount()

        return allArticles.map { article ->
            val articleLocs = detectLocalityFromText("${article.title} ${article.snippet}")
            val contentSim = computeContentBasedSimilarity(
                profile = profile,
                itemCategories = listOf(article.category),
                itemLocalities = articleLocs
            )
            var rawScore = contentSim.toDouble()
            var reasonEn = "Trending in Balasore District"
            var reasonOdia = "ବାଲେଶ୍ୱର ଜିଲ୍ଲାର ପ୍ରମୁଖ ଖବର"

            val catCount = preferredNewsCategories[article.category] ?: 0
            val catAffinity = profile.categoryAffinities[article.category] ?: 0
            if (catCount > 0 || catAffinity > 0) {
                rawScore += ((catCount * 5) + (catAffinity * 2.5)).coerceAtMost(16.0)
                reasonEn = "Content match: you read & saved ${article.category} updates"
                reasonOdia = "ଆପଣ ${article.category} ଖବର ପଢ଼ିଥିବାରୁ"
            }

            val matchedSpot = savedSpots.firstOrNull { spot ->
                val keyword = spot.name.substringBefore(" ")
                val locKeyword = spot.location.substringBefore(",")
                article.title.contains(keyword, ignoreCase = true) ||
                    article.snippet.contains(keyword, ignoreCase = true) ||
                    article.title.contains(locKeyword, ignoreCase = true)
            }
            if (matchedSpot != null) {
                rawScore += 10.0
                reasonEn = "Related to your saved location: ${matchedSpot.name}"
                reasonOdia = "ଆପଣଙ୍କ ସଂରକ୍ଷିତ ସ୍ଥାନ '${matchedSpot.odiaName}' ସମ୍ବନ୍ଧୀୟ ଖବର"
            }

            val matchedLocality = profile.localityAffinities.entries
                .sortedByDescending { it.value }
                .firstOrNull { (loc, _) ->
                    article.title.contains(loc, ignoreCase = true) || article.snippet.contains(loc, ignoreCase = true)
                }
            if (matchedLocality != null && matchedSpot == null) {
                rawScore += 6.0
                reasonEn = "Matches your activity in ${matchedLocality.key}"
                reasonOdia = "${matchedLocality.key} ଅଞ୍ଚଳ ପ୍ରତି ଆପଣଙ୍କ ଆଗ୍ରହ ଆଧାରରେ"
            }

            if (!profile.viewedArticleIds.contains(article.id)) {
                rawScore += 4.0
            }

            val finalPercent = rawScore.roundToInt().coerceIn(80, 99)
            RecommendedArticleItem(
                article = article,
                matchScorePercent = finalPercent,
                reasonEn = reasonEn,
                reasonOdia = reasonOdia,
                matchedCategory = article.category
            )
        }
            .sortedByDescending { it.matchScorePercent }
            .take(limit)
    }

    /**
     * Generates personalized Weather & Coastal Alerts using Content-Based Filtering
     * over the user's saved/visited locations, viewed news categories, and custom alert preferences.
     */
    fun getRecommendedWeatherAlerts(
        context: Context? = null,
        weatherPrefs: UserWeatherPreferences = UserWeatherPreferences(),
        externalSavedLocationIds: Set<String> = emptySet(),
        limit: Int = 4,
        profileOverride: UserBehaviorProfile? = null
    ): List<RecommendedWeatherAlertItem> {
        val profile = profileOverride ?: context?.let { loadBehaviorProfile(it) } ?: _behaviorState.value
        val allSavedLocIds = profile.savedLocationIds + externalSavedLocationIds
        val savedSpots = BalasoreRepository.hotspots.filter { allSavedLocIds.contains(it.id) }
        val savedLocalities = savedSpots.map { it.location.substringBefore(",").trim() }.toSet()

        data class RawWeatherAlertTemplate(
            val id: String,
            val titleEn: String,
            val titleOdia: String,
            val advisoryEn: String,
            val advisoryOdia: String,
            val locality: String,
            val alertCategory: String,
            val severity: String,
            val featureTags: List<String>,
            val isEnabledInPrefs: Boolean
        )

        val candidates = listOf(
            RawWeatherAlertTemplate(
                id = "rec_wx_chandipur_tide",
                titleEn = "Chandipur Intertidal Surge & Safe Walk Window",
                titleOdia = "ଚାନ୍ଦିପୁର ଉଚ୍ଚ ଜୁଆର ଓ ସୁରକ୍ଷିତ ସମୁଦ୍ର ଭ୍ରମଣ ସମୟ",
                advisoryEn = "Receding seabed safe walk window active for next 2.5 hrs. High tide surge returns at 4:45 PM (${weatherPrefs.highTideAlertHoursBefore}h advance alert active).",
                advisoryOdia = "ଆଗାମୀ ୨.୫ ଘଣ୍ଟା ପର୍ଯ୍ୟନ୍ତ ସମୁଦ୍ର ଶଯ୍ୟା ଭ୍ରମଣ ସୁରକ୍ଷିତ। ଅପରାହ୍ନ ୪:୪୫ ରେ ଉଚ୍ଚ ଜୁଆର ଆସିବ।",
                locality = "Chandipur",
                alertCategory = "Coastal Tide",
                severity = "HIGH",
                featureTags = listOf("Beach", "Tourism", "Weather", "Chandipur"),
                isEnabledInPrefs = weatherPrefs.notifyHighTide || weatherPrefs.notifySafeTideWindow
            ),
            RawWeatherAlertTemplate(
                id = "rec_wx_subarnarekha_flood",
                titleEn = "Subarnarekha & Budhabalanga River Basin Watch",
                titleOdia = "ସୁବର୍ଣ୍ଣରେଖା ଓ ବୁଢ଼ାବଳଙ୍ଗ ନଦୀ ଜଳସ୍ତର ସତର୍କତା",
                advisoryEn = "Rajghat gauge steady at 8.9m (below 10.36m danger mark). Moderate upstream rainfall over Jaleswar & Bhograi.",
                advisoryOdia = "ରାଜଘାଟ ଜଳସ୍ତର ୮.୯ ମିଟରରେ ସ୍ଥିର ଅଛି (ବିପଦ ସଙ୍କେତ ୧୦.୩୬ ମିଟର ତଳେ)।",
                locality = "Jaleswar & Bhograi",
                alertCategory = "River Flood",
                severity = "MODERATE",
                featureTags = listOf("Weather", "Civic", "Beach", "Talasari", "Bhograi"),
                isEnabledInPrefs = weatherPrefs.notifyFloodAlerts || weatherPrefs.notifyRainfall
            ),
            RawWeatherAlertTemplate(
                id = "rec_wx_nilagiri_kuldiha_rain",
                titleEn = "Panchalingeswar & Kuldiha Hill Stream Monsoon Advisory",
                titleOdia = "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଓ କୁଲଡିହା ପାହାଡ଼ ଝରଣା ବର୍ଷା ପରାମର୍ଶ",
                advisoryEn = "Light afternoon shower expected over Devgiri hills; perennial stream over the 5 Shiva Lingams has brisk flow — wear grip footwear.",
                advisoryOdia = "ଦେବଗିରି ପାହାଡ଼ରେ ହାଲୁକା ବର୍ଷା ସମ୍ଭାବନା; ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଝରଣା ଜଳପ୍ରବାହ ତୀବ୍ର ରହିଛି।",
                locality = "Nilagiri",
                alertCategory = "Hill Monsoon",
                severity = "ADVISORY",
                featureTags = listOf("Temple", "Wildlife", "Heritage", "Nilagiri", "Panchalingeswar", "Kuldiha"),
                isEnabledInPrefs = weatherPrefs.notifyPanchalingeswar || weatherPrefs.notifyKuldihaSafari
            ),
            RawWeatherAlertTemplate(
                id = "rec_wx_balasore_heat_squall",
                titleEn = "Balasore Town UV Index & Afternoon Kalbaisakhi Watch",
                titleOdia = "ବାଲେଶ୍ୱର ସହର ତାପମାତ୍ରା ଓ ଅପରାହ୍ନ କାଳବୈଶାଖୀ ସୂଚନା",
                advisoryEn = "Peak daytime temperature near 34°C (Your custom alert threshold: ${weatherPrefs.maxTempThreshold}°C). Sea breeze cooling expected after 3:30 PM.",
                advisoryOdia = "ମଧ୍ୟାହ୍ନ ତାପମାତ୍ରା ୩୪°C (ଆପଣଙ୍କ ସତର୍କତା ସୀମା: ${weatherPrefs.maxTempThreshold}°C)। ଅପରାହ୍ନ ୩:୩୦ ପରେ ସମୁଦ୍ର ପବନ ବହିବ।",
                locality = "Balasore Town",
                alertCategory = "Heat & Squall",
                severity = "MODERATE",
                featureTags = listOf("Civic", "Local News", "Heritage", "Balasore Town", "Remuna"),
                isEnabledInPrefs = weatherPrefs.notifySevereHeat || weatherPrefs.notifyCyclone
            ),
            RawWeatherAlertTemplate(
                id = "rec_wx_bay_cyclone_track",
                titleEn = "Bay of Bengal Marine Wind & Fisherman Telemetry",
                titleOdia = "ବଙ୍ଗୋପସାଗର ସାମୁଦ୍ରିକ ପବନ ଓ ମତ୍ସ୍ୟଜୀବୀ ପରାମର୍ଶ",
                advisoryEn = "Surface wind 22–28 km/h from SE off Balaramgadi & Dhamra coast. Normal harbor operations; no cyclone depression active.",
                advisoryOdia = "ବଳରାମଗଡ଼ି ଉପକୂଳରେ ପବନ ବେଗ ଘଣ୍ଟା ପ୍ରତି ୨୨-୨୮ କି.ମି.। କୌଣସି ବାତ୍ୟା ସଙ୍କେତ ନାହିଁ।",
                locality = "Balaramgadi & Coast",
                alertCategory = "Cyclone & Marine",
                severity = "ADVISORY",
                featureTags = listOf("Weather", "Defense", "Beach", "Chandipur", "Balaramgadi"),
                isEnabledInPrefs = weatherPrefs.notifyCyclone
            )
        )

        return candidates.map { item ->
            val baseSim = computeContentBasedSimilarity(
                profile = profile,
                itemCategories = item.featureTags,
                itemLocalities = listOf(item.locality)
            )
            var score = baseSim.toDouble()
            var reasonEn = "Matched to your Balasore weather & locality profile"
            var reasonOdia = "ଆପଣଙ୍କ ବାଲେଶ୍ୱର ପାଣିପାଗ ଓ ଅଞ୍ଚଳ ଆଗ୍ରହ ଆଧାରରେ"

            val matchingSavedSpot = savedSpots.firstOrNull { spot ->
                item.featureTags.any { tag ->
                    spot.category.equals(tag, ignoreCase = true) ||
                        spot.location.contains(tag, ignoreCase = true) ||
                        spot.name.contains(tag, ignoreCase = true)
                }
            }
            if (matchingSavedSpot != null) {
                score += 9.0
                reasonEn = "Because you saved ${matchingSavedSpot.name} (${matchingSavedSpot.location})"
                reasonOdia = "ଆପଣ '${matchingSavedSpot.odiaName}' ସେଭ୍ କରିଥିବାରୁ"
            } else if (savedLocalities.any { item.locality.contains(it, ignoreCase = true) }) {
                score += 7.0
                reasonEn = "Active weather watch for your saved locality (${item.locality})"
                reasonOdia = "ଆପଣଙ୍କ ସଂରକ୍ଷିତ ଅଞ୍ଚଳ (${item.locality}) ପାଇଁ ପାଣିପାଗ ସୂଚନା"
            }

            if (item.isEnabledInPrefs) {
                score += 5.0
            }

            RecommendedWeatherAlertItem(
                id = item.id,
                titleEn = item.titleEn,
                titleOdia = item.titleOdia,
                advisoryEn = item.advisoryEn,
                advisoryOdia = item.advisoryOdia,
                locality = item.locality,
                alertCategory = item.alertCategory,
                severity = item.severity,
                matchScorePercent = score.roundToInt().coerceIn(81, 99),
                reasonEn = reasonEn,
                reasonOdia = reasonOdia,
                matchedFeatures = item.featureTags.take(3)
            )
        }
            .sortedByDescending { it.matchScorePercent }
            .take(limit)
    }

    fun getTopAffinityTags(context: Context): List<Pair<String, Int>> {
        val profile = loadBehaviorProfile(context)
        val combined = mutableMapOf<String, Int>()
        profile.categoryAffinities.forEach { (k, v) -> combined[k] = (combined[k] ?: 0) + v }
        profile.localityAffinities.forEach { (k, v) -> combined[k] = (combined[k] ?: 0) + v }
        return combined.entries
            .sortedByDescending { it.value }
            .take(6)
            .map { it.key to it.value }
    }
}
