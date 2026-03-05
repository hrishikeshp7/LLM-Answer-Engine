package com.llmengine.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.llmengine.app.data.model.AppSettings
import com.llmengine.app.data.model.MemoryMode

/**
 * Manages secure storage of sensitive data (API keys) and app settings.
 * Uses EncryptedSharedPreferences for API key storage.
 */
class SecurePreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        SECURE_PREFS_FILE,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val settingsPrefs: SharedPreferences =
        context.getSharedPreferences(SETTINGS_PREFS_FILE, Context.MODE_PRIVATE)

    // --- Brave API Key ---

    fun saveBraveApiKey(apiKey: String) {
        securePrefs.edit().putString(KEY_BRAVE_API, apiKey).apply()
    }

    fun getBraveApiKey(): String? {
        return securePrefs.getString(KEY_BRAVE_API, null)
    }

    fun clearBraveApiKey() {
        securePrefs.edit().remove(KEY_BRAVE_API).apply()
    }

    fun hasBraveApiKey(): Boolean {
        return !getBraveApiKey().isNullOrBlank()
    }

    // --- App Settings ---

    fun saveSettings(settings: AppSettings) {
        settingsPrefs.edit().apply {
            putInt(KEY_MAX_TOKENS, settings.maxTokens)
            putFloat(KEY_TEMPERATURE, settings.temperature)
            putInt(KEY_CPU_THREADS, settings.cpuThreads)
            putString(KEY_MEMORY_MODE, settings.memoryMode.name)
            putBoolean(KEY_WEB_SEARCH, settings.webSearchEnabled)
            apply()
        }
    }

    fun loadSettings(): AppSettings {
        return AppSettings(
            maxTokens = settingsPrefs.getInt(KEY_MAX_TOKENS, 512),
            temperature = settingsPrefs.getFloat(KEY_TEMPERATURE, 0.7f),
            cpuThreads = settingsPrefs.getInt(KEY_CPU_THREADS, 4),
            memoryMode = try {
                MemoryMode.valueOf(settingsPrefs.getString(KEY_MEMORY_MODE, MemoryMode.BALANCED.name)!!)
            } catch (_: Exception) {
                MemoryMode.BALANCED
            },
            webSearchEnabled = settingsPrefs.getBoolean(KEY_WEB_SEARCH, false)
        )
    }

    // --- Setup state ---

    fun isSetupComplete(): Boolean {
        return settingsPrefs.getBoolean(KEY_SETUP_COMPLETE, false)
    }

    fun setSetupComplete(complete: Boolean) {
        settingsPrefs.edit().putBoolean(KEY_SETUP_COMPLETE, complete).apply()
    }

    companion object {
        private const val SECURE_PREFS_FILE = "llm_engine_secure_prefs"
        private const val SETTINGS_PREFS_FILE = "llm_engine_settings"
        private const val KEY_BRAVE_API = "brave_api_key"
        private const val KEY_MAX_TOKENS = "max_tokens"
        private const val KEY_TEMPERATURE = "temperature"
        private const val KEY_CPU_THREADS = "cpu_threads"
        private const val KEY_MEMORY_MODE = "memory_mode"
        private const val KEY_WEB_SEARCH = "web_search_enabled"
        private const val KEY_SETUP_COMPLETE = "setup_complete"
    }
}
