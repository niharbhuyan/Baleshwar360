package com.example.ui.features.heritage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.components.openInGoogleMaps

data class ArtisanPassportSite(
    val id: String,
    val nameEn: String,
    val nameOd: String,
    val category: String,
    val locationBlock: String,
    val descriptionEn: String,
    val descriptionOd: String,
    val craftSpecialty: String,
    val contactPerson: String,
    val contactPhone: String,
    val latitude: Double,
    val longitude: Double,
    val badgeEmoji: String
)

val BALASORE_HERITAGE_PASSPORT_SITES = listOf(
    ArtisanPassportSite(
        id = "remuna_khirachora",
        nameEn = "Khirachora Gopinatha Temple & Khira Bhog",
        nameOd = "କ୍ଷୀରଚୋରା ଗୋପୀନାଥ ମନ୍ଦିର ଓ ଅମୃତ କ୍ଷୀର ଭୋଗ",
        category = "Spiritual & Culinary Heritage",
        locationBlock = "Remuna Block (~9 km from Town)",
        descriptionEn = "12th-century Vaishnavite shrine famous for 'Khira Bhog' offered daily in earthen pots. Holy site visited by Sri Chaitanya Mahaprabhu.",
        descriptionOd = "ଦ୍ୱାଦଶ ଶତାବ୍ଦୀର ପ୍ରସିଦ୍ଧ ବୈଷ୍ଣବ ପୀଠ। ଶ୍ରୀ ଚୈତନ୍ୟ ମହାପ୍ରଭୁ ଓ ମାଧବେନ୍ଦ୍ର ପୁରୀଙ୍କ ସ୍ମୃତି ବିଜଡ଼ିତ ସୁସ୍ୱାଦୁ କ୍ଷୀରଭୋଗ।",
        craftSpecialty = "Authentic Condensed Milk Bhog & Earthen Pots",
        contactPerson = "Temple Trust Office",
        contactPhone = "06782-273220",
        latitude = 21.5280,
        longitude = 86.8732,
        badgeEmoji = "🏺"
    ),
    ArtisanPassportSite(
        id = "kasba_terracotta",
        nameEn = "Kasba Terracotta & Traditional Clay Guild",
        nameOd = "କସବା ପାରମ୍ପରିକ ଟେରାକୋଟା ଓ ମୃଣ୍ମୟ ଶିଳ୍ପୀ କ୍ଲଷ୍ଟର",
        category = "Living Craft Guild",
        locationBlock = "Kasba Village, Balasore Sadar",
        descriptionEn = "Heritage village where master potters craft hand-thrown terracotta bells, sacred water pitchers, and intricate decorative figurines from local silt clay.",
        descriptionOd = "ମାଟିର ହାତକଳା, ପୋଡ଼ାମାଟିର ଘଣ୍ଟି, ସାଜସଜ୍ଜା ସାମଗ୍ରୀ ଓ ମାଠିଆ ତିଆରି କରୁଥିବା ପାରମ୍ପରିକ କୁମ୍ଭକାର ଗ୍ରାମ।",
        craftSpecialty = "Wood-Fired Red Terracotta Garden Ware & Idols",
        contactPerson = "Kasba Potters Cooperative",
        contactPhone = "9437281050",
        latitude = 21.4920,
        longitude = 86.9240,
        badgeEmoji = "🧱"
    ),
    ArtisanPassportSite(
        id = "remuna_bell_metal",
        nameEn = "Remuna Bell Metal & Brass Casting Artisans",
        nameOd = "ରେମୁଣା କଂସା ଓ ପିତ୍ତଳ ହସ୍ତଶିଳ୍ପ ସମବାୟ",
        category = "Traditional Metallurgy Craft",
        locationBlock = "Kalyanpur / Remuna",
        descriptionEn = "Centuries-old artisanal lost-wax and hand-hammered casting of resonant bronze cymbals (Manjira), brass pooja vessels, and heirloom culinary utensils.",
        descriptionOd = "ହାତପିଟା କଂସା ବାସନ, ପିତ୍ତଳ ପୂଜା ଥାଳି, ଘଣ୍ଟ ଓ ମଞ୍ଜିରା ଗଢ଼ୁଥିବା ବଂଶାନୁକ୍ରମିକ କାରିଗର।",
        craftSpecialty = "Pure 78/22 Copper-Tin Bell Metal Utensils",
        contactPerson = "Shree Jagannath Kansari Samiti",
        contactPhone = "9861054320",
        latitude = 21.5310,
        longitude = 86.8790,
        badgeEmoji = "🔔"
    ),
    ArtisanPassportSite(
        id = "panchalingeswar_sanctum",
        nameEn = "Panchalingeswar Devagiri Perennial Stream",
        nameOd = "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଦେବଗିରି ପାହାଡ଼ ନିରନ୍ତର ଝରଣା ପୀଠ",
        category = "Nature Trek & Spiritual Sanctum",
        locationBlock = "Nilagiri Block (~30 km from Town)",
        descriptionEn = "Scenic Devagiri hill trek where crystal-clear natural mountain springs continuously bathe five ancient submerged Shiva Lingas in their natural rock bed.",
        descriptionOd = "ଦେବଗିରି ପାହାଡ଼ କୋଳରେ ପ୍ରାକୃତିକ ଶୀତଳ ଝରଣାର ଜଳଧାରାରେ ନିମଜ୍ଜିତ ପାଞ୍ଚଟି ପ୍ରାଚୀନ ଶିବଲିଙ୍ଗ।",
        craftSpecialty = "Spiritual Trek, Kuldiha Foothills & Nilagiri Honey",
        contactPerson = "Panchalingeswar Temple Office",
        contactPhone = "06782-233450",
        latitude = 21.4332,
        longitude = 86.7165,
        badgeEmoji = "💧"
    ),
    ArtisanPassportSite(
        id = "chandaneswar_shrine",
        nameEn = "Chandaneswar Shiva Sanctum & Chadak Mela",
        nameOd = "ଚନ୍ଦନେଶ୍ୱର ଶିବ ପୀଠ ଓ ପ୍ରସିଦ୍ଧ ଚଡ଼କ ମେଳା",
        category = "Pilgrimage & Folk Tradition",
        locationBlock = "Bhograi Block (Northern Coastal Border)",
        descriptionEn = "Ancient pilgrimage center near Bengal-Odisha border, home to the centuries-old Chadak and Uda folk festival where lakhs of pilgrims observe holy vows.",
        descriptionOd = "ଓଡ଼ିଶା-ବଙ୍ଗ ସୀମାନ୍ତରେ ଥିବା ପ୍ରସିଦ୍ଧ ଶୈବ ପୀଠ, ଐତିହାସିକ ନୀଳପର୍ବ ଓ ଚଡ଼କ ମେଳା।",
        craftSpecialty = "Bhograi Betel Leaf & Coastal Folk Rituals",
        contactPerson = "Devalaya Management",
        contactPhone = "06781-232140",
        latitude = 21.6180,
        longitude = 87.5210,
        badgeEmoji = "🔱"
    ),
    ArtisanPassportSite(
        id = "fakir_mohan_memorial",
        nameEn = "Fakir Mohan Senapati Memorial & Bhasha Smruti",
        nameOd = "ବ୍ୟାସକବି ଫକୀର ମୋହନ ସେନାପତି ସ୍ମୃତିପୀଠ (ଶାନ୍ତିକାନନ)",
        category = "Literary & Cultural Beacon",
        locationBlock = "Mallikashpur, Balasore Town",
        descriptionEn = "The residence and literary sanctuary (Shanti Kanan) of Vyasakabi Fakir Mohan Senapati, the pioneer of modern Indian realism and savior of the Odia language.",
        descriptionOd = "ଆଧୁନିକ ଓଡ଼ିଆ ସାହିତ୍ୟର ଜନକ ବ୍ୟାସକବିଙ୍କ ବାସଭବନ ଓ ଶାନ୍ତିକାନନ। ଓଡ଼ିଆ ଭାଷା ଆନ୍ଦୋଳନର ଐତିହାସିକ କେନ୍ଦ୍ର।",
        craftSpecialty = "Original Manuscripts & Odia Language Heritage",
        contactPerson = "Fakir Mohan Sahitya Parishad",
        contactPhone = "06782-261540",
        latitude = 21.4985,
        longitude = 86.9320,
        badgeEmoji = "📜"
    )
)

