package com.example.presentation.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.editor.*

fun localize(englishText: String, isRtl: Boolean): String {
    if (!isRtl) return englishText
    return when (englishText) {
        "File" -> "ملف"
        "Home" -> "الرئيسية"
        "Insert" -> "إدراج"
        "Design" -> "تصميم"
        "Layout" -> "تخطيط"
        "View" -> "عرض"
        "Clipboard" -> "الحافظة"
        "Paste" -> "لصق"
        "Cut" -> "قص"
        "Copy" -> "نسخ"
        "Font" -> "خط"
        "Black" -> "أسود"
        "Dark Red" -> "أحمر داكن"
        "Red" -> "أحمر"
        "Orange" -> "برتقالي"
        "Green" -> "أخضر"
        "Blue" -> "أزرق"
        "Purple" -> "بنفسجي"
        "No Color" -> "بدون لون"
        "Yellow" -> "أصفر"
        "Light Green" -> "أخضر فاتح"
        "Cyan" -> "سماوي"
        "Pink" -> "وردي"
        "Paragraph" -> "فقرة"
        "Pages" -> "صفحات"
        "Page Break" -> "فاصل صفحات"
        "Tables" -> "جداول"
        "3x3 Table" -> "جدول 3x3"
        "Illustrations" -> "رسومات توضيحية"
        "Picture" -> "صورة"
        "Shapes" -> "أشكال"
        "Header & Footer" -> "رأس وتذييل"
        "Header" -> "رأس الصفحة"
        "Footer" -> "تذييل الصفحة"
        "Page Number" -> "رقم الصفحة"
        "Page Setup" -> "إعداد الصفحة"
        "Page Background" -> "خلفية الصفحة"
        "Watermark" -> "العلامة المائية"
        "Draft" -> "مسودة"
        "Confidential" -> "سري للغاية"
        "Remove Watermark" -> "إزالة العلامة المائية"
        "Page Color" -> "لون الصفحة"
        "White" -> "أبيض"
        "Cream" -> "كريمي"
        "Light Gray" -> "رمادي فاتح"
        "Page Borders" -> "حدود الصفحة"
        "Borders and Shading" -> "حدود وتظليل"
        "Setting" -> "الإعداد"
        "None" -> "بلا"
        "Box" -> "إحاطة"
        "Shadow" -> "ظل"
        "Style" -> "النمط"
        "Solid" -> "متصل"
        "Dashed" -> "متقطع"
        "Dotted" -> "منقط"
        "Double" -> "مزدوج"
        "Width" -> "العرض"
        "Color" -> "اللون"
        "OK" -> "موافق"
        "Cancel" -> "إلغاء"
        "Margins" -> "الهوامش"
        "Normal Margins" -> "هوامش عادية"
        "Narrow Margins" -> "هوامش ضيقة"
        "Moderate Margins" -> "هوامش متوسطة"
        "Wide Margins" -> "هوامش عريضة"
        "Orientation" -> "الاتجاه"
        "Portrait" -> "عمودي"
        "Landscape" -> "أفقي"
        "Size" -> "الحجم"
        "A4" -> "A4"
        "A3" -> "A3"
        "LETTER" -> "Letter"
        "LEGAL" -> "Legal"
        "A5" -> "A5"
        "Zoom" -> "تكبير/تصغير"
        "Zoom In" -> "تكبير"
        "Zoom Out" -> "تصغير"
        "100%" -> "100%"
        "Document Views" -> "طرق عرض المستند"
        "Multi-Page View" -> "عرض متعدد الصفحات"
        "Header/Footer" -> "الرأس/التذييل"
        "Rectangle" -> "مستطيل"
        "Oval" -> "بيضاوي"
        "Arrow" -> "سهم"
        "Line" -> "خط مستقيم"
        "Star" -> "نجمة"
        else -> englishText
    }
}

