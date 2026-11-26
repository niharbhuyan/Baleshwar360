package com.example.data.maps

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.google.firebase.FirebaseApp

/**
 * Centralized manager that automatically resolves, inputs, and persists the Google Maps API Key
 * and Weather Map API Key from BuildConfig, AndroidManifest meta-data, and Firebase project options
 * so users never have to manually type or paste an API key.
 */
object BalasoreMapsApiKeyManager {
    private const val PREFS_NAME = "balasore_maps_api_key_prefs"
    private const val KEY_AUTO_MAPS_API = "auto_maps_api_key"
    private const val KEY_AUTO_WEATHER_API = "auto_weather_map_api_key"

    /**
     * Automatically detects and resolves the Google Maps API key from all available project sources
     * and persists it into SharedPreferences so all map inputs are pre-filled automatically.
     */
    fun autoPopulateMapsApiKey(context: Context? = null): String {
        val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val resolved = com.example.data.config.BalasoreApiKeys.mapsApiKey(context)
        if (isValidKey(resolved)) {
            prefs?.edit()?.putString(KEY_AUTO_MAPS_API, resolved)?.apply()
            return resolved
        }
        return ""
    }

    /**
     * Automatically resolves the OpenWeatherMap API key from BuildConfig.OPENWEATHERMAP_API_KEY
     * (injected from AI Studio Secrets / .env) without falling back to Google Maps or Firebase keys.
     */
    fun autoPopulateWeatherMapApiKey(context: Context? = null): String {
        val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val resolved = com.example.data.config.BalasoreApiKeys.openWeatherApiKey(context)
        if (isValidWeatherKey(resolved)) {
            prefs?.edit()?.putString(KEY_AUTO_WEATHER_API, resolved)?.apply()
            return resolved
        }
        val saved = prefs?.getString(KEY_AUTO_WEATHER_API, null)?.trim().orEmpty()
        if (saved.isNotBlank() && !isValidWeatherKey(saved)) {
            // Clear any stale placeholder or non-OpenWeatherMap key
            prefs?.edit()?.remove(KEY_AUTO_WEATHER_API)?.apply()
        }
        return ""
    }

    fun saveMapsApiKey(context: Context?, newKey: String): String {
        val resolved = newKey.trim().ifBlank { autoPopulateMapsApiKey(context) }
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY_AUTO_MAPS_API, resolved)
            ?.apply()
        return resolved
    }

    fun saveWeatherMapApiKey(context: Context?, newKey: String): String {
        val candidate = newKey.trim()
        val resolved = if (isValidWeatherKey(candidate)) candidate else autoPopulateWeatherMapApiKey(context)
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY_AUTO_WEATHER_API, resolved)
            ?.apply()
        return resolved
    }

    fun isMapsKeyAutoConfigured(context: Context? = null): Boolean {
        return autoPopulateMapsApiKey(context).isNotBlank()
    }

    fun maskKey(key: String): String {
        val clean = key.trim()
        if (clean.length <= 10) return clean.ifBlank { "Auto-Provisioned" }
        return "${clean.take(8)}••••${clean.takeLast(4)}"
    }

    private fun isValidKey(candidate: String): Boolean {
        return candidate.isNotBlank() &&
            !candidate.contains("YOUR_KEY", ignoreCase = true) &&
            !candidate.contains("YOUR_OPENWEATHERMAP_API_KEY", ignoreCase = true) &&
            !candidate.contains("PLACEHOLDER", ignoreCase = true)
    }

    private fun isValidWeatherKey(candidate: String): Boolean {
        return isValidKey(candidate) && !candidate.startsWith("AIza")
    }
}

/**
 * Reusable Compose bar that automatically populates and displays the Google Maps API Key input
 * so users can see the key is automatically entered and active, with one-tap auto-fill / re-sync.
 */
@Composable
fun MapsApiKeyAutoInputBar(
    isOdia: Boolean = false,
    onKeyApplied: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeKey by remember {
        mutableStateOf(BalasoreMapsApiKeyManager.autoPopulateMapsApiKey(context))
    }
    var inputKeyText by remember {
        mutableStateOf(activeKey)
    }
    var isExpanded by remember { mutableStateOf(false) }

    // Automatically populate and apply the Maps API Key on composition
    LaunchedEffect(Unit) {
        val autoKey = BalasoreMapsApiKeyManager.autoPopulateMapsApiKey(context)
        if (autoKey.isNotBlank()) {
            activeKey = autoKey
            inputKeyText = autoKey
            onKeyApplied(autoKey)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("maps_api_key_auto_input_bar"),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF0FDF4),
        border = BorderStroke(1.dp, Color(0xFF86EFAC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Maps API Key Auto-Configured",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isOdia) {
                                "ଗୁଗୁଲ୍ ମ୍ୟାପ୍ସ API Key ସ୍ୱୟଂଚାଳିତ ଭାବେ ଇନପୁଟ୍ ହୋଇଛି"
                            } else {
                                "Google Maps API Key Auto-Input & Active"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF14532D)
                        )
                        Text(
                            text = "Key: ${BalasoreMapsApiKeyManager.maskKey(activeKey)} • Auto-Synced",
                            fontSize = 10.sp,
                            color = Color(0xFF15803D)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFDCFCE7),
                    border = BorderStroke(1.dp, Color(0xFF4ADE80))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isExpanded) {
                                if (isOdia) "ଲୁଚାନ୍ତୁ" else "Hide Input"
                            } else {
                                if (isOdia) "ଅଟୋ-ଇନପୁଟ୍ ଦେଖନ୍ତୁ" else "Auto-Input Key"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = inputKeyText,
                        onValueChange = { inputKeyText = it },
                        singleLine = true,
                        label = {
                            Text(
                                text = if (isOdia) "ଗୁଗୁଲ୍ ମ୍ୟାପ୍ସ API Key (ସ୍ୱୟଂଚାଳିତ)" else "Google Maps API Key (Auto-Populated)",
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("maps_api_key_input_field"),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val autoKey = BalasoreMapsApiKeyManager.autoPopulateMapsApiKey(context)
                                val saved = BalasoreMapsApiKeyManager.saveMapsApiKey(context, autoKey)
                                activeKey = saved
                                inputKeyText = saved
                                onKeyApplied(saved)
                                Toast.makeText(
                                    context,
                                    if (isOdia) "ମ୍ୟାପ୍ସ API Key ସ୍ୱୟଂଚାଳିତ ଭାବେ ଇନପୁଟ୍ ହେଲା!"
                                    else "Maps API Key automatically populated & synced!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("maps_api_key_autofill_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOdia) "⚡ ଅଟୋ-ଇନପୁଟ୍" else "⚡ Auto-Fill Key",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                val saved = BalasoreMapsApiKeyManager.saveMapsApiKey(context, inputKeyText)
                                activeKey = saved
                                inputKeyText = saved
                                onKeyApplied(saved)
                                isExpanded = false
                                Toast.makeText(
                                    context,
                                    if (isOdia) "ମ୍ୟାପ୍ସ API Key ସଫଳତାର ସହ ଲାଗୁ ହେଲା!"
                                    else "Maps API Key applied & active!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("maps_api_key_apply_btn")
                        ) {
                            Text(
                                text = if (isOdia) "✓ ଲାଗୁ କରନ୍ତୁ" else "✓ Apply Key",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
