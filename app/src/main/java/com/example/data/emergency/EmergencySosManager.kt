package com.example.data.emergency

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.Locale

/**
 * Pre-set emergency contact numbers and profiles for the Balasore SOS feature.
 */
data class EmergencySosContact(
    val id: String,
    val nameEn: String,
    val nameOr: String,
    val relationEn: String,
    val relationOr: String,
    val phoneNumber: String,
    val isPrimary: Boolean = true
)

/**
 * Result data model emitted when an SOS trigger executes.
 */
data class SosTriggerResult(
    val isSuccess: Boolean,
    val latitude: Double,
    val longitude: Double,
    val locationDescription: String,
    val nearestPoliceStationName: String,
    val nearestPoliceNumber: String,
    val smsRecipients: List<String>,
    val smsMessageBody: String,
    val callInitiated: Boolean,
    val errorMessage: String? = null
)

/**
 * Core Emergency SOS Dispatch Engine.
 *
 * Coordinates:
 * 1. Immediate GPS coordinate acquisition (fine location fallback to coarse & last-known).
 * 2. Pre-set emergency contacts SMS dispatch with Google Maps link and timestamp.
 * 3. Nearest police control room automatic telephony dialing intent.
 */
object EmergencySosManager {

    private const val TAG = "EmergencySosManager"

    // Default Pre-set Emergency Contacts for Balasore Citizens
    val DEFAULT_SOS_CONTACTS = listOf(
        EmergencySosContact(
            id = "sos_contact_1",
            nameEn = "Family Emergency Contact",
            nameOr = "ପରିବାର ସଦସ୍ୟ (ଜରୁରୀକାଳୀନ)",
            relationEn = "Primary Guardian / Relative",
            relationOr = "ଅଭିଭାବକ",
            phoneNumber = "9437012345",
            isPrimary = true
        ),
        EmergencySosContact(
            id = "sos_contact_2",
            nameEn = "Secondary Emergency Guardian",
            nameOr = "ଦ୍ୱିତୀୟ ସହାୟତା ସମ୍ପର୍କୀୟ",
            relationEn = "Trusted Local Contact",
            relationOr = "ସ୍ଥାନୀୟ ବନ୍ଧୁ",
            phoneNumber = "9861098765",
            isPrimary = false
        )
    )

    // Balasore Central Police Control Rooms
    const val POLICE_CONTROL_ROOM_112 = "112"
    const val BALASORE_SP_OFFICE_CONTROL = "06782-262026"
    const val BALASORE_TOWN_POLICE_PS = "06782-262022"

    /**
     * Resolves the user's best GPS coordinates with immediate fallback to cached/last-known.
     */
    @SuppressLint("MissingPermission")
    fun getCurrentCoordinates(
        context: Context,
        onLocationResolved: (Double, Double, String) -> Unit
    ) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        val defaultLat = 21.4934
        val defaultLng = 86.9135
        val defaultName = "Balasore City Center / Station"

