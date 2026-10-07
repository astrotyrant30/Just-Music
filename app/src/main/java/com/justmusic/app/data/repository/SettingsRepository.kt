package com.justmusic.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.justmusic.app.data.model.SeekBarStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "just_music_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val SKIP_SILENCE = booleanPreferencesKey("skip_silence")
        val SEEK_BAR_STYLE = stringPreferencesKey("seek_bar_style")
        val MIN_DURATION_MS = longPreferencesKey("min_duration_ms")
        val LAST_PLAYED_SONG_ID = longPreferencesKey("last_played_song_id")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
        val USER_NAME = stringPreferencesKey("user_name")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.THEME_MODE] ?: "DARK"
    }

    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.USER_NAME] ?: "Music Lover"
    }

    val skipSilenceEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SKIP_SILENCE] ?: true
    }

    val seekBarStyle: Flow<SeekBarStyle> = context.dataStore.data.map { prefs ->
        val name = prefs[PreferencesKeys.SEEK_BAR_STYLE] ?: SeekBarStyle.WAVEFORM.name
        runCatching { SeekBarStyle.valueOf(name) }.getOrDefault(SeekBarStyle.WAVEFORM)
    }

    val minDurationMs: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.MIN_DURATION_MS] ?: 30_000L
    }

    val lastPlayedSongId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.LAST_PLAYED_SONG_ID]
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun setSkipSilenceEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SKIP_SILENCE] = enabled
        }
    }

    suspend fun setSeekBarStyle(style: SeekBarStyle) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SEEK_BAR_STYLE] = style.name
        }
    }

    suspend fun setMinDurationMs(durationMs: Long) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.MIN_DURATION_MS] = durationMs
        }
    }

    suspend fun setLastPlayedSongId(songId: Long) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.LAST_PLAYED_SONG_ID] = songId
        }
    }
}
