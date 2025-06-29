package com.example.soulvent.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.soulvent.data.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    // Create an instance of our new SettingsManager
    private val settingsManager = SettingsManager(application)

    // Expose the theme flow as a StateFlow, so the UI can collect it
    val themeName: StateFlow<String> = settingsManager.themeNameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Default" // Provide an initial value
    )

    // The function to change the theme now uses the SettingsManager
    fun setTheme(themeName: String) {
        viewModelScope.launch {
            settingsManager.setTheme(themeName)
        }
    }
}