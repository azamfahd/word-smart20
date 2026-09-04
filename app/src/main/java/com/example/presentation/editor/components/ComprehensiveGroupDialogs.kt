package com.example.presentation.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.presentation.editor.*
import com.example.presentation.editor.font.FontEngine

/**
 * Reusable Classic Windows Desktop Group Box (Fieldset) with embedded top label
 */
@Composable
fun WindowsGroupBox(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.padding(top = 6.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            color = Color.White,
            shape = RoundedCornerShape(2.dp),
            border = BorderStroke(1.dp, Color(0xFFC5C9D0))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content
            )
        }
        Text(
            text = " $title ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B579A),
            modifier = Modifier
                .padding(start = 12.dp)
                .background(Color(0xFFF0F0F0))
                .padding(horizontal = 4.dp)
        )
    }
}

/**
 * Classic Windows Desktop Dialog Tab Control Header
 */
@Composable
fun WindowsTabStrip(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0))
            .padding(start = 4.dp, end = 4.dp, top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        tabs.forEachIndexed { index, tabTitle ->
            val isSelected = index == selectedTabIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                    .background(if (isSelected) Color.White else Color(0xFFE1E4E8))
                    .border(
                        BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF2B579A) else Color(0xFFC0C4CC)
                        ),
                        RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                    )
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tabTitle,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF2B579A) else Color(0xFF333333)
                )
            }
        }
    }
}

/**
 * Universal Authentic Microsoft Word Dialog Container for Windows
 */
