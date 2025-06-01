package com.example.minimap.ui.theme

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.minimap.data.preferences.SettingsKeys
import com.example.minimap.data.preferences.SettingsRepository
import com.example.minimap.data.preferences.settingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WifiScanWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val CHANNEL_ID = "wifi_alerts_channel"
        private const val CHANNEL_NAME = "WiFi Alerts"
        private const val NOTIF_ID = 1001
    }

    @SuppressLint("MissingPermission", "ServiceCast")
    override suspend fun doWork(): Result {
        // 1) Load Preferences Repo
        val settingsRepo = SettingsRepository(context)

        // 2) Check if option "Auto Scan" is still activated
        val autoScanEnabled = settingsRepo.context.settingsDataStore.data
            .map { prefs -> prefs[SettingsKeys.AUTO_SCAN_ENABLED] ?: false }
            .first()

        if (!autoScanEnabled) {
            // If AutoScan is disabled, stop Worker (Result success to not retry)
            return Result.success()
        }

        // 3) Launch Wifi scan and retrieve results
        return withContext(Dispatchers.IO) {
            try {
                val wifiManager =
                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

                // Need ACCESS_FINE_LOCATION permission
                if (!wifiManager.isWifiEnabled) {
                    // Activer le Wi-Fi si besoin (ou retourner success si on ne veut pas forcer l'activation)
                    // wifiManager.isWifiEnabled = true // attention : cela peut demander une action utilisateur sous API>29
                }

                val success = wifiManager.startScan()
                // If scan does not launch, still send success
                if (!success) {
                    return@withContext Result.success()
                }

                // Retrieve results in final scan
                val scanResults: List<ScanResult> = wifiManager.scanResults

                // 4) Filter non-safe networks
                // Selon vos critères, « non sécurisé » = capabilités contiennent “[ESS]” sans “WPA”/“WPA2”/“WPA3”
                val insecureNetworks = scanResults.filter { result ->
                    val caps = result.capabilities.uppercase()
                    // Exemple simple : pas de « WEP » ni « WPA »
                    !(caps.contains("WPA") || caps.contains("WEP"))
                }

                // 5) Si au moins un réseau non sécurisé est trouvé ET si l’option « Notifications » est activée, envoyer une notif
                val notificationEnabled = settingsRepo.context.settingsDataStore.data
                    .map { prefs -> prefs[SettingsKeys.NOTIFICATION_ENABLED] ?: false }
                    .first()

                if (insecureNetworks.isNotEmpty() && notificationEnabled) {
                    sendNotification(insecureNetworks.size)
                }

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.retry()
            }
        }
    }

    /**
     * Builds and sends a summary notification to report the unsecured network(s).
     * You can customize the style or text (for example, list SSIDs).
     */
    private fun sendNotification(countInsecure: Int) {
        val notifManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1) Create channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existing = notifManager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alertes pour réseaux Wi-Fi non sécurisés"
                }
                notifManager.createNotificationChannel(channel)
            }
        }

        // 2) Build notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("Réseau Wi-Fi non sécurisé détecté")
            .setContentText("$countInsecure réseau(s) en accès libre trouvé(s).")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // 3) display notification
        notifManager.notify(NOTIF_ID, builder.build())
    }
}