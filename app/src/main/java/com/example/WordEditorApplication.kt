package com.example

import android.app.Application

class WordEditorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any global dependencies or logging here.
        // e.g., Crashlytics, DI (Hilt/Koin), Timber, etc.
    }
}
