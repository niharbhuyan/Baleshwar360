package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.BalasoreWeatherAlert
import com.example.data.model.NewsArticle
import com.example.data.remote.EmergencyAlertDto

/**
 * Utility helper to invoke Android's native system ShareSheet (ACTION_SEND + Intent.createChooser).
 * Formats rich messages for sharing local news and emergency weather/civic alerts with others.
 */
object ShareHelper {

    /**
     * Builds formatted share text for a news article including deep link.
     */
    fun buildNewsShareText(
        title: String,
        snippet: String,
        source: String,
        category: String,
        odiaTitle: String = "",
        articleId: String = "latest"
    ): Pair<String, String> {
        val safeId = articleId.ifBlank { "latest" }
        val deepLinkUrl = "https://baleshwar360.app/news/$safeId"
        val appSchemeLink = "baleshwar360://news/$safeId"
        val subject = "Balasore News: $title"
        val builder = StringBuilder()
        builder.append("📰 ").append(title).append("\n")
        if (odiaTitle.isNotBlank() && odiaTitle != title) {
            builder.append("🇮🇳 ").append(odiaTitle).append("\n")
        }
        builder.append("\n")
        if (snippet.isNotBlank()) {
            builder.append(snippet).append("\n\n")
        }
        builder.append("Category: ").append(category).append("\n")
        builder.append("Source: ").append(source).append("\n")
        builder.append("🔗 Open in App: ").append(deepLinkUrl).append("\n")
        builder.append("📲 Deep Link: ").append(appSchemeLink).append("\n\n")
        builder.append("Shared via Baleshwar 360 App - Hyperlocal News & Alerts")
        return Pair(subject, builder.toString().trim())
    }

    /**
     * Builds pre-formatted WhatsApp message containing article title, snippet, and app deep link.
     */
    fun buildNewsWhatsAppMessage(
        article: NewsArticle,
        isOdia: Boolean = false
    ): String {
        val primaryTitle = if (isOdia && article.odiaTitle.isNotBlank()) article.odiaTitle else article.title
        val primarySnippet = if (isOdia && article.odiaSnippet.isNotBlank()) article.odiaSnippet else article.snippet
        val safeId = article.id.ifBlank { "latest" }
        val deepLinkUrl = "https://baleshwar360.app/news/$safeId"
        val appSchemeLink = "baleshwar360://news/$safeId"
        return buildString {
            append("*📰 ").append(primaryTitle).append("*\n")
            if (!isOdia && article.odiaTitle.isNotBlank() && article.odiaTitle != primaryTitle) {
                append("_").append(article.odiaTitle).append("_\n")
            }
            append("\n")
            append(primarySnippet).append("\n\n")
            append("🏷️ *").append(article.category).append("* • ").append(article.source).append("\n")
            append("🔗 *Read on Baleshwar 360:* ").append(deepLinkUrl).append("\n")
            append("📲 *App Deep Link:* ").append(appSchemeLink)
        }
    }