/**
 * Heritage & Artisan Trail Passport Sheet for Balasore 360.
 * Allows users to check in at Balasore's historical landmarks and artisan guilds,
 * stamp their digital cultural passport, connect directly with master craftsmen,
 * and share their heritage exploration journey via Android ShareSheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeritageArtisanTrailPassportSheet(
    language: AppLanguage,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA

    // Track checked in stamps using persistent in-memory state
    var checkedInSiteIds by remember { mutableStateOf(setOf("remuna_khirachora", "kasba_terracotta")) }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier.testTag("heritage_artisan_passport_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFD97706), Color(0xFFB45309))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏺", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isOdia) "ଐତିହ୍ୟ ଓ ହସ୍ତଶିଳ୍ପ ପାସପୋର୍ଟ" else "Heritage & Artisan Passport",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isOdia) "ବାଲେଶ୍ୱର ସଂସ୍କୃତି • ଶିଳ୍ପୀ ଯୋଗାଯୋଗ" else "Cultural Passport • Direct Artisan Hub",
                            fontSize = 11.5.sp,
                            color = Color(0xFFD97706),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("btn_close_heritage_passport")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFFBEB),
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isOdia) "ପାସପୋର୍ଟ ଷ୍ଟାମ୍ପ ସଂଗ୍ରହ ପ୍ରଗତି" else "Passport Stamp Collection Progress",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "${checkedInSiteIds.size} / ${BALASORE_HERITAGE_PASSPORT_SITES.size} ${if (isOdia) "ଟି ସ୍ଥାନ ଚେକ୍-ଇନ୍ ସମ୍ପନ୍ନ" else "Cultural Landmarks Stamped"}",
                                fontSize = 11.5.sp,
                                color = Color(0xFFB45309)
                            )
                        }

                        Button(
                            onClick = {
                                val shareText = "🏺 BALASORE HERITAGE & ARTISAN TRAIL PASSPORT\n" +
                                        "━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                                        "🏆 Progress: ${checkedInSiteIds.size}/${BALASORE_HERITAGE_PASSPORT_SITES.size} Heritage Sites Stamped!\n\n" +
                                        "📍 Sites Explored:\n" +
                                        BALASORE_HERITAGE_PASSPORT_SITES.filter { checkedInSiteIds.contains(it.id) }
                                            .joinToString("\n") { "• ${it.badgeEmoji} ${if (isOdia) it.nameOd else it.nameEn} (${it.locationBlock})" } +
                                        "\n\nDiscover the vanishing sea, Remuna Khira Bhog, and brass metal crafts on Baleshwar 360 App!"
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "My Balasore Heritage Passport Progress")
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Heritage Passport"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (checkedInSiteIds.size.toFloat() / BALASORE_HERITAGE_PASSPORT_SITES.size).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFD97706),
                        trackColor = Color(0xFFFEF3C7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sites & Guilds List
            LazyColumn(
                modifier = Modifier.fillMaxHeight(0.85f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(BALASORE_HERITAGE_PASSPORT_SITES, key = { it.id }) { site ->
                    val isCheckedIn = checkedInSiteIds.contains(site.id)

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCheckedIn) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isCheckedIn) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(modifier = Modifier.weight(1f)) {
                                    Text(site.badgeEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isOdia) site.nameOd else site.nameEn,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = site.category,
                                            fontSize = 11.sp,
                                            color = Color(0xFFD97706),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "📍 ${site.locationBlock}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCheckedIn) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = if (isCheckedIn) "✓ STAMPED" else "UNVISITED",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.5.sp,
                                        color = if (isCheckedIn) Color(0xFF166534) else Color(0xFF64748B),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isOdia) site.descriptionOd else site.descriptionEn,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEF3C7).copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "⭐ Specialty: ${site.craftSpecialty}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        checkedInSiteIds = if (isCheckedIn) {
                                            checkedInSiteIds - site.id
                                        } else {
                                            Toast.makeText(context, "🎉 Passport Stamped at ${site.nameEn}!", Toast.LENGTH_SHORT).show()
                                            checkedInSiteIds + site.id
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCheckedIn) Color(0xFF16A34A) else Color(0xFFD97706)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isCheckedIn) Icons.Default.CheckCircle else Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isCheckedIn) "Checked In" else "Stamp Visit",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${site.contactPhone}"))
                                        context.startActivity(intent)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Contact", fontSize = 11.5.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        openInGoogleMaps(context, site.latitude, site.longitude, site.nameEn)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Nav", fontSize = 11.5.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
