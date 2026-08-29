package com.example.presentation.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.presentation.editor.font.FontEngine
import com.example.presentation.editor.font.FontMetadata

/**
 * Authentic Microsoft Word Style Font Dropdown Menu for Windows.
 * Opens directly below the Font Name selector in the Ribbon.
 */
@Composable
fun WordFontDropdownMenu(
    currentFont: String,
    isRtl: Boolean,
    onFontSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allFonts = remember { FontEngine.getAllFonts() }
    val recentFonts = remember { FontEngine.getRecentFonts() }

    val filteredFonts = remember(searchQuery, allFonts) {
        if (searchQuery.isBlank()) {
            allFonts
        } else {
            allFonts.filter {
                it.family.contains(searchQuery, ignoreCase = true) ||
                it.arabicName.contains(searchQuery, ignoreCase = true) ||
                it.displayName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            modifier = Modifier
                .width(300.dp)
                .heightIn(max = 420.dp),
            shape = RoundedCornerShape(2.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, Color(0xFFA6B2C0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Search Input Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = if (isRtl) "ابحث عن اسم الخط..." else "Search font name...",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF0F172A)),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        } else null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2B579A),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                HorizontalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 4.dp))

                // Font List Container
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    // Recently Used Section
                    if (searchQuery.isBlank() && recentFonts.isNotEmpty()) {
                        item {
                            Text(
                                text = if (isRtl) "الخطوط المستخدمة مؤخراً (Recently Used Fonts)" else "Recently Used Fonts",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2B579A),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        items(recentFonts, key = { "recent_${it.id}" }) { fontMeta ->
                            FontDropdownRow(
                                fontMeta = fontMeta,
                                isSelected = currentFont.equals(fontMeta.family, ignoreCase = true),
                                isRtl = isRtl,
                                onClick = {
                                    onFontSelected(fontMeta.family)
                                    onDismiss()
                                }
                            )
                        }

                        item {
                            HorizontalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }

                    // All Fonts Section Header
                    item {
                        Text(
                            text = if (isRtl) "جميع الخطوط المتاحة (All Fonts)" else "All Available Fonts",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2B579A),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (filteredFonts.isEmpty()) {
                        item {
                            Text(
                                text = if (isRtl) "لا يوجد خط بهذا الاسم" else "No matching fonts found",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                            )
                        }
                    } else {
                        items(filteredFonts, key = { "all_${it.id}" }) { fontMeta ->
                            FontDropdownRow(
                                fontMeta = fontMeta,
                                isSelected = currentFont.equals(fontMeta.family, ignoreCase = true),
                                isRtl = isRtl,
                                onClick = {
                                    onFontSelected(fontMeta.family)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FontDropdownRow(
    fontMeta: FontMetadata,
    isSelected: Boolean,
    isRtl: Boolean,
    onClick: () -> Unit
) {
    val fontFamily = remember(fontMeta.family) {
        FontEngine.getFontFamily(fontMeta.family)
    }

    val backgroundColor = if (isSelected) Color(0xFFE5F1FB) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Selected Checkmark
            Box(modifier = Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color(0xFF2B579A),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Rendered Font Name in actual Typeface
            Text(
                text = fontMeta.displayName,
                fontFamily = fontFamily,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = Color(0xFF1E293B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        // Arabic Name Subtitle if distinct
        if (fontMeta.arabicName.isNotBlank() && !fontMeta.arabicName.equals(fontMeta.displayName, ignoreCase = true)) {
            Text(
                text = fontMeta.arabicName,
                fontSize = 11.sp,
                color = if (isSelected) Color(0xFF1D4ED8) else Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