    /**
     * Specialized 'Share via WhatsApp' action that constructs a pre-formatted message
     * containing the article title, snippet, and a deep link to the app.
     */
    fun shareNewsViaWhatsApp(
        context: Context,
        article: NewsArticle,
        isOdia: Boolean = false
    ) {
        val message = buildNewsWhatsAppMessage(article, isOdia)
        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(whatsappIntent)
        } catch (_: Exception) {
            // Fallback to system share sheet if WhatsApp is not installed on the device/emulator
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Balasore News: ${article.title}")
                putExtra(Intent.EXTRA_TEXT, message)
            }
            val chooser = Intent.createChooser(fallbackIntent, "Share via WhatsApp").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }

    /**
     * Builds formatted share text and deep link for an individual .feature-box utility or topic,
     * consistent with the News Card share functionality.
     */
    fun buildFeatureBoxShareText(
        featureKey: String,
        title: String,
        odiaTitle: String = "",
        description: String,
        category: String,
        isOdia: Boolean = false
    ): Pair<String, String> {
        val slug = featureKey.lowercase().replace(" ", "_")
        val deepLinkUrl = "https://baleshwar360.app/feature/$slug"
        val appSchemeLink = "baleshwar360://feature/$slug"
        val primaryTitle = if (isOdia && odiaTitle.isNotBlank()) odiaTitle else title
        val subject = "Baleshwar 360 Feature: $primaryTitle"
        val builder = StringBuilder()
        builder.append("✨ ").append(primaryTitle).append("\n")
        if (odiaTitle.isNotBlank() && odiaTitle != primaryTitle) {
            builder.append("🇮🇳 ").append(odiaTitle).append("\n")
        }
        builder.append("\n")
        if (description.isNotBlank()) {
            builder.append(description).append("\n\n")
        }
        builder.append("Category: ").append(category).append("\n")
        builder.append("🔗 Open Utility in App: ").append(deepLinkUrl).append("\n")
        builder.append("📲 Deep Link: ").append(appSchemeLink).append("\n\n")
        builder.append("Shared via Baleshwar 360 App - Hyperlocal News & Utilities")
        return Pair(subject, builder.toString().trim())
    }

    /**
     * Shares a deep link to a specific .feature-box utility or topic via Android's ShareSheet.
     */
    fun shareFeatureBoxDeepLink(
        context: Context,
        featureKey: String,
        title: String,
        odiaTitle: String = "",
        description: String,
        category: String,
        isOdia: Boolean = false
    ) {
        val (subject, text) = buildFeatureBoxShareText(
            featureKey = featureKey,
            title = title,
            odiaTitle = odiaTitle,
            description = description,
            category = category,
            isOdia = isOdia
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Baleshwar 360 Feature").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Share a local Balasore news article via Android's ShareSheet.
     */
    fun shareNewsArticle(
        context: Context,
        title: String,
        snippet: String,
        source: String,
        category: String,
        odiaTitle: String = "",
        articleId: String = "latest"
    ) {
        val (subject, text) = buildNewsShareText(title, snippet, source, category, odiaTitle, articleId)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Balasore News").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Share a NewsArticle model object via Android's ShareSheet.
     */
    fun shareNews(context: Context, article: NewsArticle, isOdia: Boolean = false) {
        val title = if (isOdia && article.odiaTitle.isNotBlank()) article.odiaTitle else article.title
        val snippet = if (isOdia && article.odiaSnippet.isNotBlank()) article.odiaSnippet else article.snippet
        shareNewsArticle(
            context = context,
            title = title,
            snippet = snippet,
            source = article.source,
            category = article.category,
            odiaTitle = if (!isOdia) article.odiaTitle else "",
            articleId = article.id
        )
    }

    /**
     * Builds formatted share text for an emergency alert.
     */
    fun buildEmergencyAlertText(alert: EmergencyAlertDto): Pair<String, String> {
        val isCyclone = alert.type.contains("CYCLONE", ignoreCase = true)
        val alertTypeLabel = if (isCyclone) "LIVE CYCLONE WARNING" else "RIVER SPIKE ALERT"
        val subject = "🚨 BALESHWAR 360 EMERGENCY ALERT: ${alert.title}"

        val builder = StringBuilder()
        builder.append("🚨 BALESHWAR 360 EMERGENCY ALERT 🚨\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("⚠️ ").append(alert.title).append("\n")
        if (alert.odiaTitle.isNotBlank()) {
            builder.append("🇮🇳 ").append(alert.odiaTitle).append("\n")
        }
        builder.append("Status: ").append(alertTypeLabel).append("\n\n")

        if (alert.summary.isNotBlank()) {
            builder.append(alert.summary).append("\n\n")
        }

        if (alert.windSpeedKmph != null) {
            builder.append("💨 Wind Speed: ").append(alert.windSpeedKmph).append(" km/h gusts\n")
        }
        if (alert.waterLevelMeters != null && alert.dangerLevelMeters != null) {
            builder.append("🌊 Water Level: ").append(alert.waterLevelMeters).append("m (Danger: ").append(alert.dangerLevelMeters).append("m)\n")
        }

        if (alert.actionRequired.isNotBlank()) {
            builder.append("\n🛑 Required Action:\n").append(alert.actionRequired).append("\n")
        }

        if (alert.emergencyHelpline.isNotBlank()) {
            builder.append("\n📞 Control Room Helpline: ").append(alert.emergencyHelpline).append("\n")
        }

        builder.append("\nShared via Baleshwar 360 App - Stay safe and alert your community!")
        return Pair(subject, builder.toString().trim())
    }

    /**
     * Share a live emergency alert (e.g., Cyclone Warning, River Level Spike) via Android's ShareSheet.
     */
    fun shareEmergencyAlert(
        context: Context,
        alert: EmergencyAlertDto
    ) {
        val (subject, text) = buildEmergencyAlertText(alert)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Emergency Alert").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Builds formatted share text for a time-sensitive weather alert.
     */
    fun buildWeatherAlertText(
        alert: BalasoreWeatherAlert,
        validityRemainingText: String = ""
    ): Pair<String, String> {
        val subject = "⚠️ Weather Alert: ${alert.title} - Baleshwar 360"
        val builder = StringBuilder()
        builder.append("⚠️ BALESHWAR 360 WEATHER ALERT\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        builder.append(alert.category.emoji).append(" ").append(alert.title).append("\n")
        if (alert.odiaTitle.isNotBlank()) {
            builder.append(alert.odiaTitle).append("\n")
        }
        builder.append("Severity: ").append(alert.severity.levelName).append("\n")
        if (validityRemainingText.isNotBlank()) {
            builder.append("Validity: ").append(validityRemainingText).append("\n")
        }
        builder.append("\n").append(alert.summary).append("\n\n")

        if (alert.affectedZones.isNotEmpty()) {
            builder.append("📍 Affected Zones: ")
                .append(alert.affectedZones.joinToString(", ") { it.zoneName })
                .append("\n\n")
        }

        if (alert.actionableInstructions.isNotEmpty()) {
            builder.append("🛡️ Safety Guidelines:\n")
            alert.actionableInstructions.take(3).forEachIndexed { i, step ->
                builder.append("${i + 1}. ").append(step).append("\n")
            }
            builder.append("\n")
        }

        if (alert.emergencyContact.isNotBlank()) {
            builder.append("📞 Helpline: ").append(alert.emergencyContact).append("\n")
        }
        builder.append("Authority: ").append(alert.issuingAuthority).append("\n\n")
        builder.append("Shared via Baleshwar 360 App")
        return Pair(subject, builder.toString().trim())
    }

    /**
     * Share a time-sensitive weather alert via Android's ShareSheet.
     */
    fun shareWeatherAlert(
        context: Context,
        alert: BalasoreWeatherAlert,
        validityRemainingText: String = ""
    ) {
        val (subject, text) = buildWeatherAlertText(alert, validityRemainingText)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Weather Alert").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Share DRDO ITR Missile Launch Alert & Coastal Fishermen Safety Advisory via Android's ShareSheet.
     */
    fun shareDrdoAdvisory(
        context: Context,
        advisory: com.example.data.model.DrdoAdvisory,
        isOdia: Boolean = false
    ) {
        val title = if (isOdia && advisory.odiaTitle.isNotBlank()) advisory.odiaTitle else advisory.title
        val subject = "🚀 DRDO ITR Coastal Advisory: $title"
        val builder = StringBuilder()
        builder.append("🚀 DRDO ITR CHANDIPUR DEFENSE & MARITIME ADVISORY\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("📍 ").append(title).append("\n")
        if (advisory.odiaTitle.isNotBlank() && !isOdia) {
            builder.append("🇮🇳 ").append(advisory.odiaTitle).append("\n")
        }
        builder.append("Status: ").append(advisory.status).append("\n")
        builder.append("Date & Time: ").append(advisory.dateOrTime).append("\n")
        builder.append("Sea Zone: ").append(advisory.seaZone).append("\n\n")

        builder.append("🐟 Coastal Fishermen Advisory:\n")
        builder.append(advisory.fishermenAdvisory).append("\n\n")

        if (advisory.sonicBoomWarning) {
            builder.append("⚡ Caution: Potential Sonic Boom or High-Decibel Acoustic Warning in effect.\n\n")
        }

        advisory.heritageMilestone?.let {
            builder.append("⭐ Milestone: ").append(it).append("\n\n")
        }

        builder.append("Shared via Baleshwar 360 App - Maritime Safety & Local Defense Updates")
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, builder.toString().trim())
        }
        val chooser = Intent.createChooser(sendIntent, "Share Coastal Defense Advisory").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Share Chandipur Beach High Tide & Safe Walk Window advisory via Android's ShareSheet.
     */
    fun shareTideAlert(
        context: Context,
        status: String,
        walkWindow: String,
        highTideTime: String,
        isOdia: Boolean = false
    ) {
        val subject = "🌊 Chandipur Vanishing Sea & High Tide Advisory"
        val builder = StringBuilder()
        builder.append("🌊 CHANDIPUR BEACH TIDAL & WALK WINDOW ADVISORY 🌊\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("🏖️ Status: ").append(status).append("\n")
        builder.append("🚶 Safe Walk Window: ").append(walkWindow).append("\n")
        builder.append("⏰ High Tide Return Time: ").append(highTideTime).append("\n\n")
        builder.append("⚠️ Safety Note: Sea water returns rapidly during high tide. Return to the beach embankment before the safe walk window expires.\n\n")
        builder.append("Shared via Baleshwar 360 App - Chandipur Coastal Safety")

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, builder.toString().trim())
        }
        val chooser = Intent.createChooser(sendIntent, "Share Chandipur Tide Alert").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Share an Emergency Helpline contact card via Android's ShareSheet.
     */
    fun shareEmergencyContact(
        context: Context,
        title: String,
        department: String,
        phoneNumber: String,
        isOdia: Boolean = false
    ) {
        val subject = "🚨 Baleshwar Emergency Helpline: $title"
        val builder = StringBuilder()
        builder.append("🚨 BALESHWAR EMERGENCY CONTACT\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("🏢 ").append(title).append("\n")
        if (department.isNotBlank()) {
            builder.append("Department: ").append(department).append("\n")
        }
        builder.append("📞 Direct Helpline: ").append(phoneNumber).append("\n\n")
        builder.append("Save this contact or dial immediately in case of emergency.\n")
        builder.append("Shared via Baleshwar 360 App - Local Civic & Emergency Safety")

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, builder.toString().trim())
        }
        val chooser = Intent.createChooser(sendIntent, "Share Emergency Helpline").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Share River Gauge Water Level & Flood Early Warning alert via Android's ShareSheet.
     */
    fun shareRiverGaugeAlert(
        context: Context,
        gauge: com.example.data.model.RiverGauge,
        isOdia: Boolean = false
    ) {
        val river = if (isOdia && gauge.odiaRiverName.isNotBlank()) gauge.odiaRiverName else gauge.riverName
        val subject = "🌊 Baleshwar Flood Alert: $river Level (${gauge.status})"
        val builder = StringBuilder()
        builder.append("🌊 BALESHWAR 360 RIVER WATER LEVEL & FLOOD EARLY WARNING\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("📍 River: ").append(river).append("\n")
        builder.append("🏢 Station: ").append(gauge.stationName).append("\n")
        builder.append("📊 Current Level: ").append(gauge.currentLevelMeters).append(" meters\n")
        builder.append("⚠️ Warning Level: ").append(gauge.warningLevelMeters).append(" meters\n")
        builder.append("🚨 Danger Level: ").append(gauge.dangerLevelMeters).append(" meters\n")
        builder.append("📌 Status: ").append(gauge.status).append("\n")
        builder.append("📈 Trend: ").append(gauge.trend).append("\n\n")
        if (gauge.status.equals("DANGER", ignoreCase = true) || gauge.status.equals("ALERT", ignoreCase = true)) {
            builder.append("⚡ Urgent: Water level is near or exceeding warning threshold. Stay tuned to SRC Odisha & District Emergency Office (1077).\n\n")
        }
        builder.append("Shared via Baleshwar 360 App - Central Water Commission & District Disaster Management")

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, builder.toString().trim())
        }
        val chooser = Intent.createChooser(sendIntent, "Share River Flood Advisory").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Generic helper to share custom formatted text via Android's native ShareSheet.
     */
    fun shareCustomText(
        context: Context,
        title: String,
        text: String
    ) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(chooser) }
    }
}