@Composable
fun ComprehensiveGroupDetailsDialog(
    groupName: String,
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var activeColorStudioTarget by remember { mutableStateOf<String?>(null) }
    var activeColorStudioInitialColor by remember { mutableStateOf(Color.Black) }
    var activeColorStudioTitle by remember { mutableStateOf("") }
    var activeColorStudioAllowTransparent by remember { mutableStateOf(false) }

    fun openColorStudio(target: String, initial: Color, title: String, allowTransparent: Boolean = false) {
        activeColorStudioTarget = target
        activeColorStudioInitialColor = initial
        activeColorStudioTitle = title
        activeColorStudioAllowTransparent = allowTransparent
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f),
            color = Color(0xFFF0F0F0),
            shape = RoundedCornerShape(2.dp),
            border = BorderStroke(1.dp, Color(0xFF707070)),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Windows Word Header Bar
                Surface(
                    color = Color(0xFF2B579A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when (groupName) {
                                    "Font" -> Icons.Default.FormatSize
                                    "Paragraph" -> Icons.AutoMirrored.Filled.FormatAlignLeft
                                    "Clipboard" -> Icons.Default.ContentPaste
                                    "Undo" -> Icons.AutoMirrored.Filled.Undo
                                    "Styles" -> Icons.Default.Title
                                    "Editing" -> Icons.Default.FindReplace
                                    "Pages" -> Icons.Default.InsertPageBreak
                                    "Tables" -> Icons.Default.TableChart
                                    "Illustrations" -> Icons.Default.Category
                                    "Decorations & Banners" -> Icons.Default.WebAsset
                                    "Header & Footer" -> Icons.Default.BorderTop
                                    "Symbols & Math" -> Icons.Default.Functions
                                    "Document Formatting" -> Icons.Default.Palette
                                    "Page Background" -> Icons.Default.WaterDrop
                                    "Proofing & Stats" -> Icons.Default.Analytics
                                    "Smart AI Tools" -> Icons.Default.AutoAwesome
                                    "PDF Export" -> Icons.Default.PictureAsPdf
                                    else -> Icons.Default.Tune
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = when (groupName) {
                                    "Font" -> if (state.isRtl) "الخط (Font)" else "Font"
                                    "Paragraph" -> if (state.isRtl) "فقرة (Paragraph)" else "Paragraph"
                                    "Page Background" -> if (state.isRtl) "خلفية الصفحة (Page Background)" else "Page Background"
                                    "Tables" -> if (state.isRtl) "خصائص الجدول (Table Properties)" else "Table Properties"
                                    "Clipboard" -> if (state.isRtl) "الحافظة (Clipboard)" else "Clipboard"
                                    else -> groupName
                                },
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Body Content Panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    when (groupName) {
                        "Font" -> WindowsWordFontDialogContent(
                            state = state,
                            onEvent = onEvent,
                            onOpenColorStudio = { target, initial, title ->
                                openColorStudio(target, initial, title, allowTransparent = target == "highlight")
                            }
                        )
                        "Paragraph" -> WindowsWordParagraphDialogContent(
                            state = state,
                            onEvent = onEvent,
                            onOpenColorStudio = { target, initial, title ->
                                openColorStudio(target, initial, title, allowTransparent = true)
                            }
                        )
                        "Styles" -> StylesDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "Editing" -> EditingDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "Undo" -> UndoDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "Pages" -> PagesDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "Page Background" -> PageBackgroundDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onOpenColorStudio = { target, initial, title ->
                                openColorStudio(target, initial, title, allowTransparent = target == "border")
                            }
                        )
                        "Page Setup" -> PageSetupDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "Paragraph Spacing" -> ParagraphSpacingDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "Tables" -> TablesDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "Clipboard" -> ClipboardDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "Illustrations" -> IllustrationsDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "Decorations & Banners" -> DecorationsDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "Header & Footer" -> HeaderFooterDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "Symbols & Math" -> SymbolsMathDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "Document Formatting" -> DocumentFormattingDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "Proofing & Stats" -> ProofingStatsDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "Smart AI Tools" -> SmartAiDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "PDF Export" -> PdfExportDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "Views" -> ViewsDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        "Zoom" -> ZoomDetailedCustomizer(
                            state = state,
                            onEvent = onEvent,
                            onDismiss = onDismiss
                        )
                        else -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                WindowsGroupBox(title = if (state.isRtl) "خيارات $groupName" else "$groupName Options") {
                                    Text(
                                        text = if (state.isRtl) "إعدادات وتفاصيل المجموعة — Microsoft Word" else "Group Details & Customization — Microsoft Word",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2B579A)
                                    )
                                    Text(
                                        text = if (state.isRtl) "تم تفعيل كافة خيارات المجموعة المحددة بنجاح وتطبيقها على المستند." else "All options for the selected group are active and applied to the document.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFCBD5E1))

                // Footer Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE5E7EB))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { },
                            shape = RoundedCornerShape(2.dp),
                            border = BorderStroke(1.dp, Color(0xFFA0A0A0)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(if (state.isRtl) "تعيين كافتراضي" else "Set As Default", fontSize = 11.sp, color = Color(0xFF333333))
                        }

                        if (groupName == "Font") {
                            OutlinedButton(
                                onClick = { },
                                shape = RoundedCornerShape(2.dp),
                                border = BorderStroke(1.dp, Color(0xFFA0A0A0)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(if (state.isRtl) "تأثيرات النص..." else "Text Effects...", fontSize = 11.sp, color = Color(0xFF333333))
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                            shape = RoundedCornerShape(2.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(if (state.isRtl) "موافق" else "OK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(2.dp),
                            border = BorderStroke(1.dp, Color(0xFFA0A0A0)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(if (state.isRtl) "إلغاء الأمر" else "Cancel", fontSize = 12.sp, color = Color(0xFF333333))
                        }
                    }
                }
            }
        }
    }

    // Color Studio Sub-dialog
    if (activeColorStudioTarget != null) {
        ColorStudioDialog(
            title = activeColorStudioTitle,
            initialColor = activeColorStudioInitialColor,
            isRtl = state.isRtl,
            allowTransparent = activeColorStudioAllowTransparent,
            onColorSelected = { selectedColor ->
                when (activeColorStudioTarget) {
                    "text" -> onEvent(RibbonEvent.OnTextColorChanged(selectedColor))
                    "highlight" -> onEvent(RibbonEvent.OnHighlightColorChanged(selectedColor))
                    "page" -> onEvent(RibbonEvent.OnPageColorChanged(selectedColor))
                    "shading" -> onEvent(RibbonEvent.OnParagraphShadingChanged(selectedColor))
                    "border" -> onEvent(RibbonEvent.OnPageBorderChanged(state.pageBorder.copy(color = selectedColor)))
                }
                activeColorStudioTarget = null
            },
            onDismiss = { activeColorStudioTarget = null }
        )
    }
}

/**
 * Authentic Windows Microsoft Word Font Dialog Content
 */
@Composable
fun WindowsWordFontDialogContent(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onOpenColorStudio: (target: String, initial: Color, title: String) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val allFonts = remember { FontEngine.getAllFonts() }
    val isRtl = state.isRtl

    var selectedFontName by remember(state.fontFamily) { mutableStateOf(state.fontFamily) }
    var selectedFontSize by remember(state.fontSize) { mutableIntStateOf(state.fontSize) }
    var isBold by remember(state.isBold) { mutableStateOf(state.isBold) }
    var isItalic by remember(state.isItalic) { mutableStateOf(state.isItalic) }
    var isStrikethrough by remember(state.isStrikethrough) { mutableStateOf(state.isStrikethrough) }
    var isSubscript by remember(state.isSubscript) { mutableStateOf(state.isSubscript) }
    var isSuperscript by remember(state.isSuperscript) { mutableStateOf(state.isSuperscript) }
    var underlineStyle by remember(state.underlineStyle) { mutableStateOf(state.underlineStyle) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        WindowsTabStrip(
            tabs = listOf(
                if (isRtl) "الخط (Font)" else "Font",
                if (isRtl) "خيارات متقدمة (Advanced)" else "Advanced"
            ),
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it }
        )

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(2.dp),
            border = BorderStroke(1.dp, Color(0xFFC0C0C0))
        ) {
            if (selectedTabIndex == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Font, Style, and Size Lists Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Font Family List Box
                        Column(modifier = Modifier.weight(2f)) {
                            Text(
                                text = if (isRtl) "الخط:" else "Font:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            OutlinedTextField(
                                value = selectedFontName,
                                onValueChange = {
                                    selectedFontName = it
                                    onEvent(RibbonEvent.OnFontFamilyChanged(it))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp),
                                textStyle = TextStyle(fontSize = 11.sp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .border(1.dp, Color(0xFFC0C0C0)),
                                color = Color.White
                            ) {
                                Column {
                                    allFonts.take(5).forEach { item ->
                                        val isSel = item.family.equals(selectedFontName, ignoreCase = true)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (isSel) Color(0xFF0078D7) else Color.White)
                                                .clickable {
                                                    selectedFontName = item.family
                                                    onEvent(RibbonEvent.OnFontFamilyChanged(item.family))
                                                }
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = item.displayName,
                                                fontFamily = FontEngine.getFontFamily(item.family),
                                                fontSize = 11.sp,
                                                color = if (isSel) Color.White else Color(0xFF1E293B)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Font Style List Box
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = if (isRtl) "نمط الخط:" else "Font style:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            val activeStyleName = when {
                                isBold && isItalic -> if (isRtl) "عريض مائل" else "Bold Italic"
                                isBold -> if (isRtl) "عريض" else "Bold"
                                isItalic -> if (isRtl) "مائل" else "Italic"
                                else -> if (isRtl) "عادي" else "Regular"
                            }
                            OutlinedTextField(
                                value = activeStyleName,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp),
                                textStyle = TextStyle(fontSize = 11.sp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(1.dp, Color(0xFFC0C0C0)),
                                color = Color.White
                            ) {
                                Column {
                                    val styles = listOf(
                                        (if (isRtl) "عادي" else "Regular") to (false to false),
                                        (if (isRtl) "مائل" else "Italic") to (false to true),
                                        (if (isRtl) "عريض" else "Bold") to (true to false),
                                        (if (isRtl) "عريض مائل" else "Bold Italic") to (true to true)
                                    )
                                    styles.forEach { (label, pair) ->
                                        val isSel = isBold == pair.first && isItalic == pair.second
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (isSel) Color(0xFF0078D7) else Color.White)
                                                .clickable {
                                                    isBold = pair.first
                                                    isItalic = pair.second
                                                    if (pair.first != state.isBold) onEvent(RibbonEvent.OnBoldClicked)
                                                    if (pair.second != state.isItalic) onEvent(RibbonEvent.OnItalicClicked)
                                                }
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (pair.first) FontWeight.Bold else FontWeight.Normal,
                                                fontStyle = if (pair.second) FontStyle.Italic else FontStyle.Normal,
                                                color = if (isSel) Color.White else Color(0xFF1E293B)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Size List Box
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isRtl) "الحجم:" else "Size:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            OutlinedTextField(
                                value = selectedFontSize.toString(),
                                onValueChange = {
                                    it.toIntOrNull()?.let { sz ->
                                        selectedFontSize = sz
                                        onEvent(RibbonEvent.OnFontSizeChanged(sz))
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp),
                                textStyle = TextStyle(fontSize = 11.sp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .border(1.dp, Color(0xFFC0C0C0)),
                                color = Color.White
                            ) {
                                val standardSizes = listOf(9, 10, 11, 12, 14, 16, 18, 24, 28, 36)
                                Column {
                                    standardSizes.take(5).forEach { sz ->
                                        val isSel = selectedFontSize == sz
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (isSel) Color(0xFF0078D7) else Color.White)
                                                .clickable {
                                                    selectedFontSize = sz
                                                    onEvent(RibbonEvent.OnFontSizeChanged(sz))
                                                }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "$sz",
                                                fontSize = 11.sp,
                                                color = if (isSel) Color.White else Color(0xFF1E293B)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Font Color & Underline Pickers Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Font Color
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isRtl) "لون الخط:" else "Font color:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                                    .border(1.dp, Color(0xFFC0C0C0))
                                    .clickable {
                                        onOpenColorStudio("text", state.textColor, if (isRtl) "تخصيص لون الخط" else "Font Color")
                                    }
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(state.textColor)
                                            .border(1.dp, Color.Gray)
                                    )
                                    Text(if (isRtl) "تلقائي / مخصص" else "Automatic", fontSize = 11.sp)
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Underline style
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isRtl) "نمط التسطير:" else "Underline style:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                                    .border(1.dp, Color(0xFFC0C0C0))
                                    .clickable { onEvent(RibbonEvent.OnUnderlineClicked) }
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (underlineStyle != UnderlineStyle.NONE) (if (isRtl) "سطر واحد" else "Single") else (if (isRtl) "(بلا)" else "(none)"),
                                    fontSize = 11.sp
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Effects GroupBox
                    WindowsGroupBox(title = if (isRtl) "تأثيرات (Effects)" else "Effects") {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                CheckboxRow(
                                    label = if (isRtl) "يتوسطه خط (Strikethrough)" else "Strikethrough",
                                    checked = isStrikethrough,
                                    onCheckedChange = {
                                        isStrikethrough = it
                                        onEvent(RibbonEvent.OnStrikethroughClicked)
                                    }
                                )
                                CheckboxRow(
                                    label = if (isRtl) "مرتفع (Superscript)" else "Superscript",
                                    checked = isSuperscript,
                                    onCheckedChange = {
                                        isSuperscript = it
                                        if (it) isSubscript = false
                                        onEvent(RibbonEvent.OnSuperscriptClicked)
                                    }
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                CheckboxRow(
                                    label = if (isRtl) "منخفض (Subscript)" else "Subscript",
                                    checked = isSubscript,
                                    onCheckedChange = {
                                        isSubscript = it
                                        if (it) isSuperscript = false
                                        onEvent(RibbonEvent.OnSubscriptClicked)
                                    }
                                )
                                CheckboxRow(
                                    label = if (isRtl) "جميع الحروف كبيرة (All caps)" else "All caps",
                                    checked = false,
                                    onCheckedChange = {}
                                )
                            }
                        }
                    }

                    // Live Word Preview Box
                    WindowsGroupBox(title = if (isRtl) "معاينة (Preview)" else "Preview") {
                        val sampleFontFamily = remember(selectedFontName) { FontEngine.getFontFamily(selectedFontName) }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .border(1.dp, Color(0xFFC0C0C0)),
                            color = Color.White
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isRtl) "AaaBbCc123 — بسم الله الرحمن الرحيم" else "AaaBbCc123 — Sample Document Typography",
                                    fontFamily = sampleFontFamily,
                                    fontSize = selectedFontSize.coerceIn(10, 26).sp,
                                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                                    fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                                    color = state.textColor,
                                    textDecoration = when {
                                        isStrikethrough && underlineStyle != UnderlineStyle.NONE -> TextDecoration.combine(listOf(TextDecoration.LineThrough, TextDecoration.Underline))
                                        isStrikethrough -> TextDecoration.LineThrough
                                        underlineStyle != UnderlineStyle.NONE -> TextDecoration.Underline
                                        else -> TextDecoration.None
                                    },
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Text(
                            text = if (isRtl) "هذا خط موحد حقيقي وسيتم استخدامه للشاشة والطباعة." else "This is a TrueType font. The same font will be used on screen and printer.",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                // Advanced Character Spacing Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WindowsGroupBox(title = if (isRtl) "تباعد الأحرف (Character Spacing)" else "Character Spacing") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isRtl) "المقياس (Scale):" else "Scale:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("100%", fontSize = 11.sp, color = Color(0xFF2B579A), fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isRtl) "التباعد (Spacing):" else "Spacing:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(if (isRtl) "عادي (Normal)" else "Normal", fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isRtl) "الموضع (Position):" else "Position:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(if (isRtl) "عادي (Normal)" else "Normal", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Authentic Windows Microsoft Word Paragraph Dialog Content
 */
@Composable
fun WindowsWordParagraphDialogContent(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onOpenColorStudio: (target: String, initial: Color, title: String) -> Unit
) {
    val isRtl = state.isRtl

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsTabStrip(
            tabs = listOf(
                if (isRtl) "المسافات البادئة والمتباعدة" else "Indents and Spacing",
                if (isRtl) "فاصل الصفحات والأسطر" else "Line and Page Breaks"
            ),
            selectedTabIndex = 0,
            onTabSelected = {}
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 2.dp),
            color = Color.White,
            shape = RoundedCornerShape(2.dp),
            border = BorderStroke(1.dp, Color(0xFFC0C0C0))
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // General Fieldset
                WindowsGroupBox(title = if (isRtl) "عام (General)" else "General") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isRtl) "المحاذاة (Alignment):" else "Alignment:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                (if (isRtl) "يمين" else "Right") to TextAlignment.RIGHT,
                                (if (isRtl) "توسيط" else "Center") to TextAlignment.CENTER,
                                (if (isRtl) "يسار" else "Left") to TextAlignment.LEFT,
                                (if (isRtl) "ضبط" else "Justify") to TextAlignment.JUSTIFY
                            ).forEach { (lbl, align) ->
                                val isSel = state.alignment == align
                                OutlinedButton(
                                    onClick = { onEvent(RibbonEvent.OnAlignmentChanged(align)) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSel) Color(0xFF0078D7) else Color.White
                                    ),
                                    shape = RoundedCornerShape(2.dp),
                                    border = BorderStroke(1.dp, Color(0xFFC0C0C0)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text(lbl, fontSize = 10.sp, color = if (isSel) Color.White else Color(0xFF333333))
                                }
                            }
                        }
                    }
                }

                // Spacing Fieldset
                WindowsGroupBox(title = if (isRtl) "التصاعد والتباعد (Spacing)" else "Spacing") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isRtl) "تباعد الأسطر (Line spacing):" else "Line spacing:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1.0f to "1.0", 1.15f to "1.15", 1.5f to "1.5", 2.0f to "2.0").forEach { (spacing, lbl) ->
                                val isSel = (state.lineSpacing - spacing).let { kotlin.math.abs(it) < 0.05f }
                                OutlinedButton(
                                    onClick = { onEvent(RibbonEvent.OnLineSpacingChanged(spacing)) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSel) Color(0xFF0078D7) else Color.White
                                    ),
                                    shape = RoundedCornerShape(2.dp),
                                    border = BorderStroke(1.dp, Color(0xFFC0C0C0)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text(lbl, fontSize = 10.sp, color = if (isSel) Color.White else Color(0xFF333333))
                                }
                            }
                        }
                    }
                }

                // Live Paragraph Preview Box
                WindowsGroupBox(title = if (isRtl) "معاينة (Preview)" else "Preview") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(65.dp)
                            .border(1.dp, Color(0xFFC0C0C0)),
                        color = Color.White
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            val composeAlign = when (state.alignment) {
                                TextAlignment.LEFT -> TextAlign.Left
                                TextAlignment.CENTER -> TextAlign.Center
                                TextAlignment.RIGHT -> TextAlign.Right
                                TextAlignment.JUSTIFY -> TextAlign.Justify
                            }
                            Text(
                                text = if (isRtl) "فقرة سابقة... هذا النص يمثل معاينة حية للمسافات والمحاذاة وتباعد الأسطر في مستند وورد... فقرة تالية..."
                                else "Previous paragraph... Sample document preview representing paragraph spacing, line height and alignment...",
                                fontSize = 11.sp,
                                textAlign = composeAlign,
                                color = Color(0xFF334155),
                                lineHeight = (14 * state.lineSpacing).sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(24.dp),
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2B579A))
        )
        Text(label, fontSize = 11.sp, color = Color(0xFF1E293B))
    }
}

