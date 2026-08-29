package com.example.presentation.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presentation.editor.components.DocumentCanvas
import com.example.presentation.editor.components.FileMenuScreen
import com.example.presentation.editor.components.WordRibbon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // SAF Launcher for Opening Files
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            viewModel.loadFromUri(selectedUri, context)
        }
    }

    val authManager = remember { com.example.presentation.auth.AuthManager(context) }
    val cloudSyncManager = remember { com.example.presentation.cloud.CloudSyncManager() }
    
    LaunchedEffect(state.isSavingToCloud) {
        if (state.isSavingToCloud) {
            val user = authManager.currentUser
            if (user != null) {
                try {
                    val docId = state.cloudDocId ?: java.util.UUID.randomUUID().toString()
                    val bytes = DocxEngine.exportDocxToByteArray(state)
                    cloudSyncManager.saveDocument(
                        userId = user.uid,
                        docId = docId,
                        title = state.documentTitle,
                        docxBytes = bytes
                    )
                    viewModel.processEvent(RibbonEvent.OnCloudDocIdSaved(docId))
                    android.widget.Toast.makeText(context, "تم الحفظ في السحابة بنجاح", android.widget.Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "فشل الحفظ: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            } else {
                android.widget.Toast.makeText(context, "يرجى تسجيل الدخول أولاً للحفظ في السحابة", android.widget.Toast.LENGTH_SHORT).show()
            }
            viewModel.clearSavingToCloudFlag()
        }
    }
    
    // SAF Launcher for Saving Files
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    ) { uri: Uri? ->
        uri?.let { saveUri ->
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openOutputStream(saveUri)?.use { outputStream ->
                            DocxEngine.exportDocx(state, outputStream)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                viewModel.processEvent(RibbonEvent.OnToggleFileMenu)
            }
        }
    }
    
    // Dynamic Localization Toggle (RTL / LTR)
    val layoutDirection = if (state.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE5E9EE))
            ) {
                // Desktop Ribbon UI
                WordRibbon(
                    state = state,
                    onEvent = { 
                        if (it is RibbonEvent.OnSaveDocumentClicked) {
                            val uri = state.currentUri
                            if (uri != null) {
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                                                DocxEngine.exportDocx(state, outputStream)
                                            }
                                        } catch (e: Exception) { e.printStackTrace() }
                                    }
                                }
                            } else saveFileLauncher.launch("${state.documentTitle}.docx")
                        } else if (it is RibbonEvent.OnExportPdfClicked) {
                            viewModel.exportPdf(context)
                        } else {
                            viewModel.processEvent(it)
                        }
                    },
                    onBackClick = onNavigateBack,
                    onOpenFileClick = {
                        openFileLauncher.launch(
                            arrayOf(
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/msword",
                                "*/*"
                            )
                        )
                    },
                    onSaveFileClick = {
                        val uri = state.currentUri
                        if (uri != null) {
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    try {
                                        context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                                            DocxEngine.exportDocx(state, outputStream)
                                        }
                                    } catch (e: Exception) { e.printStackTrace() }
                                }
                            }
                        } else {
                            saveFileLauncher.launch("${state.documentTitle}.docx")
                        }
                    }
                )

                // Microsoft Word Authentic "Protected View" Banner (شريط التحذير الأصفر لمعاينة القالب)
                if (state.isProtectedView) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFF8C4), // Exact Microsoft Word desktop yellow message bar background
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5C158)) // Authentic Office amber border
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "عرض محمي",
                                    tint = Color(0xFF8F6B00), // Authentic Office dark amber icon tint
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (state.isRtl) 
                                        "عرض محمي: تم فتح هذا القالب في وضع المعاينة لحفظ تنسيقات الخطوط وهوامش الورقة والأبعاد. انقر فوق زر تمكين التحرير للبدء بالتعديل والكتابة." 
                                        else "Protected View: This template was opened in preview mode to preserve formatting and margins. Click Enable Editing to start writing.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF323130), // Exact Microsoft Office dark neutral body text color
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Button(
                                onClick = { viewModel.processEvent(RibbonEvent.OnEnableEditing) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White, // Office clean white button on message bar
                                    contentColor = Color(0xFF201F1E) // Office dark text
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8A8886)), // Office subtle border
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(2.dp), // Authentic Office sharp-rounded button
                                modifier = Modifier.height(28.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "تحرير",
                                        tint = Color(0xFF1B5E20), // Office green edit icon accent
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (state.isRtl) "تمكين التحرير" else "Enable Editing",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF201F1E)
                                    )
                                }
                            }
                        }
                    }
                }

                // Multi-Page A4 Document Canvas
                DocumentCanvas(
                    state = state,
                    onEvent = { viewModel.processEvent(it) },
                    modifier = Modifier.weight(1f)
                )

                // Dedicated Office Status Bar (شريط الحالة العُلوِي/السُفلي المخصص)
                EditorStatusBar(
                    state = state,
                    onEvent = { viewModel.processEvent(it) }
                )
            }
            
            // Full-screen Backstage File Menu Overlay
            if (state.isFileMenuOpen) {
                FileMenuScreen(
                    state = state,
                    onEvent = { 
                        if (it is RibbonEvent.OnSaveDocumentClicked) {
                            val uri = state.currentUri
                            if (uri != null) {
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                                                DocxEngine.exportDocx(state, outputStream)
                                            }
                                        } catch (e: Exception) { e.printStackTrace() }
                                    }
                                }
                            } else saveFileLauncher.launch("${state.documentTitle}.docx")
                        } else {
                            viewModel.processEvent(it) 
                        }
                    },
                    onExportRequested = { filename, format ->
                        if (format == ".pdf") {
                            viewModel.exportPdf(context)
                        } else {
                            saveFileLauncher.launch("$filename$format")
                        }
                    },
                    onImportRequested = {
                        openFileLauncher.launch(
                            arrayOf(
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/msword",
                                "*/*"
                            )
                        )
                    }
                )
            }

            if (state.showWordCountDialog) {
                WordCountDialog(
                    state = state,
                    onDismiss = { viewModel.processEvent(RibbonEvent.OnDismissWordCountClicked) }
                )
            }

            if (state.showFindReplaceDialog) {
                FindReplaceDialog(
                    state = state,
                    onDismiss = { viewModel.processEvent(RibbonEvent.OnDismissFindReplaceDialog) },
                    onFindReplace = { findText, replaceText ->
                        viewModel.processEvent(RibbonEvent.OnFindAndReplaceClicked(findText, replaceText))
                    }
                )
            }

            if (state.showExportPdfSuccessDialog) {
                PdfExportSuccessDialog(
                    state = state,
                    onDismiss = { viewModel.processEvent(RibbonEvent.OnDismissExportPdfDialog) }
                )
            }

            state.activeGroupDetailsDialog?.let { groupName ->
                GroupDetailsDialog(
                    groupName = groupName,
                    state = state,
                    onEvent = { viewModel.processEvent(it) },
                    onDismiss = { viewModel.processEvent(RibbonEvent.OnDismissGroupDetails) }
                )
            }
        }
    }
}

