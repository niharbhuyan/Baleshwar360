package com.example.ui.features.municipal

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.municipal.BalasoreMunicipalWards
import com.example.data.municipal.BalasoreWard
import com.example.data.municipal.MunicipalZone
import com.example.ui.theme.BentoPrimaryBlue
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900

/**
 * Interactive Balasore Municipal Wards Directory & Civic Explorer.
 * Displays all 31 administrative wards of Balasore Municipality with search,
 * zone filtering, landmarks, sanitation supervisors, and map location actions.
 */
@Composable
fun BalasoreWardsDirectorySheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    onLocateWardOnMap: ((BalasoreWard) -> Unit)? = null,
    onReportCivicIssueInWard: ((BalasoreWard) -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    val allWards = remember { BalasoreMunicipalWards.getAllWards() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedZone by remember { mutableStateOf(MunicipalZone.ALL) }

    // Filtered wards
    val filteredWards = remember(searchQuery, selectedZone) {
        allWards.filter { ward ->
            val matchesZone = (selectedZone == MunicipalZone.ALL) || (ward.zone == selectedZone)
            val query = searchQuery.trim().lowercase()
            val matchesQuery = query.isEmpty() ||
                ward.wardNumber.toString() == query ||
                ward.nameEn.lowercase().contains(query) ||
                ward.nameOd.contains(query) ||
                ward.prominentLocalities.lowercase().contains(query) ||
                ward.keyLandmarks.any { it.lowercase().contains(query) }
            matchesZone && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("balasore_wards_directory_sheet")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🏛️", fontSize = 22.sp)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isOdia) "ବାଲେଶ୍ୱର ପୌରପାଳିକା: ସମସ୍ତ ୩୧ ୱାର୍ଡ" else "Balasore Municipality Wards (1-31)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate900
                        )
                    )
                    Text(
                        text = if (isOdia) "ପ୍ରତ୍ୟେକ ୱାର୍ଡର ବିବରଣୀ, ସ୍ଥାନୀୟ ଅଞ୍ଚଳ ଓ ସମସ୍ୟା ଦାଖଲ" else "Official Ward Boundaries, Localities & Sanitation Directory",
                        fontSize = 11.sp,
                        color = BentoSlate600
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_wards_directory_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Close",
                    tint = BentoSlate800
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Quick Municipal Telemetry Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFEFF6FF),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isOdia) "୩୧ ଟି ପୌର ୱାର୍ଡ • ୪୨,୫୦୦+ ଘର" else "31 Administrative Wards • 42,500+ Households",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E40AF)
                    )
                    Text(
                        text = if (isOdia) "ପୌରପାଳିକା ନିୟନ୍ତ୍ରଣ କକ୍ଷ: ୦୬୭୮୨-୨୬୨୦୨୪" else "Municipality 24x7 Helpline: 06782-262024",
                        fontSize = 10.5.sp,
                        color = BentoSlate700
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                    modifier = Modifier.clickable {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:06782262024"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = BentoPrimaryBlue, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(text = "Helpline", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BentoPrimaryBlue)
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("wards_directory_search_input"),
            placeholder = {
                Text(
                    text = if (isOdia) "ୱାର୍ଡ ନମ୍ବର (୧-୩୧), ଅଞ୍ଚଳ କିମ୍ବା ସ୍ଥାନ ଖୋଜନ୍ତୁ..." else "Search Ward No (1-31), locality or landmark...",
                    fontSize = 12.sp
                )
            },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = BentoSlate600)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = BentoSlate600)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = BentoPrimaryBlue,
                unfocusedBorderColor = BentoSlate200
            )
        )

        Spacer(Modifier.height(8.dp))

        // Zone Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(MunicipalZone.entries) { zone ->
                val isSel = selectedZone == zone
                FilterChip(
                    selected = isSel,
                    onClick = { selectedZone = zone },
                    label = {
                        Text(
                            text = if (isOdia) zone.labelOd else zone.labelEn,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0F172A),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = BentoSlate800
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSel) Color(0xFF0F172A) else BentoSlate200)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Header Results Count
        Text(
            text = if (isOdia) "ମିଳିଥିବା ୱାର୍ଡ ସଂଖ୍ୟା: ${filteredWards.size} / ୩୧" else "Showing ${filteredWards.size} of 31 Municipal Wards",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = BentoSlate600,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        Spacer(Modifier.height(6.dp))

        // Ward Cards List
        if (filteredWards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", fontSize = 36.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isOdia) "କୌଣସି ୱାର୍ଡ ମିଳିଲା ନାହିଁ" else "No wards matching your search",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate700
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredWards, key = { it.wardNumber }) { ward ->
                    WardDirectoryItemCard(
                        ward = ward,
                        language = language,
                        onLocateOnMap = {
                            onLocateWardOnMap?.invoke(ward)
                        },
                        onReportIssue = {
                            onReportCivicIssueInWard?.invoke(ward)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Individual card representation for a Balasore Municipal Ward.
 */
@Composable
fun WardDirectoryItemCard(
    ward: BalasoreWard,
    language: AppLanguage,
    onLocateOnMap: () -> Unit,
    onReportIssue: () -> Unit
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ward_card_${ward.wardNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BentoSlate200)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Ward Badge + Zone Tag + SLA rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "Ward ${ward.wardNumber}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEDE9FE),
                        border = BorderStroke(1.dp, Color(0xFFC4B5FD))
                    ) {
                        Text(
                            text = if (isOdia) ward.zone.labelOd else ward.zone.labelEn,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6D28D9),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                ) {
                    Text(
                        text = "⭐ ${ward.slaResolutionRatePercent}% SLA Fixed",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Ward Title & Odia script
            Text(
                text = ward.nameEn,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = BentoSlate900
            )
            Text(
                text = ward.nameOd,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = BentoPrimaryBlue
            )

            Spacer(Modifier.height(6.dp))

            // Prominent Localities
            Text(
                text = "📍 Localities: ${if (isOdia) ward.prominentLocalitiesOd else ward.prominentLocalities}",
                fontSize = 11.sp,
                color = BentoSlate700,
                lineHeight = 16.sp
            )

            Spacer(Modifier.height(4.dp))

            // Key Landmarks
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏛️ Landmarks: ${ward.keyLandmarks.joinToString(" • ")}",
                    fontSize = 10.5.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(8.dp))

            // Sanitation & Households Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sanitation Inspector: ${ward.sanitationSupervisor}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoSlate800
                    )
                    Text(
                        text = "Households: ~${ward.estimatedHouseholds} • PIN ${ward.pinCode}",
                        fontSize = 9.5.sp,
                        color = Color(0xFF64748B)
                    )
                }

                IconButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ward.sanitationSupervisorPhone.replace(" ", "")}"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = "Call Supervisor", tint = Color(0xFF15803D), modifier = Modifier.size(15.dp))
                }
            }

            Spacer(Modifier.height(10.dp))

            // Action Buttons: View on Map & Report Issue
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onLocateOnMap,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoPrimaryBlue),
                    border = BorderStroke(1.dp, BentoPrimaryBlue),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("locate_ward_btn_${ward.wardNumber}")
                ) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isOdia) "ମ୍ୟାପ୍‌ରେ ଦେଖନ୍ତୁ" else "View on Map",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onReportIssue,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("report_issue_ward_btn_${ward.wardNumber}")
                ) {
                    Icon(imageVector = Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isOdia) "ସମସ୍ୟା ଦାଖଲ" else "Report Issue",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