        if (!hasFine && !hasCoarse) {
            onLocationResolved(defaultLat, defaultLng, defaultName)
            return
        }

        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager == null) {
                onLocationResolved(defaultLat, defaultLng, defaultName)
                return
            }

            // Check providers: GPS, Network, Passive
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )

            var bestLocation: Location? = null
            for (provider in providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        if (bestLocation == null || loc.time > bestLocation.time) {
                            bestLocation = loc
                        }
                    }
                }
            }

            if (bestLocation != null) {
                val desc = "Balasore Lat: %.4f, Lng: %.4f".format(Locale.US, bestLocation.latitude, bestLocation.longitude)
                onLocationResolved(bestLocation.latitude, bestLocation.longitude, desc)
                return
            }

            // If no cached location, request quick one-shot update or fall back
            var callbackTriggered = false
            val handler = Handler(Looper.getMainLooper())
            val timeoutRunnable = Runnable {
                if (!callbackTriggered) {
                    callbackTriggered = true
                    onLocationResolved(defaultLat, defaultLng, defaultName)
                }
            }
            handler.postDelayed(timeoutRunnable, 1500)

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (!callbackTriggered) {
                        callbackTriggered = true
                        handler.removeCallbacks(timeoutRunnable)
                        try {
                            locationManager.removeUpdates(this)
                        } catch (_: Exception) {}
                        val desc = "Balasore Lat: %.4f, Lng: %.4f".format(Locale.US, location.latitude, location.longitude)
                        onLocationResolved(location.latitude, location.longitude, desc)
                    }
                }
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            }

            val activeProvider = when {
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                else -> null
            }

            if (activeProvider != null) {
                locationManager.requestSingleUpdate(activeProvider, listener, Looper.getMainLooper())
            } else {
                handler.removeCallbacks(timeoutRunnable)
                onLocationResolved(defaultLat, defaultLng, defaultName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location: ${e.message}")
            onLocationResolved(defaultLat, defaultLng, defaultName)
        }
    }

    /**
     * Determines nearest police station or control room from EmergencyServicesRepository.
     */
    fun getNearestPoliceControlRoom(lat: Double, lng: Double): EmergencyServiceLocation {
        val nearestPolice = EmergencyServicesRepository.getNearestServices(
            userLat = lat,
            userLng = lng,
            categoryFilter = EmergencyCategory.POLICE
        ).firstOrNull()

        return nearestPolice ?: EmergencyServiceLocation(
            id = "pol_control_112",
            name = "Balasore Police Control Room (Dial 112)",
            odiaName = "ବାଲେଶ୍ୱର ପୋଲିସ କଣ୍ଟ୍ରୋଲ୍ ରୁମ୍ (୧୧୨)",
            category = EmergencyCategory.POLICE,
            latitude = 21.4930,
            longitude = 86.9240,
            address = "Balasore Police District Headquarters, Cinema Chhak",
            odiaAddress = "ବାଲେଶ୍ୱର ପୋଲିସ ମୁଖ୍ୟାଳୟ",
            phone = POLICE_CONTROL_ROOM_112,
            is24x7 = true,
            capacityOrBeds = "24/7 Mobile Quick Response Team & PCR Patrol",
            facilities = listOf("Emergency 112 Dispatch", "Immediate PCR Dispatch", "Live Radio Desk")
        )
    }

    /**
     * Composes the emergency SOS message text with live Google Maps coordinate link.
     */
    fun buildSosMessage(lat: Double, lng: Double, locationDesc: String): String {
        val mapsUrl = "https://maps.google.com/?q=%.5f,%.5f".format(Locale.US, lat, lng)
        return "EMERGENCY SOS! I need immediate help. My current GPS location: $mapsUrl ($locationDesc). Please alert Balasore Police or send help urgently!"
    }

    /**
     * Dispatches SMS to pre-set emergency contacts.
     * Uses SmsManager if SEND_SMS permission is granted, otherwise falls back to ACTION_SENDTO intent.
     */
    fun sendSosSms(
        context: Context,
        recipients: List<String>,
        message: String
    ): Boolean {
        val hasSmsPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasSmsPermission) {
            try {
                @Suppress("DEPRECATION")
                val smsManager = SmsManager.getDefault()
                val parts = smsManager.divideMessage(message)
                for (number in recipients) {
                    val cleanNumber = number.replace("-", "").replace(" ", "").trim()
                    if (parts.size > 1) {
                        smsManager.sendMultipartTextMessage(cleanNumber, null, parts, null, null)
                    } else {
                        smsManager.sendTextMessage(cleanNumber, null, message, null, null)
                    }
                }
                return true
            } catch (e: Exception) {
                Log.e(TAG, "SmsManager send failed, launching fallback intent: ${e.message}")
            }
        }

        // Fallback: Launch SMS application with pre-filled numbers and text
        try {
            val cleanNumbers = recipients.joinToString(";") { it.replace("-", "").trim() }
            val smsUri = Uri.parse("smsto:$cleanNumbers")
            val intent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Fallback SMS intent failed: ${e.message}")
            return false
        }
    }

    /**
     * Initiates immediate call to the nearest police station or 112 control room.
     */
    fun dialPoliceControlRoom(context: Context, phoneNumber: String): Boolean {
        return try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${phoneNumber.replace("-", "").trim()}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(dialIntent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Dial intent failed: ${e.message}")
            false
        }
    }

    /**
     * Initiates an immediate direct call to Balasore Emergency Helpline 112 using CALL_PHONE permission.
     * If CALL_PHONE permission is granted, uses Intent.ACTION_CALL for immediate 1-tap dialing.
     * Otherwise or upon security fallback, falls back to Intent.ACTION_DIAL.
     */
    fun makeDirectEmergencyCall(context: Context, phoneNumber: String = POLICE_CONTROL_ROOM_112): Boolean {
        val cleanNumber = phoneNumber.replace("-", "").trim()
        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCallPermission) {
            try {
                val callIntent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$cleanNumber")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(callIntent)
                return true
            } catch (e: Exception) {
                Log.w(TAG, "Direct ACTION_CALL failed, falling back to ACTION_DIAL: ${e.message}")
            }
        }

        return dialPoliceControlRoom(context, cleanNumber)
    }

    /**
     * Retrieves saved emergency contacts or defaults.
     */
    fun getSavedSosContacts(context: Context): List<EmergencySosContact> {
        val prefs = context.getSharedPreferences("balasore_sos_prefs", Context.MODE_PRIVATE)
        val count = prefs.getInt("contact_count", -1)
        if (count <= 0) return DEFAULT_SOS_CONTACTS

        val list = mutableListOf<EmergencySosContact>()
        for (i in 0 until count) {
            val id = prefs.getString("contact_${i}_id", "contact_$i") ?: "contact_$i"
            val name = prefs.getString("contact_${i}_name", "Contact ${i + 1}") ?: "Contact ${i + 1}"
            val phone = prefs.getString("contact_${i}_phone", "") ?: ""
            val relation = prefs.getString("contact_${i}_relation", "Emergency Contact") ?: "Emergency Contact"
            if (phone.isNotBlank()) {
                list.add(
                    EmergencySosContact(
                        id = id,
                        nameEn = name,
                        nameOr = name,
                        relationEn = relation,
                        relationOr = relation,
                        phoneNumber = phone,
                        isPrimary = i == 0
                    )
                )
            }
        }
        return if (list.isNotEmpty()) list else DEFAULT_SOS_CONTACTS
    }

    /**
     * Persists customized emergency contacts.
     */
    fun saveSosContacts(context: Context, contacts: List<EmergencySosContact>) {
        val editor = context.getSharedPreferences("balasore_sos_prefs", Context.MODE_PRIVATE).edit()
        editor.putInt("contact_count", contacts.size)
        contacts.forEachIndexed { i, c ->
            editor.putString("contact_${i}_id", c.id)
            editor.putString("contact_${i}_name", c.nameEn)
            editor.putString("contact_${i}_phone", c.phoneNumber)
            editor.putString("contact_${i}_relation", c.relationEn)
        }
        editor.apply()
    }

    /**
     * Evaluates if a spoken transcript matches the emergency trigger command.
     */
    fun isEmergencyVoiceCommand(spokenText: String): Boolean {
        val lower = spokenText.lowercase().trim()
        val keywords = listOf(
            "call balasore emergency",
            "call emergency",
            "balasore emergency",
            "emergency",
            "call 112",
            "dial 112",
            "112",
            "police",
            "sos",
            "help",
            "ambulance",
            "ପୋଲିସ",
            "ଜରୁରୀକାଳୀନ",
            "ସାହାଯ୍ୟ କରନ୍ତୁ",
            "ଏମରଜେନ୍ସି"
        )
        return keywords.any { lower.contains(it) }
    }

    /**
     * Persistent SOS Overlay Trigger:
     * Instantly initiates a phone call to 112, resolves GPS location,
     * and sends coordinates with Google Maps link to pre-configured emergency contacts.
     */
    fun triggerInstantSosOverlayAction(
        context: Context,
        onStatus: (String) -> Unit
    ) {
        val contacts = getSavedSosContacts(context)
        // 1. Immediately initiate 112 phone call
        val callSuccess = makeDirectEmergencyCall(context, POLICE_CONTROL_ROOM_112)
        onStatus("Calling 112 Emergency Control Room...")

        // 2. Concurrently resolve GPS and dispatch location SMS to emergency contacts
        getCurrentCoordinates(context) { lat, lng, locationDesc ->
            val recipientNumbers = contacts.map { it.phoneNumber }
            val messageBody = buildSosMessage(lat, lng, locationDesc)
            val smsSuccess = sendSosSms(context, recipientNumbers, messageBody)
            Log.i(TAG, "Instant SOS: 112 called ($callSuccess), SMS sent to $recipientNumbers ($smsSuccess)")
            onStatus("112 Dialed. GPS location sent to ${recipientNumbers.size} emergency contacts.")
        }
    }

    /**
     * Main Trigger Method: Executes GPS coordinate resolution, SMS sharing, and police phone call.
     */
    fun triggerSos(
        context: Context,
        customContacts: List<EmergencySosContact> = DEFAULT_SOS_CONTACTS,
        onComplete: (SosTriggerResult) -> Unit
    ) {
        getCurrentCoordinates(context) { lat, lng, locationDesc ->
            val nearestPolice = getNearestPoliceControlRoom(lat, lng)
            val recipientNumbers = customContacts.map { it.phoneNumber }
            val messageBody = buildSosMessage(lat, lng, locationDesc)

            val smsSuccess = sendSosSms(context, recipientNumbers, messageBody)
            val callSuccess = dialPoliceControlRoom(context, nearestPolice.phone)

            val result = SosTriggerResult(
                isSuccess = true,
                latitude = lat,
                longitude = lng,
                locationDescription = locationDesc,
                nearestPoliceStationName = nearestPolice.name,
                nearestPoliceNumber = nearestPolice.phone,
                smsRecipients = recipientNumbers,
                smsMessageBody = messageBody,
                callInitiated = callSuccess
            )
            onComplete(result)
        }
    }
}
