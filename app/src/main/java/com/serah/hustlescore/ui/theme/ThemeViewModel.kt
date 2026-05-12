package com.serah.hustlescore.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = ThemePreferences(application)

    // ✅ Exposed as StateFlow so Compose can collect it
    val isDarkMode: StateFlow<Boolean> = prefs.isDarkMode
        .stateIn(
            scope          = viewModelScope,
            started        = SharingStarted.WhileSubscribed(5000),
            initialValue   = false
        )

    fun toggleTheme() {
        viewModelScope.launch {
            prefs.setDarkMode(!isDarkMode.value)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setDarkMode(enabled)
        }
    }
}