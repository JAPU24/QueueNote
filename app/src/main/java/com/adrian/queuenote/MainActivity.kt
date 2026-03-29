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
            val transRepo = remember { TranslationRepository() }

            val themeMode by store.themeModeFlow.collectAsState(initial = "auto")
            val language by store.languageFlow.collectAsState(initial = "auto")
            
            // Estado para los strings dinámicos de la API
            var appStrings by remember { mutableStateOf(AppStrings()) }

            // Lógica de Idioma + API
            LaunchedEffect(language) {
                val targetLang = if (language == "auto") {
                    Locale.getDefault().language
                } else language

                // 1. Cambiamos el Locale físico del sistema
                val locale = Locale(targetLang)
                Locale.setDefault(locale)
                val resources = context.resources
                val configuration = resources.configuration
                configuration.setLocale(locale)
                resources.updateConfiguration(configuration, resources.displayMetrics)

                // 2. LLAMADA A LA API EXTRA DE IDIOMAS
                scope.launch {
                    appStrings = transRepo.fetchStrings(targetLang)
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
                    },
                    appStrings = appStrings // Pasamos los strings descargados
                )
            }
        }
    }
}
