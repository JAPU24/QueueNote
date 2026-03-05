package com.adrian.queuenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.adrian.queuenote.ui.theme.AppNav
import com.adrian.queuenote.ui.theme.QueueNoteTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val store = SettingsStore(context)
            val scope = rememberCoroutineScope()

            val isDarkMode by store.darkModeFlow.collectAsState(initial = false)

            QueueNoteTheme(darkTheme = isDarkMode) {
                AppNav(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { enabled ->
                        scope.launch {
                            store.setDarkMode(enabled)
                        }
                    }
                )
            }
        }
    }
}