@Composable
fun EditorStatusBar(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp),
        color = Color(0xFFF1F5F9), // Light Office Slate Gray
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Document Stats & Info (Left section)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (state.isRtl) "صفحة 1 | ${state.pageSize.name}" else "Page 1 | ${state.pageSize.name}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = if (state.isRtl) "العرض: ${(state.zoomScale * 100).toInt()}%" else "Zoom: ${(state.zoomScale * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Medium
                )
            }

            // Dedicated Zoom & View Mode Controls (Right section)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // View Mode Quick Toggles (Windows Office Style)
                IconButton(
                    onClick = { onEvent(RibbonEvent.OnViewModeChanged(ViewMode.PRINT_LAYOUT)) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewAgenda,
                        contentDescription = "Print Layout",
                        tint = if (state.viewMode == ViewMode.PRINT_LAYOUT) MaterialTheme.colorScheme.primary else Color(0xFF64748B),
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = { onEvent(RibbonEvent.OnViewModeChanged(ViewMode.WEB_LAYOUT)) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Web Layout",
                        tint = if (state.viewMode == ViewMode.WEB_LAYOUT) MaterialTheme.colorScheme.primary else Color(0xFF64748B),
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = { onEvent(RibbonEvent.OnViewModeChanged(ViewMode.READ_MODE)) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Read Mode",
                        tint = if (state.viewMode == ViewMode.READ_MODE) MaterialTheme.colorScheme.primary else Color(0xFF64748B),
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { onEvent(RibbonEvent.OnZoomChanged(state.zoomScale - 0.15f)) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(15.dp)
                    )
                }

                Text(
                    text = "${(state.zoomScale * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                IconButton(
                    onClick = { onEvent(RibbonEvent.OnZoomChanged(state.zoomScale + 0.15f)) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedButton(
                    onClick = { onEvent(RibbonEvent.OnZoomChanged(1.0f)) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier.height(22.dp),
                    shape = RoundedCornerShape(11.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FitScreen,
                        contentDescription = "100%",
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "100%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun WordCountDialog(
    state: EditorState,
    onDismiss: () -> Unit
) {
    val totalWords = remember(state.blocks) {
        state.blocks.filterIsInstance<TextBlock>().sumOf { block ->
            val txt = block.text.text.trim()
            if (txt.isEmpty()) 0 else txt.split(Regex("\\s+")).size
        }
    }
    val totalChars = remember(state.blocks) {
        state.blocks.filterIsInstance<TextBlock>().sumOf { it.text.text.length }
    }
    val totalParagraphs = remember(state.blocks) {
        state.blocks.count { it is TextBlock }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (state.isRtl) "إحصائيات المستند والكلمات" else "Document & Word Statistics", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatRow(label = if (state.isRtl) "عدد الكلمات الكلي:" else "Total Words:", value = "$totalWords")
                StatRow(label = if (state.isRtl) "عدد الأحرف (مع الفراغات):" else "Characters (with spaces):", value = "$totalChars")
                StatRow(label = if (state.isRtl) "عدد الفقرات:" else "Paragraphs:", value = "$totalParagraphs")
                StatRow(label = if (state.isRtl) "عدد الجداول والأشكال:" else "Tables & Shapes:", value = "${state.blocks.count { it !is TextBlock }}")
                StatRow(label = if (state.isRtl) "نمط الصفحة الحالي:" else "Page Layout Size:", value = state.pageSize.name)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (state.isRtl) "موافق" else "OK")
            }
        }
    )
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = Color(0xFF475569))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
    }
}

@Composable
fun PdfExportSuccessDialog(
    state: EditorState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (state.isRtl) "تم تصدير ملف PDF بنجاح!" else "PDF Exported Successfully!", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text(
                text = if (state.isRtl) 
                    "تم إنشاء مستند PDF عالي الجودة وحفظه. يمكنك مشاركته أو استعراضه فوراً عبر أي قارئ ملفات PDF."
                else 
                    "A high-quality PDF document has been generated and saved. You can share or view it immediately.",
                fontSize = 13.sp,
                color = Color(0xFF334155)
            )
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val uri = state.exportedPdfUri
                        if (uri != null) {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "مشاركة PDF"))
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text(if (state.isRtl) "مشاركة PDF" else "Share PDF")
                }

                TextButton(onClick = onDismiss) {
                    Text(if (state.isRtl) "إغلاق" else "Close")
                }
            }
        }
    )
}

