package com.example.soulvent.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soulvent.model.Meditation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MeditationViewModel : ViewModel() {

    private val _meditations = MutableStateFlow<List<Meditation>>(emptyList())
    val meditations: StateFlow<List<Meditation>> = _meditations

    init {
        loadMeditations()
    }

    private fun loadMeditations() {
        viewModelScope.launch {
            // In a real app, you would fetch this from a remote source like Firestore
            _meditations.value = listOf(
                Meditation("1", "Morning Gratitude", "Start your day with a positive mindset.", "YOUR_AUDIO_URL_1", "Stress & Anxiety"),
                Meditation("2", "Deep Sleep Relaxation", "A calming meditation to help you fall asleep.", "YOUR_AUDIO_URL_2", "Sleep & Relaxation"),
                Meditation("3", "Mindful Walking", "A guided meditation for your daily walk.", "YOUR_AUDIO_URL_3", "Walking Meditations")
            )
        }
    }
}