package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.PhonelinkRing
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.NightlightRound
import com.example.ui.components.OfflineVectorRasterMapDashboardCard
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.admob.AdMobManager
import com.example.data.model.AppLanguage
import com.example.data.model.EmergencyContact
import com.example.data.model.SeafoodCatch
import com.example.data.model.TransitSchedule
import com.example.ui.components.AdMobBannerCard
import com.example.ui.components.AdMobVerificationCard
import com.example.ui.components.AppWatermarkOverlay
import com.example.ui.components.EmergencySosLongPressCard
import com.example.ui.components.BalasorePamphletScannerSheet
import com.example.data.pamphlet.PamphletCategory
import androidx.compose.material.icons.filled.QrCodeScanner
import com.example.ui.components.WatermarkOpacity
import com.example.ui.components.WatermarkStyle
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate400
import com.example.ui.theme.BentoSlate500
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import com.example.ui.components.LanguageSwitcherBanner
import com.example.ui.components.SettingsLanguageToggleCard
import com.example.ui.components.CitizenVoicePollingCard
import com.example.util.BalasoreHapticHelper

import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Share
import com.example.ui.viewmodel.ThemeMode
import com.example.ui.viewmodel.UniqueFeatureSheetType
import com.example.util.PlayStoreUpdateManager
import com.example.util.UpdateUIState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EssentialsScreen(
    emergencyContacts: List<EmergencyContact>,
    transitList: List<TransitSchedule>,
    seafoodCatches: List<SeafoodCatch>,
    language: AppLanguage,
    updateState: UpdateUIState = UpdateUIState.Idle,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    isWatermarkEnabled: Boolean = true,
    watermarkStyle: WatermarkStyle = WatermarkStyle.CENTER_EMBLEM,
    watermarkOpacity: WatermarkOpacity = WatermarkOpacity.MEDIUM,
    onWatermarkToggle: (Boolean) -> Unit = {},
    onWatermarkStyleChange: (WatermarkStyle) -> Unit = {},
    onWatermarkOpacityChange: (WatermarkOpacity) -> Unit = {},
    isHourlyAutoRefreshEnabled: Boolean = true,
    lastHourlyRefreshTimestamp: Long = System.currentTimeMillis(),
    nextHourlyRefreshMinutesRemaining: Int = 60,
    autoRefreshCycleCount: Int = 0,
    onHourlyAutoRefreshToggle: (Boolean) -> Unit = {},
    onTriggerManualHourlyRefresh: () -> Unit = {},
    onCheckForUpdates: () -> Unit = {},
    onTriggerUpdate: () -> Unit = {},
    onCompleteUpdate: () -> Unit = {},
    onLanguageChange: (AppLanguage) -> Unit = {},
    onToggleLanguage: () -> Unit = {},
    onOpenFeatureSheet: (UniqueFeatureSheetType) -> Unit = {},
    savedEmergencyContactIds: Set<String> = emptySet(),
    onToggleFavoriteContact: (EmergencyContact) -> Unit = {},
    isBatterySavingModeEnabled: Boolean = false,
    onBatterySavingModeToggle: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var subscribedRouteIds by remember { mutableStateOf(setOf<String>()) }
    var showEmergencyPamphletScanner by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("essentials_screen_list"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.ODIA) "ଜରୁରୀକାଳୀନ ଓ ସହାୟତା ଡିରେକ୍ଟୋରୀ" else "Emergency & City Essentials",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = BentoSlate900
                    )
                )
                Text(
                    text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ଡାକ୍ତରଖାନା, ପୋଲିସ, ବାହାବଳପୁର ମାଛ ବଜାର ଓ ପରିବହନ" else "Hospitals, police, fresh harbor seafood index & transit schedules",
                    style = MaterialTheme.typography.bodyMedium.copy(color = BentoSlate500)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Application Settings: Battery-Saving Mode Toggle Card (Throttles News & Bus Schedule Background Syncs)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("essentials_battery_saving_mode_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBatterySavingModeEnabled) Color(0xFFECFDF5) else Color(0xFFF8FAFC)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isBatterySavingModeEnabled) Color(0xFF10B981) else Color(0xFFCBD5E1)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (language == AppLanguage.ODIA) {
                                        "🔋 ବ୍ୟାଟେରୀ ସେଭିଂ ମୋଡ୍ (Battery-Saving Mode)"
                                    } else {
                                        "🔋 Battery-Saving Mode (App Settings)"
                                    },
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp,
                                    color = if (isBatterySavingModeEnabled) Color(0xFF065F46) else BentoSlate900
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (isBatterySavingModeEnabled) {
                                    if (language == AppLanguage.ODIA) {
                                        "ସକ୍ରିୟ: ଖବର (୩୦ ମି.) ଓ ବସ୍ ସମୟ ସାରଣୀ (୬୦ ସେ.) ବ୍ୟାକଗ୍ରାଉଣ୍ଡ ସିଙ୍କ୍ ହ୍ରାସ କରାଯାଇଛି"
                                    } else {
                                        "Enabled: Background data syncs reduced for News (30m) & Bus Schedules (60s) to extend battery life"
                                    }
                                } else {
                                    if (language == AppLanguage.ODIA) {
                                        "ବ୍ୟାଟେରୀ ବଞ୍ଚାଇବା ପାଇଁ ଖବର ଓ ବସ୍ ସମୟ ସାରଣୀ ବ୍ୟାକଗ୍ରାଉଣ୍ଡ ସିଙ୍କ୍ ହ୍ରାସ କରନ୍ତୁ"
                                    } else {
                                        "Reduce background data sync frequency for News & Bus Schedule modules to extend device life"
                                    }
                                },
                                fontSize = 11.sp,
                                color = if (isBatterySavingModeEnabled) Color(0xFF047857) else BentoSlate600
                            )
                        }
                        Switch(
                            checked = isBatterySavingModeEnabled,
                            onCheckedChange = { enabled ->
                                onBatterySavingModeToggle(enabled)
                            },
                            modifier = Modifier.testTag("essentials_battery_saving_mode_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Launch Utilities
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.TOTO_AUTO_FARE_CARD) }
                            .testTag("essentials_toto_card_btn"),
                        color = Color(0xFFFEF9C3),
                        border = BorderStroke(1.dp, Color(0xFFFDE047))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🛺", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଟୋଟୋ ଭଡ଼ା" else "Toto Fare",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF854D0E)
                                )
                                Text("RTA Chart", fontSize = 9.sp, color = Color(0xFFA16207))
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.BALASORE_CAMPUS_CAREER_BOARD) }
                            .testTag("essentials_campus_btn"),
                        color = Color(0xFFE0F2FE),
                        border = BorderStroke(1.dp, Color(0xFF7DD3FC))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎓", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "କ୍ୟାମ୍ପସ୍ ବସ୍" else "FMU Campus",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF075985)
                                )
                                Text("Bus & Notice", fontSize = 9.sp, color = Color(0xFF0369A1))
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.BALASORE_AUTO_UPDATE_CENTER) }
                            .testTag("essentials_auto_sync_btn"),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔄", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଲାଇଭ୍ ସିଙ୍କ୍" else "Auto-Sync",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                                Text("Every 15m", fontSize = 9.sp, color = Color(0xFF15803D))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Second Quick Launch Utilities Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.BALASORE_JUNCTION_RADAR) }
                            .testTag("essentials_junction_radar_btn"),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFF93C5FD))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🚆", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଟ୍ରେନ୍ ରାଡାର" else "Rail Radar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                                Text("BLS Junction", fontSize = 9.sp, color = Color(0xFF2563EB))
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.NIGHT_CHEMIST_SANJEEVANI) }
                            .testTag("essentials_night_rx_btn"),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💊", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "୨୪h ଔଷଧାଳୟ" else "24h Chemist",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                                Text("Oxygen SOS", fontSize = 9.sp, color = Color(0xFFDC2626))
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.TPNODL_WATCO_MONITOR) }
                            .testTag("essentials_power_water_btn"),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚡", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ବିଦ୍ୟୁତ୍/ଜଳ" else "Power/Water",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text("Feeder Status", fontSize = 9.sp, color = Color(0xFFB45309))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Third Quick Launch Utilities Row: Civic, Health & Marine Safety
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.MO_SEVA_KENDRA_CITIZEN) }
                            .testTag("essentials_moseva_btn"),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🌾", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ମୋ ସେବା" else "Mo Seva",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                                Text("e-District", fontSize = 9.sp, color = Color(0xFF15803D))
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.FM_MCH_MEDICAL_COLLEGE_OPD) }
                            .testTag("essentials_fmmch_opd_btn"),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFECACA))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏥", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଏଫଏମ MCH" else "FM MCH OPD",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                                Text("Specialists", fontSize = 9.sp, color = Color(0xFFDC2626))
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.MARINE_FISHERMEN_SAFETY) }
                            .testTag("essentials_marine_safety_btn"),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚓", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ସମୁଦ୍ର SOS" else "Marine SOS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                                Text("Coast Guard", fontSize = 9.sp, color = Color(0xFF2563EB))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fourth Quick Launch Utilities Row: Nature, SHG Crafts & Scholarships
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.KULDIHA_ECO_CAMP_SAFARI) }
                            .testTag("essentials_kuldiha_safari_btn"),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🐘", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "କୁଲଡିହା ସଫାରୀ" else "Kuldiha Safari",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF14532D)
                                )
                                Text("Rissia Camp", fontSize = 9.sp, color = Color(0xFF16A34A))
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.SABAI_GRASS_MISSION_SHAKTI) }
                            .testTag("essentials_sabai_craft_btn"),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🧺", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ସବାଇ ଶିଳ୍ପ" else "Sabai Craft",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text("SHG Haat", fontSize = 9.sp, color = Color(0xFFD97706))
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.BALASORE_STUDENT_CAREER_SCHOLARSHIP) }
                            .testTag("essentials_scholarship_btn"),
                        color = Color(0xFFF0F9FF),
                        border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎓", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଛାତ୍ରବୃତ୍ତି" else "Scholarships",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                )
                                Text("Career Desk", fontSize = 9.sp, color = Color(0xFF0284C7))
                            }
                        }
                    }
                }
            }
        }

        // 7-Day OpenWeatherMap Forecast Component in 'City Essentials' Tab
        item {
            com.example.ui.components.BalasoreSevenDayWeatherForecastCard(
                language = language,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // GPS-Aware 'Quick SOS' Nearest Active Emergency Contacts Panel with 1-Tap Calling
        item {
            var selectedGpsZoneIndex by remember { mutableStateOf(0) }
            val gpsZones = listOf(
                Triple("Station Square", 21.4942, 86.9317),
                Triple("FM Medical College", 21.5014, 86.9125),
                Triple("Chandipur Coast", 21.4442, 87.0219),
                Triple("Remuna Golei NH-16", 21.5198, 86.8854)
            )
            val activeZone = gpsZones[selectedGpsZoneIndex]

            data class NearestSosContact(
                val id: String,
                val nameEn: String,
                val nameOdia: String,
                val roleBadge: String,
                val phone: String,
                val lat: Double,
                val lon: Double,
                val accentColor: Color
            )

            val rawSosContacts = remember {
                listOf(
                    NearestSosContact(
                        id = "police_112",
                        nameEn = "Balasore Police Control Room (ERSS 112)",
                        nameOdia = "ବାଲେଶ୍ୱର ପୋଲିସ କଣ୍ଟ୍ରୋଲ୍ ରୁମ୍ (୧୧୨)",
                        roleBadge = "24x7 ACTIVE • PATROL",
                        phone = "112",
                        lat = 21.4928,
                        lon = 86.9335,
                        accentColor = Color(0xFFDC2626)
                    ),
                    NearestSosContact(
                        id = "trauma_108",
                        nameEn = "FM Medical College Trauma & 108 Ambulance",
                        nameOdia = "ଏଫ୍.ଏମ୍ ମେଡିକାଲ ଟ୍ରମା ଓ ୧୦୮ ଆମ୍ବୁଲାନ୍ସ",
                        roleBadge = "24x7 ICU • TRAUMA",
                        phone = "108",
                        lat = 21.5010,
                        lon = 86.9130,
                        accentColor = Color(0xFF0284C7)
                    ),
                    NearestSosContact(
                        id = "fire_odraf_101",
                        nameEn = "Balasore Fire & ODRAF Disaster Rescue",
                        nameOdia = "ବାଲେଶ୍ୱର ଅଗ୍ନିଶମ ଓ ODRAF ଉଦ୍ଧାର ଦଳ",
                        roleBadge = "24x7 ACTIVE • RESCUE",
                        phone = "101",
                        lat = 21.4965,
                        lon = 86.9260,
                        accentColor = Color(0xFFEA580C)
                    ),
                    NearestSosContact(
                        id = "coast_guard_1554",
                        nameEn = "Chandipur Marine Police & Coast Guard SOS",
                        nameOdia = "ଚାନ୍ଦିପୁର ମେରାଇନ୍ ପୋଲିସ ଓ କୋଷ୍ଟ ଗାର୍ଡ",
                        roleBadge = "COASTAL • MARINE SOS",
                        phone = "1554",
                        lat = 21.4460,
                        lon = 87.0195,
                        accentColor = Color(0xFF0D9488)
                    )
                )
            }

            fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
                val r = 6371.0
                val dLat = Math.toRadians(lat2 - lat1)
                val dLon = Math.toRadians(lon2 - lon1)
                val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                    kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                    kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
                val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
                return r * c
            }

            val sortedNearestContacts = remember(selectedGpsZoneIndex) {
                rawSosContacts.map { contact ->
                    val distKm = haversineKm(activeZone.second, activeZone.third, contact.lat, contact.lon)
                    val etaMins = kotlin.math.max(2, (distKm * 2.1).toInt())
                    Triple(contact, distKm, etaMins)
                }.sortedBy { it.second }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("quick_sos_gps_nearest_panel"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (language == AppLanguage.ODIA)
                                        "🚨 କୁଇକ୍ SOS • ନିକଟତମ ଜରୁରୀକାଳୀନ ସହାୟତା"
                                    else
                                        "🚨 Quick SOS • Nearest Active Emergency Units",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "📍 GPS Lock: ${activeZone.first} (${String.format(Locale.US, "%.4f°N, %.4f°E", activeZone.second, activeZone.third)})",
                                fontSize = 10.5.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDC2626)
                        ) {
                            Text(
                                text = "GPS LIVE",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // GPS Zone Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        gpsZones.forEachIndexed { idx, zone ->
                            val isSelected = selectedGpsZoneIndex == idx
                            Surface(
                                onClick = {
                                    BalasoreHapticHelper.vibrateToggle(context)
                                    selectedGpsZoneIndex = idx
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFFEF4444) else Color(0xFF1E293B),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFFFCA5A5) else Color(0xFF334155)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_sos_gps_zone_chip_$idx")
                            ) {
                                Text(
                                    text = zone.first.substringBefore(" "),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subtle pulse animation for Quick SOS 1-tap dialer buttons
                    val quickSosPulseTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "quick_sos_dialer_pulse")
                    val quickSosDialerScale by quickSosPulseTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.055f,
                        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                            animation = androidx.compose.animation.core.tween(
                                durationMillis = 950,
                                easing = androidx.compose.animation.core.FastOutSlowInEasing
                            ),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                        ),
                        label = "quick_sos_dialer_scale"
                    )
                    val quickSosHaloAlpha by quickSosPulseTransition.animateFloat(
                        initialValue = 0.35f,
                        targetValue = 0.90f,
                        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                            animation = androidx.compose.animation.core.tween(
                                durationMillis = 950,
                                easing = androidx.compose.animation.core.FastOutSlowInEasing
                            ),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                        ),
                        label = "quick_sos_halo_alpha"
                    )

                    // Sorted Nearest Active Emergency Contacts with 1-Tap Calling
                    sortedNearestContacts.forEach { (contact, distKm, etaMins) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, contact.accentColor.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("quick_sos_nearest_card_${contact.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = contact.accentColor.copy(alpha = 0.22f)
                                        ) {
                                            Text(
                                                text = contact.roleBadge,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFF8FAFC),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f km", distKm)} • ~$etaMins min ETA",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF34D399)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = if (language == AppLanguage.ODIA) contact.nameOdia else contact.nameEn,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Button(
                                    onClick = {
                                        BalasoreHapticHelper.vibrateEmergencySos(context)
                                        runCatching {
                                            context.startActivity(
                                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}")).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = contact.accentColor),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.2.dp, Color.White.copy(alpha = quickSosHaloAlpha)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .scale(quickSosDialerScale)
                                        .testTag("quick_sos_call_btn_${contact.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call ${contact.phone}",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Call ${contact.phone}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SOS Home Screen Widget Preview & Quick Emergency Actions (Police, Fire, Ambulance & SOS SMS Trigger)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("essentials_sos_home_widget_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    "🚨 SOS ହୋମ୍ ସ୍କ୍ରିନ୍ ୱିଜେଟ୍ (SOS Home Widget)"
                                } else {
                                    "🚨 SOS Home Screen Widget & Quick Dial"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.5.sp,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    "ପୋଲିସ୍ (୧୧୨), ଅଗ୍ନିଶମ (୧୦୧), ଆମ୍ବୁଲାନ୍ସ (୧୦୮) ଓ ଜରୁରୀ GPS ମେସେଜ୍"
                                } else {
                                    "1-tap Police (112), Fire (101), Ambulance (108) & Emergency GPS Message Trigger"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFFB91C1C)
                            )
                        }
                        Surface(
                            onClick = {
                                com.example.widget.BalasoreSosWidgetProvider.updateAllWidgets(context)
                                val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
                                val provider = android.content.ComponentName(
                                    context,
                                    com.example.widget.BalasoreSosWidgetProvider::class.java
                                )
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                                    appWidgetManager.isRequestPinAppWidgetSupported
                                ) {
                                    appWidgetManager.requestPinAppWidget(provider, null, null)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "SOS Widget refreshed! Long-press Home Screen -> Widgets -> Baleshwar 360 Emergency SOS",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFDC2626),
                            modifier = Modifier.testTag("pin_sos_home_widget_btn")
                        ) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "+ ୱିଜେଟ୍ ଯୋଡ଼ନ୍ତୁ" else "+ Pin Widget",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val sosWidgetPulseTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "sos_widget_dial_pulse")
                    val sosWidgetScale by sosWidgetPulseTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.045f,
                        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                            animation = androidx.compose.animation.core.tween(
                                durationMillis = 1050,
                                easing = androidx.compose.animation.core.FastOutSlowInEasing
                            ),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                        ),
                        label = "sos_widget_scale"
                    )
                    val sosWidgetBorderAlpha by sosWidgetPulseTransition.animateFloat(
                        initialValue = 0.45f,
                        targetValue = 1.0f,
                        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                            animation = androidx.compose.animation.core.tween(
                                durationMillis = 1050,
                                easing = androidx.compose.animation.core.FastOutSlowInEasing
                            ),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                        ),
                        label = "sos_widget_border_alpha"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("🚓 Police", "112", "sos_widget_call_police_btn"),
                            Triple("🚒 Fire", "101", "sos_widget_call_fire_btn"),
                            Triple("🚑 Ambulance", "108", "sos_widget_call_ambulance_btn")
                        ).forEach { (label, number, tag) ->
                            Surface(
                                onClick = {
                                    BalasoreHapticHelper.vibrateEmergencySos(context)
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = sosWidgetBorderAlpha)),
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(sosWidgetScale)
                                    .testTag(tag)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF991B1B))
                                    Text("Dial $number", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Offline Voice-Guided Coastal Evacuation Companion (Odia/English TTS)
        item {
            com.example.ui.components.VoiceGuidedEvacuationCompanionCard(
                language = language,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Live Subarnarekha & Budhabalanga River Flood Gauge Monitor & Widget Launcher
        item {
            com.example.ui.components.LiveRiverFloodGaugeCard(
                language = language,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Interactive Artisan Marketplace & GI Craft Passport
        item {
            com.example.ui.components.ArtisanMarketplaceGiPassportCard(
                language = language,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Balasore Movie Theatres, Multiplexes & Live Showtimes Directory
        item {
            com.example.ui.components.BalasoreMovieTheatresCard(
                language = language,
                onOpenFullSheet = {
                    onOpenFeatureSheet(UniqueFeatureSheetType.BALASORE_MOVIE_THEATRES)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Real-Time Local Bus Tracker Component (Simulated API Service for Balasore Regional Public Transport)
        item {
            com.example.ui.components.LocalBusTracker(
                language = language,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                onRouteSelected = { _ ->
                    // Inline stop timeline expands automatically; users can also open full transit hub from quick actions
                }
            )
        }

        // Interactive Citizen Voice Polling Component (Voting on Local Initiatives with Offline Room & Firestore Cloud Sync)
        item {
            CitizenVoicePollingCard(
                language = language,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Citizen Civic Eye Hero Card (Potholes, Drainage & Municipal Issues)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.CITIZEN_CIVIC_EYE) }
                    .testTag("essentials_citizen_civic_eye_hero_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📸", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ସିଟିଜେନ୍ ସିଭିକ୍ ଆଇ" else "Citizen Civic Eye",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.25f))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text("ROOM DB", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                                    }
                                }
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ରାସ୍ତା ଖାଲ, ଡ୍ରେନେଜ୍ ଓ ଆବର୍ଜନା ଜିଓ-ଫଟୋ ସହ ଦାଖଲ କରନ୍ତୁ" else "Report Potholes & Drainage with Geolocated Photos",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Citizen Civic Eye",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🕳️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Road Potholes", fontSize = 10.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌊", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Drain Clogs", fontSize = 10.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("GPS Tagged", fontSize = 10.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Citizen Contribution Leaderboard Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.CITIZEN_CONTRIBUTION_LEADERBOARD) }
                    .testTag("essentials_citizen_leaderboard_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFFBEB)
                ),
                border = BorderStroke(1.dp, Color(0xFFFCD34D))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏆", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ନାଗରିକ ଯୋଗଦାନ ମାନ୍ୟତା" else "Citizen Contribution Leaderboard",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFD97706).copy(alpha = 0.15f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ସମ୍ମାନ" else "HONORS",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                            Text(
                                text = if (language == AppLanguage.ODIA) "ଦାଖଲ ଓ ସମାଧାନ ହୋଇଥିବା ଅଭିଯୋଗ ଆଧାରରେ ବାଲେଶ୍ୱର ପୌରପାଳିକା ସ୍ଥାନ" else "Rankings, points & achievement badges for resolved civic issues",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Leaderboard",
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // FEATURE 5: Bahabalpur & Kasafal Fresh Seafood Catch Index & Harbor Rates
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("seafood_catch_index_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFBAE6FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SetMeal,
                                    contentDescription = "Seafood",
                                    tint = OceanBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ବାହାବଳପୁର ଓ କସାଫଳ ମତ୍ସ୍ୟ ସୂଚକ" else "Harbor Fresh Seafood Index",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BentoSlate900,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = "Bahabalpur & Kasafal Landing Harbors",
                                    style = MaterialTheme.typography.labelSmall.copy(color = BentoSlate500)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "FRESH TODAY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFB45309),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tip banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF0FDF4))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚓ Wholesale harbor auction: 05:30 AM – 08:30 AM daily at Bahabalpur Jetty",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF166534),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    seafoodCatches.forEach { catchItem ->
                        SeafoodCatchItemView(item = catchItem, language = language)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { onOpenFeatureSheet(UniqueFeatureSheetType.HARBOR_CATCH_RATES) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_harbor_rates_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🐟", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Live Harbor Trawler Bells & Daily Price Ticker", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Immediate GPS SOS Long-Press Action Card
        item {
            EmergencySosLongPressCard(
                language = language,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Official Disaster & Emergency Pamphlets (ML Kit QR Scanner) Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { showEmergencyPamphletScanner = true }
                    .testTag("card_emergency_pamphlet_scanner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Emergency Pamphlet QR",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "ଜରୁରୀକାଳୀନ ପାମ୍ପଲେଟ୍ କ୍ୟୁଆର୍ ସ୍କାନର୍" else "Emergency Pamphlet QR Scanner",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ML KIT",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA)
                                "ବାଲେଶ୍ୱର ଜିଲ୍ଲା ବାତ୍ୟା ସୁରକ୍ଷା, ODRAF ବିପର୍ଯ୍ୟୟ SOP ଓ FM ମେଡିକାଲ ହେଲ୍ପଲାଇନ ପାମ୍ପଲେଟ୍ ସ୍କାନ୍ କରନ୍ତୁ"
                            else
                                "Scan Balasore DDMA Cyclone & Flood SOP, ODRAF protocols & FM Medical Trauma emergency pamphlets",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Emergency Section Title
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = "Emergency",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Emergency Helplines (24x7)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate900
                    )
                )
            }
        }

        // Live Nearest Emergency Services Map Visualizer Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.NEAREST_EMERGENCY_SERVICES_MAP) }
                    .testTag("card_nearest_emergency_services_map"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.5.dp, Color(0xFFFCA5A5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ନିକଟତମ ଜରୁରୀକାଳୀନ ମ୍ୟାପ୍" else "Nearest Emergency Services Map",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF991B1B)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFDC2626)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA) "ଆପଣଙ୍କ ସ୍ଥିତି ଅନୁସାରେ ଥାନା, ହସ୍ପିଟାଲ ଓ ଆଶ୍ରୟସ୍ଥଳୀର ଦୂରତା ଓ ନେଭିଗେସନ୍" else "Hospitals, police & shelters relative to your coordinate with driving ETA",
                                fontSize = 11.5.sp,
                                color = Color(0xFF7F1D1D),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Offline Vector Raster Map Provider Dashboard Component with visibility & mode controls
        item {
            OfflineVectorRasterMapDashboardCard(
                language = language,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                onOpenFullMap = { onOpenFeatureSheet(UniqueFeatureSheetType.BALASORE_OFFLINE_MAP_TILES) },
                initialIsVisible = true,
                initialIsOffline = true
            )
        }

        // Pinned Quick Access: Saved Emergency Numbers (Room DB)
        val savedContacts = emergencyContacts.filter { savedEmergencyContactIds.contains(it.id) }
        if (savedContacts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("saved_emergency_contacts_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ପ୍ରିୟ ଜରୁରୀକାଳୀନ ନମ୍ବର (ତ୍ୱରିତ ଡାଏଲ୍)" else "Saved Quick-Dial Numbers",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = "${savedContacts.size} Saved",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Emergency Contacts List
        items(emergencyContacts, key = { it.id }) { contact ->
            EmergencyContactCard(
                contact = contact,
                language = language,
                isSaved = savedEmergencyContactIds.contains(contact.id),
                onToggleFavorite = { onToggleFavoriteContact(contact) },
                onCall = {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.number}"))
                    context.startActivity(dialIntent)
                }
            )
        }

        // Balasore Medical & Healthcare Network Card (Daily Auto-Updated)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("essentials_medical_network_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                border = BorderStroke(1.dp, Color(0xFFBAE6FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🩺", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Medical & Healthcare Network",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE0F2FE))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "DAILY AUTO-SYNC",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Real-time doctor OPD schedules, private hospitals & nursing homes, 24x7 pharmacies, generic stores, and diagnostic pathology labs across Balasore.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF0C4A6E),
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onOpenFeatureSheet(UniqueFeatureSheetType.PRIVATE_HOSPITAL_DIRECTORY) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("🏥 Hospitals", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onOpenFeatureSheet(UniqueFeatureSheetType.DOCTORS_DIRECTORY) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("🩺 Doctors", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onOpenFeatureSheet(UniqueFeatureSheetType.MEDICINE_STORES_DIRECTORY) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("💊 Medicine", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onOpenFeatureSheet(UniqueFeatureSheetType.PATHOLOGY_LAB_DIRECTORY) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("🔬 Pathology", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onOpenFeatureSheet(UniqueFeatureSheetType.POLYCLINIC_DIRECTORY) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("🏥 Clinics", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Cyclone Shelter Compass & Emergency Kit Action Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("essentials_cyclone_shelter_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌀", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cyclone Shelters & Emergency Kit",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Locate nearest high-ground cyclone shelters with offline GPS coordinates and interactive essential survival pack checklist.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF14532D),
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onOpenFeatureSheet(UniqueFeatureSheetType.CYCLONE_RESILIENCE) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Shelter Compass", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onOpenFeatureSheet(UniqueFeatureSheetType.BALASORE_MAP_EXPLORER) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Offline Shelters Map", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { onOpenFeatureSheet(UniqueFeatureSheetType.CYCLONE_SHELTER_WIDGET_MANAGER) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_open_shelter_widget_manager"),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA) "🛡️ ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳ ଓ ବ୍ଲକ ହେଲ୍ପଲାଇନ୍ ୱିଜେଟ୍ ସେଟ୍ କରନ୍ତୁ" else "🛡️ Add Shelter & Block Helpline Widget to Home Screen",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        }

        // Transit Section Title
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Train,
                    contentDescription = "Transit",
                    tint = OceanBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Baleshwar Trains & Bus Schedules",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate900
                    )
                )
            }
        }

        // FEATURE: Live Balasore City Bus & Public Transit API Suite
        item {
            val isOdia = language == AppLanguage.ODIA
            com.example.ui.components.ExpandableTransitFeatureBox(
                iconEmoji = "🚌",
                iconBgColor = Color(0xFF0284C7).copy(alpha = 0.12f),
                title = if (isOdia) "ଲାଇଭ୍ ସିଟି ବସ୍ ଓ ରୁଟ୍ ସୂଚୀ" else "Live City Bus & Local Transport",
                badgeText = "LIVE GPS",
                badgeBgColor = Color(0xFFDCFCE7),
                badgeBorderColor = Color(0xFFBBF7D0),
                badgeTextColor = Color(0xFF15803D),
                summary = if (isOdia) "ସହଦେବଖୁଣ୍ଟା, ଚାନ୍ଦିପୁର, ରେମୁଣା, ଏଫ୍.ଏମ୍.ୟୁ. ଓ ନୀଳଗିରି ଲାଇଭ୍ ବସ୍ GPS ଟ୍ରାକିଂ" else "Real-time bus GPS, next stop ETA & CRUT Mo Bus route timetable",
                detailedOverview = if (isOdia) {
                    "ସହଦେବଖୁଣ୍ଟା କେନ୍ଦ୍ରୀୟ ବସ୍ ଟର୍ମିନାଲ୍, ଚାନ୍ଦିପୁର ବେଳାଭୂମି, ରେମୁଣା ଗୋଲେଇ, ଏଫ୍.ଏମ୍. ବିଶ୍ୱବିଦ୍ୟାଳୟ ଓ ନୀଳଗିରି କରିଡରରେ ଚାଲୁଥିବା ସିଟି ବସ୍ ଓ OSRTC ସେବାର ଲାଇଭ୍ GPS ଟ୍ରାକିଂ ଏବଂ ଆଗମନ ସମୟ।"
                } else {
                    "Track real-time city buses across Sahadevkhunta Central Stand, Chandipur-on-Sea, Remuna Golei, FM University, and Nilagiri corridors with live GPS stop progression and seat occupancy."
                },
                bulletHighlights = if (isOdia) {
                    listOf(
                        "ପ୍ରତି ୨୦ ସେକେଣ୍ଡରେ ସ୍ୱୟଂକ୍ରିୟ GPS ଷ୍ଟପ୍ ଓ ETA ଅପଡେଟ୍",
                        "ରୁଟ୍ 71–74 ମୋ ବସ୍ ଓ OSRTC ଶଟଲ୍ ଭଡ଼ା ସୂଚୀ",
                        "ଅଫଲାଇନ୍ ସମୟସାରଣୀ କ୍ୟାଶ୍ ଓ ଷ୍ଟପ୍ ଆଲର୍ଟ"
                    )
                } else {
                    listOf(
                        "20-second auto-refreshing GPS stop telemetry & approaching pulse badges",
                        "Complete fare chart & stop-by-stop timelines for Chandipur, Remuna & Nilagiri",
                        "Instant route filtering with offline cached fallback schedules"
                    )
                },
                accentColor = OceanBlue,
                borderColor = Color(0xFFBAE6FD),
                isOdia = isOdia,
                launchButtonTag = "launch_local_transport_btn",
                onOpenHub = { onOpenFeatureSheet(UniqueFeatureSheetType.LOCAL_TRANSPORT) }
            )
        }

        // FEATURE: Real-Time Balasore Train Running Status & Push Notification Alerts
        item {
            val isOdia = language == AppLanguage.ODIA
            com.example.ui.components.ExpandableTransitFeatureBox(
                iconEmoji = "🚆",
                iconBgColor = Color(0xFF16A34A).copy(alpha = 0.12f),
                title = if (isOdia) "ବାଲେଶ୍ୱର ଲାଇଭ୍ ଟ୍ରେନ୍ ଓ ପୁସ୍ ଆଲର୍ଟ" else "Live Train Status & Push Alerts",
                badgeText = "BLS STATION",
                badgeBgColor = Color(0xFFDCFCE7),
                badgeBorderColor = Color(0xFFBBF7D0),
                badgeTextColor = Color(0xFF15803D),
                summary = if (isOdia) "ବାଲେଶ୍ୱର (BLS) ଆଗମନ, ପ୍ରସ୍ଥାନ, ବିଳମ୍ବ ସ୍ଥିତି, ପ୍ଲାଟଫର୍ମ ଓ ଟ୍ରେନ୍ ପୁସ୍ ନୋଟିଫିକେସନ୍" else "Real-time arrival/departure, delay telemetry, platform assignments & custom train alerts",
                detailedOverview = if (isOdia) {
                    "ଦକ୍ଷିଣ-ପୂର୍ବ ରେଳପଥର ବାଲେଶ୍ୱର ଜଙ୍କସନ୍ (BLS), ଜଳେଶ୍ୱର, ସୋର ଓ ବସ୍ତା ଷ୍ଟେସନ ଦେଇ ଯାଉଥିବା ବନ୍ଦେ ଭାରତ, ଧଉଳି, କରମଣ୍ଡଳ ଓ ପୁରୁଷୋତ୍ତମ ଏକ୍ସପ୍ରେସର ଲାଇଭ୍ ପ୍ଲାଟଫର୍ମ ଏବଂ ବିଳମ୍ବ ସ୍ଥିତି।"
                } else {
                    "Monitor South Eastern Railway arrivals and departures at Balasore Junction (BLS), Jaleswar, Soro, and Basta including Vande Bharat, Dhauli, Coromandel, and MEMU locals."
                },
                bulletHighlights = if (isOdia) {
                    listOf(
                        "ଲାଇଭ୍ ପ୍ଲାଟଫର୍ମ ନମ୍ବର (PF 1–4) ଓ କୋଚ୍ ପୋଜିସନ୍ ଗାଇଡ୍",
                        "ଟ୍ରେନ୍ ବିଳମ୍ବ ଓ ଆଗମନ ପାଇଁ କଷ୍ଟମ୍ ପୁସ୍ ନୋଟିଫିକେସନ୍ ଆଲର୍ଟ",
                        "ରେଳ ସୁରକ୍ଷା ବଳ (RPF 139) ଓ ଷ୍ଟେସନ ହେଲ୍ପডেସ୍କ"
                    )
                } else {
                    listOf(
                        "Live platform indicators (PF 1–4) & coach position guide at BLS Junction",
                        "Custom push notification alerts for train delays and station approach",
                        "Direct RPF 139 helpline & station cloakroom/retiring room directory"
                    )
                },
                accentColor = Color(0xFF16A34A),
                borderColor = Color(0xFF86EFAC),
                isOdia = isOdia,
                launchButtonTag = "launch_live_train_status_btn",
                onOpenHub = { onOpenFeatureSheet(UniqueFeatureSheetType.BALASORE_LIVE_TRAIN_STATUS) },
                delayMillis = 25
            )
        }

        // FEATURE: Balasore Bus Stand (Sahadevkhunta) Live Schedules (Local Room DB with reactive filter)
        item {
            val isOdia = language == AppLanguage.ODIA
            com.example.ui.components.ExpandableTransitFeatureBox(
                iconEmoji = "🚏",
                iconBgColor = Color(0xFFFEF3C7),
                title = if (isOdia) "ବାଲେଶ୍ୱର ବସ୍ ଷ୍ଟାଣ୍ଡ ଲାଇଭ୍ ସୂଚୀ" else "Baleshwar Bus Stand Live Timetable",
                badgeText = "ROOM DB",
                badgeBgColor = Color(0xFFFEF3C7),
                badgeBorderColor = Color(0xFFFDE68A),
                badgeTextColor = Color(0xFFB45309),
                summary = if (isOdia) "ସହଦେବଖୁଣ୍ଟା ଓ ପୁରୁଣା ବସ୍ ଷ୍ଟାଣ୍ଡରୁ ଗନ୍ତବ୍ୟସ୍ଥଳ ଓ ସମୟ ଫିଲ୍ଟର୍ ସହିତ ଲାଇଭ୍ ଟାଇମଟେବୁଲ୍" else "Sahadevkhunta & Old Bus Stand timetables with destination & departure time search filter",
                detailedOverview = if (isOdia) {
                    "ସହଦେବଖୁଣ୍ଟା ମୁଖ୍ୟ ବସ୍ ଷ୍ଟାଣ୍ଡରୁ ଭୁବନେଶ୍ୱର, କଟକ, ବାରିପଦା, ଦୀଘା, ଭଦ୍ରକ, ଜଳେଶ୍ୱର ଓ ଉଦଳା ଅଭିମୁଖେ ଯାଉଥିବା ସରକାରୀ OSRTC ଓ ଘରୋଇ ଏକ୍ସପ୍ରେସ୍ ବସ୍‌ର ସମ୍ପୂର୍ଣ୍ଣ ଅଫଲାଇନ୍ Room DB ସୂଚୀ।"
                } else {
                    "Search and filter offline Room DB bus schedules departing Sahadevkhunta Central Bus Terminal toward Bhubaneswar, Cuttack, Baripada, Digha, Jaleswar, and Udala."
                },
                bulletHighlights = if (isOdia) {
                    listOf(
                        "ଗନ୍ତବ୍ୟସ୍ଥଳ ଓ ପ୍ରସ୍ଥାନ ସମୟ ଅନୁଯାୟୀ ତୁରନ୍ତ ଫିଲ୍ଟର୍",
                        "ଏସି ଡିଲକ୍ସ, ଭଲଭୋ ଓ ସାଧାରଣ ଏକ୍ସପ୍ରେସ୍ ଟିକେଟ୍ ଦର",
                        "ସମ୍ପୂର୍ଣ୍ଣ ଅଫଲାଇନ୍ Room ଡାଟାବେସ୍ ସମର୍ଥିତ"
                    )
                } else {
                    listOf(
                        "Instant filtering by destination city, bus operator, and departure window",
                        "Verified bay numbers, fare tiers (AC Deluxe, Express, Ordinary) & contacts",
                        "100% offline-ready Room SQLite database with reactive Flow queries"
                    )
                },
                accentColor = Color(0xFFD97706),
                borderColor = Color(0xFFF59E0B).copy(alpha = 0.5f),
                isOdia = isOdia,
                launchButtonTag = "launch_balasore_bus_stand_schedules_btn",
                onOpenHub = { onOpenFeatureSheet(UniqueFeatureSheetType.BALASORE_BUS_STAND_LIVE_SCHEDULES) },
                delayMillis = 50
            )
        }

        // Transit List
        items(transitList, key = { it.id }) { item ->
            val isSubscribed = subscribedRouteIds.contains(item.id)
            TransitScheduleCard(
                item = item,
                isSubscribedToAlerts = isSubscribed,
                onToggleAlertNotifications = {
                    val nextSubscribed = if (isSubscribed) {
                        subscribedRouteIds - item.id
                    } else {
                        subscribedRouteIds + item.id
                    }
                    subscribedRouteIds = nextSubscribed
                    val toastMessage = if (!isSubscribed) {
                        "Push notifications enabled for ${item.routeName} alerts & schedule updates"
                    } else {
                        "Alert notifications muted for ${item.routeName}"
                    }
                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                }
            )
        }

        // AdMob Banner Placement in Essentials
        item {
            AdMobBannerCard(
                adUnitId = AdMobManager.AD_UNIT_BANNER_ESSENTIALS,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        // Language & Locale Settings Card (ViewModel & Local String Resources)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                SettingsLanguageToggleCard(
                    currentLanguage = language,
                    onLanguageChange = onLanguageChange,
                    onToggleLanguage = onToggleLanguage,
                    isAutoUpdateActive = isHourlyAutoRefreshEnabled
                )
            }
        }

        // App Theme & Appearance Setting Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("app_theme_selection_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (themeMode) {
                                    ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                    ThemeMode.HIGH_CONTRAST_DARK -> Icons.Default.NightlightRound
                                },
                                contentDescription = "Theme Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଆପ୍ ରୂପରେଖା ଓ ଡାର୍କ ମୋଡ୍" else "Global Theme & Dark Mode",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "PERSISTED",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = when (themeMode) {
                                    ThemeMode.SYSTEM -> if (language == AppLanguage.ODIA) "ସିଷ୍ଟମ୍ ଡିଫଲ୍ଟ ଅନୁସାରେ ସ୍ୱୟଂକ୍ରିୟ (ପରବର୍ତ୍ତୀ ସେସନ ପାଇଁ ସଂରକ୍ଷିତ)" else "Follows system setting automatically (Persists across sessions)"
                                    ThemeMode.LIGHT -> if (language == AppLanguage.ODIA) "ସର୍ବଦା ଲାଇଟ୍ ମୋଡ୍ (ଉଜ୍ଜ୍ୱଳ ଡିସପ୍ଲେ)" else "Always light mode (Persists across sessions)"
                                    ThemeMode.DARK -> if (language == AppLanguage.ODIA) "ସର୍ବଦା ଡାର୍କ ମୋଡ୍ (ନିଶାର୍ଦ୍ଧ ଜରୁରୀକାଳୀନ ବ୍ୟବହାର ପାଇଁ ଅନୁକୂଳ)" else "Dark mode for low-light & nighttime emergency readability"
                                    ThemeMode.HIGH_CONTRAST_DARK -> if (language == AppLanguage.ODIA) "ନିଶାର୍ଦ୍ଧ ହାଇ-କଣ୍ଟ୍ରାଷ୍ଟ OLED ଡାର୍କ (ଜରୁରୀ ସୂଚନା ସ୍ପଷ୍ଟ ପଢ଼ିବା ପାଇଁ)" else "High-contrast OLED black for nighttime emergency usage & zero eye-strain"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Theme selector pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ThemeOptionPill(
                            title = if (language == AppLanguage.ODIA) "ସିଷ୍ଟମ୍" else "System",
                            icon = Icons.Default.BrightnessAuto,
                            isSelected = themeMode == ThemeMode.SYSTEM,
                            onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_btn_system")
                        )
                        ThemeOptionPill(
                            title = if (language == AppLanguage.ODIA) "ଲାଇଟ୍" else "Light",
                            icon = Icons.Default.LightMode,
                            isSelected = themeMode == ThemeMode.LIGHT,
                            onClick = { onThemeModeChange(ThemeMode.LIGHT) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_btn_light")
                        )
                        ThemeOptionPill(
                            title = if (language == AppLanguage.ODIA) "ଡାର୍କ" else "Dark",
                            icon = Icons.Default.DarkMode,
                            isSelected = themeMode == ThemeMode.DARK,
                            onClick = { onThemeModeChange(ThemeMode.DARK) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_btn_dark")
                        )
                        ThemeOptionPill(
                            title = if (language == AppLanguage.ODIA) "ନିଶାର୍ଦ୍ଧ" else "Night",
                            icon = Icons.Default.NightlightRound,
                            isSelected = themeMode == ThemeMode.HIGH_CONTRAST_DARK,
                            onClick = { onThemeModeChange(ThemeMode.HIGH_CONTRAST_DARK) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_btn_high_contrast")
                        )
                    }
                }
            }
        }

        // Full App Watermark & Official Logo Branding Setting Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("watermark_settings_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_app_icon_circle),
                                    contentDescription = "App Watermark Logo",
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଆପ୍ ୱାଟରମାର୍କ୍ ଓ ଲୋଗୋ" else "Full App Watermark",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଆପ୍ ନାମ ଓ ସରକାରୀ ଲୋଗୋ ସମଗ୍ର ସ୍କ୍ରିନ୍ରେ ସ୍ୱଚ୍ଛ ଭାବେ ପ୍ରଦର୍ଶିତ" else "Translucent name & logo watermark across full app",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        androidx.compose.material3.Switch(
                            checked = isWatermarkEnabled,
                            onCheckedChange = onWatermarkToggle,
                            modifier = Modifier.testTag("watermark_toggle_switch")
                        )
                    }

                    if (isWatermarkEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (language == AppLanguage.ODIA) "ୱାଟରମାର୍କ୍ ଶୈଳୀ (Style):" else "Watermark Presentation Style:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Style selector pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            WatermarkOptionPill(
                                title = if (language == AppLanguage.ODIA) "କେନ୍ଦ୍ର ସିଲ୍" else "Center Seal",
                                isSelected = watermarkStyle == WatermarkStyle.CENTER_EMBLEM,
                                onClick = { onWatermarkStyleChange(WatermarkStyle.CENTER_EMBLEM) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("watermark_style_emblem")
                            )
                            WatermarkOptionPill(
                                title = if (language == AppLanguage.ODIA) "ଡାଇଗୋନାଲ୍" else "Diagonal",
                                isSelected = watermarkStyle == WatermarkStyle.DIAGONAL_TILES,
                                onClick = { onWatermarkStyleChange(WatermarkStyle.DIAGONAL_TILES) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("watermark_style_diagonal")
                            )
                            WatermarkOptionPill(
                                title = if (language == AppLanguage.ODIA) "କୋଣ ଷ୍ଟାମ୍ପ" else "Stamp",
                                isSelected = watermarkStyle == WatermarkStyle.CORNER_STAMP,
                                onClick = { onWatermarkStyleChange(WatermarkStyle.CORNER_STAMP) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("watermark_style_stamp")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (language == AppLanguage.ODIA) "ଦୃଶ୍ୟମାନତା ଓ ସ୍ୱଚ୍ଛତା (Opacity):" else "Visibility & Opacity:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Opacity selector pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            WatermarkOptionPill(
                                title = "Subtle 4.5%",
                                isSelected = watermarkOpacity == WatermarkOpacity.SUBTLE,
                                onClick = { onWatermarkOpacityChange(WatermarkOpacity.SUBTLE) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("watermark_opacity_subtle")
                            )
                            WatermarkOptionPill(
                                title = "Standard 8.5%",
                                isSelected = watermarkOpacity == WatermarkOpacity.MEDIUM,
                                onClick = { onWatermarkOpacityChange(WatermarkOpacity.MEDIUM) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("watermark_opacity_medium")
                            )
                            WatermarkOptionPill(
                                title = "Clear 14%",
                                isSelected = watermarkOpacity == WatermarkOpacity.PROMINENT,
                                onClick = { onWatermarkOpacityChange(WatermarkOpacity.PROMINENT) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("watermark_opacity_prominent")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive live watermark preview box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .testTag("watermark_preview_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            AppWatermarkOverlay(
                                enabled = true,
                                style = watermarkStyle,
                                opacity = watermarkOpacity
                            )
                            Text(
                                text = "Preview: Active on all screens & tabs",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Google Play Store Auto-Updates Status & Control Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("play_store_auto_updates_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = "Auto Update",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଗୁଗୁଲ୍ ପ୍ଲେ ଅଟୋ-ଅପଡେଟ୍" else "Play Store Auto-Updates",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Version ${com.example.BuildConfig.VERSION_NAME} (Build ${com.example.BuildConfig.VERSION_CODE})",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Auto-Update: ON",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (language == AppLanguage.ODIA)
                            "ଗୁଗୁଲ୍ ପ୍ଲେ ଷ୍ଟୋର ମାଧ୍ୟମରେ ସ୍ୱୟଂକ୍ରିୟ ଭାବେ ନୂତନ ସଂସ୍କରଣ ଓ ଚାନ୍ଦିପୁର ଜୁଆର ତଥ୍ୟ ସ୍ୱୟଂ ଅପଡେଟ୍ ହୋଇଥାଏ।"
                        else
                            "Google Play In-App Updates and automatic background updates are active. The Play Store automatically updates this app over Wi-Fi when charging.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = BentoSlate600,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic State Indicator
                    when (updateState) {
                        is UpdateUIState.Checking -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = OceanBlue
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Checking Google Play Store for latest version...",
                                    style = MaterialTheme.typography.bodySmall.copy(color = BentoSlate700)
                                )
                            }
                        }
                        is UpdateUIState.UpdateAvailable -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "New update available on Google Play (v${updateState.availableVersionCode})!",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onTriggerUpdate,
                                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlueDark),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Download & Install Update", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        is UpdateUIState.Downloading -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE0F2FE))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Downloading update from Google Play...",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OceanBlueDark
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (updateState.totalBytes > 0) {
                                    val progress = updateState.bytesDownloaded.toFloat() / updateState.totalBytes.toFloat()
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = OceanBlue
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${updateState.bytesDownloaded / (1024 * 1024)} MB / ${updateState.totalBytes / (1024 * 1024)} MB",
                                        style = MaterialTheme.typography.labelSmall.copy(color = BentoSlate500)
                                    )
                                } else {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = OceanBlue)
                                }
                            }
                        }
                        is UpdateUIState.Downloaded -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Update downloaded and ready to apply!",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534)
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onCompleteUpdate,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Restart App to Complete Update", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        is UpdateUIState.UpToDate -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF0FDF4))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Your app is on the latest version.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF166534)
                                    )
                                )
                            }
                        }
                        is UpdateUIState.Info -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = BentoSlate500,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = updateState.message,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = BentoSlate600,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                        else -> {}
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCheckForUpdates,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = BentoSlate700
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Check Update",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoSlate700
                                )
                            )
                        }

                        Button(
                            onClick = { PlayStoreUpdateManager.openPlayStore(context) },
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shop,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Open Play Store",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Baleshwar 360 v${com.example.BuildConfig.VERSION_NAME} App Bundle")
                                putExtra(Intent.EXTRA_TEXT, "Baleshwar 360 v${com.example.BuildConfig.VERSION_NAME} (Build ${com.example.BuildConfig.VERSION_CODE}) Signed Release AAB Bundle ready for Google Play Store upload: Baleshwar360-v${com.example.BuildConfig.VERSION_NAME}-release.aab")
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share AAB Release Details")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_aab_bundle_btn"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Export AAB Release Info (v${com.example.BuildConfig.VERSION_NAME})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        // Hourly Auto-Refresh & Background Data Sync Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("hourly_auto_refresh_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Hourly Refresh",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ପ୍ରତି ଘଣ୍ଟା ସ୍ୱୟଂକ୍ରିୟ ରିଫ୍ରେଶ୍" else "Hourly Auto-Refresh",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = if (language == AppLanguage.ODIA)
                                        "ପାଣିପାଗ, ଜରୁରୀ ସୂଚନା ଓ ଦୈନିକ ତଥ୍ୟ ପ୍ରତି ଘଣ୍ଟାରେ ସ୍ୱୟଂ ଅପଡେଟ୍ ହୁଏ"
                                    else
                                        "Auto-refreshes weather, emergency alerts & daily pulse every 60 min",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                        Switch(
                            checked = isHourlyAutoRefreshEnabled,
                            onCheckedChange = onHourlyAutoRefreshToggle,
                            modifier = Modifier.testTag("hourly_auto_refresh_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status pill row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(lastHourlyRefreshTimestamp))
                            Text(
                                text = "Last Sync: $timeStr",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = BentoSlate600,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            if (autoRefreshCycleCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• Cycle #$autoRefreshCycleCount",
                                    style = MaterialTheme.typography.labelSmall.copy(color = BentoSlate500)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isHourlyAutoRefreshEnabled) Color(0xFFEFF6FF) else Color(0xFFF1F5F9)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isHourlyAutoRefreshEnabled) "Next in: ${nextHourlyRefreshMinutesRemaining}m" else "Paused",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHourlyAutoRefreshEnabled) Color(0xFF1D4ED8) else BentoSlate500,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onTriggerManualHourlyRefresh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trigger_hourly_refresh_now_btn"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) "ବର୍ତ୍ତମାନ ରିଫ୍ରେଶ୍ କରନ୍ତୁ" else "Refresh All Data Now (Hourly Sync)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        )
                    }
                }
            }
        }

        // Google Play Store Required In-App Privacy Policy & Compliance Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("privacy_policy_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BentoSlate100)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Baleshwar 360",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate900
                                )
                            )
                            Text(
                                text = "v${com.example.BuildConfig.VERSION_NAME} • Developer: Nihar Bhuyan",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = BentoSlate500
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Play Store Verified",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Baleshwar 360 respects your privacy. No personal identifying information is stored or sold. We adhere strictly to Google Play Developer policies.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = BentoSlate600,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onOpenFeatureSheet(UniqueFeatureSheetType.PRIVACY_POLICY) },
                            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("open_in_app_privacy_policy_btn")
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "In-App Policy", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                val privacyUrl = "https://ais-pre-honzkaxc7yscu2h42zeagl-613265325843.asia-east1.run.app/privacy"
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl))
                                    context.startActivity(intent)
                                } catch (_: Throwable) {}
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("open_web_privacy_policy_btn")
                        ) {
                            Text(text = "Web Policy ↗", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Google AdMob & app-ads.txt Setup and Verification Center
        item {
            AdMobVerificationCard(
                language = language,
                onCopyAppAdsTxt = {
                    AdMobManager.copyAppAdsTxtSnippet(context)
                },
                onShareAppAdsTxt = {
                    AdMobManager.shareAppAdsTxt(context)
                }
            )
        }
    }

    if (showEmergencyPamphletScanner) {
        BalasorePamphletScannerSheet(
            language = language,
            initialPamphletCategory = PamphletCategory.EMERGENCY,
            onDismiss = { showEmergencyPamphletScanner = false }
        )
    }
}

