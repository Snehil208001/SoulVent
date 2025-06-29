package com.example.soulvent.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.soulvent.data.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GratitudeViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager(application)

    val gratitudeNotes: StateFlow<List<String>> = settingsManager.gratitudeNotesFlow
        .map { it.toList().sorted() } // Keep the list sorted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addNote(note: String) {
        if (note.isNotBlank()) {
            viewModelScope.launch {
                settingsManager.addGratitudeNote(note)
            }
        }
    }

    fun deleteNote(note: String) {
        viewModelScope.launch {
            settingsManager.deleteGratitudeNote(note)
        }
    }
}