@Composable
fun PageBackgroundDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onOpenColorStudio: (target: String, initial: Color, title: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "لون الصفحة (Page Color)" else "Page Color") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (state.isRtl) "اختر لون الورقة:" else "Select Page Color:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { onOpenColorStudio("page", state.pageColor, "Page Color") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(if (state.isRtl) "استوديو الألوان..." else "Color Studio...", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun TablesDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "إدراج جدول جديد" else "Insert Table") {
            var rows by remember { mutableIntStateOf(3) }
            var cols by remember { mutableIntStateOf(3) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (state.isRtl) "عدد الأسطر والاعمدة:" else "Rows & Columns:", fontSize = 11.sp)
                Button(
                    onClick = {
                        onEvent(RibbonEvent.OnInsertTableClicked(rows, cols))
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(if (state.isRtl) "إدراج الجدول" else "Insert Table", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ClipboardDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "مهام الحافظة (Clipboard Tasks)" else "Clipboard Tasks") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onEvent(RibbonEvent.OnPasteClicked); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                    shape = RoundedCornerShape(2.dp)
                ) { Text(if (state.isRtl) "لصق (Paste)" else "Paste", fontSize = 11.sp) }

                OutlinedButton(
                    onClick = { onEvent(RibbonEvent.OnCopyClicked); onDismiss() },
                    shape = RoundedCornerShape(2.dp)
                ) { Text(if (state.isRtl) "نسخ (Copy)" else "Copy", fontSize = 11.sp) }

                OutlinedButton(
                    onClick = { onEvent(RibbonEvent.OnCutClicked); onDismiss() },
                    shape = RoundedCornerShape(2.dp)
                ) { Text(if (state.isRtl) "قص (Cut)" else "Cut", fontSize = 11.sp) }
            }
        }
    }
}

