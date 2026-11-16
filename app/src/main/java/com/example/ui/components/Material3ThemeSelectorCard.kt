package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.LocalAppTheme
import com.example.ui.viewmodel.ThemeMode

/**
 * Interactive Material 3 Theme Selector Card.
 * Allows users to toggle between Light, Dark, and System-Default themes,
 * ensuring all UI components adapt dynamically using Material 3 semantic tokens.
 */
@Composable
fun Material3ThemeSelectorCard(
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    val themeState = LocalAppTheme.current
    val currentMode = themeState.themeMode
    val isOdia = language == AppLanguage.ODIA

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("material3_theme_selector_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Theme Palette",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOdia) "ଥିମ୍ ପ୍ରୋଭାଇଡର୍ (Material 3)" else "Material 3 Theme Provider",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (isOdia) "ଲାଇଟ୍, ଡାର୍କ ଓ ସିଷ୍ଟମ୍ ଡିଫଲ୍ଟ ମଧ୍ୟରେ ପରିବର୍ତ୍ତନ କରନ୍ତୁ"
                            else "Dynamic Material 3 tokens for Light, Dark & System",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Active Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = when (currentMode) {
                            ThemeMode.SYSTEM -> if (isOdia) "ସିଷ୍ଟମ୍" else "SYSTEM"
                            ThemeMode.LIGHT -> if (isOdia) "ଲାଇଟ୍" else "LIGHT"
                            ThemeMode.DARK -> if (isOdia) "ଡାର୍କ" else "DARK"
                            ThemeMode.HIGH_CONTRAST_DARK -> if (isOdia) "OLED" else "OLED"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Option Toggle Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeOptionRow(
                    title = if (isOdia) "ସିଷ୍ଟମ୍ ଡିଫଲ୍ଟ (ସ୍ୱୟଂକ୍ରିୟ)" else "System Default",
                    subtitle = if (isOdia) "ଡିଭାଇସ୍ ସେଟିଂସ୍ ଅନୁଯାୟୀ ସ୍ୱୟଂକ୍ରିୟ ଭାବେ ବଦଳେ" else "Follows Android OS day/night mode automatically",
                    icon = Icons.Default.BrightnessAuto,
                    isSelected = currentMode == ThemeMode.SYSTEM,
                    tag = "theme_toggle_system",
                    onClick = { themeState.onThemeModeChange(ThemeMode.SYSTEM) }
                )

                ThemeOptionRow(
                    title = if (isOdia) "ଲାଇଟ୍ ଥିମ୍ (ଉଜ୍ଜ୍ୱଳ ଡିସପ୍ଲେ)" else "Light Theme",
                    subtitle = if (isOdia) "ବାଲେଶ୍ୱର ସମୁଦ୍ରକୂଳ ନୀଳ ଓ ପରିଷ୍କାର ଲାଇଟ୍ ଟୋକନ୍" else "Crisp Ocean Blue & daylight Material 3 design tokens",
                    icon = Icons.Default.LightMode,
                    isSelected = currentMode == ThemeMode.LIGHT,
                    tag = "theme_toggle_light",
                    onClick = { themeState.onThemeModeChange(ThemeMode.LIGHT) }
                )

                ThemeOptionRow(
                    title = if (isOdia) "ଡାର୍କ ଥିମ୍ (ନିଶାର୍ଦ୍ଧ ଡିସପ୍ଲେ)" else "Dark Theme",
                    subtitle = if (isOdia) "ଆଖି ଉପରେ ଚାପ କମାଇବା ପାଇଁ ଗାଢ଼ ସ୍ଲେଟ୍ ରଙ୍ଗ" else "Deep night-sky dark theme with soothing slate tones",
                    icon = Icons.Default.DarkMode,
                    isSelected = currentMode == ThemeMode.DARK,
                    tag = "theme_toggle_dark",
                    onClick = { themeState.onThemeModeChange(ThemeMode.DARK) }
                )

                ThemeOptionRow(
                    title = if (isOdia) "OLED ହାଇ-କଣ୍ଟ୍ରାଷ୍ଟ ଡାର୍କ" else "High-Contrast OLED Dark",
                    subtitle = if (isOdia) "ଜରୁରୀକାଳୀନ ବାତ୍ୟା ସତର୍କତା ଓ ଶୂନ୍ୟ ଆଖି-କ୍ଲାନ୍ତି" else "AMOLED pitch black for night reading & emergency alerts",
                    icon = Icons.Default.NightlightRound,
                    isSelected = currentMode == ThemeMode.HIGH_CONTRAST_DARK,
                    tag = "theme_toggle_high_contrast",
                    onClick = { themeState.onThemeModeChange(ThemeMode.HIGH_CONTRAST_DARK) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic M3 Design Token Preview Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = if (isOdia) "ସକ୍ରିୟ Material 3 ଡିଜାଇନ୍ ଟୋକନ୍:" else "Active Material 3 Design Tokens:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TokenSwatch("Primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
                        TokenSwatch("Container", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                        TokenSwatch("Surface", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
                        TokenSwatch("Variant", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                        TokenSwatch("Outline", MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.background)
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        label = "border_anim"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent,
        label = "bg_anim"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun TokenSwatch(label: String, color: Color, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 8.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace
        )
    }
}
