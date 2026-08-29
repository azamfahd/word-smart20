package com.example.presentation.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.editor.*

fun getPreviewFontFamily(fontName: String): FontFamily {
    return AppFonts.getFontFamily(fontName)
}

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
        "Print Layout" -> "تخطيط الطباعة"
        "Web Layout" -> "تخطيط الويب"
        "Read Mode" -> "وضع القراءة"
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
    onEvent: (RibbonEvent) -> Unit,
    onBackClick: () -> Unit,
    onOpenFileClick: () -> Unit,
    onSaveFileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Quick Access Toolbar (شريط الوصول السريع العلوي - Word Top Header)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF185ABD))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Side: Back Arrow + Word Logo + Document Title (Safe responsive grouping)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Back Button (إغلاق / العودة للشاشة الرئيسية)
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "العودة",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Word Icon Brand
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = Color(0xFF107C41),
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("W", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color(0xFF60A5FA).copy(alpha = 0.4f)))

                // Document Title (No more overlapping or clashing!)
                Text(
                    text = "${state.documentTitle}.docx - Word",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            // Right Side: Quick Access Actions (Save, Open, Undo, Redo, Language) + Quick Search Box
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                QuickAccessButton(icon = Icons.Default.Save, tooltip = "حفظ") {
                    onSaveFileClick()
                }
                QuickAccessButton(icon = Icons.Default.FolderOpen, tooltip = "فتح ملف") {
                    onOpenFileClick()
                }
                QuickAccessButton(icon = Icons.AutoMirrored.Filled.Undo, tooltip = "تراجع", enabled = state.canUndo) {
                    onEvent(RibbonEvent.OnUndoClicked)
                }
                QuickAccessButton(icon = Icons.AutoMirrored.Filled.Redo, tooltip = "إعادة", enabled = state.canRedo) {
                    onEvent(RibbonEvent.OnRedoClicked)
                }
                QuickAccessButton(icon = Icons.Default.Translate, tooltip = "اتجاه الواجهة") {
                    onEvent(RibbonEvent.OnLanguageToggled)
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(1.dp)
                        .height(14.dp)
                        .background(Color(0xFF60A5FA).copy(alpha = 0.4f))
                )

                // Quick Search Box
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF104EAD), // Elegant slightly lighter blue for search
                    modifier = Modifier
                        .width(85.dp)
                        .height(24.dp)
                        .clickable { onEvent(RibbonEvent.OnShowFindReplaceDialog) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (state.isRtl) "بحث..." else "Search...",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Ribbon Tab Headers (File, Home, Insert, Design, Layout, Review, View)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF185ABD)) // Signature MS Word Blue
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp)
                .padding(top = 4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            RibbonTab.entries.forEach { tab ->
                RibbonTabItem(
                    tab = tab,
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

        // Ribbon Toolbar Area - Uniform Height 96dp with Smooth Horizontal Scroll
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .background(Color(0xFFF8FAFC))
                .border(width = (0.5).dp, color = Color(0xFFE2E8F0))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (state.activeTab) {
                    RibbonTab.HOME -> HomeTabContent(state, onEvent)
                    RibbonTab.INSERT -> InsertTabContent(state, onEvent)
                    RibbonTab.DESIGN -> DesignTabContent(state, onEvent)
                    RibbonTab.LAYOUT -> LayoutTabContent(state, onEvent)
                    RibbonTab.REVIEW -> ReviewTabContent(state, onEvent)
                    RibbonTab.VIEW -> ViewTabContent(state, onEvent)
                    else -> HomeTabContent(state, onEvent)
                }
            }
        }
    }
}

