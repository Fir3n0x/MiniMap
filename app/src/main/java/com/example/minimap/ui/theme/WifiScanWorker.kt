package com.example.minimap.ui.theme

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.minimap.R
import com.example.minimap.data.preferences.SettingsRepository
import com.example.minimap.model.WifiClassifier
import com.example.minimap.model.WifiNetworkInfo
import com.example.minimap.model.WifiScannerViewModel
import com.example.minimap.model.WifiSecurityLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WifiScanWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val CHANNEL_ID = "wifi_alerts_channel"
        private const val FOREGROUND_CHANNEL_ID = "wifi_scan_foreground_channel"
        private const val NOTIF_ID = 1001
        private const val FOREGROUND_NOTIF_ID = 1002
    }



    @SuppressLint("MissingPermission", "ServiceCast")
    override suspend fun doWork(): Result {

        // 1. Configure notification channels
        createNotificationChannels()

        // 2. Start in foreground service if Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setForeground(createForegroundInfo("Starting WiFi scan..."))
        }

        return try {
            // 3. Retrieve preferences
            val settingsRepo = SettingsRepository(context)
            val autoScanEnabled = settingsRepo.autoScanEnabledFlow.first()
            val notificationEnabled = settingsRepo.notificationEnabledFlow.first()
            val autoSaveEnabled = settingsRepo.autoSaveEnabledFlow.first()

            if (!autoScanEnabled) return Result.success()

            // 4. Initialize wifi
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                wifiManager.isWifiEnabled = true
                delay(2000)
            }

            // 5. Launch scan
            updateForegroundNotification("Scanning networks...")
            val success = wifiManager.startScan()
            if (!success) return Result.success()

            // 6. Handle results
            val scanResults = wifiManager.scanResults ?: return Result.retry()
            val networks = processScanResults(scanResults)

            // 7. Save if enabled
            if (autoSaveEnabled) {
                updateForegroundNotification("Saving results...")
                saveNewNetworks(networks)
            }

            // 8. Notify if necessary
            val insecureNetworks = networks.filter { isNetworkInsecure(it) }
            if (insecureNetworks.isNotEmpty() && notificationEnabled) {
                sendAlertNotification(insecureNetworks)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal pour les alertes
            val alertChannel = NotificationChannel(
                CHANNEL_ID,
                "WiFi Security Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for insecure WiFi networks"
            }

            // Canal pour le foreground service
            val foregroundChannel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                "WiFi Scan Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background WiFi scanning service"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(alertChannel)
            notificationManager.createNotificationChannel(foregroundChannel)
        }
    }

    private fun createForegroundInfo(progressText: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setContentTitle("WiFi Security Scan")
            .setContentText(progressText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        return ForegroundInfo(FOREGROUND_NOTIF_ID, notification)
    }

    private suspend fun updateForegroundNotification(text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setForeground(createForegroundInfo(text))
        }
    }

    private fun processScanResults(results: List<ScanResult>): List<WifiNetworkInfo> {

        // Initialize classifier
        val wifiClassifier = WifiClassifier(context)
        val viewModel = WifiScannerViewModel()

        // GPS location
        var currentLatitude = 0.0
        var currentLongitude = 0.0



        return results.mapNotNull { result ->
            val ssid = result.SSID.takeIf { it.isNotBlank() } ?: return@mapNotNull null

            viewModel.fetchLastLocation { location ->
                currentLatitude = location.latitude
                currentLongitude = location.longitude
            }

            val features = wifiClassifier.extractFeatures(result,context)
            val securityLevel = wifiClassifier.predictSecurityLevel(features)

            WifiNetworkInfo(
                ssid = ssid,
                bssid = result.BSSID,
                rssi = result.level,
                frequency = result.frequency,
                capabilities = result.capabilities,
                timestamp = System.currentTimeMillis(),
                label = securityLevel,
                timestampFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date()),
                latitude = currentLatitude,
                longitude = currentLongitude
            )
        }.distinctBy { "${it.ssid}:${it.bssid}" }
    }

    private fun isNetworkInsecure(network: WifiNetworkInfo): Boolean {
        return network.label.toString() == WifiSecurityLevel.DANGEROUS.toString()
    }

    private fun saveNewNetworks(networks: List<WifiNetworkInfo>) {
        val knownNetworks = readWifiNetworksFromCsv(context, "wifis_dataset.csv")
        val knownKeys = knownNetworks.map { "${it.ssid}:${it.bssid}" }.toSet()

        val newNetworks = networks.filterNot {
            knownKeys.contains("${it.ssid}:${it.bssid}")
        }

        if (newNetworks.isNotEmpty()) {
            appendNewWifisToCsv(context, "wifis_dataset.csv", newNetworks)
        }
    }

    private fun sendAlertNotification(networks: List<WifiNetworkInfo>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Style Inbox
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("${networks.size} insecure networks found")

        networks.take(5).forEach {
            inboxStyle.addLine("${it.ssid} (${it.label})")
        }
        if (networks.size > 5) {
            inboxStyle.addLine("... and ${networks.size - 5} more")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Insecure WiFi Detected")
            .setContentText("${networks.size} insecure networks found")
            .setStyle(inboxStyle) // Utiliser le style configuré
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIF_ID, notification)
    }
}