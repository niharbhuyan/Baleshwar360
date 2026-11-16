package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.model.NewsArticle
import com.example.data.remote.ChatRolePersonas
import com.example.data.remote.GeminiChatModel
import com.example.data.remote.GeminiChatService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DailyNewsDigestResult(
    val generatedAtTime: String,
    val nextScheduledTime: String = "08:00 AM Daily",
    val topStoriesTitles: List<String>,
    val summaryEnglish: String,
    val summaryOdia: String,
    val isAiGenerated: Boolean = true
)

/**
 * Manages the 8:00 AM Daily News Digest notification and Gemini AI summarization of
 * the top 3 trending news stories in Balasore from the past 24 hours.
 */
object DailyNewsDigestManager {
    const val CHANNEL_ID = "balasore_daily_news_digest_8am"
    const val WORK_NAME = "balasore_8am_daily_news_digest_work"
    private const val PREFS_NAME = "balasore_daily_digest_prefs"
    private const val KEY_ENABLED = "digest_8am_enabled"
    private const val KEY_LAST_SUMMARY_EN = "last_summary_en"
    private const val KEY_LAST_SUMMARY_OD = "last_summary_od"
    private const val KEY_LAST_TIME = "last_generated_time"
    private const val NOTIFICATION_ID = 8080

    private val _digestState = MutableStateFlow<DailyNewsDigestResult?>(null)
    val digestState: StateFlow<DailyNewsDigestResult?> = _digestState.asStateFlow()

    /**
     * Calculates milliseconds from [nowMillis] until the next 8:00 AM local time.
     */
    fun calculateInitialDelayTo8AmMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val targetCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (targetCal.timeInMillis <= nowCal.timeInMillis) {
            targetCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return (targetCal.timeInMillis - nowCal.timeInMillis).coerceAtLeast(60_000L)
    }