@Composable
fun RibbonTabItem(tab: RibbonTab, title: String, isSelected: Boolean, onClick: () -> Unit) {
    val isFileTab = tab == RibbonTab.FILE
    val backgroundColor = when {
        isFileTab -> Color(0xFF107C41) // Signature Word File Tab Green Accent
        isSelected -> Color(0xFFF8FAFC)
        else -> Color.Transparent
    }
    val textColor = when {
        isFileTab -> Color.White
        isSelected -> Color(0xFF185ABD) // Signature MS Word Blue instead of clashing theme color
        else -> Color.White.copy(alpha = 0.9f)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = title, // Capitalized "Home", "Insert" instead of screamed uppercase "HOME"
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected || isFileTab) FontWeight.Bold else FontWeight.Medium
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
    var showCaseMenu by remember { mutableStateOf(false) }
    var showStylesMenu by remember { mutableStateOf(false) }
    var showTextColorStudio by remember { mutableStateOf(false) }
    var showHighlightColorStudio by remember { mutableStateOf(false) }

    val fontFamilies = listOf(
        "Aptos", "Aptos Display", "Aptos Serif",
        "Arial", "Arial Black", "Arial Narrow", 
        "Calibri", "Calibri Light", "Cambria", "Candara", "Century Gothic",
        "Comic Sans MS", "Consolas", "Constantia", "Corbel", "Courier New",
        "Franklin Gothic Medium", "Garamond", "Georgia", "Impact", 
        "Lucida Console", "Lucida Sans Unicode", "Palatino Linotype",
        "Segoe UI", "Segoe UI Light", "Segoe UI Semibold", 
        "Tahoma", "Times New Roman", "Trebuchet MS", "Verdana",
        "Aldhabi", "Amiri", "Andalus", "Arabic Typesetting", "Cairo", 
        "Dubai", "Kufi", "Naskh", "Sakkal Majalla", "Simplified Arabic", "Traditional Arabic"
    )
    val fontDescriptions = mapOf(
        "Aptos" to "لاتيني حديث", "Aptos Display" to "حديث عريض", "Aptos Serif" to "حديث مزخرف",
        "Arial" to "أساسي", "Arial Black" to "أساسي عريض", "Arial Narrow" to "أساسي نحيف",
        "Calibri" to "افتراضي (أوفيس)", "Calibri Light" to "افتراضي رفيع",
        "Cambria" to "رسمي", "Candara" to "أنيق", "Century Gothic" to "هندسي",
        "Comic Sans MS" to "عفوي", "Consolas" to "برمجي", "Constantia" to "رسمي", "Corbel" to "حديث",
        "Courier New" to "آلة كاتبة", "Franklin Gothic Medium" to "عريض", "Garamond" to "كلاسيكي قديم",
        "Georgia" to "كلاسيكي", "Impact" to "ملصقات", "Lucida Console" to "برمجي",
        "Lucida Sans Unicode" to "رموز دولية", "Palatino Linotype" to "كتابي",
        "Segoe UI" to "واجهات", "Segoe UI Light" to "واجهات رفيع", "Segoe UI Semibold" to "واجهات عريض",
        "Tahoma" to "شائع", "Times New Roman" to "رسمي كلاسيكي",
        "Trebuchet MS" to "حديث", "Verdana" to "واضح للشاشات",
        "Aldhabi" to "عربي ديواني", "Amiri" to "عربي نسخ", "Andalus" to "عربي أندلسي",
        "Arabic Typesetting" to "عربي طباعي", "Cairo" to "عربي حديث",
        "Dubai" to "عربي معاصر", "Kufi" to "عربي كوفي", "Naskh" to "عربي نسخ قياسي",
        "Sakkal Majalla" to "عربي مجلات", "Simplified Arabic" to "عربي مبسط", "Traditional Arabic" to "عربي تقليدي"
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Undo / Redo Group
        RibbonGroup(localize("Undo", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Undo")) }) {
            Column {
                RibbonSmallButton(Icons.AutoMirrored.Filled.Undo, localize("Undo", state.isRtl), enabled = state.canUndo) { onEvent(RibbonEvent.OnUndoClicked) }
                RibbonSmallButton(Icons.AutoMirrored.Filled.Redo, localize("Redo", state.isRtl), enabled = state.canRedo) { onEvent(RibbonEvent.OnRedoClicked) }
            }
        }

        VerticalDivider()

        // 2. Clipboard Group
        RibbonGroup(localize("Clipboard", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Clipboard")) }) {
            RibbonLargeButton(Icons.Default.ContentPaste, localize("Paste", state.isRtl)) { onEvent(RibbonEvent.OnPasteClicked) }
            Column {
                RibbonSmallButton(Icons.Default.ContentCut, localize("Cut", state.isRtl)) { onEvent(RibbonEvent.OnCutClicked) }
                RibbonSmallButton(Icons.Default.ContentCopy, localize("Copy", state.isRtl)) { onEvent(RibbonEvent.OnCopyClicked) }
            }
        }

        VerticalDivider()

        // 3. Font Group (Exhaustive MS Word Font Settings)
        RibbonGroup(localize("Font", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Font")) }) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Row 1: Font Family, Size, Increase/Decrease, Case, Clear Formatting
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        DropdownSelector(text = state.fontFamily, width = 110.dp) { showFontMenu = true }
                        if (showFontMenu) {
                            WordFontDropdownMenu(
                                currentFont = state.fontFamily,
                                isRtl = state.isRtl,
                                onFontSelected = { font ->
                                    onEvent(RibbonEvent.OnFontFamilyChanged(font))
                                },
                                onDismiss = { showFontMenu = false }
                            )
                        }
                    }

                    Box {
                        DropdownSelector(text = state.fontSize.toString(), width = 52.dp) { showSizeMenu = true }
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

                    RibbonButton(Icons.Default.FormatSize) { onEvent(RibbonEvent.OnIncreaseFontSizeClicked) }

                    // Change Case Dropdown (Aa)
                    Box {
                        RibbonButton(Icons.Default.TextFields) { showCaseMenu = true }
                        DropdownMenu(expanded = showCaseMenu, onDismissRequest = { showCaseMenu = false }) {
                            DropdownMenuItem(text = { Text(if (state.isRtl) "حالة الجملة (Sentence case)" else "Sentence case") }, onClick = { onEvent(RibbonEvent.OnChangeCaseClicked("Sentence case")); showCaseMenu = false })
                            DropdownMenuItem(text = { Text(if (state.isRtl) "حروف صغيرة (lowercase)" else "lowercase") }, onClick = { onEvent(RibbonEvent.OnChangeCaseClicked("lowercase")); showCaseMenu = false })
                            DropdownMenuItem(text = { Text(if (state.isRtl) "حروف كبيرة (UPPERCASE)" else "UPPERCASE") }, onClick = { onEvent(RibbonEvent.OnChangeCaseClicked("UPPERCASE")); showCaseMenu = false })
                            DropdownMenuItem(text = { Text(if (state.isRtl) "كبّر كل كلمة (Capitalize Each Word)" else "Capitalize Each Word") }, onClick = { onEvent(RibbonEvent.OnChangeCaseClicked("Capitalize Each Word")); showCaseMenu = false })
                        }
                    }

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
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = Color(0xFF185ABD), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (state.isRtl) "ألوان إضافية وتخصيص كامل..." else "More Colors...", fontWeight = FontWeight.Bold, color = Color(0xFF185ABD))
                                    }
                                },
                                onClick = {
                                    showTextColorStudio = true
                                    showTextColorMenu = false
                                }
                            )
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
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = Color(0xFF185ABD), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (state.isRtl) "ألوان إضافية وتخصيص كامل..." else "More Colors...", fontWeight = FontWeight.Bold, color = Color(0xFF185ABD))
                                    }
                                },
                                onClick = {
                                    showHighlightColorStudio = true
                                    showHighlightMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showTextColorStudio) {
            ColorStudioDialog(
                initialColor = state.textColor,
                title = if (state.isRtl) "استوديو لون الخط (RGB / HEX / وورد)" else "Text Color Studio",
                isRtl = state.isRtl,
                allowTransparent = false,
                onColorSelected = { color -> onEvent(RibbonEvent.OnTextColorChanged(color)) },
                onDismiss = { showTextColorStudio = false }
            )
        }

        if (showHighlightColorStudio) {
            ColorStudioDialog(
                initialColor = state.highlightColor,
                title = if (state.isRtl) "استوديو لون تمييز النص (RGB / HEX)" else "Highlight Color Studio",
                isRtl = state.isRtl,
                allowTransparent = true,
                onColorSelected = { color -> onEvent(RibbonEvent.OnHighlightColorChanged(color)) },
                onDismiss = { showHighlightColorStudio = false }
            )
        }

        VerticalDivider()

        // 4. Paragraph Group
        RibbonGroup(localize("Paragraph", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Paragraph")) }) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    RibbonToggleButton(Icons.Default.FormatListBulleted, state.isBulletedList) { onEvent(RibbonEvent.OnBulletedListToggled) }
                    RibbonToggleButton(Icons.Default.FormatListNumbered, state.isNumberedList) { onEvent(RibbonEvent.OnNumberedListToggled) }
                    Spacer(modifier = Modifier.width(4.dp))
                    RibbonButton(Icons.AutoMirrored.Filled.FormatIndentDecrease) { onEvent(RibbonEvent.OnDecreaseIndentClicked) }
                    RibbonButton(Icons.AutoMirrored.Filled.FormatIndentIncrease) { onEvent(RibbonEvent.OnIncreaseIndentClicked) }
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    RibbonToggleButton(
                        icon = Icons.Default.Numbers,
                        isChecked = state.numeralSystem == NumeralSystem.ARABIC
                    ) {
                        val newSystem = if (state.numeralSystem == NumeralSystem.WESTERN) NumeralSystem.ARABIC else NumeralSystem.WESTERN
                        onEvent(RibbonEvent.OnNumeralSystemChanged(newSystem))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    RibbonToggleButton(Icons.AutoMirrored.Filled.FormatAlignLeft, state.alignment == TextAlignment.LEFT) { onEvent(RibbonEvent.OnAlignmentChanged(TextAlignment.LEFT)) }
                    RibbonToggleButton(Icons.Default.FormatAlignCenter, state.alignment == TextAlignment.CENTER) { onEvent(RibbonEvent.OnAlignmentChanged(TextAlignment.CENTER)) }
                    RibbonToggleButton(Icons.AutoMirrored.Filled.FormatAlignRight, state.alignment == TextAlignment.RIGHT) { onEvent(RibbonEvent.OnAlignmentChanged(TextAlignment.RIGHT)) }
                    RibbonToggleButton(Icons.Default.FormatAlignJustify, state.alignment == TextAlignment.JUSTIFY) { onEvent(RibbonEvent.OnAlignmentChanged(TextAlignment.JUSTIFY)) }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box {
                        DropdownSelector(text = "${state.lineSpacing}", width = 56.dp) { showLineSpacingMenu = true }
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

        VerticalDivider()

        // 5. Styles Group
        RibbonGroup(localize("Styles", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Styles")) }) {
            Box {
                RibbonLargeButton(Icons.Default.Title, localize("Styles", state.isRtl)) { showStylesMenu = true }
                DropdownMenu(expanded = showStylesMenu, onDismissRequest = { showStylesMenu = false }) {
                    DropdownMenuItem(text = { Text("عادي (Normal)") }, onClick = { onEvent(RibbonEvent.OnApplyHeadingStyle("Normal")); showStylesMenu = false })
                    DropdownMenuItem(text = { Text("العنوان الرئيسي (Title)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E3A8A)) }, onClick = { onEvent(RibbonEvent.OnApplyHeadingStyle("Title")); showStylesMenu = false })
                    DropdownMenuItem(text = { Text("العنوان الفرعي (Subtitle)", fontSize = 14.sp, color = Color(0xFF4B5563)) }, onClick = { onEvent(RibbonEvent.OnApplyHeadingStyle("Subtitle")); showStylesMenu = false })
                    DropdownMenuItem(text = { Text("عنوان 1 (Heading 1)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2563EB)) }, onClick = { onEvent(RibbonEvent.OnApplyHeadingStyle("Heading 1")); showStylesMenu = false })
                    DropdownMenuItem(text = { Text("عنوان 2 (Heading 2)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1D4ED8)) }, onClick = { onEvent(RibbonEvent.OnApplyHeadingStyle("Heading 2")); showStylesMenu = false })
                    DropdownMenuItem(text = { Text("عنوان 3 (Heading 3)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF374151)) }, onClick = { onEvent(RibbonEvent.OnApplyHeadingStyle("Heading 3")); showStylesMenu = false })
                }
            }
        }

        VerticalDivider()

        // 6. Editing Group
        RibbonGroup(localize("Editing", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Editing")) }) {
            RibbonLargeButton(Icons.Default.FindReplace, localize("Find & Replace", state.isRtl)) {
                onEvent(RibbonEvent.OnShowFindReplaceDialog)
            }
        }
    }
}

@Composable
fun InsertTabContent(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    var showShapeMenu by remember { mutableStateOf(false) }
    var showSymbolMenu by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        RibbonGroup(localize("Pages", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Pages")) }) {
            RibbonLargeButton(Icons.Default.InsertPageBreak, localize("Page Break", state.isRtl)) { onEvent(RibbonEvent.OnInsertPageBreakClicked) }
        }
        VerticalDivider()
        RibbonGroup(localize("Tables", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Tables")) }) {
            RibbonLargeButton(Icons.Default.TableChart, localize("Table", state.isRtl)) { onEvent(RibbonEvent.OnInsertTableClicked(3, 3)) }
        }
        VerticalDivider()
        RibbonGroup(localize("Illustrations", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Illustrations")) }) {
            RibbonLargeButton(Icons.Default.Image, localize("Picture", state.isRtl)) { onEvent(RibbonEvent.OnPickImageRequested) }
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
        RibbonGroup(localize("Decorations & Banners", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Decorations & Banners")) }) {
            RibbonLargeButton(Icons.Default.WebAsset, localize("Banner", state.isRtl)) {
                onEvent(RibbonEvent.OnInsertBannerClicked("عنوان الترويسة الرئيسي", "مستندك المحترف"))
            }
            RibbonLargeButton(Icons.Default.MarkUnreadChatAlt, localize("Callout", state.isRtl)) {
                onEvent(RibbonEvent.OnInsertCalloutClicked("ملاحظة هامة", "أدخل الملاحظة البارزة هنا..."))
            }
            RibbonLargeButton(Icons.Default.HorizontalRule, localize("Divider", state.isRtl)) {
                onEvent(RibbonEvent.OnInsertDividerClicked)
            }
        }
        VerticalDivider()
        RibbonGroup(localize("Header & Footer", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Header & Footer")) }) {
            Column {
                RibbonSmallButton(Icons.Default.BorderTop, localize("Header", state.isRtl)) { onEvent(RibbonEvent.OnToggleHeaderFooterMode) }
                RibbonSmallButton(Icons.Default.BorderBottom, localize("Footer", state.isRtl)) { onEvent(RibbonEvent.OnToggleHeaderFooterMode) }
                RibbonSmallButton(Icons.Default.Numbers, localize("Page Number", state.isRtl)) { onEvent(RibbonEvent.OnInsertPageNumberClicked) }
            }
        }
        VerticalDivider()
        RibbonGroup(localize("Symbols & Math", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Symbols & Math")) }) {
            Box {
                RibbonLargeButton(Icons.Default.Functions, localize("Symbols", state.isRtl)) { showSymbolMenu = true }
                DropdownMenu(expanded = showSymbolMenu, onDismissRequest = { showSymbolMenu = false }) {
                    listOf("∑", "π", "√", "∞", "±", "≤", "≥", "≠", "α", "β", "Ω", "∆", "€", "$", "£", "¥").forEach { sym ->
                        DropdownMenuItem(
                            text = { Text(sym, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                            onClick = {
                                onEvent(RibbonEvent.OnInsertSymbolClicked(sym))
                                showSymbolMenu = false
                            }
                        )
                    }
                }
            }
            RibbonLargeButton(Icons.Default.Draw, localize("Signature", state.isRtl)) {
                onEvent(RibbonEvent.OnInsertSignatureLineClicked)
            }
        }
    }
}

@Composable
fun DesignTabContent(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    var showWatermarkMenu by remember { mutableStateOf(false) }
    var showPageColorMenu by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }
    var showBordersDialog by remember { mutableStateOf(false) }
    var showPageColorStudio by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        RibbonGroup(localize("Document Formatting", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Document Formatting")) }) {
            Box {
                RibbonLargeButton(Icons.Default.Palette, localize("Themes", state.isRtl)) { showThemeMenu = true }
                DropdownMenu(expanded = showThemeMenu, onDismissRequest = { showThemeMenu = false }) {
                    DropdownMenuItem(text = { Text(localize("Office (Standard)", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnApplyDocumentTheme("Office")); showThemeMenu = false })
                    DropdownMenuItem(text = { Text(localize("Modern Clean", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnApplyDocumentTheme("Modern")); showThemeMenu = false })
                    DropdownMenuItem(text = { Text(localize("Formal Paper", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnApplyDocumentTheme("Formal")); showThemeMenu = false })
                    DropdownMenuItem(text = { Text(localize("Classic Style", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnApplyDocumentTheme("Classic")); showThemeMenu = false })
                }
            }
            RibbonLargeButton(Icons.Default.Style, localize("Style Set", state.isRtl)) {
                onEvent(RibbonEvent.OnFontFamilyChanged("Calibri"))
            }
        }
        VerticalDivider()
        RibbonGroup(localize("Page Background", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Page Background")) }) {
            // Watermark
            Box {
                RibbonLargeButton(Icons.Default.WaterDrop, localize("Watermark", state.isRtl)) { showWatermarkMenu = true }
                DropdownMenu(expanded = showWatermarkMenu, onDismissRequest = { showWatermarkMenu = false }) {
                    DropdownMenuItem(text = { Text(localize("Draft", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnWatermarkChanged("DRAFT")); showWatermarkMenu = false })
                    DropdownMenuItem(text = { Text(localize("Confidential", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnWatermarkChanged("CONFIDENTIAL")); showWatermarkMenu = false })
                    DropdownMenuItem(text = { Text(localize("Urgent", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnWatermarkChanged("URGENT")); showWatermarkMenu = false })
                    Divider()
                    DropdownMenuItem(text = { Text(localize("Remove Watermark", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnWatermarkChanged("")); showWatermarkMenu = false })
                }
            }

            // Page Color
            Box {
                RibbonLargeButton(Icons.Default.FormatColorFill, localize("Page Color", state.isRtl)) { showPageColorMenu = true }
                DropdownMenu(expanded = showPageColorMenu, onDismissRequest = { showPageColorMenu = false }) {
                    DropdownMenuItem(text = { Text(localize("White", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnPageColorChanged(Color.White)); showPageColorMenu = false })
                    DropdownMenuItem(text = { Text(localize("Cream Warm", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnPageColorChanged(Color(0xFFFFFDD0))); showPageColorMenu = false })
                    DropdownMenuItem(text = { Text(localize("Light Slate", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnPageColorChanged(Color(0xFFF3F4F6))); showPageColorMenu = false })
                    DropdownMenuItem(text = { Text(localize("Soft Blue", state.isRtl)) }, onClick = { onEvent(RibbonEvent.OnPageColorChanged(Color(0xFFEFF6FF))); showPageColorMenu = false })
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ColorLens, contentDescription = null, tint = Color(0xFF185ABD), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isRtl) "ألوان إضافية وتخصيص كامل..." else "More Colors...", fontWeight = FontWeight.Bold, color = Color(0xFF185ABD))
                            }
                        },
                        onClick = {
                            showPageColorStudio = true
                            showPageColorMenu = false
                        }
                    )
                }
            }

            // Page Borders
            RibbonLargeButton(Icons.Default.BorderStyle, localize("Page Borders", state.isRtl)) { showBordersDialog = true }
        }
    }

    if (showPageColorStudio) {
        ColorStudioDialog(
            initialColor = state.pageColor,
            title = if (state.isRtl) "استوديو لون خلفية الصفحة (RGB / HEX / أوفيس)" else "Page Color Studio",
            isRtl = state.isRtl,
            allowTransparent = false,
            onColorSelected = { color -> onEvent(RibbonEvent.OnPageColorChanged(color)) },
            onDismiss = { showPageColorStudio = false }
        )
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
fun ReviewTabContent(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        RibbonGroup(localize("Proofing & Stats", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Proofing & Stats")) }) {
            RibbonLargeButton(Icons.Default.Analytics, localize("Word Count", state.isRtl)) {
                onEvent(RibbonEvent.OnShowWordCountClicked)
            }
            RibbonLargeButton(Icons.Default.Spellcheck, localize("Proofing", state.isRtl)) {
                onEvent(RibbonEvent.OnLanguageToggled)
            }
        }
        VerticalDivider()
        RibbonGroup(localize("Smart AI Tools", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Smart AI Tools")) }) {
            RibbonLargeButton(Icons.Default.AutoAwesome, localize("AI Enhancer", state.isRtl)) {
                onEvent(RibbonEvent.OnShowWordCountClicked)
            }
            RibbonLargeButton(Icons.Default.FormatClear, localize("Clear Format", state.isRtl)) {
                onEvent(RibbonEvent.OnClearFormattingClicked)
            }
        }
        VerticalDivider()
        RibbonGroup(localize("PDF Export", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("PDF Export")) }) {
            RibbonLargeButton(Icons.Default.PictureAsPdf, localize("Export PDF", state.isRtl)) {
                onEvent(RibbonEvent.OnExportPdfClicked)
            }
        }
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
    var showBorderColorStudio by remember { mutableStateOf(false) }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(localize("Color", state.isRtl), fontWeight = FontWeight.Medium)
                    OutlinedButton(
                        onClick = { showBorderColorStudio = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (state.isRtl) "مخصص..." else "Custom...", fontSize = 11.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ColorOption(Color.Black, color == Color.Black) { color = Color.Black }
                    ColorOption(Color.Red, color == Color.Red) { color = Color.Red }
                    ColorOption(Color.Blue, color == Color.Blue) { color = Color.Blue }
                    ColorOption(Color(0xFF16A34A), color == Color(0xFF16A34A)) { color = Color(0xFF16A34A) } // Green
                    if (color !in listOf(Color.Black, Color.Red, Color.Blue, Color(0xFF16A34A))) {
                        ColorOption(color, true) {}
                    }
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

    if (showBorderColorStudio) {
        ColorStudioDialog(
            initialColor = color,
            title = if (state.isRtl) "استوديو لون إطار الصفحة" else "Border Color Studio",
            isRtl = state.isRtl,
            allowTransparent = false,
            onColorSelected = { selected -> color = selected },
            onDismiss = { showBorderColorStudio = false }
        )
    }
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        RibbonGroup(localize("Document Views", state.isRtl)) {
            if (state.isProtectedView) {
                RibbonLargeButton(
                    icon = Icons.Default.Edit,
                    label = localize("Edit Document", state.isRtl),
                    onClick = { onEvent(RibbonEvent.OnEnableEditing) }
                )
            }
            RibbonLargeButton(
                icon = Icons.Default.ViewAgenda,
                label = localize("Print Layout", state.isRtl),
                onClick = { onEvent(RibbonEvent.OnViewModeChanged(ViewMode.PRINT_LAYOUT)) }
            )
            RibbonLargeButton(
                icon = Icons.Default.Language,
                label = localize("Web Layout", state.isRtl),
                onClick = { onEvent(RibbonEvent.OnViewModeChanged(ViewMode.WEB_LAYOUT)) }
            )
            RibbonLargeButton(
                icon = Icons.Default.MenuBook,
                label = localize("Read Mode", state.isRtl),
                onClick = { onEvent(RibbonEvent.OnViewModeChanged(ViewMode.READ_MODE)) }
            )
        }
        VerticalDivider()
        RibbonGroup(localize("Zoom", state.isRtl)) {
            RibbonLargeButton(Icons.Default.ZoomIn, localize("Zoom In", state.isRtl)) { onEvent(RibbonEvent.OnZoomChanged(state.zoomScale + 0.2f)) }
            RibbonLargeButton(Icons.Default.ZoomOut, localize("Zoom Out", state.isRtl)) { onEvent(RibbonEvent.OnZoomChanged(state.zoomScale - 0.2f)) }
            RibbonLargeButton(Icons.Default.RestartAlt, localize("100%", state.isRtl)) { onEvent(RibbonEvent.OnZoomChanged(1.0f)) }
        }
        VerticalDivider()
        RibbonGroup(localize("Header & Footer", state.isRtl)) {
            RibbonLargeButton(Icons.Default.EditNote, localize("Header/Footer", state.isRtl)) { onEvent(RibbonEvent.OnToggleHeaderFooterMode) }
        }
    }
}

// --- Reusable Ribbon Widgets ---

@Composable
fun RibbonGroup(
    title: String,
    onLaunchDetails: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = title, fontSize = 11.sp, color = Color(0xFF64748B))
            if (onLaunchDetails != null) {
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFE2E8F0))
                        .clickable { onLaunchDetails() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NorthEast,
                        contentDescription = "Group Launcher Arrow",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickAccessButton(
    icon: ImageVector,
    tooltip: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(24.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = if (enabled) Color.White else Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(15.dp)
        )
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
fun RibbonLargeButton(icon: ImageVector, label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(26.dp), tint = if (enabled) Color(0xFF1E293B) else Color(0xFF94A3B8))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 11.sp, color = if (enabled) Color(0xFF1E293B) else Color(0xFF94A3B8))
    }
}

@Composable
fun RibbonButton(icon: ImageVector, enabled: Boolean = true, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(28.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = if (enabled) Color(0xFF1E293B) else Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
    }
}

@Composable
fun RibbonColorButton(icon: ImageVector, color: Color, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, tint = if (enabled) Color(0xFF1E293B) else Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
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
fun RibbonToggleButton(icon: ImageVector, isChecked: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    val bg = if (isChecked) Color(0xFFBFDBFE) else Color.Transparent
    val tint = if (!enabled) Color(0xFF94A3B8) else if (isChecked) Color(0xFF1D4ED8) else Color(0xFF1E293B)

    Box(
        modifier = Modifier
            .size(28.dp)
            .background(bg, RoundedCornerShape(3.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun RibbonSmallButton(icon: ImageVector, label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(16.dp), tint = if (enabled) Color(0xFF1E293B) else Color(0xFF94A3B8))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 11.sp, color = if (enabled) Color(0xFF1E293B) else Color(0xFF94A3B8))
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
