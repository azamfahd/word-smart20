package com.example.presentation.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Dark Blue Sidebar
            Column(
                modifier = Modifier
                    .width(200.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF2B579A)) // MS Word Blue
                    .padding(vertical = 16.dp)
            ) {
                // Back arrow
                IconButton(onClick = { onEvent(RibbonEvent.OnToggleFileMenu) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                FileMenuOption(Icons.Default.Info, "المعلومات") { onEvent(RibbonEvent.OnToggleFileMenu) }
                FileMenuOption(Icons.Default.Add, "جديد") { 
                    onEvent(RibbonEvent.OnNewDocument)
                    onEvent(RibbonEvent.OnToggleFileMenu)
                }
                FileMenuOption(Icons.Default.FolderOpen, "فتح") { onImportRequested() }
                FileMenuOption(Icons.Default.CloudUpload, "حفظ في السحابة") {
                    onEvent(RibbonEvent.OnSaveToCloudClicked)
                    onEvent(RibbonEvent.OnToggleFileMenu)
                }
                FileMenuOption(Icons.Default.Save, "حفظ كملف محلي") { 
                    if (state.currentUri != null) {
                        onEvent(RibbonEvent.OnSaveDocumentClicked)
                        onEvent(RibbonEvent.OnToggleFileMenu)
                    } else {
                        onEvent(RibbonEvent.OnShowSaveAsDialog)
                    }
                }
                FileMenuOption(Icons.Default.SaveAs, "حفظ باسم") { onEvent(RibbonEvent.OnShowSaveAsDialog) }
                FileMenuOption(Icons.Default.Print, "طباعة") { onEvent(RibbonEvent.OnToggleFileMenu) }
                FileMenuOption(Icons.Default.Close, "إغلاق") { onEvent(RibbonEvent.OnToggleFileMenu) }
            }
            
            // Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(32.dp)
            ) {
                if (state.showSaveAsDialog) {
                    SaveAsPanel(onEvent = onEvent, onExportRequested = onExportRequested)
                } else {
                    Text("معلومات المستند", style = MaterialTheme.typography.headlineMedium)
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
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = Color.White, fontSize = 16.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveAsPanel(
    onEvent: (RibbonEvent) -> Unit,
    onExportRequested: (String, String) -> Unit
) {
    var filename by remember { mutableStateOf("Document1") }
    var expanded by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableStateOf(".docx") }
    val formats = listOf(".docx", ".pdf", ".rtf", ".txt", ".odt", ".html")
    
    Column(modifier = Modifier.fillMaxWidth(0.6f)) {
        Text("حفظ باسم", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = filename,
            onValueChange = { filename = it },
            label = { Text("اسم الملف") },
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
                label = { Text("التنسيق") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                formats.forEach { format ->
                    DropdownMenuItem(
                        text = { Text(format) },
                        onClick = {
                            selectedFormat = format
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { onEvent(RibbonEvent.OnDismissSaveAsDialog) }) {
                Text("إلغاء")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onExportRequested(filename, selectedFormat) }) {
                Text("تصفح وحفظ")
            }
        }
    }
}
