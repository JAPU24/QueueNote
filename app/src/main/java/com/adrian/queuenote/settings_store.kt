package com.adrian.queuenote

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {

    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    private val LANGUAGE_KEY = stringPreferencesKey("language")

    // Modos: "light", "dark", "auto"
    val themeModeFlow: Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[THEME_MODE_KEY] ?: "auto"
        }

    // Idiomas: "es", "en", "auto"
    val languageFlow: Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[LANGUAGE_KEY] ?: "auto"
        }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = lang
        }
    }
}
