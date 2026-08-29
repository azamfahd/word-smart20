package com.example

import android.app.Application
import android.util.Log
import com.example.presentation.editor.font.FontEngine
import com.google.firebase.FirebaseApp

class WordEditorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        try {
            FontEngine.initialize(this)
        } catch (e: Exception) {
            Log.e("FontEngineInit", "FontEngine initialization error", e)
        }

        try {
            val apps = FirebaseApp.getApps(this)
            if (apps.isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            Log.d("FirebaseInit", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("FirebaseInit", "Firebase could not be initialized. Cloud features will be disabled. Make sure google-services.json is present.", e)
        }
    }
}