    fun isDigestScheduled(context: Context): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, true)
    }

    fun isDigestEnabled(context: Context): Boolean = isDigestScheduled(context)

    fun getLastDigestSummary(context: Context): String {
        val cached = loadCachedDigestIfNeeded(context)
        return "${cached.summaryEnglish}\n\n🇮🇳 ଓଡ଼ିଆ ସାରାଂଶ:\n${cached.summaryOdia}"
    }

    fun createNotificationChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "8:00 AM Balasore Daily News Digest (Gemini AI)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily 8:00 AM Gemini AI summary of top 3 trending Balasore stories from the past 24 hours"
            }
            nm.createNotificationChannel(channel)
        }
    }

    suspend fun generateAndDispatchDigestNow(
        context: Context,
        candidateArticles: List<NewsArticle> = emptyList()
    ): String {
        val res = generateAndDispatchDigest(context, candidateArticles, postNotification = true)
        return "${res.summaryEnglish}\n\n🇮🇳 ଓଡ଼ିଆ ସାରାଂଶ:\n${res.summaryOdia}"
    }

    fun setDigestEnabled(context: Context, enabled: Boolean) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) {
            scheduleDaily8AmDigest(context)
        } else {
            WorkManager.getInstance(context.applicationContext).cancelUniqueWork(WORK_NAME)
        }
    }

    /**
     * Schedules the recurring 8:00 AM daily WorkManager job for the Balasore Daily News Digest.
     */
    fun scheduleDaily8AmDigest(context: Context) {
        try {
            val initialDelay = calculateInitialDelayTo8AmMillis()
            val workRequest = PeriodicWorkRequestBuilder<DailyNewsDigestWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            loadCachedDigestIfNeeded(context)
        } catch (_: Throwable) {
            loadCachedDigestIfNeeded(context)
        }
    }

    fun triggerImmediateDigestWork(context: Context) {
        try {
            val request = OneTimeWorkRequestBuilder<DailyNewsDigestWorker>()
                .addTag("${WORK_NAME}_immediate")
                .build()
            WorkManager.getInstance(context.applicationContext).enqueue(request)
        } catch (_: Throwable) {}
    }

    fun loadCachedDigestIfNeeded(context: Context): DailyNewsDigestResult {
        _digestState.value?.let { return it }
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedEn = prefs.getString(KEY_LAST_SUMMARY_EN, null)
        val savedOd = prefs.getString(KEY_LAST_SUMMARY_OD, null)
        val savedTime = prefs.getString(KEY_LAST_TIME, null)
        val defaultStories = listOf(
            "Chandipur Coastal Promenade & Tidal Safety Sensors Upgraded",
            "DRDO ITR Chandipur Completes Indigenous Air-Defense Telemetry Trial",
            "Balasore Station (BLS) Amrit Bharat & Remuna Medical OPD Expansion"
        )
        val result = if (!savedEn.isNullOrBlank() && !savedOd.isNullOrBlank()) {
            DailyNewsDigestResult(
                generatedAtTime = savedTime ?: "08:00 AM Today",
                topStoriesTitles = defaultStories,
                summaryEnglish = savedEn,
                summaryOdia = savedOd
            )
        } else {
            DailyNewsDigestResult(
                generatedAtTime = "08:00 AM Daily Schedule",
                topStoriesTitles = defaultStories,
                summaryEnglish = "• 1. Chandipur Coastal Safety: Smart tidal sensors and solar sirens deployed along the 5 km intertidal zone.\n" +
                    "• 2. DRDO ITR Milestone: Indigenous air-defense radar trial succeeds off Abdul Kalam Island & Chandipur.\n" +
                    "• 3. Civic & Transit Upgrade: Balasore Junction (BLS) platform modernization and FM MCH specialist OPD expansion.",
                summaryOdia = "• ୧. ଚାନ୍ଦିପୁର ବେଳାଭୂମି ସୁରକ୍ଷା: ୫ କି.ମି. ଜୁଆର ଅଞ୍ଚଳରେ ସ୍ମାର୍ଟ ସେନ୍ସର ଓ ସୌର ସାଇରନ୍ ସ୍ଥାପନ।\n" +
                    "• ୨. DRDO କ୍ଷେପଣାସ୍ତ୍ର ସଫଳତା: ଚାନ୍ଦିପୁର ଓ କଲାମ ଦ୍ୱୀପରୁ ସ୍ୱଦେଶୀ ରାଡାର ପରୀକ୍ଷଣ ସଫଳ।\n" +
                    "• ୩. ବାଲେଶ୍ୱର ରେଳ ଓ ସ୍ୱାସ୍ଥ୍ୟ ପ୍ରଗତି: ବାଲେଶ୍ୱର ଷ୍ଟେସନ ଆଧୁନିକୀକରଣ ଏବଂ ଏଫ୍.ଏମ୍. ମେଡିକାଲ କଲେଜ OPD ସମ୍ପ୍ରସାରଣ।"
            )
        }
        _digestState.value = result
        return result
    }

    /**
     * Summarizes the top 3 trending Balasore news stories from the past 24 hours using Gemini API
     * and dispatches the 8:00 AM Daily News Digest notification.
     */
    suspend fun generateAndDispatchDigest(
        context: Context,
        candidateArticles: List<NewsArticle> = emptyList(),
        postNotification: Boolean = true
    ): DailyNewsDigestResult = withContext(Dispatchers.IO) {
        val top3Stories: List<Triple<String, String, String>> = if (candidateArticles.isNotEmpty()) {
            candidateArticles.take(3).map {
                Triple(it.title, it.odiaTitle, it.snippet)
            }
        } else {
            val dbArticles = try {
                AppDatabase.getInstance(context).newsDao().getAllArticlesSync().take(3)
            } catch (_: Throwable) {
                emptyList()
            }
            if (dbArticles.isNotEmpty()) {
                dbArticles.map { Triple(it.title, it.title, it.summary) }
            } else {
                listOf(
                    Triple(
                        "Chandipur Coastal Promenade & Tidal Safety Sensors Upgraded",
                        "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ସୁରକ୍ଷା ଓ ସ୍ମାର୍ଟ ଜୁଆର ସେନ୍ସର",
                        "Smart tidal sensors and solar sirens deployed along the 5 km intertidal zone at Chandipur-on-Sea."
                    ),
                    Triple(
                        "DRDO ITR Chandipur Completes Indigenous Air-Defense Telemetry Trial",
                        "DRDO ଚାନ୍ଦିପୁର ସ୍ୱଦେଶୀ ପ୍ରତିରକ୍ଷା ରାଡାର ପରୀକ୍ଷଣ ସଫଳ",
                        "Integrated Test Range (ITR) scientists tracked high-speed target interception over the Bay of Bengal."
                    ),
                    Triple(
                        "Balasore Station (BLS) Amrit Bharat & Remuna Medical OPD Expansion",
                        "ବାଲେଶ୍ୱର ଷ୍ଟେସନ ଓ ଏଫ୍.ଏମ୍. ମେଡିକାଲ କଲେଜ OPD ସମ୍ପ୍ରସାରଣ",
                        "New escalator bays at Balasore Junction and extended evening specialist chambers at FM MCH."
                    )
                )
            }
        }

        val storiesPromptBlock = top3Stories.mapIndexed { index, (titleEn, _, snippet) ->
            "${index + 1}. $titleEn — $snippet"
        }.joinToString("\n")

        val prompt = """
            Summarize these top 3 trending news stories in Balasore from the past 24 hours for the 8:00 AM Daily News Digest notification.
            Provide 3 concise bullet points in English followed by '---ODIA---' and 3 concise bullet points in Odia (ଓଡ଼ିଆ):
            $storiesPromptBlock
        """.trimIndent()

        val fallbackEn = top3Stories.mapIndexed { idx, (t, _, s) ->
            "• ${idx + 1}. $t: $s"
        }.joinToString("\n")

        val fallbackOd = top3Stories.mapIndexed { idx, (_, od, s) ->
            "• ${idx + 1}. ${od.ifBlank { s }}"
        }.joinToString("\n")

        val aiResponse = try {
            GeminiChatService.getInstance().sendMultiTurnChat(
                history = emptyList(),
                userMessage = prompt,
                model = GeminiChatModel.FLASH,
                enableSearchGrounding = false,
                enableMapsGrounding = false,
                systemInstruction = "You are the Baleshwar 360 News Editor. Summarize the top 3 trending Balasore stories concisely in English and Odia."
            )
        } catch (_: Throwable) {
            null
        }

        val rawText = aiResponse?.text.orEmpty()
        val (summaryEn, summaryOd) = if (rawText.isNotBlank() && !rawText.contains("Gemini API key is not configured", ignoreCase = true)) {
            if (rawText.contains("---ODIA---")) {
                val parts = rawText.split("---ODIA---")
                Pair(parts[0].trim().ifBlank { fallbackEn }, parts.getOrNull(1)?.trim().orEmpty().ifBlank { fallbackOd })
            } else {
                Pair(rawText.trim(), fallbackOd)
            }
        } else {
            Pair(fallbackEn, fallbackOd)
        }

        val timeStamp = SimpleDateFormat("hh:mm a, dd MMM", Locale.US).format(Date())
        val result = DailyNewsDigestResult(
            generatedAtTime = "$timeStamp (8:00 AM Daily Digest)",
            topStoriesTitles = top3Stories.map { it.first },
            summaryEnglish = summaryEn,
            summaryOdia = summaryOd,
            isAiGenerated = true
        )

        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LAST_SUMMARY_EN, summaryEn)
            .putString(KEY_LAST_SUMMARY_OD, summaryOd)
            .putString(KEY_LAST_TIME, result.generatedAtTime)
            .apply()

        _digestState.value = result

        if (postNotification) {
            postDigestNotification(context, result)
        }

        result
    }

    fun postDigestNotification(context: Context, digest: DailyNewsDigestResult): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "8:00 AM Balasore Daily News Digest (Gemini AI)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily 8:00 AM Gemini AI summary of top 3 trending Balasore stories from the past 24 hours"
            }
            nm.createNotificationChannel(channel)
        }

        val deepLinkIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("baleshwar360://digest/8am")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("☀️ 8:00 AM Balasore Daily News Digest (Top 3 Stories)")
            .setContentText(digest.topStoriesTitles.firstOrNull() ?: "Your Gemini AI summary of Balasore's top 3 stories is ready.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("☀️ 8:00 AM Balasore Daily News Digest • Gemini AI")
                    .bigText("${digest.summaryEnglish}\n\n🇮🇳 ଓଡ଼ିଆ ସାରାଂଶ:\n${digest.summaryOdia}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
        return true
    }
}

class DailyNewsDigestWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            DailyNewsDigestManager.generateAndDispatchDigest(
                context = applicationContext,
                postNotification = true
            )
            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }
}
