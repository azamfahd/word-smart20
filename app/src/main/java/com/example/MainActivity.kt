package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
        enableEdgeToEdge()

        val startDestination = if (intent?.data != null) "editor" else "start"
        handleIncomingIntent(intent)

        setContent {
            WordEditorTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
                                onOpenFile = { uri ->
                                    editorViewModel.loadFromUri(uri, applicationContext)
                                    navController.navigate("editor") {
                                        popUpTo("start") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("editor") {
                            MainScreen(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = editorViewModel
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