@Composable
fun EmergencyContactCard(
    contact: EmergencyContact,
    language: AppLanguage,
    onCall: () -> Unit,
    isSaved: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("emergency_card_${contact.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, BentoSlate100)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (contact.number == "112" || contact.number == "101") Color(0xFFFFECEB) else Color(0xFFE0F2FE)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (contact.category.contains("Police")) Icons.Default.LocalPolice else Icons.Default.LocalHospital,
                        contentDescription = "Contact Icon",
                        tint = if (contact.number == "112" || contact.number == "101") Color.Red else OceanBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (language == AppLanguage.ODIA) contact.odiaName else contact.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate900
                        )
                    )
                    Text(
                        text = contact.description,
                        style = MaterialTheme.typography.labelSmall.copy(color = BentoSlate500)
                    )
                    Text(
                        text = "Dial: ${contact.number}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = OceanBlueDark
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Favorite / Bookmark Button (Room Database persistent bookmark)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isSaved) Color(0xFFFEF3C7) else BentoSlate100)
                        .clickable { onToggleFavorite() }
                        .testTag("bookmark_contact_${contact.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isSaved) "Remove from saved emergency directory" else "Save emergency contact",
                        tint = if (isSaved) AmberGold else BentoSlate600,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Subtle continuous pulse animation on the 1-tap Emergency Call button
                val callPulseTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "contact_call_pulse_${contact.id}")
                val callPulseScale by callPulseTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.08f,
                    animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                        animation = androidx.compose.animation.core.tween(
                            durationMillis = 1000,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        ),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                    ),
                    label = "contact_call_scale_${contact.id}"
                )
                val callBorderAlpha by callPulseTransition.animateFloat(
                    initialValue = 0.35f,
                    targetValue = 0.95f,
                    animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                        animation = androidx.compose.animation.core.tween(
                            durationMillis = 1000,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        ),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                    ),
                    label = "contact_call_border_${contact.id}"
                )

                // Call Button with subtle pulse & glowing ring
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .scale(callPulseScale)
                        .clip(CircleShape)
                        .background(
                            if (contact.number == "112" || contact.number == "101" || contact.number == "108")
                                Color(0xFFFEE2E2)
                            else
                                Color(0xFFDCFCE7)
                        )
                        .border(
                            BorderStroke(
                                1.5.dp,
                                (if (contact.number == "112" || contact.number == "101" || contact.number == "108")
                                    Color(0xFFEF4444)
                                else
                                    EmeraldGreen).copy(alpha = callBorderAlpha)
                            ),
                            CircleShape
                        )
                        .clickable { onCall() }
                        .testTag("call_button_${contact.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = if (contact.number == "112" || contact.number == "101" || contact.number == "108")
                            Color(0xFFDC2626)
                        else
                            EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TransitScheduleCard(
    item: TransitSchedule,
    isSubscribedToAlerts: Boolean = false,
    onToggleAlertNotifications: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("transit_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isSubscribedToAlerts) OceanBlue.copy(alpha = 0.5f) else BentoSlate100)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isSubscribedToAlerts) Color(0xFFE0F2FE) else BentoSlate100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.type == "Train") Icons.Default.Train else Icons.Default.DirectionsBus,
                            contentDescription = item.type,
                            tint = OceanBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = item.routeName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BentoSlate900
                            )
                        )
                        Text(
                            text = "${item.origin} → ${item.destination}",
                            style = MaterialTheme.typography.labelSmall.copy(color = BentoSlate500)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = item.time,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OceanBlueDark
                            )
                        )
                        Text(
                            text = item.status,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (item.status.contains("Delay", ignoreCase = true) || item.status.contains("Alert", ignoreCase = true)) Color(0xFFDC2626) else EmeraldGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    IconButton(
                        onClick = onToggleAlertNotifications,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isSubscribedToAlerts) Color(0xFFDCFCE7) else BentoSlate100)
                            .testTag("transit_notify_btn_${item.id}")
                    ) {
                        Icon(
                            imageVector = if (isSubscribedToAlerts) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = "Set Route Alert Push Notification",
                            tint = if (isSubscribedToAlerts) Color(0xFF16A34A) else BentoSlate500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (isSubscribedToAlerts) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF0FDF4))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Push alerts active for ${item.routeName} schedule updates & delays",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF15803D),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SeafoodCatchItemView(
    item: SeafoodCatch,
    language: AppLanguage
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(BorderStroke(1.dp, BentoSlate100), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (language == AppLanguage.ODIA) item.odiaName else item.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate900
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = item.qualityGrade,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                fontSize = 9.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Harbor: ${item.harbor} • ${item.freshness}",
                    style = MaterialTheme.typography.labelSmall.copy(color = BentoSlate500)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.priceRangeKg,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = OceanBlueDark
                    )
                )
                Text(
                    text = "Wholesale/kg",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = BentoSlate400,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        modifier = modifier.height(38.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
private fun WatermarkOptionPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        modifier = modifier.height(36.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
