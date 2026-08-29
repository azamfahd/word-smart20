package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.presentation.editor.EditorViewModel
import com.example.presentation.editor.MainScreen
import com.example.presentation.editor.StartScreen
import com.example.ui.theme.WordEditorTheme

class MainActivity : ComponentActivity() {

    private val editorViewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        com.example.ui.theme.ThemeManager.init(applicationContext)

        val startDestination = if (intent?.data != null) "editor" else "start"
        handleIncomingIntent(intent)

        setContent {
            WordEditorTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("start") {
                            StartScreen(
                                onNewDocument = {
                                    editorViewModel.createNewBlankDocument()
                                    navController.navigate("editor") {
                                        popUpTo("start") { inclusive = true }
                                    }
                                },
                                onNewDocumentFromTemplate = { template ->
                                    editorViewModel.createNewDocumentFromTemplate(
                                        title = template.title,
                                        blocks = template.generateBlocks(),
                                        pageBorder = template.pageBorder,
                                        pageColor = template.pageColor,
                                        pageOrientation = template.pageOrientation,
                                        pageSize = template.pageSize,
                                        pageMargin = template.pageMargin,
                                        pageStripeStyle = template.stripeStyle,
                                        pageAccentColor = template.primaryColor,
                                        pageSecondaryColor = template.secondaryColor
                                    )
                                    navController.navigate("editor") {
                                        popUpTo("start") { inclusive = true }
                                    }
                                },
                                onOpenFile = { uri ->
                                    editorViewModel.loadFromUri(uri, applicationContext)
                                    navController.navigate("editor") {
                                        popUpTo("start") { inclusive = true }
                                    }
                                },
                                onLoadFromCloud = { cloudDoc ->
                                    editorViewModel.processEvent(com.example.presentation.editor.RibbonEvent.OnLoadFromCloud(cloudDoc.id, cloudDoc.dataBase64))
                                    editorViewModel.processEvent(com.example.presentation.editor.RibbonEvent.OnDocumentTitleChanged(cloudDoc.title))
                                    navController.navigate("editor") {
                                        popUpTo("start") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("editor") {
                            MainScreen(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = editorViewModel,
                                onNavigateBack = {
                                    navController.navigate("start") {
                                        popUpTo("editor") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val uri: Uri? = intent?.data
        if (uri != null) {
            if (intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                }
            }
            editorViewModel.loadFromUri(uri, applicationContext)
        }
    }
}