@Composable
fun FindReplaceDialog(
    state: EditorState,
    onDismiss: () -> Unit,
    onFindReplace: (findText: String, replaceText: String) -> Unit
) {
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.FindReplace, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (state.isRtl) "البحث والاستبدال" else "Find & Replace", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = findText,
                    onValueChange = { findText = it },
                    label = { Text(if (state.isRtl) "البحث عن النص" else "Find What") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = replaceText,
                    onValueChange = { replaceText = it },
                    label = { Text(if (state.isRtl) "الاستبدال بـ" else "Replace With") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onFindReplace(findText, replaceText)
                    },
                    enabled = findText.isNotEmpty()
                ) {
                    Text(if (state.isRtl) "استبدال الكل" else "Replace All")
                }

                TextButton(onClick = onDismiss) {
                    Text(if (state.isRtl) "إلغاء" else "Cancel")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsDialog(
    groupName: String,
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (groupName) {
                        "Font" -> Icons.Default.FormatSize
                        "Paragraph" -> Icons.Default.FormatAlignLeft
                        "Clipboard" -> Icons.Default.ContentPaste
                        "Undo" -> Icons.Default.Undo
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
                        else -> Icons.Default.Settings
                    },
                    contentDescription = null,
                    tint = Color(0xFF185ABD),
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = if (state.isRtl) "تفاصيل مجموعة $groupName" else "$groupName Group Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF185ABD)
                )
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    when (groupName) {
                        "Font" -> {
                            // Section: Latin Font and Font Styles (Authentic Word Layout)
                            Text(
                                if (state.isRtl) "تنسيقات الخط (Font Properties):" else "Font Formatting Properties:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B)
                            )

                            // Font Family Selector Scrollable List Box
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = if (state.isRtl) "الخط (Font):" else "Font:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                
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
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
                                        .background(Color.White)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        fontFamilies.forEach { font ->
                                            val isSelected = state.fontFamily == font
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(if (isSelected) Color(0xFF0078D4) else Color.Transparent)
                                                    .clickable { onEvent(RibbonEvent.OnFontFamilyChanged(font)) }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = font, 
                                                    fontSize = 14.sp,
                                                    fontFamily = com.example.presentation.editor.components.getPreviewFontFamily(font),
                                                    color = if (isSelected) Color.White else Color(0xFF1E293B)
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                Text(
                                                    text = fontDescriptions[font] ?: "",
                                                    fontSize = 10.sp,
                                                    color = if (isSelected) Color(0xFFE2E8F0) else Color(0xFF94A3B8)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Font Style & Size Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Font Style (نمط الخط)
                                Column(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                        .background(Color.White)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = if (state.isRtl) "نمط الخط (Style):" else "Font Style:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(1.dp, if (state.isBold) Color(0xFF185ABD) else Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                                                .background(if (state.isBold) Color(0xFFEFF6FF) else Color.Transparent)
                                                .clickable { onEvent(RibbonEvent.OnBoldClicked) }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(if (state.isRtl) "عريض" else "Bold", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(1.dp, if (state.isItalic) Color(0xFF185ABD) else Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                                                .background(if (state.isItalic) Color(0xFFEFF6FF) else Color.Transparent)
                                                .clickable { onEvent(RibbonEvent.OnItalicClicked) }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(if (state.isRtl) "مائل" else "Italic", fontSize = 11.sp, fontStyle = FontStyle.Italic)
                                        }
                                    }
                                }

                                // Font Size (حجم الخط بمقاييس دقيقة)
                                Column(
                                    modifier = Modifier
                                        .weight(0.8f)
                                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                        .background(Color.White)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = if (state.isRtl) "الحجم (Size):" else "Size:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        IconButton(
                                            onClick = { onEvent(RibbonEvent.OnDecreaseFontSizeClicked) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, null, modifier = Modifier.size(12.dp))
                                        }
                                        Text(text = "${state.fontSize}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        IconButton(
                                            onClick = { onEvent(RibbonEvent.OnIncreaseFontSizeClicked) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Add, null, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }

                            // Effects (تأثيرات إضافية)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = if (state.isRtl) "التأثيرات (Effects):" else "Effects:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onEvent(RibbonEvent.OnUnderlineClicked) }
                                    ) {
                                        Checkbox(checked = state.isUnderline, onCheckedChange = { onEvent(RibbonEvent.OnUnderlineClicked) })
                                        Text(if (state.isRtl) "تسطير (Underline)" else "Underline", fontSize = 11.sp)
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onEvent(RibbonEvent.OnStrikethroughClicked) }
                                    ) {
                                        Checkbox(checked = state.isStrikethrough, onCheckedChange = { onEvent(RibbonEvent.OnStrikethroughClicked) })
                                        Text(if (state.isRtl) "يتوسطه خط" else "Strikethrough", fontSize = 11.sp)
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onEvent(RibbonEvent.OnSuperscriptClicked) }
                                    ) {
                                        Checkbox(checked = state.isSuperscript, onCheckedChange = { onEvent(RibbonEvent.OnSuperscriptClicked) })
                                        Text(if (state.isRtl) "مرتفع (Superscript)" else "Superscript", fontSize = 11.sp)
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onEvent(RibbonEvent.OnSubscriptClicked) }
                                    ) {
                                        Checkbox(checked = state.isSubscript, onCheckedChange = { onEvent(RibbonEvent.OnSubscriptClicked) })
                                        Text(if (state.isRtl) "منخفض (Subscript)" else "Subscript", fontSize = 11.sp)
                                    }
                                }
                            }

                            // Theme Colors Palette (لوحة الألوان الرسمية لمايكروسوفت وورد)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = if (state.isRtl) "لون الخط (Font Color):" else "Font Color:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val wordColors = listOf(
                                        Color(0xFF000000), // Black
                                        Color(0xFF185ABD), // Word Blue
                                        Color(0xFFC00000), // Dark Red
                                        Color(0xFF00B050), // Green
                                        Color(0xFF7030A0), // Purple
                                        Color(0xFFFFC000)  // Gold Orange
                                    )
                                    wordColors.forEach { color ->
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .border(
                                                    width = if (state.textColor == color) 2.dp else 1.dp,
                                                    color = if (state.textColor == color) Color(0xFF185ABD) else Color(0xFFCBD5E1),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .background(color, RoundedCornerShape(12.dp))
                                                .clickable { onEvent(RibbonEvent.OnTextColorChanged(color)) }
                                        )
                                    }
                                }
                            }

                            // Preview Box: Authentic Word Double Bordered Frame (مربع المعاينة الفعلي لوورد)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = if (state.isRtl) "المعاينة (Preview):" else "Preview:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFE2E8F0))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (state.isRtl) "أبجد هوز - مستند مايكروسوفت وورد" else "AaBbCcYyZz - Microsoft Word Preview",
                                        fontFamily = when (state.fontFamily) {
                                            "Cairo" -> androidx.compose.ui.text.font.FontFamily.SansSerif
                                            "Amiri" -> androidx.compose.ui.text.font.FontFamily.Serif
                                            "Courier New" -> androidx.compose.ui.text.font.FontFamily.Monospace
                                            else -> androidx.compose.ui.text.font.FontFamily.Default
                                        },
                                        fontSize = state.fontSize.sp,
                                        fontWeight = if (state.isBold) FontWeight.Bold else FontWeight.Normal,
                                        fontStyle = if (state.isItalic) FontStyle.Italic else FontStyle.Normal,
                                        textDecoration = if (state.isUnderline) TextDecoration.Underline else TextDecoration.None,
                                        color = state.textColor,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        "Paragraph" -> {
                            // Section: Indents and Spacing (مواءمة الفقرات والسطور لوورد)
                            Text(
                                if (state.isRtl) "التباعد والمحاذاة (Indents & Spacing):" else "Indents and Spacing Details:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B)
                            )

                            // Alignments Group Box
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = if (state.isRtl) "المحاذاة العامة (General Alignment):" else "General Alignment:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val alignments = listOf(
                                        Triple(TextAlignment.LEFT, Icons.Default.FormatAlignLeft, if (state.isRtl) "يسار" else "Left"),
                                        Triple(TextAlignment.CENTER, Icons.Default.FormatAlignCenter, if (state.isRtl) "وسط" else "Center"),
                                        Triple(TextAlignment.RIGHT, Icons.Default.FormatAlignRight, if (state.isRtl) "يمين" else "Right"),
                                        Triple(TextAlignment.JUSTIFY, Icons.Default.FormatAlignJustify, if (state.isRtl) "ضبط" else "Justify")
                                    )
                                    alignments.forEach { (align, icon, name) ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (state.alignment == align) Color(0xFF185ABD) else Color(0xFFE2E8F0),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .background(if (state.alignment == align) Color(0xFFEFF6FF) else Color.Transparent)
                                                .clickable { onEvent(RibbonEvent.OnAlignmentChanged(align)) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = if (state.alignment == align) Color(0xFF185ABD) else Color.DarkGray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = name,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (state.alignment == align) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (state.alignment == align) Color(0xFF185ABD) else Color.Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Spacing Settings Box (تباعد الأسطر والفقرات)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Spacing Choices
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                        .background(Color.White)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = if (state.isRtl) "تباعد الأسطر (Line Spacing):" else "Line Spacing:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        val spacings = listOf(1.0f, 1.15f, 1.5f, 2.0f)
                                        spacings.forEach { spacing ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (state.lineSpacing == spacing) Color(0xFF185ABD) else Color(0xFFE2E8F0),
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .background(if (state.lineSpacing == spacing) Color(0xFFEFF6FF) else Color.Transparent)
                                                    .clickable { onEvent(RibbonEvent.OnLineSpacingChanged(spacing)) }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${spacing}x",
                                                    fontSize = 10.sp,
                                                    fontWeight = if (state.lineSpacing == spacing) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (state.lineSpacing == spacing) Color(0xFF185ABD) else Color.Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Indentation / Direction
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = if (state.isRtl) "اتجاه النص ونظام الأرقام (Direction & Numerals):" else "Text Direction & Numerals:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onEvent(RibbonEvent.OnNumeralSystemChanged(NumeralSystem.WESTERN)) }
                                    ) {
                                        RadioButton(
                                            selected = state.numeralSystem == NumeralSystem.WESTERN,
                                            onClick = { onEvent(RibbonEvent.OnNumeralSystemChanged(NumeralSystem.WESTERN)) }
                                        )
                                        Text(if (state.isRtl) "أرقام عربية (1, 2, 3)" else "Western (1, 2, 3)", fontSize = 11.sp)
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onEvent(RibbonEvent.OnNumeralSystemChanged(NumeralSystem.ARABIC)) }
                                    ) {
                                        RadioButton(
                                            selected = state.numeralSystem == NumeralSystem.ARABIC,
                                            onClick = { onEvent(RibbonEvent.OnNumeralSystemChanged(NumeralSystem.ARABIC)) }
                                        )
                                        Text(if (state.isRtl) "أرقام هندية (١, ٢, ٣)" else "Hindi (١, ٢, ٣)", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        "Clipboard" -> {
                            Text(
                                if (state.isRtl) "حافظة ميكروسوفت وورد الذكية:" else "Microsoft Word Clipboard History:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                if (state.isRtl) "اضغط على أي عنصر لنسخه وإدراجه مباشرة في المحرر:" else "Click any snippet below to insert it directly into your document:",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            val snippets = if (state.isRtl) {
                                listOf(
                                    "التاريخ: ${System.currentTimeMillis()}",
                                    "السادة أعضاء مجلس الإدارة المحترمين،",
                                    "يرجى العلم بأنه تم اعتماد القرار رسمياً.",
                                    "الموضوع: تقرير العمل الدوري والشامل",
                                    "شاكرين لكم حسن تعاونكم الدائم معنا."
                                )
                            } else {
                                listOf(
                                    "Subject: Executive Weekly Status Report",
                                    "Best regards,",
                                    "Thank you for your continuous support and collaboration.",
                                    "Date: August 28, 2026",
                                    "Draft Version 1.0 - Confidiential"
                                )
                            }

                            snippets.forEach { snippet ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onEvent(RibbonEvent.OnInsertCalloutClicked("", snippet))
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = snippet, fontSize = 12.sp, maxLines = 1)
                                    }
                                }
                            }
                        }

                        "Undo" -> {
                            Text(
                                if (state.isRtl) "سجل عمليات التراجع والإعادة:" else "Undo & Redo State History Log:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { onEvent(RibbonEvent.OnUndoClicked) },
                                    enabled = state.canUndo,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185ABD))
                                ) {
                                    Icon(imageVector = Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (state.isRtl) "تراجع" else "Undo")
                                }

                                Button(
                                    onClick = { onEvent(RibbonEvent.OnRedoClicked) },
                                    enabled = state.canRedo,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185ABD))
                                ) {
                                    Icon(imageVector = Icons.Default.Redo, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (state.isRtl) "إعادة" else "Redo")
                                }
                            }

                            Text(
                                text = if (state.isRtl) "تسمح لك الحافظة بالتراجع عن آخر 50 عملية قمت بها في المستند بأمان تام وبشكل فوري." else "The Word state manager allows you to roll back the last 50 edits securely.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        "Styles" -> {
                            Text(
                                if (state.isRtl) "تطبيق أنماط ميكروسوفت السريعة:" else "Apply Predefined Heading Styles:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            val styles = listOf(
                                "Title" to if (state.isRtl) "عنوان رئيسي عريض (Title)" else "Title",
                                "Subtitle" to if (state.isRtl) "عنوان فرعي جانبي (Subtitle)" else "Subtitle",
                                "Heading 1" to if (state.isRtl) "ترويسة رقم 1 (Heading 1)" else "Heading 1",
                                "Heading 2" to if (state.isRtl) "ترويسة رقم 2 (Heading 2)" else "Heading 2",
                                "Heading 3" to if (state.isRtl) "ترويسة رقم 3 (Heading 3)" else "Heading 3",
                                "Normal" to if (state.isRtl) "نص عادي افتراضي (Normal)" else "Normal"
                            )

                            styles.forEach { (styleKey, styleLabel) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onEvent(RibbonEvent.OnApplyHeadingStyle(styleKey))
                                            onDismiss()
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Title, contentDescription = null, tint = Color(0xFF185ABD))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = styleLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        "Editing" -> {
                            Text(
                                if (state.isRtl) "خيارات البحث والاستبدال المتقدمة:" else "Advanced Find & Replace Settings:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Button(
                                onClick = {
                                    onDismiss()
                                    onEvent(RibbonEvent.OnShowFindReplaceDialog)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185ABD))
                            ) {
                                Icon(imageVector = Icons.Default.FindReplace, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isRtl) "فتح مربع البحث والاستبدال" else "Open Find & Replace Box")
                            }

                            Text(
                                text = if (state.isRtl) "البحث السريع واستبدال المصطلحات يحافظ على اتساق المستند بالكامل بنقرة واحدة." else "Quick find & replace helps keep your formatting and terms consistent throughout the document.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        "Pages" -> {
                            Text(
                                if (state.isRtl) "أدوات إدراج وتحكم بالصفحات:" else "Insert Pages & Boundaries Control:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = {
                                    onEvent(RibbonEvent.OnInsertPageBreakClicked)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.InsertPageBreak, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isRtl) "إدراج فاصل صفحات فوري" else "Insert Immediate Page Break")
                            }
                        }

                        "Tables" -> {
                            Text(
                                if (state.isRtl) "معالج إدراج الجداول الاحترافية:" else "Insert Professional Tables Builder:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            var tableRows by remember { mutableStateOf(3f) }
                            var tableCols by remember { mutableStateOf(3f) }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(if (state.isRtl) "عدد الصفوف: ${tableRows.toInt()}" else "Rows: ${tableRows.toInt()}", fontSize = 13.sp)
                                Slider(
                                    value = tableRows,
                                    onValueChange = { tableRows = it },
                                    valueRange = 1f..10f,
                                    steps = 8
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(if (state.isRtl) "عدد الأعمدة: ${tableCols.toInt()}" else "Columns: ${tableCols.toInt()}", fontSize = 13.sp)
                                Slider(
                                    value = tableCols,
                                    onValueChange = { tableCols = it },
                                    valueRange = 1f..10f,
                                    steps = 8
                                )
                            }

                            Button(
                                onClick = {
                                    onEvent(RibbonEvent.OnInsertTableClicked(tableRows.toInt(), tableCols.toInt()))
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41))
                            ) {
                                Icon(imageVector = Icons.Default.TableChart, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isRtl) "إدراج جدول ${tableRows.toInt()}×${tableCols.toInt()} الآن" else "Insert Table ${tableRows.toInt()}x${tableCols.toInt()} Now")
                            }
                        }

                        "Illustrations" -> {
                            Text(
                                if (state.isRtl) "إدراج أشكال ذكية ورسوم توضيحية:" else "Insert Illustrations & Shapes:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            val shapes = listOf(
                                ShapeType.RECTANGLE to if (state.isRtl) "مستطيل (Rectangle)" else "Rectangle",
                                ShapeType.OVAL to if (state.isRtl) "شكل بيضاوي (Oval)" else "Oval",
                                ShapeType.STAR to if (state.isRtl) "نجمة تميز (Star)" else "Star",
                                ShapeType.ARROW to if (state.isRtl) "سهم توجيهي (Arrow)" else "Arrow"
                            )

                            shapes.forEach { (type, label) ->
                                Button(
                                    onClick = {
                                        onEvent(RibbonEvent.OnInsertShapeClicked(type))
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color.Black)
                                ) {
                                    Icon(imageVector = Icons.Default.Category, contentDescription = null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = label, fontSize = 13.sp)
                                }
                            }
                        }

                        "Decorations & Banners" -> {
                            Text(
                                if (state.isRtl) "إدراج لافتات تزيينية ومربعات نصوص:" else "Decorations & Informational Banners:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Button(
                                onClick = {
                                    onEvent(RibbonEvent.OnInsertBannerClicked("ترويسة المستند الرسمية", "وثيقة معتمدة وموثقة"))
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.WebAsset, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isRtl) "إدراج لافتة (Banner) عريضة" else "Insert Wide Title Banner")
                            }

                            Button(
                                onClick = {
                                    onEvent(RibbonEvent.OnInsertCalloutClicked("تنبيه هام جداً", "يرجى التحقق من صحة وصلاحية المستند قبل مشاركته."))
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
                            ) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isRtl) "إدراج مربع تنبيه (Callout Box)" else "Insert Callout Alert Box")
                            }

                            Button(
                                onClick = {
                                    onEvent(RibbonEvent.OnInsertDividerClicked)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0), contentColor = Color.Black)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isRtl) "إدراج خط فاصل مرن" else "Insert Section Divider Line")
                            }
                        }

                        "Header & Footer" -> {
                            Text(
                                if (state.isRtl) "أدوات ترويسة وتذييل الصفحات:" else "Headers, Footers & Margins Tools:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Button(
                                onClick = {
                                    onEvent(RibbonEvent.OnToggleHeaderFooterMode)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.BorderTop, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isRtl) "التبديل إلى ترويسة/تذييل المستند" else "Toggle Document Header / Footer View")
                            }

                            Button(
                                onClick = {
                                    onEvent(RibbonEvent.OnInsertPageNumberClicked)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41))
                            ) {
                                Icon(imageVector = Icons.Default.BorderBottom, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isRtl) "إدراج ترقيم تلقائي للصفحات" else "Insert Auto Page Numbers")
                            }
                        }

                        "Symbols & Math" -> {
                            Text(
                                if (state.isRtl) "إدراج رموز رياضية ولغات مخصصة:" else "Insert Math Symbols & Special Glyphs:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            val mathSymbols = listOf(
                                "Σ", "Ω", "π", "μ", "θ", "±", "÷", "×",
                                "≠", "≤", "≥", "∞", "√", "∫", "α", "β"
                            )

                            // Display as a beautiful layout
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (i in 0 until 4) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        for (j in 0 until 4) {
                                            val sym = mathSymbols[i * 4 + j]
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFF8FAFC))
                                                    .clickable {
                                                        onEvent(RibbonEvent.OnInsertSymbolClicked(sym))
                                                        onDismiss()
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = sym, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "Document Formatting" -> {
                            Text(
                                if (state.isRtl) "خيارات التخطيط والسمات الشاملة:" else "Document Global Themes & Formatting:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            val themes = listOf(
                                "Classic Word" to "Classic Blue",
                                "Corporate Emerald" to "Olive Green",
                                "Crimson Velvet" to "Crimson Red",
                                "Warm Orange" to "Warm Orange"
                            )

                            themes.forEach { (themeKey, label) ->
                                Button(
                                    onClick = {
                                        onEvent(RibbonEvent.OnApplyDocumentTheme(themeKey))
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color.Black)
                                ) {
                                    Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = Color(0xFF185ABD))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = label, fontSize = 13.sp)
                                }
                            }
                        }

                        "Page Background" -> {
                            Text(
                                if (state.isRtl) "إدراج علامة مائية وخلفية الصفحة:" else "Watermarks & Canvas Background Color:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            var watermarkInput by remember { mutableStateOf(state.watermarkText) }

                            OutlinedTextField(
                                value = watermarkInput,
                                onValueChange = {
                                    watermarkInput = it
                                    onEvent(RibbonEvent.OnWatermarkChanged(it))
                                },
                                label = { Text(if (state.isRtl) "نص العلامة المائية" else "Watermark Text") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val colors = listOf(Color.White, Color(0xFFF1F5F9), Color(0xFFFEF3C7), Color(0xFFECFDF5), Color(0xFFEFF6FF))
                                colors.forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .border(
                                                width = if (state.pageColor == color) 2.dp else 1.dp,
                                                color = if (state.pageColor == color) Color(0xFF185ABD) else Color.Gray,
                                                shape = RoundedCornerShape(18.dp)
                                            )
                                            .background(color, RoundedCornerShape(18.dp))
                                            .clickable { onEvent(RibbonEvent.OnPageColorChanged(color)) }
                                    )
                                }
                            }
                        }

                        "Proofing & Stats" -> {
                            Text(
                                if (state.isRtl) "تحليل وإحصائيات المستند بالكامل:" else "Complete Document Statistics:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(if (state.isRtl) "عدد الكلمات:" else "Words Count:", fontWeight = FontWeight.Medium)
                                        Text("${state.blocks.sumOf { if (it is TextBlock) it.text.text.split("\\s+".toRegex()).size else 0 }}", fontWeight = FontWeight.Bold)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(if (state.isRtl) "عدد الحروف:" else "Characters Count:", fontWeight = FontWeight.Medium)
                                        Text("${state.blocks.sumOf { if (it is TextBlock) it.text.text.length else 0 }}", fontWeight = FontWeight.Bold)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(if (state.isRtl) "عدد الفقرات (Blocks):" else "Paragraph Blocks:", fontWeight = FontWeight.Medium)
                                        Text("${state.blocks.size}", fontWeight = FontWeight.Bold)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(if (state.isRtl) "وقت القراءة المتوقع:" else "Estimated Read Time:", fontWeight = FontWeight.Medium)
                                        Text(if (state.isRtl) "دقيقة واحدة" else "1 minute", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        "Smart AI Tools" -> {
                            Text(
                                if (state.isRtl) "أدوات ميكروسوفت كوبايلوت للذكاء الاصطناعي:" else "Microsoft Copilot AI Enhancements:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            var aiPromptInput by remember { mutableStateOf("") }

                            OutlinedTextField(
                                value = aiPromptInput,
                                onValueChange = { aiPromptInput = it },
                                label = { Text(if (state.isRtl) "اطلب من الذكاء الاصطناعي إعادة صياغة أو كتابة فقرة..." else "Ask AI Copilot to write or rewrite...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )

                            Button(
                                onClick = {
                                    onDismiss()
                                    onEvent(RibbonEvent.OnShowWordCountClicked) // placeholder
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isRtl) "توليد بالذكاء الاصطناعي" else "Generate with Copilot AI")
                            }
                        }

                        "PDF Export" -> {
                            Text(
                                if (state.isRtl) "تفضيلات تصدير المستند كـ PDF:" else "Export Document to PDF Preferences:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Button(
                                onClick = {
                                    onEvent(RibbonEvent.OnExportPdfClicked)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185ABD))
                            ) {
                                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isRtl) "البدء في تصدير PDF الآن" else "Start Exporting PDF Now")
                            }
                        }

                        else -> {
                            Text(
                                text = if (state.isRtl) "تفاصيل المجموعة المحددة ستعرض إعدادات متقدمة مخصصة." else "Selected group details will showcase advanced customizable configurations.",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185ABD))
            ) {
                Text(if (state.isRtl) "موافق" else "OK")
            }
        }
    )
}