@Composable
fun WordRibbon(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = Color(0xFFD0D7DE))
    ) {
        // Ribbon Tab Headers (File, Home, Insert, Layout, View)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF185ABD)) // Signature MS Word Blue
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            RibbonTab.entries.forEach { tab ->
                RibbonTabItem(
                    title = localize(tab.title, state.isRtl),
                    isSelected = state.activeTab == tab,
                    onClick = { 
                        if (tab == RibbonTab.FILE) {
                            onEvent(RibbonEvent.OnToggleFileMenu)
                        } else {
                            onEvent(RibbonEvent.ChangeTab(tab))
                        }
                    }
                )
            }
        }

        // Ribbon Toolbar Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F4F6))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            when (state.activeTab) {
                RibbonTab.HOME -> HomeTabContent(state, onEvent)
                RibbonTab.INSERT -> InsertTabContent(state, onEvent)
                RibbonTab.DESIGN -> DesignTabContent(state, onEvent)
                RibbonTab.LAYOUT -> LayoutTabContent(state, onEvent)
                RibbonTab.VIEW -> ViewTabContent(state, onEvent)
                else -> HomeTabContent(state, onEvent)
            }
        }
    }
}

@Composable
fun RibbonTabItem(title: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFFF3F4F6) else Color.Transparent
    val textColor = if (isSelected) Color(0xFF185ABD) else Color.White

    Box(
        modifier = Modifier
            .background(backgroundColor, shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(
            text = title.uppercase(),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun HomeTabContent(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    var showFontMenu by remember { mutableStateOf(false) }
    var showSizeMenu by remember { mutableStateOf(false) }
    var showLineSpacingMenu by remember { mutableStateOf(false) }
    var showTextColorMenu by remember { mutableStateOf(false) }
    var showHighlightMenu by remember { mutableStateOf(false) }

    val fontFamilies = listOf(
        "Calibri", "Arial", "Times New Roman", "Tahoma", "Segoe UI",
        "Cairo", "Amiri", "Roboto", "Courier New", "Georgia"
    )
    val fontSizes = listOf(8, 9, 10, 11, 12, 14, 16, 18, 20, 24, 28, 36, 48, 72)
    val lineSpacings = listOf(1.0f, 1.15f, 1.5f, 2.0f, 2.5f, 3.0f)
    
    val textColors = listOf(
        Color.Black to localize("Black", state.isRtl),
        Color(0xFFB91C1C) to localize("Dark Red", state.isRtl),
        Color(0xFFDC2626) to localize("Red", state.isRtl),
        Color(0xFFD97706) to localize("Orange", state.isRtl),
        Color(0xFF16A34A) to localize("Green", state.isRtl),
        Color(0xFF2563EB) to localize("Blue", state.isRtl),
        Color(0xFF7C3AED) to localize("Purple", state.isRtl)
    )

    val highlightColors = listOf(
        Color.Transparent to localize("No Color", state.isRtl),
        Color(0xFFFEF08A) to localize("Yellow", state.isRtl),
        Color(0xFFBBF7D0) to localize("Light Green", state.isRtl),
        Color(0xFFA5F3FC) to localize("Cyan", state.isRtl),
        Color(0xFFFBCFE8) to localize("Pink", state.isRtl)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Clipboard Group
        RibbonGroup(localize("Clipboard", state.isRtl)) {
            RibbonLargeButton(Icons.Default.ContentPaste, localize("Paste", state.isRtl)) { onEvent(RibbonEvent.OnPasteClicked) }
            Column {
                RibbonSmallButton(Icons.Default.ContentCut, localize("Cut", state.isRtl)) { onEvent(RibbonEvent.OnCutClicked) }
                RibbonSmallButton(Icons.Default.ContentCopy, localize("Copy", state.isRtl)) { onEvent(RibbonEvent.OnCopyClicked) }
            }
        }

        VerticalDivider()

        // 2. Font Group (Fully Interactive)
        RibbonGroup(localize("Font", state.isRtl)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Row 1: Font Family, Size, Clear Formatting
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        DropdownSelector(text = state.fontFamily, width = 120.dp) { showFontMenu = true }
                        DropdownMenu(expanded = showFontMenu, onDismissRequest = { showFontMenu = false }) {
                            fontFamilies.forEach { font ->
                                DropdownMenuItem(
                                    text = { Text(font) },
                                    onClick = {
                                        onEvent(RibbonEvent.OnFontFamilyChanged(font))
                                        showFontMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Box {
                        DropdownSelector(text = state.fontSize.toString(), width = 56.dp) { showSizeMenu = true }
                        DropdownMenu(expanded = showSizeMenu, onDismissRequest = { showSizeMenu = false }) {
                            fontSizes.forEach { size ->
                                DropdownMenuItem(
                                    text = { Text("$size") },
                                    onClick = {
                                        onEvent(RibbonEvent.OnFontSizeChanged(size))
                                        showSizeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    RibbonButton(Icons.Default.FormatClear) { onEvent(RibbonEvent.OnClearFormattingClicked) }
                }

                // Row 2: Formatting Toggles (Bold, Italic, Underline, Strike, Sub, Super, Colors)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    RibbonToggleButton(Icons.Default.FormatBold, state.isBold) { onEvent(RibbonEvent.OnBoldClicked) }
                    RibbonToggleButton(Icons.Default.FormatItalic, state.isItalic) { onEvent(RibbonEvent.OnItalicClicked) }
                    RibbonToggleButton(Icons.Default.FormatUnderlined, state.isUnderline) { onEvent(RibbonEvent.OnUnderlineClicked) }
                    RibbonToggleButton(Icons.Default.FormatStrikethrough, state.isStrikethrough) { onEvent(RibbonEvent.OnStrikethroughClicked) }
                    RibbonToggleButton(Icons.Default.VerticalAlignBottom, state.isSubscript) { onEvent(RibbonEvent.OnSubscriptClicked) }
                    RibbonToggleButton(Icons.Default.VerticalAlignTop, state.isSuperscript) { onEvent(RibbonEvent.OnSuperscriptClicked) }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Text Color Dropdown
                    Box {
                        RibbonColorButton(Icons.Default.FormatColorText, state.textColor) { showTextColorMenu = true }
                        DropdownMenu(expanded = showTextColorMenu, onDismissRequest = { showTextColorMenu = false }) {
                            textColors.forEach { (color, name) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(16.dp).background(color, CircleShape).border(1.dp, Color.Gray, CircleShape))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(name)
                                        }
                                    },
                                    onClick = {
                                        onEvent(RibbonEvent.OnTextColorChanged(color))
                                        showTextColorMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Highlight Color Dropdown
                    Box {
                        RibbonColorButton(Icons.Default.FormatColorFill, state.highlightColor) { showHighlightMenu = true }
                        DropdownMenu(expanded = showHighlightMenu, onDismissRequest = { showHighlightMenu = false }) {
                            highlightColors.forEach { (color, name) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(16.dp).background(if (color == Color.Transparent) Color.White else color, CircleShape).border(1.dp, Color.Gray, CircleShape))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(name)
                                        }
                                    },
                                    onClick = {
                                        onEvent(RibbonEvent.OnHighlightColorChanged(color))
                                        showHighlightMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        VerticalDivider()

        // 3. Paragraph Group (Exhaustive & Fully Interactive)
        RibbonGroup(localize("Paragraph", state.isRtl)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Lists, Indentation, Numeral System Toggle
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    RibbonToggleButton(Icons.Default.FormatListBulleted, state.isBulletedList) { onEvent(RibbonEvent.OnBulletedListToggled) }
                    RibbonToggleButton(Icons.Default.FormatListNumbered, state.isNumberedList) { onEvent(RibbonEvent.OnNumberedListToggled) }
                    Spacer(modifier = Modifier.width(4.dp))
                    RibbonButton(Icons.AutoMirrored.Filled.FormatIndentDecrease) { onEvent(RibbonEvent.OnDecreaseIndentClicked) }
                    RibbonButton(Icons.AutoMirrored.Filled.FormatIndentIncrease) { onEvent(RibbonEvent.OnIncreaseIndentClicked) }
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    // Numeral System Toggle (123 vs ١٢٣)
                    RibbonToggleButton(
                        icon = Icons.Default.Numbers,
                        isChecked = state.numeralSystem == NumeralSystem.ARABIC
                    ) {
                        val newSystem = if (state.numeralSystem == NumeralSystem.WESTERN) NumeralSystem.ARABIC else NumeralSystem.WESTERN
                        onEvent(RibbonEvent.OnNumeralSystemChanged(newSystem))
                    }
                }

                // Alignments, Line Spacing, Text Direction
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    RibbonToggleButton(Icons.AutoMirrored.Filled.FormatAlignLeft, state.alignment == TextAlignment.LEFT) { onEvent(RibbonEvent.OnAlignmentChanged(TextAlignment.LEFT)) }
                    RibbonToggleButton(Icons.Default.FormatAlignCenter, state.alignment == TextAlignment.CENTER) { onEvent(RibbonEvent.OnAlignmentChanged(TextAlignment.CENTER)) }
                    RibbonToggleButton(Icons.AutoMirrored.Filled.FormatAlignRight, state.alignment == TextAlignment.RIGHT) { onEvent(RibbonEvent.OnAlignmentChanged(TextAlignment.RIGHT)) }
                    RibbonToggleButton(Icons.Default.FormatAlignJustify, state.alignment == TextAlignment.JUSTIFY) { onEvent(RibbonEvent.OnAlignmentChanged(TextAlignment.JUSTIFY)) }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Line Spacing Dropdown
                    Box {
                        DropdownSelector(text = "${state.lineSpacing}", width = 64.dp) { showLineSpacingMenu = true }
                        DropdownMenu(expanded = showLineSpacingMenu, onDismissRequest = { showLineSpacingMenu = false }) {
                            lineSpacings.forEach { spacing ->
                                DropdownMenuItem(
                                    text = { Text("$spacing") },
                                    onClick = {
                                        onEvent(RibbonEvent.OnLineSpacingChanged(spacing))
                                        showLineSpacingMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    RibbonToggleButton(Icons.AutoMirrored.Filled.FormatTextdirectionLToR, !state.isTextRtl) { onEvent(RibbonEvent.OnTextDirectionToggled) }
                    RibbonToggleButton(Icons.AutoMirrored.Filled.FormatTextdirectionRToL, state.isTextRtl) { onEvent(RibbonEvent.OnTextDirectionToggled) }
                }
            }
        }
    }
}

@Composable
fun InsertTabContent(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    var showShapeMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RibbonGroup(localize("Pages", state.isRtl)) {
            RibbonLargeButton(Icons.Default.InsertPageBreak, localize("Page Break", state.isRtl)) { onEvent(RibbonEvent.OnInsertPageBreakClicked) }
        }
        VerticalDivider()
        RibbonGroup(localize("Tables", state.isRtl)) {
            RibbonLargeButton(Icons.Default.TableChart, localize("3x3 Table", state.isRtl)) { onEvent(RibbonEvent.OnInsertTableClicked(3, 3)) }
        }
        VerticalDivider()
        RibbonGroup(localize("Illustrations", state.isRtl)) {
            RibbonLargeButton(Icons.Default.Image, localize("Picture", state.isRtl)) { onEvent(RibbonEvent.OnInsertImageWithUri("embedded_image.png")) }
            Box {
                RibbonLargeButton(Icons.Default.Category, localize("Shapes", state.isRtl)) { showShapeMenu = true }
                DropdownMenu(expanded = showShapeMenu, onDismissRequest = { showShapeMenu = false }) {
                    ShapeType.entries.forEach { shape ->
                        DropdownMenuItem(
                            text = { Text(localize(shape.name.lowercase().replaceFirstChar { it.uppercase() }, state.isRtl)) },
                            onClick = {
                                onEvent(RibbonEvent.OnInsertShapeClicked(shape))
                                showShapeMenu = false
                            }
                        )
                    }
                }
            }
        }
        VerticalDivider()
        RibbonGroup(localize("Header & Footer", state.isRtl)) {
            Column {
                RibbonSmallButton(Icons.Default.BorderTop, localize("Header", state.isRtl)) { onEvent(RibbonEvent.OnToggleHeaderFooterMode) }
                RibbonSmallButton(Icons.Default.BorderBottom, localize("Footer", state.isRtl)) { onEvent(RibbonEvent.OnToggleHeaderFooterMode) }
                RibbonSmallButton(Icons.Default.Numbers, localize("Page Number", state.isRtl)) { onEvent(RibbonEvent.OnInsertPageNumberClicked) }
            }
        }
    }
}

@Composable
fun DesignTabContent(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    var showWatermarkMenu by remember { mutableStateOf(false) }
    var showPageColorMenu by remember { mutableStateOf(false) }
    var showBordersDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RibbonGroup(localize("Page Background", state.isRtl)) {
            // Watermark
            Box {
                RibbonLargeButton(Icons.Default.WaterDrop, localize("Watermark", state.isRtl)) { showWatermarkMenu = true }
                DropdownMenu(expanded = showWatermarkMenu, onDismissRequest = { showWatermarkMenu = false }) {
                    DropdownMenuItem(text = { Text(localize("Draft", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnWatermarkChanged("DRAFT")); showWatermarkMenu = false })
                    DropdownMenuItem(text = { Text(localize("Confidential", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnWatermarkChanged("CONFIDENTIAL")); showWatermarkMenu = false })
                    Divider()
                    DropdownMenuItem(text = { Text(localize("Remove Watermark", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnWatermarkChanged("")); showWatermarkMenu = false })
                }
            }

            // Page Color
            Box {
                RibbonLargeButton(Icons.Default.FormatColorFill, localize("Page Color", state.isRtl)) { showPageColorMenu = true }
                DropdownMenu(expanded = showPageColorMenu, onDismissRequest = { showPageColorMenu = false }) {
                    DropdownMenuItem(text = { Text(localize("White", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnPageColorChanged(Color.White)); showPageColorMenu = false })
                    DropdownMenuItem(text = { Text(localize("Cream", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnPageColorChanged(Color(0xFFFFFDD0))); showPageColorMenu = false })
                    DropdownMenuItem(text = { Text(localize("Light Gray", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnPageColorChanged(Color(0xFFF3F4F6))); showPageColorMenu = false })
                }
            }

            // Page Borders
            RibbonLargeButton(Icons.Default.BorderStyle, localize("Page Borders", state.isRtl)) { showBordersDialog = true }
        }
    }

    if (showBordersDialog) {
        BordersAndShadingDialog(
            state = state,
            onDismiss = { showBordersDialog = false },
            onApply = { border ->
                onEvent(RibbonEvent.OnPageBorderChanged(border))
                showBordersDialog = false
            }
        )
    }
}

@Composable
fun BordersAndShadingDialog(
    state: EditorState,
    onDismiss: () -> Unit,
    onApply: (PageBorder) -> Unit
) {
    var setting by remember { mutableStateOf(state.pageBorder.setting) }
    var style by remember { mutableStateOf(state.pageBorder.style) }
    var color by remember { mutableStateOf(state.pageBorder.color) }
    var widthPt by remember { mutableFloatStateOf(state.pageBorder.widthPt) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localize("Borders and Shading", state.isRtl), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Setting
                Text(localize("Setting", state.isRtl), fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BorderSettingOption(localize("None", state.isRtl), setting == BorderSetting.NONE) { setting = BorderSetting.NONE }
                    BorderSettingOption(localize("Box", state.isRtl), setting == BorderSetting.BOX) { setting = BorderSetting.BOX }
                    BorderSettingOption(localize("Shadow", state.isRtl), setting == BorderSetting.SHADOW) { setting = BorderSetting.SHADOW }
                }

                Divider()

                // Style
                Text(localize("Style", state.isRtl), fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BorderStyleOption(localize("Solid", state.isRtl), style == BorderStyle.SOLID) { style = BorderStyle.SOLID }
                    BorderStyleOption(localize("Dashed", state.isRtl), style == BorderStyle.DASHED) { style = BorderStyle.DASHED }
                    BorderStyleOption(localize("Dotted", state.isRtl), style == BorderStyle.DOTTED) { style = BorderStyle.DOTTED }
                    BorderStyleOption(localize("Double", state.isRtl), style == BorderStyle.DOUBLE) { style = BorderStyle.DOUBLE }
                }

                Divider()

                // Width
                Text(localize("Width", state.isRtl), fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BorderWidthOption("0.5 pt", widthPt == 0.5f) { widthPt = 0.5f }
                    BorderWidthOption("1.5 pt", widthPt == 1.5f) { widthPt = 1.5f }
                    BorderWidthOption("3.0 pt", widthPt == 3.0f) { widthPt = 3.0f }
                    BorderWidthOption("4.5 pt", widthPt == 4.5f) { widthPt = 4.5f }
                }

                Divider()

                // Color
                Text(localize("Color", state.isRtl), fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorOption(Color.Black, color == Color.Black) { color = Color.Black }
                    ColorOption(Color.Red, color == Color.Red) { color = Color.Red }
                    ColorOption(Color.Blue, color == Color.Blue) { color = Color.Blue }
                    ColorOption(Color(0xFF16A34A), color == Color(0xFF16A34A)) { color = Color(0xFF16A34A) } // Green
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(PageBorder(setting, style, color, widthPt)) }) {
                Text(localize("OK", state.isRtl))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localize("Cancel", state.isRtl))
            }
        }
    )
}

@Composable
fun BorderSettingOption(text: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = isSelected, onClick = onClick, label = { Text(text) })
}

@Composable
fun BorderStyleOption(text: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = isSelected, onClick = onClick, label = { Text(text) })
}

@Composable
fun BorderWidthOption(text: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = isSelected, onClick = onClick, label = { Text(text) })
}

@Composable
fun ColorOption(optionColor: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(optionColor, CircleShape)
            .border(if (isSelected) 3.dp else 1.dp, if (isSelected) Color.Gray else Color.LightGray, CircleShape)
            .clickable { onClick() }
    )
}

@Composable
fun LayoutTabContent(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    var showMarginMenu by remember { mutableStateOf(false) }
    var showOrientationMenu by remember { mutableStateOf(false) }
    var showSizeMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RibbonGroup(localize("Page Setup", state.isRtl)) {
            Box {
                RibbonLargeButton(Icons.Default.Margin, localize("Margins", state.isRtl)) { showMarginMenu = true }
                DropdownMenu(expanded = showMarginMenu, onDismissRequest = { showMarginMenu = false }) {
                    PageMargin.entries.forEach { margin ->
                        val marginName = margin.name.lowercase().replaceFirstChar { it.uppercase() } + " Margins"
                        DropdownMenuItem(
                            text = { Text(localize(marginName, state.isRtl)) },
                            onClick = {
                                onEvent(RibbonEvent.OnPageMarginChanged(margin))
                                showMarginMenu = false
                            }
                        )
                    }
                }
            }

            Box {
                val icon = if (state.pageOrientation == PageOrientation.PORTRAIT) Icons.Default.StayCurrentPortrait else Icons.Default.StayCurrentLandscape
                RibbonLargeButton(icon, localize("Orientation", state.isRtl)) { showOrientationMenu = true }
                DropdownMenu(expanded = showOrientationMenu, onDismissRequest = { showOrientationMenu = false }) {
                    PageOrientation.entries.forEach { orientation ->
                        val orientationName = orientation.name.lowercase().replaceFirstChar { it.uppercase() }
                        DropdownMenuItem(
                            text = { Text(localize(orientationName, state.isRtl)) },
                            onClick = {
                                onEvent(RibbonEvent.OnPageOrientationChanged(orientation))
                                showOrientationMenu = false
                            }
                        )
                    }
                }
            }

            Box {
                RibbonLargeButton(Icons.Default.Description, localize("Size", state.isRtl)) { showSizeMenu = true }
                DropdownMenu(expanded = showSizeMenu, onDismissRequest = { showSizeMenu = false }) {
                    PageSize.entries.forEach { size ->
                        DropdownMenuItem(
                            text = { Text(localize(size.name, state.isRtl)) },
                            onClick = {
                                onEvent(RibbonEvent.OnPageSizeChanged(size))
                                showSizeMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ViewTabContent(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RibbonGroup(localize("Zoom", state.isRtl)) {
            RibbonLargeButton(Icons.Default.ZoomIn, localize("Zoom In", state.isRtl)) { onEvent(RibbonEvent.OnZoomChanged(state.zoomScale + 0.2f)) }
            RibbonLargeButton(Icons.Default.ZoomOut, localize("Zoom Out", state.isRtl)) { onEvent(RibbonEvent.OnZoomChanged(state.zoomScale - 0.2f)) }
            RibbonLargeButton(Icons.Default.RestartAlt, localize("100%", state.isRtl)) { onEvent(RibbonEvent.OnZoomChanged(1.0f)) }
        }
        VerticalDivider()
        RibbonGroup(localize("Document Views", state.isRtl)) {
            RibbonLargeButton(Icons.Default.ViewAgenda, localize("Multi-Page View", state.isRtl)) { /* Default active */ }
            RibbonLargeButton(Icons.Default.EditNote, localize("Header/Footer", state.isRtl)) { onEvent(RibbonEvent.OnToggleHeaderFooterMode) }
        }
    }
}

// --- Reusable Ribbon Widgets ---

@Composable
fun RibbonGroup(title: String, content: @Composable RowScope.() -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, fontSize = 11.sp, color = Color(0xFF64748B))
    }
}

@Composable
fun DropdownSelector(text: String, width: Dp, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .width(width)
            .height(28.dp)
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(3.dp))
            .background(Color.White, RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, fontSize = 12.sp, maxLines = 1, color = Color.Black)
        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", modifier = Modifier.size(16.dp), tint = Color.DarkGray)
    }
}

@Composable
fun RibbonLargeButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(26.dp), tint = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 11.sp, color = Color(0xFF1E293B))
    }
}

@Composable
fun RibbonButton(icon: ImageVector, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(28.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF1E293B), modifier = Modifier.size(18.dp))
    }
}

@Composable
fun RibbonColorButton(icon: ImageVector, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF1E293B), modifier = Modifier.size(18.dp))
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(3.dp)
                    .background(if (color == Color.Transparent) Color.Black else color)
            )
        }
    }
}

@Composable
fun RibbonToggleButton(icon: ImageVector, isChecked: Boolean, onClick: () -> Unit) {
    val bg = if (isChecked) Color(0xFFBFDBFE) else Color.Transparent
    val tint = if (isChecked) Color(0xFF1D4ED8) else Color(0xFF1E293B)

    Box(
        modifier = Modifier
            .size(28.dp)
            .background(bg, RoundedCornerShape(3.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun RibbonSmallButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(16.dp), tint = Color(0xFF1E293B))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 11.sp, color = Color(0xFF1E293B))
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .height(52.dp)
            .width(1.dp)
            .background(Color(0xFFCBD5E1))
            .padding(horizontal = 4.dp)
    )
}
