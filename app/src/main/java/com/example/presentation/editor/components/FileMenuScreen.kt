package com.example.presentation.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.editor.EditorState
import com.example.presentation.editor.RibbonEvent

@Composable
fun FileMenuScreen(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    onExportRequested: (String, String) -> Unit, // filename, format
    onImportRequested: () -> Unit
) {
    if (!state.isFileMenuOpen) return

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = Color.White
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isCompact = maxWidth < 600.dp

            if (isCompact) {
                // Mobile Layout for File Menu
                if (state.showSaveAsDialog) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { onEvent(RibbonEvent.OnDismissSaveAsDialog) }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF2B579A))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.isRtl) "حفظ باسم" else "Save As",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        SaveAsPanel(
                            isCompact = true,
                            isRtl = state.isRtl,
                            onEvent = onEvent,
                            onExportRequested = onExportRequested
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF2B579A))
                    ) {
                        // Mobile Top Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onEvent(RibbonEvent.OnToggleFileMenu) }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.isRtl) "قائمة ملف" else "File Menu",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 8.dp)
                        ) {
                            FileMenuOption(Icons.Default.Info, if (state.isRtl) "معلومات المستند" else "Document Info") { 
                                onEvent(RibbonEvent.OnToggleFileMenu) 
                            }
                            FileMenuOption(Icons.Default.Add, if (state.isRtl) "مستند جديد" else "New Document") { 
                                onEvent(RibbonEvent.OnNewDocument)
                                onEvent(RibbonEvent.OnToggleFileMenu)
                            }
                            FileMenuOption(Icons.Default.FolderOpen, if (state.isRtl) "فتح ملف Word" else "Open Word File") { 
                                onImportRequested() 
                            }
                            FileMenuOption(Icons.Default.CloudUpload, if (state.isRtl) "حفظ في السحابة" else "Save to Cloud") {
                                onEvent(RibbonEvent.OnSaveToCloudClicked)
                                onEvent(RibbonEvent.OnToggleFileMenu)
                            }
                            FileMenuOption(Icons.Default.Save, if (state.isRtl) "حفظ كملف محلي" else "Save Document") { 
                                if (state.currentUri != null) {
                                    onEvent(RibbonEvent.OnSaveDocumentClicked)
                                    onEvent(RibbonEvent.OnToggleFileMenu)
                                } else {
                                    onEvent(RibbonEvent.OnShowSaveAsDialog)
                                }
                            }
                            FileMenuOption(Icons.Default.SaveAs, if (state.isRtl) "حفظ باسم (Word / PDF)" else "Save As (Word / PDF)") { 
                                onEvent(RibbonEvent.OnShowSaveAsDialog) 
                            }
                            FileMenuOption(Icons.Default.PictureAsPdf, if (state.isRtl) "تصدير فوري كـ PDF" else "Quick Export as PDF") { 
                                onEvent(RibbonEvent.OnExportPdfClicked)
                                onEvent(RibbonEvent.OnToggleFileMenu)
                            }
                            FileMenuOption(Icons.Default.Close, if (state.isRtl) "إغلاق القائمة" else "Close Menu") { 
                                onEvent(RibbonEvent.OnToggleFileMenu) 
                            }
                        }
                    }
                }
            } else {
                // Desktop / Tablet Layout (MS Word Backstage style)
                Row(modifier = Modifier.fillMaxSize()) {
                    // Dark Blue Sidebar
                    Column(
                        modifier = Modifier
                            .width(220.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF2B579A)) // MS Word Blue
                            .padding(vertical = 16.dp)
                    ) {
                        // Back arrow
                        IconButton(
                            onClick = { onEvent(RibbonEvent.OnToggleFileMenu) },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        FileMenuOption(Icons.Default.Info, if (state.isRtl) "المعلومات" else "Info") { 
                            onEvent(RibbonEvent.OnToggleFileMenu) 
                        }
                        FileMenuOption(Icons.Default.Add, if (state.isRtl) "جديد" else "New") { 
                            onEvent(RibbonEvent.OnNewDocument)
                            onEvent(RibbonEvent.OnToggleFileMenu)
                        }
                        FileMenuOption(Icons.Default.FolderOpen, if (state.isRtl) "فتح" else "Open") { 
                            onImportRequested() 
                        }
                        FileMenuOption(Icons.Default.CloudUpload, if (state.isRtl) "حفظ في السحابة" else "Save to Cloud") {
                            onEvent(RibbonEvent.OnSaveToCloudClicked)
                            onEvent(RibbonEvent.OnToggleFileMenu)
                        }
                        FileMenuOption(Icons.Default.Save, if (state.isRtl) "حفظ" else "Save") { 
                            if (state.currentUri != null) {
                                onEvent(RibbonEvent.OnSaveDocumentClicked)
                                onEvent(RibbonEvent.OnToggleFileMenu)
                            } else {
                                onEvent(RibbonEvent.OnShowSaveAsDialog)
                            }
                        }
                        FileMenuOption(Icons.Default.SaveAs, if (state.isRtl) "حفظ باسم" else "Save As") { 
                            onEvent(RibbonEvent.OnShowSaveAsDialog) 
                        }
                        FileMenuOption(Icons.Default.PictureAsPdf, if (state.isRtl) "تصدير كـ PDF" else "Export as PDF") { 
                            onEvent(RibbonEvent.OnExportPdfClicked)
                            onEvent(RibbonEvent.OnToggleFileMenu)
                        }
                        FileMenuOption(Icons.Default.Close, if (state.isRtl) "إغلاق" else "Close") { 
                            onEvent(RibbonEvent.OnToggleFileMenu) 
                        }
                    }
                    
                    // Content Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(32.dp)
                    ) {
                        if (state.showSaveAsDialog) {
                            SaveAsPanel(
                                isCompact = false,
                                isRtl = state.isRtl,
                                onEvent = onEvent,
                                onExportRequested = onExportRequested
                            )
                        } else {
                            Column {
                                Text(
                                    text = if (state.isRtl) "معلومات المستند" else "Document Info",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "${state.documentTitle}.docx",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileMenuOption(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveAsPanel(
    isCompact: Boolean = false,
    isRtl: Boolean = true,
    onEvent: (RibbonEvent) -> Unit,
    onExportRequested: (String, String) -> Unit
) {
    var filename by remember { mutableStateOf("Document1") }
    var expanded by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableStateOf(".docx") }
    val formats = listOf(".docx", ".pdf", ".rtf", ".txt", ".odt", ".html")
    
    Column(
        modifier = Modifier
            .fillMaxWidth(if (isCompact) 1f else 0.6f)
            .verticalScroll(rememberScrollState())
    ) {
        if (!isCompact) {
            Text(
                text = if (isRtl) "حفظ باسم" else "Save As",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        OutlinedTextField(
            value = filename,
            onValueChange = { filename = it },
            label = { Text(if (isRtl) "اسم الملف" else "File Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedFormat,
                onValueChange = {},
                readOnly = true,
                label = { Text(if (isRtl) "تنسيق الملف" else "File Format") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                formats.forEach { format ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                when (format) {
                                    ".docx" -> "Word Document (*.docx)"
                                    ".pdf" -> "PDF Document (*.pdf)"
                                    ".rtf" -> "Rich Text Format (*.rtf)"
                                    ".txt" -> "Plain Text (*.txt)"
                                    ".odt" -> "OpenDocument Text (*.odt)"
                                    ".html" -> "Web Page (*.html)"
                                    else -> format
                                }
                            ) 
                        },
                        onClick = {
                            selectedFormat = format
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = { onEvent(RibbonEvent.OnDismissSaveAsDialog) }) {
                Text(if (isRtl) "إلغاء" else "Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onExportRequested(filename, selectedFormat) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185ABD))
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isRtl) "حفظ وتصدير" else "Save & Export")
            }
        }
    }
}
