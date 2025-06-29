package com.example.soulvent.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val THEME_KEY = stringPreferencesKey("theme_name")
        // NEW: Add a key for the gratitude notes
        val GRATITUDE_NOTES_KEY = stringSetPreferencesKey("gratitude_notes")
    }

    // --- Theme Functions (Existing) ---
    suspend fun setTheme(themeName: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = themeName
        }
    }

    val themeNameFlow = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "Default"
    }

    // --- NEW: Functions for Gratitude Jar ---

    /**
     * A flow that emits the set of all saved gratitude notes.
     */
    val gratitudeNotesFlow = dataStore.data.map { preferences ->
        preferences[GRATITUDE_NOTES_KEY] ?: emptySet()
    }

    /**
     * Adds a new gratitude note to the set.
     */
    suspend fun addGratitudeNote(note: String) {
        dataStore.edit { preferences ->
            val currentNotes = preferences[GRATITUDE_NOTES_KEY] ?: emptySet()
            preferences[GRATITUDE_NOTES_KEY] = currentNotes + note
        }
    }

    /**
     * Deletes a gratitude note from the set.
     */
    suspend fun deleteGratitudeNote(note: String) {
        dataStore.edit { preferences ->
            val currentNotes = preferences[GRATITUDE_NOTES_KEY] ?: emptySet()
            preferences[GRATITUDE_NOTES_KEY] = currentNotes - note
        }
    }
}