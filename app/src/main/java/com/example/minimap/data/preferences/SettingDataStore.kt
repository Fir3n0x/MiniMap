package com.example.minimap.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// On crée le DataStore “settingsDataStore” lié au Context
val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

object SettingsKeys {
    val AUTO_SCAN_ENABLED = booleanPreferencesKey("auto_scan_enabled")
    val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
    val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
}

class SettingsRepository(val context: Context) {
    // Flow<Boolean> pour savoir si AutoScan est activé
    val autoScanEnabledFlow: Flow<Boolean> = context.settingsDataStore.data
        .map { prefs -> prefs[SettingsKeys.AUTO_SCAN_ENABLED] ?: false }

    // Flow<Boolean> pour savoir si Notifications est activé
    val notificationEnabledFlow: Flow<Boolean> = context.settingsDataStore.data
        .map { prefs -> prefs[SettingsKeys.NOTIFICATION_ENABLED] ?: false }

    val vibrationEnabledFlow: Flow<Boolean> = context.settingsDataStore.data
        .map { prefs -> prefs[SettingsKeys.VIBRATION_ENABLED] ?: false }

    // Fonction pour changer l’état de l’autoScan
    suspend fun setAutoScanEnabled(isEnabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.AUTO_SCAN_ENABLED] = isEnabled
        }
    }

    // Fonction pour changer l’état des notifications
    suspend fun setNotificationEnabled(isEnabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.NOTIFICATION_ENABLED] = isEnabled
        }
    }

    suspend fun setVibrationEnabled(isEnabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.VIBRATION_ENABLED] = isEnabled
        }
    }
}