package com.adrian.queuenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.adrian.queuenote.ui.theme.AppNav
import com.adrian.queuenote.ui.theme.QueueNoteTheme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val store = SettingsStore(context)
            val scope = rememberCoroutineScope()

            val themeMode by store.themeModeFlow.collectAsState(initial = "auto")
            val language by store.languageFlow.collectAsState(initial = "auto")

            // Lógica de Idioma
            LaunchedEffect(language) {
                if (language != "auto") {
                    val locale = Locale(language)
                    Locale.setDefault(locale)
                    val resources = context.resources
                    val configuration = resources.configuration
                    configuration.setLocale(locale)
                    resources.updateConfiguration(configuration, resources.displayMetrics)
                }
            }

            val isDark = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            QueueNoteTheme(darkTheme = isDark) {
                AppNav(
                    themeMode = themeMode,
                    onSetThemeMode = { mode ->
                        scope.launch { store.setThemeMode(mode) }
                    },
                    language = language,
                    onSetLanguage = { lang ->
                        scope.launch { store.setLanguage(lang) }
                    }
                )
            }
        }
    }
}
