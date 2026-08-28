package com.example.presentation.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Save
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
    viewModel: EditorViewModel = viewModel()
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
                // Top App Bar (Desktop style)
                TopAppBar(
                    title = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${state.documentTitle}.docx - Microsoft Word Clone", 
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF185ABD)
                    ),
                    actions = {
                        // Quick Open
                        IconButton(onClick = {
                            openFileLauncher.launch(
                                arrayOf(
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "application/msword",
                                    "*/*"
                                )
                            )
                        }) {
                            Icon(imageVector = Icons.Default.FolderOpen, contentDescription = "Open", tint = Color.White)
                        }

                        // Quick Save
                        IconButton(onClick = {
                            val uri = state.currentUri
                            if (uri != null) {
                                // Overwrite existing file
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                                                DocxEngine.exportDocx(state, outputStream)
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            } else {
                                // Fallback to Save As
                                saveFileLauncher.launch("${state.documentTitle}.docx")
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "Save", tint = Color.White)
                        }
                        
                        // Dynamic Language Toggle (EN / عربي)
                        Button(
                            onClick = { viewModel.processEvent(RibbonEvent.OnLanguageToggled) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF103F91)),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = "Toggle Language", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (state.isRtl) "عربي" else "EN", color = Color.White, fontSize = 12.sp)
                        }
                    }
                )

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
                        } else {
                            viewModel.processEvent(it)
                        }
                    }
                )

                // Multi-Page A4 Document Canvas
                DocumentCanvas(
                    state = state,
                    onEvent = { viewModel.processEvent(it) },
                    modifier = Modifier.weight(1f)
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
                        saveFileLauncher.launch("$filename$format")
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
        }
    }
}
