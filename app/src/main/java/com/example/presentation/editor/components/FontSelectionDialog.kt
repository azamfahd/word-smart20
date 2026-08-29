package com.example.presentation.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSelectionDialog(
    currentFont: String,
    isRtl: Boolean,
    onFontSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMainGroup by remember { mutableStateOf<FontMainGroup?>(null) } // null = All
    var selectedSubCategory by remember { mutableStateOf<FontSubCategory?>(null) }
    var customPreviewText by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }

    // Filtered list of fonts based on search, main group, and subcategory
    val filteredFonts = remember(searchQuery, selectedMainGroup, selectedSubCategory) {
        AppFonts.allFonts.filter { item ->
            val matchesMainGroup = selectedMainGroup == null || item.mainGroup == selectedMainGroup
            val matchesSubCategory = selectedSubCategory == null || item.subCategory == selectedSubCategory
            val matchesSearch = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.arabicName.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true) ||
                    item.subCategory.titleAr.contains(searchQuery, ignoreCase = true) ||
                    item.subCategory.titleEn.contains(searchQuery, ignoreCase = true)
            matchesMainGroup && matchesSubCategory && matchesSearch
        }
    }

    // Counts for main groups
    val totalCount = AppFonts.allFonts.size
    val arabicCount = remember { AppFonts.allFonts.count { it.mainGroup == FontMainGroup.ARABIC } }
    val englishCount = remember { AppFonts.allFonts.count { it.mainGroup == FontMainGroup.ENGLISH } }
    val commonCount = remember { AppFonts.allFonts.count { it.mainGroup == FontMainGroup.COMMON } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 1. Header (Microsoft Word Font Dialog Title Bar)
                Surface(
                    color = Color(0xFF185ABD),
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FontDownload,
                                        contentDescription = "Word Font Dialog",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isRtl) "مربع حوار الخط — Microsoft Word" else "Font Dialog — Microsoft Word",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (isRtl) "تنسيق عائلات الخطوط العربية والإنجليزي والمشتركة" else "Format Arabic, English and Bilingual Font Families",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Primary Group Tabs (الكل • الخط العربي • الخط الإنجليزي • الخط المشترك)
                PrimaryTabRow(
                    selectedTabIndex = when (selectedMainGroup) {
                        null -> 0
                        FontMainGroup.ARABIC -> 1
                        FontMainGroup.ENGLISH -> 2
                        FontMainGroup.COMMON -> 3
                        else -> 0
                    },
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = Color(0xFF185ABD)
                ) {
                    Tab(
                        selected = selectedMainGroup == null,
                        onClick = {
                            selectedMainGroup = null
                            selectedSubCategory = null
                        },
                        text = {
                            Text(
                                text = if (isRtl) "جميع الخطوط ($totalCount)" else "All Fonts ($totalCount)",
                                fontSize = 12.sp,
                                fontWeight = if (selectedMainGroup == null) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = selectedMainGroup == FontMainGroup.ARABIC,
                        onClick = {
                            selectedMainGroup = FontMainGroup.ARABIC
                            selectedSubCategory = null
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF059669)))
                                Text(
                                    text = if (isRtl) "الخط العربي ($arabicCount)" else "Arabic ($arabicCount)",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedMainGroup == FontMainGroup.ARABIC) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedMainGroup == FontMainGroup.ENGLISH,
                        onClick = {
                            selectedMainGroup = FontMainGroup.ENGLISH
                            selectedSubCategory = null
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2563EB)))
                                Text(
                                    text = if (isRtl) "الخط الإنجليزي ($englishCount)" else "English ($englishCount)",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedMainGroup == FontMainGroup.ENGLISH) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedMainGroup == FontMainGroup.COMMON,
                        onClick = {
                            selectedMainGroup = FontMainGroup.COMMON
                            selectedSubCategory = null
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF7C3AED)))
                                Text(
                                    text = if (isRtl) "الخط المشترك ($commonCount)" else "Common ($commonCount)",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedMainGroup == FontMainGroup.COMMON) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Search & Custom Preview Toggle Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                if (isRtl) "ابحث عن خط بالاسم العربي أو الإنجليزي أو النمط..." else "Search font by name or style...",
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF185ABD))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )

                    // Toggle Custom Test Text Button
                    IconButton(
                        onClick = { showCustomInput = !showCustomInput },
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, if (showCustomInput) Color(0xFF185ABD) else Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                            .background(if (showCustomInput) Color(0xFFEFF6FF) else Color.Transparent, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = "Custom Sample Text",
                            tint = if (showCustomInput) Color(0xFF185ABD) else Color(0xFF64748B)
                        )
                    }
                }

                // 4. Expandable Custom Test Text Input
                AnimatedVisibility(visible = showCustomInput) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedTextField(
                            value = customPreviewText,
                            onValueChange = { customPreviewText = it },
                            placeholder = {
                                Text(if (isRtl) "اكتب جملتك المخصصة هنا لمعاينتها مباشرة بكافة الخطوط..." else "Type custom sentence to preview across all fonts...")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 5. Sub-Category Chips Row
                val subCategoriesForCurrent = remember(selectedMainGroup) {
                    when (selectedMainGroup) {
                        FontMainGroup.ARABIC -> listOf(
                            FontSubCategory.RUQAA,
                            FontSubCategory.KUFI,
                            FontSubCategory.NASKH_HERITAGE,
                            FontSubCategory.DECORATIVE,
                            FontSubCategory.MODERN_ARABIC
                        )
                        FontMainGroup.ENGLISH -> listOf(
                            FontSubCategory.SANS_SERIF,
                            FontSubCategory.SERIF_CLASSIC,
                            FontSubCategory.MONOSPACE,
                            FontSubCategory.DISPLAY_ARTISTIC
                        )
                        FontMainGroup.COMMON -> listOf(
                            FontSubCategory.OFFICE_STANDARD,
                            FontSubCategory.BILINGUAL_HARMONY
                        )
                        else -> FontSubCategory.values().toList()
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedSubCategory == null,
                        onClick = { selectedSubCategory = null },
                        label = { Text(if (isRtl) "الكل (${filteredFonts.size})" else "All (${filteredFonts.size})", fontSize = 11.sp) }
                    )
                    for (subCat in subCategoriesForCurrent) {
                        val count = AppFonts.allFonts.count {
                            (selectedMainGroup == null || it.mainGroup == selectedMainGroup) && it.subCategory == subCat
                        }
                        if (count > 0) {
                            FilterChip(
                                selected = selectedSubCategory == subCat,
                                onClick = { selectedSubCategory = if (selectedSubCategory == subCat) null else subCat },
                                label = { Text(if (isRtl) "${subCat.titleAr} ($count)" else "${subCat.titleEn} ($count)", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(6.dp))

                // 6. Font Cards List
                if (filteredFonts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isRtl) "لم يتم العثور على أي خط يطابق بحثك" else "No fonts matched your search",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredFonts, key = { it.name }) { fontItem ->
                            val isSelected = currentFont.equals(fontItem.name, ignoreCase = true)
                            val fontFamily = AppFonts.getFontFamily(fontItem.name)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onFontSelected(fontItem.name)
                                        onDismiss()
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFFAFAFA)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF185ABD) else Color(0xFFE2E8F0)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    // Row 1: Font Name, Arabic Name, Group Badges & Checkmark
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = fontItem.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = if (isSelected) Color(0xFF185ABD) else Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "(${fontItem.arabicName})",
                                                fontSize = 13.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // Main Group Badge
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(fontItem.mainGroup.badgeColorHex).copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = if (isRtl) fontItem.mainGroup.titleAr else fontItem.mainGroup.titleEn,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(fontItem.mainGroup.badgeColorHex),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            // Sub-Category Badge
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFF1F5F9)
                                            ) {
                                                Text(
                                                    text = if (isRtl) fontItem.subCategory.titleAr else fontItem.subCategory.titleEn,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF334155),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            if (isSelected) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color(0xFF185ABD),
                                                    modifier = Modifier.size(20.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Active Font",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Row 2: Live Typography Preview Box
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = Color.White,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            if (customPreviewText.isNotBlank()) {
                                                // Show custom user text in this font
                                                Text(
                                                    text = customPreviewText,
                                                    fontFamily = fontFamily,
                                                    fontSize = 17.sp,
                                                    color = Color(0xFF0F172A),
                                                    lineHeight = 25.sp
                                                )
                                            } else {
                                                // Default rich previews based on group
                                                when (fontItem.mainGroup) {
                                                    FontMainGroup.ARABIC -> {
                                                        Text(
                                                            text = fontItem.arabicPreview,
                                                            fontFamily = fontFamily,
                                                            fontSize = 17.sp,
                                                            color = Color(0xFF0F172A),
                                                            lineHeight = 25.sp
                                                        )
                                                    }
                                                    FontMainGroup.ENGLISH -> {
                                                        Text(
                                                            text = fontItem.englishPreview,
                                                            fontFamily = fontFamily,
                                                            fontSize = 15.sp,
                                                            color = Color(0xFF0F172A)
                                                        )
                                                    }
                                                    FontMainGroup.COMMON -> {
                                                        Text(
                                                            text = fontItem.arabicPreview,
                                                            fontFamily = fontFamily,
                                                            fontSize = 16.sp,
                                                            color = Color(0xFF0F172A),
                                                            lineHeight = 24.sp
                                                        )
                                                        Spacer(modifier = Modifier.height(3.dp))
                                                        Text(
                                                            text = fontItem.englishPreview,
                                                            fontFamily = fontFamily,
                                                            fontSize = 13.sp,
                                                            color = Color(0xFF475569)
                                                        )
                                                    }
                                                    else -> {
                                                        Text(
                                                            text = fontItem.arabicPreview,
                                                            fontFamily = fontFamily,
                                                            fontSize = 16.sp,
                                                            color = Color(0xFF0F172A),
                                                            lineHeight = 24.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Row 3: Description / Usage tip
                                    Text(
                                        text = fontItem.description,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(10.dp))

                // Microsoft Word Dialog Bottom Action Buttons Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentFont.isNotBlank()) {
                                onFontSelected(currentFont)
                            }
                            onDismiss()
                        },
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = if (isRtl) "تعيين كافتراضي" else "Set As Default",
                            fontSize = 12.sp,
                            color = Color(0xFF334155)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (currentFont.isNotBlank()) {
                                    onFontSelected(currentFont)
                                }
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185ABD)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (isRtl) "موافق" else "OK",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = if (isRtl) "إلغاء الأمر" else "Cancel",
                                fontSize = 13.sp,
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }
            }
        }
    }
}