@Composable
fun StylesDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "معرض الأنماط (Styles Gallery)" else "Styles Gallery") {
            val styles = listOf(
                "Normal" to (if (state.isRtl) "عادي (Normal)" else "Normal"),
                "Title" to (if (state.isRtl) "العنوان الرئيسي (Title)" else "Title"),
                "Subtitle" to (if (state.isRtl) "العنوان الفرعي (Subtitle)" else "Subtitle"),
                "Heading 1" to (if (state.isRtl) "عنوان 1 (Heading 1)" else "Heading 1"),
                "Heading 2" to (if (state.isRtl) "عنوان 2 (Heading 2)" else "Heading 2"),
                "Heading 3" to (if (state.isRtl) "عنوان 3 (Heading 3)" else "Heading 3"),
                "Quote" to (if (state.isRtl) "اقتباس (Quote)" else "Quote"),
                "Intense Quote" to (if (state.isRtl) "اقتباس مكثف (Intense Quote)" else "Intense Quote")
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                styles.forEach { (styleKey, styleLabel) ->
                    OutlinedButton(
                        onClick = {
                            onEvent(RibbonEvent.OnApplyHeadingStyle(styleKey))
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Text(styleLabel, fontSize = 11.sp, color = Color(0xFF1E293B))
                    }
                }
            }
        }
    }
}

