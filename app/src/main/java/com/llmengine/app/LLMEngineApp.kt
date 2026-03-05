package com.llmengine.app

import android.app.Application

/**
 * Application class for LLM Answer Engine.
 * Initializes app-wide dependencies on startup.
 */
class LLMEngineApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: LLMEngineApp
            private set
    }
}
