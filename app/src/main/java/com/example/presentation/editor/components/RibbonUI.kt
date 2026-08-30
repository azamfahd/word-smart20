package com.example.presentation.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
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
        "Draw" -> "رسم"
        "Design" -> "تصميم"
        "Layout" -> "تخطيط"
        "References" -> "مراجع"
        "Mailings" -> "مراسلات"
        "Review" -> "مراجعة"
        "View" -> "عرض"
        "Help" -> "تعليمات"
        "Picture Format" -> "تنسيق الصورة"
        "Table Design" -> "تصميم الجدول"
        "Shape Format" -> "تنسيق الشكل"
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
        "Undo" -> "تراجع"
        "Styles" -> "الأنماط"
        "Editing" -> "تحرير"
        "Decorations & Banners" -> "زخارف ولافتات"
        "Symbols & Math" -> "رموز ورياضيات"
        "Document Formatting" -> "تنسيق المستند"
        "Proofing & Stats" -> "تدقيق وإحصائيات"
        "Smart AI Tools" -> "أدوات الذكاء الاصطناعي"
        "PDF Export" -> "تصدير PDF"
        "Tools" -> "أدوات"
        "Format" -> "تنسيق"
        "Canvas" -> "لوحة الرسم"
        "Colors" -> "الألوان"
        "Office" -> "أوفيس"
        "Grayscale" -> "تدرج الرمادي"
        "Warm" -> "دافئ"
        "Cool" -> "بارد"
        "Fonts" -> "خطوط"
        "Paragraph Spacing" -> "تباعد الفقرات"
        "No Paragraph Space" -> "بلا تباعد فقرات"
        "Compact" -> "مضغوط"
        "Tight" -> "مشدود"
        "Open" -> "مفتوح"
        "Relaxed" -> "مسترخٍ"
        "Effects" -> "تأثيرات"
        "Set as Default" -> "تعيين كافتراضي"
        "Style Set" -> "مجموعة الأنماط"
        "Urgent" -> "عاجل"
        "Cream Warm" -> "كريمي دافئ"
        "Light Slate" -> "أردوازي فاتح"
        "Soft Blue" -> "أزرق ناعم"
        "Word Count" -> "عدد الكلمات"
        "Proofing" -> "تدقيق"
        "AI Enhancer" -> "محسّن بالذكاء الاصطناعي"
        "Clear Format" -> "مسح التنسيق"
        "Export PDF" -> "تصدير كـ PDF"
        "Edit Document" -> "تحرير المستند"
        "Highlighter" -> "قلم تمييز"
        "Eraser" -> "ممحاة"
        "Thickness" -> "السماكة"
        "Clear All" -> "مسح الكل"
        // References Tab
        "Table of Contents" -> "جدول المحتويات"
        "Footnotes" -> "حواشي سفلية"
        "Insert Footnote" -> "إدراج حاشية سفلية"
        "Citations & Bibliography" -> "المراجع والاقتباسات"
        "Insert Citation" -> "إدراج اقتباس"
        "Manage Sources" -> "إدارة المصادر"
        "Captions" -> "تسميات توضيحية"
        "Insert Caption" -> "إدراج تسمية توضيحية"
        "Index" -> "فهرس"
        "Mark Entry" -> "وضع علامة على الإدخال"
        // Mailings Tab
        "Create" -> "إنشاء"
        "Envelopes" -> "مغلفات"
        "Labels" -> "تسميات"
        "Start Mail Merge" -> "بدء دمج المراسلات"
        "Write & Insert Fields" -> "كتابة الحقول وإدراجها"
        "Insert Merge Field" -> "إدراج حقل دمج"
        "Preview Results" -> "معاينة النتائج"
        "Finish" -> "إنهاء"
        "Finish & Merge" -> "إنهاء ودمج"
        // Help Tab
        "Support" -> "الدعم"
        "Contact Support" -> "الاتصال بالدعم"
        "Feedback" -> "ملاحظات"
        "Training" -> "تدريب"
        "What's New" -> "ما الجديد"
        // Picture Format Tab
        "Adjust" -> "ضبط"
        "Corrections" -> "تصحيحات"
        "Color" -> "اللون"
        "Artistic Effects" -> "تأثيرات فنية"
        "Picture Styles" -> "أنماط الصور"
        "Picture Border" -> "حدود الصورة"
        "Picture Effects" -> "تأثيرات الصورة"
        "Arrange" -> "ترتيب"
        "Wrap Text" -> "التفاف النص"
        "Bring Forward" -> "إحضار للأمام"
        "Send Backward" -> "إرسال للخلف"
        "Crop" -> "اختصاص"
        // Table Design Tab
        "Table Style Options" -> "خيارات أنماط الجدول"
        "Header Row" -> "صف الرؤوس"
        "Total Row" -> "صف الإجمالي"
        "Banded Rows" -> "صفوف ذات تنسيق خاص"
        "Table Styles" -> "أنماط الجداول"
        "Shading" -> "تظليل"
        "Borders" -> "حدود"
        "Border Painter" -> "ناسخ الحدود"
        // Shape Format Tab
        "Insert Shapes" -> "إدراج أشكال"
        "Shape Styles" -> "أنماط الأشكال"
        "Shape Fill" -> "تعبئة الشكل"
        "Shape Outline" -> "المخطط التفصيلي للشكل"
        "Shape Effects" -> "تأثيرات الأشكال"
        "WordArt Styles" -> "أنماط WordArt"
        "Text Fill" -> "تعبئة النص"
        "Text Outline" -> "مخطط النص التفصيلي"
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
    var showOverflowMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF185ABD)) // Matches top bar seamless brand color
    ) {
        // Status Bar Top Inset (Crucial: prevents top bar from overlapping battery, clock, notifications, camera cutout)
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
        )

        // Quick Access Toolbar (شريط الوصول السريع العلوي التفاعلي المتكيف)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            val isCompact = maxWidth < 600.dp
            val isUltraNarrow = maxWidth < 360.dp

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Side: Back Arrow + Word Logo + Document Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Back Button (إغلاق / العودة للشاشة الرئيسية)
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "العودة",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
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

                    if (!isUltraNarrow) {
                        Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color(0xFF60A5FA).copy(alpha = 0.4f)))
                    }

                    // Document Title (Smartly truncated so it never overlaps or breaks the layout)
                    Text(
                        text = if (isUltraNarrow) state.documentTitle else "${state.documentTitle}.docx",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Right Side: Quick Actions
                if (isCompact) {
                    // Mobile Adaptive Compact Actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        QuickAccessButton(icon = Icons.Default.Save, tooltip = "حفظ") {
                            onSaveFileClick()
                        }
                        QuickAccessButton(icon = Icons.AutoMirrored.Filled.Undo, tooltip = "تراجع", enabled = state.canUndo) {
                            onEvent(RibbonEvent.OnUndoClicked)
                        }
                        QuickAccessButton(icon = Icons.AutoMirrored.Filled.Redo, tooltip = "إعادة", enabled = state.canRedo) {
                            onEvent(RibbonEvent.OnRedoClicked)
                        }
                        QuickAccessButton(icon = Icons.Default.Search, tooltip = "بحث واستبدال") {
                            onEvent(RibbonEvent.OnShowFindReplaceDialog)
                        }

                        // More Options Dropdown
                        Box {
                            QuickAccessButton(icon = Icons.Default.MoreVert, tooltip = "المزيد") {
                                showOverflowMenu = true
                            }

                            DropdownMenu(
                                expanded = showOverflowMenu,
                                onDismissRequest = { showOverflowMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (state.isRtl) "فتح ملف..." else "Open File...") },
                                    leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        onOpenFileClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (state.isRtl) "تصدير كـ PDF" else "Export as PDF") },
                                    leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        onEvent(RibbonEvent.OnExportPdfClicked)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (state.isRtl) "حفظ في السحابة" else "Save to Cloud") },
                                    leadingIcon = { Icon(Icons.Default.CloudUpload, contentDescription = null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        onEvent(RibbonEvent.OnSaveToCloudClicked)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (state.isRtl) "إحصائيات الكلمات" else "Word Count") },
                                    leadingIcon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        onEvent(RibbonEvent.OnShowWordCountClicked)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (state.isRtl) "تغيير اتجاه الواجهة (عربي/EN)" else "Toggle Interface Direction") },
                                    leadingIcon = { Icon(Icons.Default.Translate, contentDescription = null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        onEvent(RibbonEvent.OnLanguageToggled)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Desktop / Tablet Expanded Layout
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
                            color = Color(0xFF104EAD),
                            modifier = Modifier
                                .width(95.dp)
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
            }
        }

        // Ribbon Tab Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF185ABD)) // Signature MS Word Blue
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp)
                .padding(top = 4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            val activeBlock = state.blocks.find { it.id == state.activeBlockId }
            val contextualTabs = mutableListOf<RibbonTab>()
            if (activeBlock is ImageBlock) contextualTabs.add(RibbonTab.PICTURE_FORMAT)
            if (activeBlock is ShapeBlock) contextualTabs.add(RibbonTab.SHAPE_FORMAT)
            if (activeBlock is TableBlock) contextualTabs.add(RibbonTab.TABLE_DESIGN)
            
            val displayTabs = RibbonTab.standardTabs + contextualTabs

            displayTabs.forEach { tab ->
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
                    RibbonTab.DRAW -> DrawTabContent(state, onEvent)
                    RibbonTab.DESIGN -> DesignTabContent(state, onEvent)
                    RibbonTab.LAYOUT -> LayoutTabContent(state, onEvent)
                    RibbonTab.REFERENCES -> ReferencesTabContent(state, onEvent)
                    RibbonTab.MAILINGS -> MailingsTabContent(state, onEvent)
                    RibbonTab.REVIEW -> ReviewTabContent(state, onEvent)
                    RibbonTab.VIEW -> ViewTabContent(state, onEvent)
                    RibbonTab.HELP -> HelpTabContent(state, onEvent)
                    RibbonTab.PICTURE_FORMAT -> PictureFormatTabContent(state, onEvent)
                    RibbonTab.TABLE_DESIGN -> TableDesignTabContent(state, onEvent)
                    RibbonTab.SHAPE_FORMAT -> ShapeFormatTabContent(state, onEvent)
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
    var showUnderlineMenu by remember { mutableStateOf(false) }
    var showTextEffectsMenu by remember { mutableStateOf(false) }
    var showBulletsMenu by remember { mutableStateOf(false) }
    var showNumberingMenu by remember { mutableStateOf(false) }
    var showMultilevelMenu by remember { mutableStateOf(false) }
    var showShadingMenu by remember { mutableStateOf(false) }
    var showBordersMenu by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showPasteMenu by remember { mutableStateOf(false) }
    var showSelectMenu by remember { mutableStateOf(false) }
    var showCreateStyleDialog by remember { mutableStateOf(false) }
    var newStyleName by remember { mutableStateOf("") }
    var showTextColorStudio by remember { mutableStateOf(false) }
    var showHighlightColorStudio by remember { mutableStateOf(false) }
    var showShadingColorStudio by remember { mutableStateOf(false) }

    val fontSizes = listOf(8, 9, 10, 11, 12, 14, 16, 18, 20, 24, 28, 36, 48, 72)
    val lineSpacings = listOf(1.0f, 1.15f, 1.5f, 2.0f, 2.5f, 3.0f)
    
    // MS Word Theme Colors (10 main columns)
    val themeColors = listOf(
        Color.White to "أبيض",
        Color.Black to "أسود",
        Color(0xFFE7E6E6) to "رمادي فاتح",
        Color(0xFF44546A) to "أزرق رمادي",
        Color(0xFF4472C4) to "أزرق",
        Color(0xFFED7D31) to "برتقالي",
        Color(0xFFA5A5A5) to "رمادي",
        Color(0xFFFFC000) to "ذهبي",
        Color(0xFF5B9BD5) to "أزرق سماوي",
        Color(0xFF70AD47) to "أخضر"
    )

    val standardColors = listOf(
        Color(0xFFC00000) to "أحمر داكن",
        Color(0xFFFF0000) to "أحمر",
        Color(0xFFFFC000) to "برتقالي",
        Color(0xFFFFFF00) to "أصفر",
        Color(0xFF92D050) to "أخضر فاتح",
        Color(0xFF00B050) to "أخضر",
        Color(0xFF00B0F0) to "أزرق فاتح",
        Color(0xFF0070C0) to "أزرق",
        Color(0xFF002060) to "أزرق داكن",
        Color(0xFF7030A0) to "أرجواني"
    )

    // MS Word 15 Highlighters
    val highlightPalette = listOf(
        Color.Yellow, Color(0xFF00FF00), Color(0xFF00FFFF), Color(0xFFFF00FF),
        Color(0xFF0000FF), Color(0xFFFF0000), Color(0xFF000080), Color(0xFF008080),
        Color(0xFF008000), Color(0xFF800080), Color(0xFF800000), Color(0xFF808000),
        Color(0xFF808080), Color(0xFFC0C0C0), Color.Black
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

        // 2. Clipboard Group (Split Paste, Cut, Copy, Format Painter)
        RibbonGroup(localize("Clipboard", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Clipboard")) }) {
            // Split Paste Button
            Box {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .clickable { onEvent(RibbonEvent.OnPasteClicked) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF1E293B)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showPasteMenu = true }
                    ) {
                        Text(text = localize("Paste", state.isRtl), fontSize = 11.sp, color = Color(0xFF1E293B))
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(12.dp))
                    }
                }

                DropdownMenu(expanded = showPasteMenu, onDismissRequest = { showPasteMenu = false }) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF185ABD))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isRtl) "الاحتفاظ بتنسيق المصدر (Keep Source Formatting)" else "Keep Source Formatting")
                            }
                        },
                        onClick = {
                            onEvent(RibbonEvent.OnPasteSpecialClicked(PasteMode.KEEP_SOURCE))
                            showPasteMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MergeType, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF185ABD))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isRtl) "دمج التنسيق (Merge Formatting)" else "Merge Formatting")
                            }
                        },
                        onClick = {
                            onEvent(RibbonEvent.OnPasteSpecialClicked(PasteMode.MERGE_FORMATTING))
                            showPasteMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF185ABD))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isRtl) "الاحتفاظ بالنص فقط (Keep Text Only)" else "Keep Text Only")
                            }
                        },
                        onClick = {
                            onEvent(RibbonEvent.OnPasteSpecialClicked(PasteMode.KEEP_TEXT_ONLY))
                            showPasteMenu = false
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlaylistAddCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isRtl) "فتح سجل الحافظة (Clipboard History)..." else "Clipboard History...")
                            }
                        },
                        onClick = {
                            onEvent(RibbonEvent.OnShowGroupDetails("Clipboard"))
                            showPasteMenu = false
                        }
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    RibbonSmallButton(Icons.Default.ContentCut, localize("Cut", state.isRtl)) { onEvent(RibbonEvent.OnCutClicked) }
                    RibbonSmallButton(Icons.Default.ContentCopy, localize("Copy", state.isRtl)) { onEvent(RibbonEvent.OnCopyClicked) }
                }

                // Format Painter with active/locked indicator
                val isPainterActive = state.isFormatPainterActive
                val isPainterLocked = state.isFormatPainterLocked
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = when {
                        isPainterLocked -> Color(0xFFFEF08A)
                        isPainterActive -> Color(0xFFBFDBFE)
                        else -> Color.Transparent
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .clickable { onEvent(RibbonEvent.OnFormatPainterToggled(false)) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Brush,
                            contentDescription = "Format Painter",
                            modifier = Modifier.size(15.dp),
                            tint = if (isPainterActive || isPainterLocked) Color(0xFF1D4ED8) else Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (state.isRtl) "نسخ التنسيق" else "Format Painter",
                            fontSize = 11.sp,
                            color = if (isPainterActive || isPainterLocked) Color(0xFF1D4ED8) else Color(0xFF1E293B),
                            fontWeight = if (isPainterActive || isPainterLocked) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        VerticalDivider()

        // 3. Font Group (Comprehensive MS Word 365 Typography Controls)
        RibbonGroup(localize("Font", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Font")) }) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Row 1: Font Family, Size, Grow, Shrink, Change Case, Clear All Formatting
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

                    // Grow Font
                    IconButton(
                        onClick = { onEvent(RibbonEvent.OnIncreaseFontSizeClicked) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("A", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Icon(Icons.Default.ArrowDropUp, contentDescription = "Grow Font", modifier = Modifier.size(14.dp), tint = Color(0xFF1E293B))
                        }
                    }

                    // Shrink Font
                    IconButton(
                        onClick = { onEvent(RibbonEvent.OnDecreaseFontSizeClicked) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("A", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Shrink Font", modifier = Modifier.size(14.dp), tint = Color(0xFF1E293B))
                        }
                    }

                    // Change Case Dropdown (Aa)
                    Box {
                        IconButton(onClick = { showCaseMenu = true }, modifier = Modifier.size(28.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Aa", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF1E293B))
                            }
                        }
                        DropdownMenu(expanded = showCaseMenu, onDismissRequest = { showCaseMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(if (state.isRtl) "حالة الجملة (Sentence case.)" else "Sentence case.") },
                                onClick = { onEvent(RibbonEvent.OnChangeCaseClicked("Sentence case")); showCaseMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text(if (state.isRtl) "أحرف صغيرة (lowercase)" else "lowercase") },
                                onClick = { onEvent(RibbonEvent.OnChangeCaseClicked("lowercase")); showCaseMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text(if (state.isRtl) "أحرف كبيرة (UPPERCASE)" else "UPPERCASE") },
                                onClick = { onEvent(RibbonEvent.OnChangeCaseClicked("UPPERCASE")); showCaseMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text(if (state.isRtl) "حرف كبير في بداية كل كلمة (Capitalize Each Word)" else "Capitalize Each Word") },
                                onClick = { onEvent(RibbonEvent.OnChangeCaseClicked("Capitalize Each Word")); showCaseMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text(if (state.isRtl) "تبديل حالة الأحرف (tOGGLE cASE)" else "tOGGLE cASE") },
                                onClick = { onEvent(RibbonEvent.OnChangeCaseClicked("tOGGLE cASE")); showCaseMenu = false }
                            )
                        }
                    }

                    // Clear All Formatting
                    IconButton(
                        onClick = { onEvent(RibbonEvent.OnClearFormattingClicked) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.FormatClear, contentDescription = "Clear All Formatting", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                    }
                }

                // Row 2: Bold, Italic, Underline Dropdown, Strikethrough, Sub, Super, Text Effects, Highlight, Font Color
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    RibbonToggleButton(Icons.Default.FormatBold, state.isBold) { onEvent(RibbonEvent.OnBoldClicked) }
                    RibbonToggleButton(Icons.Default.FormatItalic, state.isItalic) { onEvent(RibbonEvent.OnItalicClicked) }
                    
                    // Underline with Style Dropdown Arrow
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .height(28.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (state.isUnderline) Color(0xFFBFDBFE) else Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clickable { onEvent(RibbonEvent.OnUnderlineClicked) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatUnderlined,
                                    contentDescription = "Underline",
                                    tint = if (state.isUnderline) Color(0xFF1D4ED8) else Color(0xFF1E293B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .padding(end = 2.dp)
                                    .clickable { showUnderlineMenu = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Underline Styles",
                                    tint = if (state.isUnderline) Color(0xFF1D4ED8) else Color(0xFF1E293B),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        DropdownMenu(expanded = showUnderlineMenu, onDismissRequest = { showUnderlineMenu = false }) {
                            listOf(
                                UnderlineStyle.SINGLE to (if (state.isRtl) "تسطير مفرد ───" else "Single ───"),
                                UnderlineStyle.DOUBLE to (if (state.isRtl) "تسطير مزدوج ═══" else "Double ═══"),
                                UnderlineStyle.DOTTED to (if (state.isRtl) "تسطير منقط ┈┈┈" else "Dotted ┈┈┈"),
                                UnderlineStyle.DASHED to (if (state.isRtl) "تسطير متقطع ╌╌╌" else "Dashed ╌╌╌"),
                                UnderlineStyle.WAVY to (if (state.isRtl) "تسطير متموج ~~~" else "Wavy ~~~")
                            ).forEach { (styleKey, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, fontWeight = FontWeight.SemiBold) },
                                    onClick = {
                                        onEvent(RibbonEvent.OnUnderlineStyleChanged(styleKey))
                                        showUnderlineMenu = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(if (state.isRtl) "مزيد من التسطيرات والألوان..." else "More Underlines & Colors...") },
                                onClick = {
                                    onEvent(RibbonEvent.OnShowGroupDetails("Font"))
                                    showUnderlineMenu = false
                                }
                            )
                        }
                    }

                    RibbonToggleButton(Icons.Default.FormatStrikethrough, state.isStrikethrough) { onEvent(RibbonEvent.OnStrikethroughClicked) }
                    RibbonToggleButton(Icons.Default.VerticalAlignBottom, state.isSubscript) { onEvent(RibbonEvent.OnSubscriptClicked) }
                    RibbonToggleButton(Icons.Default.VerticalAlignTop, state.isSuperscript) { onEvent(RibbonEvent.OnSuperscriptClicked) }

                    // Text Effects & Typography (A with glow/shadow)
                    Box {
                        IconButton(onClick = { showTextEffectsMenu = true }, modifier = Modifier.size(28.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("A", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                            }
                        }
                        DropdownMenu(expanded = showTextEffectsMenu, onDismissRequest = { showTextEffectsMenu = false }) {
                            TextEffectType.entries.forEach { effect ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = when (effect) {
                                                TextEffectType.NONE -> if (state.isRtl) "بلا تأثير" else "No Effect"
                                                TextEffectType.OUTLINE -> if (state.isRtl) "مخطط تفصيلي (Outline)" else "Outline"
                                                TextEffectType.SHADOW -> if (state.isRtl) "ظل (Shadow)" else "Shadow"
                                                TextEffectType.REFLECTION -> if (state.isRtl) "انعكاس (Reflection)" else "Reflection"
                                                TextEffectType.GLOW -> if (state.isRtl) "توهج (Glow)" else "Glow"
                                                TextEffectType.GRADIENT -> if (state.isRtl) "تدرج لوني (Gradient)" else "Gradient"
                                            },
                                            fontWeight = if (state.textEffect == effect) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onEvent(RibbonEvent.OnTextEffectChanged(effect))
                                        showTextEffectsMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Text Color Dropdown (Theme colors + Standard colors)
                    Box {
                        RibbonColorButton(Icons.Default.FormatColorText, state.textColor) { showTextColorMenu = true }
                        DropdownMenu(expanded = showTextColorMenu, onDismissRequest = { showTextColorMenu = false }) {
                            Text(
                                text = if (state.isRtl) "ألوان السمات (Theme Colors)" else "Theme Colors",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                themeColors.forEach { (c, _) ->
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .border(1.dp, Color.Gray, CircleShape)
                                            .clickable {
                                                onEvent(RibbonEvent.OnTextColorChanged(c))
                                                showTextColorMenu = false
                                            }
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                text = if (state.isRtl) "الألوان القياسية (Standard Colors)" else "Standard Colors",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                standardColors.forEach { (c, _) ->
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .border(1.dp, Color.Gray, CircleShape)
                                            .clickable {
                                                onEvent(RibbonEvent.OnTextColorChanged(c))
                                                showTextColorMenu = false
                                            }
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = Color(0xFF185ABD), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (state.isRtl) "ألوان إضافية (More Colors)..." else "More Colors...", fontWeight = FontWeight.Bold, color = Color(0xFF185ABD))
                                    }
                                },
                                onClick = {
                                    showTextColorStudio = true
                                    showTextColorMenu = false
                                }
                            )
                        }
                    }

                    // Highlight Color Dropdown (Word 15 Palette Grid + No Color)
                    Box {
                        RibbonColorButton(Icons.Default.FormatColorFill, state.highlightColor) { showHighlightMenu = true }
                        DropdownMenu(expanded = showHighlightMenu, onDismissRequest = { showHighlightMenu = false }) {
                            Text(
                                text = if (state.isRtl) "لون تمييز النص (Text Highlight Color)" else "Text Highlight Color",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                            
                            // 5x3 Grid of 15 MS Word Highlighters
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                highlightPalette.chunked(5).forEach { rowColors ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        rowColors.forEach { col ->
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(col)
                                                    .border(1.dp, Color(0xFFB0B0B0), RoundedCornerShape(2.dp))
                                                    .clickable {
                                                        onEvent(RibbonEvent.OnHighlightColorChanged(col))
                                                        showHighlightMenu = false
                                                    }
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .border(1.dp, Color.Gray, RoundedCornerShape(2.dp))
                                                .background(Color.White)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (state.isRtl) "بلا لون (No Color)" else "No Color")
                                    }
                                },
                                onClick = {
                                    onEvent(RibbonEvent.OnHighlightColorChanged(Color.Transparent))
                                    showHighlightMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = Color(0xFF185ABD), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (state.isRtl) "تخصيص كامل (Color Studio)..." else "Color Studio...", fontWeight = FontWeight.Bold, color = Color(0xFF185ABD))
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

        // 4. Paragraph Group (Bullets Library, Numbering Library, Multilevel, Indents, Sort, Non-printing, Alignment, Spacing, Shading, Borders)
        RibbonGroup(localize("Paragraph", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Paragraph")) }) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Row 1: Bullets, Numbering, Multilevel, Indents, Sort, Show/Hide ¶
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Bullets Dropdown
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .height(28.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (state.isBulletedList) Color(0xFFBFDBFE) else Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clickable { onEvent(RibbonEvent.OnBulletedListToggled) }
                            ) {
                                Icon(Icons.Default.FormatListBulleted, contentDescription = "Bullets", tint = if (state.isBulletedList) Color(0xFF1D4ED8) else Color(0xFF1E293B), modifier = Modifier.size(18.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .padding(end = 2.dp)
                                    .clickable { showBulletsMenu = true }
                            ) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = if (state.isBulletedList) Color(0xFF1D4ED8) else Color(0xFF1E293B), modifier = Modifier.size(12.dp))
                            }
                        }

                        DropdownMenu(expanded = showBulletsMenu, onDismissRequest = { showBulletsMenu = false }) {
                            Text(if (state.isRtl) "مكتبة التعداد النقطي" else "Bullet Library", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                            BulletShape.entries.forEach { shape ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = when (shape) {
                                                    BulletShape.DISC -> "●"
                                                    BulletShape.CIRCLE -> "○"
                                                    BulletShape.SQUARE -> "■"
                                                    BulletShape.HOLLOW_SQUARE -> "◻"
                                                    BulletShape.CHECKMARK -> "✔"
                                                    BulletShape.ARROW -> "➔"
                                                    BulletShape.STAR -> "✦"
                                                    BulletShape.FLORAL -> "✤"
                                                },
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF185ABD)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = when (shape) {
                                                    BulletShape.DISC -> if (state.isRtl) "نقطة مصمتة" else "Solid Disc"
                                                    BulletShape.CIRCLE -> if (state.isRtl) "دائرة مفرغة" else "Hollow Circle"
                                                    BulletShape.SQUARE -> if (state.isRtl) "مربع مصمت" else "Solid Square"
                                                    BulletShape.HOLLOW_SQUARE -> if (state.isRtl) "مربع مفرغ" else "Hollow Square"
                                                    BulletShape.CHECKMARK -> if (state.isRtl) "علامة صح" else "Checkmark"
                                                    BulletShape.ARROW -> if (state.isRtl) "سهم" else "Arrow"
                                                    BulletShape.STAR -> if (state.isRtl) "نجمة" else "Star"
                                                    BulletShape.FLORAL -> if (state.isRtl) "زخرفي" else "Floral"
                                                }
                                            )
                                        }
                                    },
                                    onClick = {
                                        onEvent(RibbonEvent.OnBulletShapeChanged(shape))
                                        showBulletsMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Numbering Dropdown
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .height(28.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (state.isNumberedList) Color(0xFFBFDBFE) else Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clickable { onEvent(RibbonEvent.OnNumberedListToggled) }
                            ) {
                                Icon(Icons.Default.FormatListNumbered, contentDescription = "Numbering", tint = if (state.isNumberedList) Color(0xFF1D4ED8) else Color(0xFF1E293B), modifier = Modifier.size(18.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .padding(end = 2.dp)
                                    .clickable { showNumberingMenu = true }
                            ) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = if (state.isNumberedList) Color(0xFF1D4ED8) else Color(0xFF1E293B), modifier = Modifier.size(12.dp))
                            }
                        }

                        DropdownMenu(expanded = showNumberingMenu, onDismissRequest = { showNumberingMenu = false }) {
                            Text(if (state.isRtl) "مكتبة الترقيم" else "Numbering Library", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                            NumberingStyle.entries.forEach { nStyle ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = when (nStyle) {
                                                    NumberingStyle.DECIMAL_DOT -> "1. 2. 3."
                                                    NumberingStyle.DECIMAL_PAREN -> "1) 2) 3)"
                                                    NumberingStyle.ARABIC_ALIF_BAA -> "أ. ب. ج."
                                                    NumberingStyle.ARABIC_INDIC -> "١. ٢. ٣."
                                                    NumberingStyle.ALPHA_UPPER -> "A. B. C."
                                                    NumberingStyle.ALPHA_LOWER -> "a. b. c."
                                                    NumberingStyle.ROMAN_UPPER -> "I. II. III."
                                                    NumberingStyle.ROMAN_LOWER -> "i. ii. iii."
                                                },
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF185ABD)
                                            )
                                        }
                                    },
                                    onClick = {
                                        onEvent(RibbonEvent.OnNumberingStyleChanged(nStyle))
                                        showNumberingMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Multilevel List Dropdown
                    Box {
                        IconButton(onClick = { showMultilevelMenu = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.AccountTree, contentDescription = "Multilevel List", modifier = Modifier.size(17.dp), tint = Color(0xFF1E293B))
                        }
                        DropdownMenu(expanded = showMultilevelMenu, onDismissRequest = { showMultilevelMenu = false }) {
                            Text(if (state.isRtl) "قائمة متعددة المستويات" else "Multilevel List Library", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                            MultilevelStyle.entries.forEach { mStyle ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = when (mStyle) {
                                                MultilevelStyle.NONE -> if (state.isRtl) "بلا" else "None"
                                                MultilevelStyle.NUMERIC_LEVELS -> "1 > 1.1 > 1.1.1"
                                                MultilevelStyle.ALPHA_NUMERIC -> "I > A > 1 > a"
                                                MultilevelStyle.HEADING_LEVELS -> "Chapter 1 > Heading 1"
                                            },
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    onClick = {
                                        onEvent(RibbonEvent.OnMultilevelStyleChanged(mStyle))
                                        showMultilevelMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    RibbonButton(Icons.AutoMirrored.Filled.FormatIndentDecrease) { onEvent(RibbonEvent.OnDecreaseIndentClicked) }
                    RibbonButton(Icons.AutoMirrored.Filled.FormatIndentIncrease) { onEvent(RibbonEvent.OnIncreaseIndentClicked) }

                    // Sort Paragraphs (A-Z)
                    IconButton(onClick = { showSortDialog = true }, modifier = Modifier.size(28.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("A↓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                    }

                    // Show / Hide Non-printing marks (¶)
                    RibbonToggleButton(
                        icon = Icons.Default.Visibility,
                        isChecked = state.showNonPrintingCharacters,
                        onClick = { onEvent(RibbonEvent.OnToggleNonPrintingCharacters) }
                    )
                }

                // Row 2: Alignment, Line & Paragraph Spacing, Shading, Borders, Direction, Numerals
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    RibbonToggleButton(Icons.AutoMirrored.Filled.FormatAlignLeft, state.alignment == TextAlignment.LEFT) { onEvent(RibbonEvent.OnAlignmentChanged(TextAlignment.LEFT)) }
                    RibbonToggleButton(Icons.Default.FormatAlignCenter, state.alignment == TextAlignment.CENTER) { onEvent(RibbonEvent.OnAlignmentChanged(TextAlignment.CENTER)) }
                    RibbonToggleButton(Icons.AutoMirrored.Filled.FormatAlignRight, state.alignment == TextAlignment.RIGHT) { onEvent(RibbonEvent.OnAlignmentChanged(TextAlignment.RIGHT)) }
                    RibbonToggleButton(Icons.Default.FormatAlignJustify, state.alignment == TextAlignment.JUSTIFY) { onEvent(RibbonEvent.OnAlignmentChanged(TextAlignment.JUSTIFY)) }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Line & Paragraph Spacing Dropdown
                    Box {
                        DropdownSelector(text = "${state.lineSpacing}", width = 56.dp) { showLineSpacingMenu = true }
                        DropdownMenu(expanded = showLineSpacingMenu, onDismissRequest = { showLineSpacingMenu = false }) {
                            lineSpacings.forEach { spacing ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (state.lineSpacing == spacing) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF185ABD))
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Text("$spacing")
                                        }
                                    },
                                    onClick = {
                                        onEvent(RibbonEvent.OnLineSpacingChanged(spacing))
                                        showLineSpacingMenu = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(if (state.isRtl) "خيارات تباعد الأسطر والفقرات..." else "Line Spacing Options...") },
                                onClick = {
                                    onEvent(RibbonEvent.OnShowGroupDetails("Paragraph"))
                                    showLineSpacingMenu = false
                                }
                            )
                        }
                    }

                    // Shading (تعبئة تظليل خلفية الفقرة)
                    Box {
                        IconButton(onClick = { showShadingMenu = true }, modifier = Modifier.size(28.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.FormatColorFill, contentDescription = "Shading", modifier = Modifier.size(16.dp), tint = Color(0xFF1E293B))
                                Box(modifier = Modifier.width(14.dp).height(2.dp).background(state.paragraphShadingColor ?: Color.Black))
                            }
                        }

                        DropdownMenu(expanded = showShadingMenu, onDismissRequest = { showShadingMenu = false }) {
                            Text(if (state.isRtl) "تظليل خلفية الفقرة (Shading)" else "Paragraph Shading", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                themeColors.forEach { (c, _) ->
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(c)
                                            .border(1.dp, Color.Gray, RoundedCornerShape(2.dp))
                                            .clickable {
                                                onEvent(RibbonEvent.OnParagraphShadingChanged(c))
                                                showShadingMenu = false
                                            }
                                    )
                                }
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(if (state.isRtl) "بلا لون (No Color)" else "No Color") },
                                onClick = {
                                    onEvent(RibbonEvent.OnParagraphShadingChanged(null))
                                    showShadingMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (state.isRtl) "ألوان إضافية..." else "More Colors...") },
                                onClick = {
                                    showShadingColorStudio = true
                                    showShadingMenu = false
                                }
                            )
                        }
                    }

                    // Paragraph Borders (حدود الفقرة)
                    Box {
                        IconButton(onClick = { showBordersMenu = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.BorderAll, contentDescription = "Borders", modifier = Modifier.size(17.dp), tint = Color(0xFF1E293B))
                        }

                        DropdownMenu(expanded = showBordersMenu, onDismissRequest = { showBordersMenu = false }) {
                            Text(if (state.isRtl) "حدود الفقرة (Paragraph Borders)" else "Paragraph Borders", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                            listOf(
                                ParagraphBorder.BOTTOM to (if (state.isRtl) "حد سفلي (Bottom Border)" else "Bottom Border"),
                                ParagraphBorder.TOP to (if (state.isRtl) "حد علوي (Top Border)" else "Top Border"),
                                ParagraphBorder.LEFT to (if (state.isRtl) "حد أيسر (Left Border)" else "Left Border"),
                                ParagraphBorder.RIGHT to (if (state.isRtl) "حد أيمن (Right Border)" else "Right Border"),
                                ParagraphBorder.NONE to (if (state.isRtl) "بلا حدود (No Border)" else "No Border"),
                                ParagraphBorder.ALL to (if (state.isRtl) "كافة الحدود (All Borders)" else "All Borders"),
                                ParagraphBorder.OUTSIDE to (if (state.isRtl) "حدود خارجية (Outside Borders)" else "Outside Borders")
                            ).forEach { (borderType, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        onEvent(RibbonEvent.OnParagraphBorderChanged(borderType))
                                        showBordersMenu = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(if (state.isRtl) "حدود وتظليل كامل..." else "Borders and Shading...") },
                                onClick = {
                                    onEvent(RibbonEvent.OnShowGroupDetails("Paragraph"))
                                    showBordersMenu = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))
                    RibbonToggleButton(Icons.AutoMirrored.Filled.FormatTextdirectionLToR, !state.isTextRtl) { onEvent(RibbonEvent.OnTextDirectionToggled) }
                    RibbonToggleButton(Icons.AutoMirrored.Filled.FormatTextdirectionRToL, state.isTextRtl) { onEvent(RibbonEvent.OnTextDirectionToggled) }
                    
                    // Numeral System Toggle
                    RibbonToggleButton(
                        icon = Icons.Default.Numbers,
                        isChecked = state.numeralSystem == NumeralSystem.ARABIC
                    ) {
                        val newSystem = if (state.numeralSystem == NumeralSystem.WESTERN) NumeralSystem.ARABIC else NumeralSystem.WESTERN
                        onEvent(RibbonEvent.OnNumeralSystemChanged(newSystem))
                    }
                }
            }
        }

        if (showShadingColorStudio) {
            ColorStudioDialog(
                initialColor = state.paragraphShadingColor ?: Color.White,
                title = if (state.isRtl) "استوديو تظليل الفقرة" else "Paragraph Shading Studio",
                isRtl = state.isRtl,
                allowTransparent = true,
                onColorSelected = { color -> onEvent(RibbonEvent.OnParagraphShadingChanged(color)) },
                onDismiss = { showShadingColorStudio = false }
            )
        }

        // Sort Dialog
        if (showSortDialog) {
            AlertDialog(
                onDismissRequest = { showSortDialog = false },
                title = { Text(if (state.isRtl) "فرز الفقرات (Sort Text)" else "Sort Text") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (state.isRtl) "اختر اتجاه الفرز للفقرات الحالية:" else "Choose sort direction for paragraphs:")
                        Button(
                            onClick = {
                                onEvent(RibbonEvent.OnSortParagraphsClicked(ascending = true))
                                showSortDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185ABD))
                        ) {
                            Text(if (state.isRtl) "فرز تصاعدي (أ -> ي / A -> Z)" else "Sort Ascending (A -> Z)")
                        }
                        OutlinedButton(
                            onClick = {
                                onEvent(RibbonEvent.OnSortParagraphsClicked(ascending = false))
                                showSortDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (state.isRtl) "فرز تنازلي (ي -> أ / Z -> A)" else "Sort Descending (Z -> A)")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSortDialog = false }) {
                        Text(if (state.isRtl) "إلغاء" else "Cancel")
                    }
                }
            )
        }

        VerticalDivider()

        // 5. Styles Group (Visual Interactive Styles Gallery in Ribbon)
        RibbonGroup(localize("Styles", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Styles")) }) {
            RibbonLargeButton(Icons.Default.Style, if (state.isRtl) "قوالب جاهزة" else "Quick Templates") {
                onEvent(RibbonEvent.OnShowTemplatesDialog)
            }
            Row(
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val builtInStyles = listOf(
                    "Normal" to (if (state.isRtl) "عادي" else "Normal"),
                    "No Spacing" to (if (state.isRtl) "بلا تباعد" else "No Spacing"),
                    "Heading 1" to (if (state.isRtl) "عنوان 1" else "Heading 1"),
                    "Heading 2" to (if (state.isRtl) "عنوان 2" else "Heading 2"),
                    "Heading 3" to (if (state.isRtl) "عنوان 3" else "Heading 3"),
                    "Title" to (if (state.isRtl) "العنوان الرئيسي" else "Title"),
                    "Subtitle" to (if (state.isRtl) "العنوان الفرعي" else "Subtitle"),
                    "Emphasis" to (if (state.isRtl) "توكيد" else "Emphasis"),
                    "Intense Emphasis" to (if (state.isRtl) "توكيد بارز" else "Intense Emphasis"),
                    "Quote" to (if (state.isRtl) "اقتباس" else "Quote"),
                    "Intense Quote" to (if (state.isRtl) "اقتباس بارز" else "Intense Quote"),
                    "Book Title" to (if (state.isRtl) "عنوان كتاب" else "Book Title"),
                    "Footnote" to (if (state.isRtl) "حاشية" else "Footnote")
                )

                builtInStyles.forEach { (styleKey, displayLabel) ->
                    val isSelected = state.selectedStyleName == styleKey
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = if (isSelected) Color(0xFFDCEAFE) else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .width(72.dp)
                            .height(52.dp)
                            .clickable { onEvent(RibbonEvent.OnApplyHeadingStyle(styleKey)) }
                    ) {
                        Column(
                            modifier = Modifier.padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "AaBbCc",
                                fontSize = when (styleKey) {
                                    "Title" -> 14.sp
                                    "Heading 1" -> 13.sp
                                    "Heading 2" -> 12.sp
                                    else -> 11.sp
                                },
                                fontWeight = when (styleKey) {
                                    "Title", "Heading 1", "Heading 2" -> FontWeight.Bold
                                    "Intense Quote" -> FontWeight.SemiBold
                                    else -> FontWeight.Normal
                                },
                                fontStyle = if (styleKey.contains("Quote")) FontStyle.Italic else FontStyle.Normal,
                                color = when (styleKey) {
                                    "Heading 1" -> Color(0xFF2563EB)
                                    "Heading 2" -> Color(0xFF1D4ED8)
                                    "Title" -> Color(0xFF1E3A8A)
                                    "Subtitle" -> Color(0xFF4B5563)
                                    else -> Color(0xFF1E293B)
                                },
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = displayLabel,
                                fontSize = 9.sp,
                                color = if (isSelected) Color(0xFF1D4ED8) else Color(0xFF64748B),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Custom User Styles
                state.customStyles.forEach { customStyle ->
                    val isSelected = state.selectedStyleName == customStyle.name
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = if (isSelected) Color(0xFFDCEAFE) else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .width(72.dp)
                            .height(52.dp)
                            .clickable { onEvent(RibbonEvent.OnApplyCustomStyle(customStyle)) }
                    ) {
                        Column(
                            modifier = Modifier.padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "AaBb",
                                fontSize = 12.sp,
                                fontWeight = if (customStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (customStyle.isItalic) FontStyle.Italic else FontStyle.Normal,
                                color = customStyle.textColor,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = customStyle.name,
                                fontSize = 9.sp,
                                color = Color(0xFF1D4ED8),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Create Style Button
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .height(52.dp)
                        .clickable { showCreateStyleDialog = true }
                        .padding(horizontal = 6.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF185ABD))
                        Text(if (state.isRtl) "إنشاء نمط..." else "Create Style...", fontSize = 8.sp, color = Color(0xFF185ABD), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Create Custom Style Dialog
        if (showCreateStyleDialog) {
            AlertDialog(
                onDismissRequest = { showCreateStyleDialog = false },
                title = { Text(if (state.isRtl) "إنشاء نمط جديد من التنسيق الحالي" else "Create New Style From Formatting") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (state.isRtl) "أدخل اسم النمط الجديد:" else "Enter style name:")
                        OutlinedTextField(
                            value = newStyleName,
                            onValueChange = { newStyleName = it },
                            placeholder = { Text(if (state.isRtl) "نمط مخصص 1" else "Custom Style 1") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newStyleName.isNotBlank()) {
                                onEvent(
                                    RibbonEvent.OnCreateCustomStyle(
                                        name = newStyleName.trim(),
                                        fontFamily = state.fontFamily,
                                        fontSize = state.fontSize,
                                        isBold = state.isBold,
                                        isItalic = state.isItalic,
                                        textColor = state.textColor,
                                        alignment = state.alignment
                                    )
                                )
                                newStyleName = ""
                                showCreateStyleDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185ABD))
                    ) {
                        Text(if (state.isRtl) "حفظ النمط" else "Save Style")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateStyleDialog = false }) {
                        Text(if (state.isRtl) "إلغاء" else "Cancel")
                    }
                }
            )
        }

        VerticalDivider()

        // 6. Editing Group (Find, Replace, Select Dropdown)
        RibbonGroup(localize("Editing", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Editing")) }) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                RibbonSmallButton(Icons.Default.Search, if (state.isRtl) "بحث (Find)" else "Find") {
                    onEvent(RibbonEvent.OnShowFindReplaceDialog)
                }
                RibbonSmallButton(Icons.Default.FindReplace, if (state.isRtl) "استبدال (Replace)" else "Replace") {
                    onEvent(RibbonEvent.OnShowFindReplaceDialog)
                }
                Box {
                    RibbonSmallButton(Icons.Default.SelectAll, if (state.isRtl) "تحديد (Select) ▾" else "Select ▾") {
                        showSelectMenu = true
                    }
                    DropdownMenu(expanded = showSelectMenu, onDismissRequest = { showSelectMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(if (state.isRtl) "تحديد الكل (Select All) — Ctrl+A" else "Select All (Ctrl+A)") },
                            onClick = {
                                onEvent(RibbonEvent.OnSelectAllClicked)
                                showSelectMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (state.isRtl) "تحديد الفقرة الحالية (Select Paragraph)" else "Select Current Paragraph") },
                            onClick = {
                                onEvent(RibbonEvent.OnSelectCurrentBlockClicked)
                                showSelectMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (state.isRtl) "تحديد النص ذي التنسيق المماثل" else "Select Text with Similar Formatting") },
                            onClick = {
                                onEvent(RibbonEvent.OnSelectSimilarFormattingClicked)
                                showSelectMenu = false
                            }
                        )
                    }
                }
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
    var showColorsMenu by remember { mutableStateOf(false) }
    var showFontsMenu by remember { mutableStateOf(false) }
    var showSpacingMenu by remember { mutableStateOf(false) }
    var showBordersDialog by remember { mutableStateOf(false) }
    var showPageColorStudio by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        RibbonGroup(localize("Document Formatting", state.isRtl), onLaunchDetails = { onEvent(RibbonEvent.OnShowGroupDetails("Document Formatting")) }) {
            RibbonLargeButton(Icons.Default.Style, if (state.isRtl) "قوالب جاهزة" else "Quick Templates") {
                onEvent(RibbonEvent.OnShowTemplatesDialog)
            }
            Box {
                RibbonLargeButton(Icons.Default.Palette, localize("Themes", state.isRtl)) { showThemeMenu = true }
                DropdownMenu(expanded = showThemeMenu, onDismissRequest = { showThemeMenu = false }) {
                    DropdownMenuItem(text = { Text(if (state.isRtl) "🔷 أوفيس القياسي (Office)" else "Office (Standard)") }, onClick = { onEvent(RibbonEvent.OnApplyDocumentTheme("Office")); showThemeMenu = false })
                    DropdownMenuItem(text = { Text(if (state.isRtl) "🎨 حديث وعصري (Modern Clean)" else "Modern Clean") }, onClick = { onEvent(RibbonEvent.OnApplyDocumentTheme("Modern")); showThemeMenu = false })
                    DropdownMenuItem(text = { Text(if (state.isRtl) "📜 كلاسيكي رسمي (Formal Paper)" else "Formal Paper") }, onClick = { onEvent(RibbonEvent.OnApplyDocumentTheme("Formal")); showThemeMenu = false })
                    DropdownMenuItem(text = { Text(if (state.isRtl) "🏛️ تقليدي أنيق (Classic Style)" else "Classic Style") }, onClick = { onEvent(RibbonEvent.OnApplyDocumentTheme("Classic")); showThemeMenu = false })
                    DropdownMenuItem(text = { Text(if (state.isRtl) "🍃 زمردي طبيعي (Emerald Fresh)" else "Emerald Fresh") }, onClick = { onEvent(RibbonEvent.OnApplyDocumentTheme("Emerald")); showThemeMenu = false })
                    DropdownMenuItem(text = { Text(if (state.isRtl) "📙 ذهبي دافئ (Warm Amber)" else "Warm Amber") }, onClick = { onEvent(RibbonEvent.OnApplyDocumentTheme("Warm Amber")); showThemeMenu = false })
                }
            }
            RibbonLargeButton(Icons.Default.Style, localize("Style Set", state.isRtl)) {
                onEvent(RibbonEvent.OnFontFamilyChanged("Calibri"))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Box {
                    RibbonSmallButton(Icons.Default.ColorLens, localize("Colors", state.isRtl)) { showColorsMenu = true }
                    DropdownMenu(expanded = showColorsMenu, onDismissRequest = { showColorsMenu = false }) {
                        DropdownMenuItem(text = { Text(localize("Office", state.isRtl)) }, onClick = { showColorsMenu = false })
                        DropdownMenuItem(text = { Text(localize("Grayscale", state.isRtl)) }, onClick = { showColorsMenu = false })
                        DropdownMenuItem(text = { Text(localize("Warm", state.isRtl)) }, onClick = { showColorsMenu = false })
                        DropdownMenuItem(text = { Text(localize("Cool", state.isRtl)) }, onClick = { showColorsMenu = false })
                    }
                }
                Box {
                    RibbonSmallButton(Icons.Default.FontDownload, localize("Fonts", state.isRtl)) { showFontsMenu = true }
                    DropdownMenu(expanded = showFontsMenu, onDismissRequest = { showFontsMenu = false }) {
                        DropdownMenuItem(text = { Text("Office (Calibri)") }, onClick = { onEvent(RibbonEvent.OnFontFamilyChanged("Calibri")); showFontsMenu = false })
                        DropdownMenuItem(text = { Text("Classic (Times New Roman)") }, onClick = { onEvent(RibbonEvent.OnFontFamilyChanged("Times New Roman")); showFontsMenu = false })
                        DropdownMenuItem(text = { Text("Modern (Arial)") }, onClick = { onEvent(RibbonEvent.OnFontFamilyChanged("Arial")); showFontsMenu = false })
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Box {
                    RibbonSmallButton(Icons.Default.FormatLineSpacing, localize("Paragraph Spacing", state.isRtl)) { showSpacingMenu = true }
                    DropdownMenu(expanded = showSpacingMenu, onDismissRequest = { showSpacingMenu = false }) {
                        DropdownMenuItem(text = { Text(localize("No Paragraph Space", state.isRtl)) }, onClick = { showSpacingMenu = false })
                        DropdownMenuItem(text = { Text(localize("Compact", state.isRtl)) }, onClick = { showSpacingMenu = false })
                        DropdownMenuItem(text = { Text(localize("Tight", state.isRtl)) }, onClick = { showSpacingMenu = false })
                        DropdownMenuItem(text = { Text(localize("Open", state.isRtl)) }, onClick = { showSpacingMenu = false })
                        DropdownMenuItem(text = { Text(localize("Relaxed", state.isRtl)) }, onClick = { showSpacingMenu = false })
                        DropdownMenuItem(text = { Text(localize("Double", state.isRtl)) }, onClick = { showSpacingMenu = false })
                    }
                }
                RibbonSmallButton(Icons.Default.AutoFixHigh, localize("Effects", state.isRtl)) { }
            }
            RibbonLargeButton(Icons.Default.CheckCircleOutline, localize("Set as Default", state.isRtl)) {
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

@Composable
fun DrawTabContent(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    var showColorMenu by remember { mutableStateOf(false) }
    var showThicknessMenu by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        RibbonGroup(localize("Tools", state.isRtl)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RibbonToggleButton(
                        icon = Icons.Default.Edit,
                        isChecked = state.isDrawingMode && !state.isHighlighterMode && !state.isEraserMode
                    ) { onEvent(RibbonEvent.OnToggleDrawingMode) }
                    Text(localize("Draw", state.isRtl), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RibbonToggleButton(
                        icon = Icons.Default.Brush,
                        isChecked = state.isDrawingMode && state.isHighlighterMode
                    ) { onEvent(RibbonEvent.OnToggleHighlighterMode) }
                    Text(localize("Highlighter", state.isRtl), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RibbonToggleButton(
                        icon = Icons.Default.FormatPaint,
                        isChecked = state.isDrawingMode && state.isEraserMode
                    ) { onEvent(RibbonEvent.OnToggleEraserMode) }
                    Text(localize("Eraser", state.isRtl), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        RibbonGroup(localize("Format", state.isRtl)) {
            Box {
                RibbonLargeButton(
                    icon = Icons.Default.Palette,
                    label = localize("Color", state.isRtl)
                ) { showColorMenu = true }
                
                DropdownMenu(expanded = showColorMenu, onDismissRequest = { showColorMenu = false }) {
                    val colors = listOf(Color.Black, Color.Red, Color.Blue, Color.Green, Color(0xFFFFEB3B))
                    colors.forEach { color ->
                        DropdownMenuItem(
                            text = { Text("Color") },
                            onClick = { 
                                onEvent(RibbonEvent.OnInkColorChanged(color))
                                showColorMenu = false
                            }
                        )
                    }
                }
            }

            Box {
                RibbonLargeButton(
                    icon = Icons.Default.Edit,
                    label = localize("Thickness", state.isRtl)
                ) { showThicknessMenu = true }
                
                DropdownMenu(expanded = showThicknessMenu, onDismissRequest = { showThicknessMenu = false }) {
                    val thicknesses = listOf(2f, 4f, 8f, 12f)
                    thicknesses.forEach { thickness ->
                        DropdownMenuItem(
                            text = { Text("${thickness.toInt()} pt") },
                            onClick = { 
                                onEvent(RibbonEvent.OnInkThicknessChanged(thickness))
                                showThicknessMenu = false
                            }
                        )
                    }
                }
            }
        }

        RibbonGroup(localize("Canvas", state.isRtl)) {
            RibbonLargeButton(
                icon = Icons.Default.Delete,
                label = localize("Clear All", state.isRtl)
            ) { onEvent(RibbonEvent.OnClearDrawing) }
        }
    }
}

@Composable
fun ReferencesTabContent(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RibbonGroup(localize("Table of Contents", state.isRtl)) {
            RibbonLargeButton(Icons.Default.MenuBook, localize("Table of Contents", state.isRtl)) {}
        }
        VerticalDivider()
        RibbonGroup(localize("Footnotes", state.isRtl)) {
            RibbonLargeButton(Icons.Default.TextFormat, localize("Insert Footnote", state.isRtl)) {}
        }
        VerticalDivider()
        RibbonGroup(localize("Citations & Bibliography", state.isRtl)) {
            RibbonLargeButton(Icons.Default.Source, localize("Insert Citation", state.isRtl)) {}
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                RibbonSmallButton(Icons.Default.Settings, localize("Manage Sources", state.isRtl)) {}
            }
        }
        VerticalDivider()
        RibbonGroup(localize("Captions", state.isRtl)) {
            RibbonLargeButton(Icons.Default.ClosedCaption, localize("Insert Caption", state.isRtl)) {}
        }
        VerticalDivider()
        RibbonGroup(localize("Index", state.isRtl)) {
            RibbonLargeButton(Icons.Default.List, localize("Mark Entry", state.isRtl)) {}
        }
    }
}

@Composable
fun MailingsTabContent(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RibbonGroup(localize("Create", state.isRtl)) {
            RibbonLargeButton(Icons.Default.Mail, localize("Envelopes", state.isRtl)) {}
            RibbonLargeButton(Icons.Default.Label, localize("Labels", state.isRtl)) {}
        }
        VerticalDivider()
        RibbonGroup(localize("Start Mail Merge", state.isRtl)) {
            RibbonLargeButton(Icons.Default.People, localize("Start Mail Merge", state.isRtl)) {}
        }
        VerticalDivider()
        RibbonGroup(localize("Write & Insert Fields", state.isRtl)) {
            RibbonLargeButton(Icons.Default.PostAdd, localize("Insert Merge Field", state.isRtl)) {}
        }
        VerticalDivider()
        RibbonGroup(localize("Preview Results", state.isRtl)) {
            RibbonLargeButton(Icons.Default.Preview, localize("Preview Results", state.isRtl)) {}
        }
        VerticalDivider()
        RibbonGroup(localize("Finish", state.isRtl)) {
            RibbonLargeButton(Icons.Default.DoneAll, localize("Finish & Merge", state.isRtl)) {}
        }
    }
}

@Composable
fun HelpTabContent(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RibbonGroup(localize("Help", state.isRtl)) {
            RibbonLargeButton(Icons.Default.HelpOutline, localize("Help", state.isRtl)) {}
            RibbonLargeButton(Icons.Default.ContactSupport, localize("Contact Support", state.isRtl)) {}
            RibbonLargeButton(Icons.Default.Feedback, localize("Feedback", state.isRtl)) {}
            RibbonLargeButton(Icons.Default.ModelTraining, localize("Training", state.isRtl)) {}
        }
        VerticalDivider()
        RibbonGroup(localize("What's New", state.isRtl)) {
            RibbonLargeButton(Icons.Default.NewReleases, localize("What's New", state.isRtl)) {}
        }
    }
}

@Composable
fun PictureFormatTabContent(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RibbonGroup(localize("Adjust", state.isRtl)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                RibbonSmallButton(Icons.Default.AutoFixHigh, localize("Corrections", state.isRtl)) {}
                RibbonSmallButton(Icons.Default.Palette, localize("Color", state.isRtl)) {}
                RibbonSmallButton(Icons.Default.Brush, localize("Artistic Effects", state.isRtl)) {}
            }
        }
        VerticalDivider()
        RibbonGroup(localize("Picture Styles", state.isRtl)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                RibbonSmallButton(Icons.Default.BorderStyle, localize("Picture Border", state.isRtl)) {}
                RibbonSmallButton(Icons.Default.AutoFixHigh, localize("Picture Effects", state.isRtl)) {}
            }
        }
        VerticalDivider()
        RibbonGroup(localize("Arrange", state.isRtl)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                RibbonSmallButton(Icons.Default.WrapText, localize("Wrap Text", state.isRtl)) {}
                RibbonSmallButton(Icons.Default.FlipToFront, localize("Bring Forward", state.isRtl)) {}
                RibbonSmallButton(Icons.Default.FlipToBack, localize("Send Backward", state.isRtl)) {}
            }
        }
        VerticalDivider()
        RibbonGroup(localize("Size", state.isRtl)) {
            RibbonLargeButton(Icons.Default.Crop, localize("Crop", state.isRtl)) {}
        }
    }
}

@Composable
fun TableDesignTabContent(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RibbonGroup(localize("Table Style Options", state.isRtl)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                RibbonSmallButton(Icons.Default.ViewStream, localize("Header Row", state.isRtl)) {}
                RibbonSmallButton(Icons.Default.Functions, localize("Total Row", state.isRtl)) {}
                RibbonSmallButton(Icons.Default.ViewAgenda, localize("Banded Rows", state.isRtl)) {}
            }
        }
        VerticalDivider()
        RibbonGroup(localize("Table Styles", state.isRtl)) {
            RibbonLargeButton(Icons.Default.FormatColorFill, localize("Shading", state.isRtl)) {}
        }
        VerticalDivider()
        RibbonGroup(localize("Borders", state.isRtl)) {
            RibbonLargeButton(Icons.Default.BorderAll, localize("Borders", state.isRtl)) {}
            RibbonLargeButton(Icons.Default.FormatPaint, localize("Border Painter", state.isRtl)) {}
        }
    }
}

@Composable
fun ShapeFormatTabContent(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RibbonGroup(localize("Insert Shapes", state.isRtl)) {
            RibbonLargeButton(Icons.Default.Category, localize("Insert Shapes", state.isRtl)) {}
        }
        VerticalDivider()
        RibbonGroup(localize("Shape Styles", state.isRtl)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                RibbonSmallButton(Icons.Default.FormatColorFill, localize("Shape Fill", state.isRtl)) {}
                RibbonSmallButton(Icons.Default.BorderStyle, localize("Shape Outline", state.isRtl)) {}
                RibbonSmallButton(Icons.Default.AutoFixHigh, localize("Shape Effects", state.isRtl)) {}
            }
        }
        VerticalDivider()
        RibbonGroup(localize("WordArt Styles", state.isRtl)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                RibbonSmallButton(Icons.Default.FormatColorText, localize("Text Fill", state.isRtl)) {}
                RibbonSmallButton(Icons.Default.TextFormat, localize("Text Outline", state.isRtl)) {}
            }
        }
        VerticalDivider()
        RibbonGroup(localize("Arrange", state.isRtl)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                RibbonSmallButton(Icons.Default.FlipToFront, localize("Bring Forward", state.isRtl)) {}
                RibbonSmallButton(Icons.Default.FlipToBack, localize("Send Backward", state.isRtl)) {}
            }
        }
    }
}