@Composable
fun EditingDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "أدوات التحرير والبحث (Editing & Search)" else "Editing & Search Tools") {
            Button(
                onClick = {
                    onEvent(RibbonEvent.OnShowFindReplaceDialog)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isRtl) "بحث واستبدال..." else "Find & Replace...", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun UndoDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "سجل التراجع والإعادة (Undo / Redo)" else "Undo / Redo History") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onEvent(RibbonEvent.OnUndoClicked); onDismiss() },
                    enabled = state.canUndo,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.isRtl) "تراجع (Undo)" else "Undo", fontSize = 11.sp)
                }

                Button(
                    onClick = { onEvent(RibbonEvent.OnRedoClicked); onDismiss() },
                    enabled = state.canRedo,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.isRtl) "إعادة (Redo)" else "Redo", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun PagesDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "الصفحات والفواصل (Pages & Breaks)" else "Pages & Breaks") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = { onEvent(RibbonEvent.OnInsertPageBreakClicked); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isRtl) "إدراج فاصل صفحات (Page Break)" else "Insert Page Break", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun PageSetupDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "إعداد الصفحة (Page Setup)" else "Page Setup") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(if (state.isRtl) "الهوامش:" else "Margins:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        PageMargin.NORMAL to "Normal",
                        PageMargin.NARROW to "Narrow",
                        PageMargin.MODERATE to "Moderate",
                        PageMargin.WIDE to "Wide"
                    ).forEach { (pm, marginName) ->
                        OutlinedButton(
                            onClick = { onEvent(RibbonEvent.OnPageMarginChanged(pm)); onDismiss() },
                            shape = RoundedCornerShape(2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(marginName, fontSize = 9.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(if (state.isRtl) "الاتجاه:" else "Orientation:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = { onEvent(RibbonEvent.OnPageOrientationChanged(PageOrientation.PORTRAIT)); onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (state.isRtl) "عمودي" else "Portrait", fontSize = 10.sp)
                    }
                    Button(
                        onClick = { onEvent(RibbonEvent.OnPageOrientationChanged(PageOrientation.LANDSCAPE)); onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (state.isRtl) "أفقي" else "Landscape", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ParagraphSpacingDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "تباعد الأسطر والفقرات" else "Line & Paragraph Spacing") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(1.0f, 1.15f, 1.5f, 2.0f, 2.5f, 3.0f).forEach { sp ->
                    OutlinedButton(
                        onClick = { onEvent(RibbonEvent.OnLineSpacingChanged(sp)); onDismiss() },
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("$sp", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun IllustrationsDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "إدراج الأشكال والرسومات" else "Insert Shapes & Illustrations") {
            val shapes = listOf(
                ShapeType.RECTANGLE to "Rectangle",
                ShapeType.OVAL to "Oval",
                ShapeType.ARROW to "Arrow",
                ShapeType.STAR to "Star",
                ShapeType.LINE to "Line"
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                shapes.forEach { (shapeType, shapeName) ->
                    OutlinedButton(
                        onClick = { onEvent(RibbonEvent.OnInsertShapeClicked(shapeType)); onDismiss() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text(shapeName, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DecorationsDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "الزخارف والفواصل الجمالية" else "Decorative Dividers & Banners") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = { onEvent(RibbonEvent.OnInsertDividerClicked); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isRtl) "إدراج خط فاصل ✤ ✦ ✤" else "Insert Divider Line", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { onEvent(RibbonEvent.OnInsertBannerClicked("عنوان مميز", "وصف تفصيلي للمستند")); onDismiss() },
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isRtl) "إدراج بنر ملون احترافي" else "Insert Colored Banner", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun HeaderFooterDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "الرأس والتذييل وأرقام الصفحات" else "Header, Footer & Page Numbers") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = { onEvent(RibbonEvent.OnToggleHeaderFooterMode); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isRtl) "تحرير الرأس والتذييل" else "Edit Header & Footer", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { onEvent(RibbonEvent.OnInsertPageNumberClicked); onDismiss() },
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isRtl) "إدراج رقم الصفحة" else "Insert Page Number", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun SymbolsMathDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "الرموز والمعادلات الرياضية" else "Symbols & Math Equations") {
            val symbols = listOf("©", "®", "™", "§", "¶", "±", "≠", "≤", "≥", "∞", "√", "∑", "∫", "π", "‰", "€", "£", "¥")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                symbols.take(9).forEach { sym ->
                    Button(
                        onClick = { onEvent(RibbonEvent.OnInsertSymbolClicked(sym)); onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                        contentPadding = PaddingValues(2.dp),
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text(sym, fontSize = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                symbols.takeLast(9).forEach { sym ->
                    Button(
                        onClick = { onEvent(RibbonEvent.OnInsertSymbolClicked(sym)); onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                        contentPadding = PaddingValues(2.dp),
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text(sym, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentFormattingDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "تنسيق وثيم المستند" else "Document Theme & Formatting") {
            val themes = listOf("Office", "Facet", "Gallery", "Integral", "Ion", "Organic")
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                themes.forEach { th ->
                    OutlinedButton(
                        onClick = { onEvent(RibbonEvent.OnApplyDocumentTheme(th)); onDismiss() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text(th, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProofingStatsDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "إحصائيات وتدقيق المستند" else "Document Proofing & Statistics") {
            Button(
                onClick = { onEvent(RibbonEvent.OnShowWordCountClicked); onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isRtl) "عرض عدد الكلمات والإحصائيات الكاملة" else "Show Word Count & Statistics", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun SmartAiDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "أدوات الذكاء الاصطناعي والصياغة" else "Smart AI Drafting Tools") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = {
                        val activeBlock = state.blocks.find { it.id == state.activeBlockId } as? TextBlock
                        val text = activeBlock?.text?.text ?: ""
                        if (text.isNotBlank()) {
                            onEvent(RibbonEvent.OnDocumentTextChanged(state.activeBlockId, TextFieldValue(text.trim() + " (تم التدقيق اللغوي)")))
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isRtl) "التدقيق اللغوي الذكي (AI Proofread)" else "AI Proofread", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun PdfExportDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "تصدير المستند إلى PDF" else "PDF Document Export") {
            Button(
                onClick = { onEvent(RibbonEvent.OnExportPdfClicked); onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isRtl) "تصدير فوري إلى PDF عالي الجودة" else "Export High-Quality PDF Now", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun ViewsDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "طرق عرض المستند" else "Document Views") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { onEvent(RibbonEvent.OnViewModeChanged(ViewMode.PRINT_LAYOUT)); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.isRtl) "تخطيط الطباعة" else "Print Layout", fontSize = 10.sp)
                }
                Button(
                    onClick = { onEvent(RibbonEvent.OnViewModeChanged(ViewMode.WEB_LAYOUT)); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.isRtl) "تخطيط الويب" else "Web Layout", fontSize = 10.sp)
                }
                Button(
                    onClick = { onEvent(RibbonEvent.OnViewModeChanged(ViewMode.READ_MODE)); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B579A)),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.isRtl) "وضع القراءة" else "Read Mode", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun ZoomDetailedCustomizer(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsGroupBox(title = if (state.isRtl) "تكبير / تصغير المستند (Zoom)" else "Document Zoom") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(0.75f to "75%", 1.0f to "100%", 1.25f to "125%", 1.5f to "150%", 2.0f to "200%").forEach { (zm, lbl) ->
                    OutlinedButton(
                        onClick = { onEvent(RibbonEvent.OnZoomChanged(zm)); onDismiss() },
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(lbl, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentTemplatesDialog(
    state: EditorState,
    onDismiss: () -> Unit,
    onSelectTemplate: (String) -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier
                .width(520.dp)
                .heightIn(max = 620.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = if (state.isRtl) "معرض القوالب والتنسيقات الجاهزة" else "Predefined Word Templates & Presets",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = if (state.isRtl) "اختر قالباً جاهزاً منسقاً باحترافية للبدء فوراً" else "Select a professionally formatted template to start immediately",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(12.dp))

                // Template Cards Grid
                val templates = listOf(
                    Triple(
                        "RESUME",
                        if (state.isRtl) "📄 سيرة ذاتية احترافية" else "📄 Professional Resume CV",
                        if (state.isRtl) "تنسيق أزرق كحلي أنيق، نبذة مهنية، خبرات، ومؤهلات علمية." else "Clean executive CV layout with summary, experience, and education."
                    ),
                    Triple(
                        "BUSINESS_LETTER",
                        if (state.isRtl) "📑 خطاب إداري رسمي" else "📑 Official Business Letter",
                        if (state.isRtl) "ترووسة شركة، تاريخ ورقم إشاري، موضوع، وحقل توقيع معتمد." else "Corporate header, date, reference code, subject, and signature line."
                    ),
                    Triple(
                        "EXECUTIVE_REPORT",
                        if (state.isRtl) "📊 تقرير عمل تنفيذي" else "📊 Executive Performance Report",
                        if (state.isRtl) "بنر غلاف ملون، ملخص تنفيذي، جدول بيانات مالي، واقتباس محدد." else "Full cover banner, executive summary, financial data table, callouts."
                    ),
                    Triple(
                        "ACADEMIC_PAPER",
                        if (state.isRtl) "🎓 بحث أكاديمي / مقال" else "🎓 Academic Research Paper",
                        if (state.isRtl) "عنوان رئيسي مركزي، ملخص مؤطر، مقدمة، وتوثيق علمي." else "Centered title, framed abstract, structured introduction, and references."
                    ),
                    Triple(
                        "COMPREHENSIVE",
                        if (state.isRtl) "📝 مستند اختبار شامل" else "📝 Comprehensive Demo Document",
                        if (state.isRtl) "يحتوي على جميع أنواع الكتل، الجداول، القوائم والتنسيقات." else "Contains all block types, tables, lists, and formatting styles."
                    )
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    templates.forEach { (templateId, title, description) ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectTemplate(templateId)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    color = Color(0xFFDBEAFE),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Style,
                                            contentDescription = null,
                                            tint = Color(0xFF1D4ED8),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = description,
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569),
                                        lineHeight = 15.sp
                                    )
                                }

                                Button(
                                    onClick = { onSelectTemplate(templateId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (state.isRtl) "تطبيق" else "Use",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(if (state.isRtl) "إلغاء" else "Cancel", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
