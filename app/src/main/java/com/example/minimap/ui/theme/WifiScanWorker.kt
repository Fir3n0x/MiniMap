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
import kotlinx.coroutines.flow.first
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
        // 1) Charger la Repo de préférences
        val settingsRepo = SettingsRepository(context)

        // 2) Vérifier si l'option « Auto Scan » est toujours activée
        val autoScanEnabled = settingsRepo.context.settingsDataStore.data
            .map { prefs -> prefs[SettingsKeys.AUTO_SCAN_ENABLED] ?: false }
            .first()

        if (!autoScanEnabled) {
            // Si on n'est plus en AutoScan, on arrête le Worker (résultat succès pour ne pas réessayer)
            return Result.success()
        }

        // 3) Lancer un scan Wi-Fi et récupérer les résultats
        return withContext(Dispatchers.IO) {
            try {
                val wifiManager =
                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

                // Nécessite que la permission ACCESS_FINE_LOCATION (ou COARSE) soit déjà accordée
                if (!wifiManager.isWifiEnabled) {
                    // Activer le Wi-Fi si besoin (ou retourner success si on ne veut pas forcer l'activation)
                    // wifiManager.isWifiEnabled = true // attention : cela peut demander une action utilisateur sous API>29
                }

                val success = wifiManager.startScan()
                // Si le scan ne démarre pas, on renvoie success quand même
                if (!success) {
                    return@withContext Result.success()
                }

                // On récupère les résultats du dernier scan
                val scanResults: List<ScanResult> = wifiManager.scanResults

                // 4) Filtrer les réseaux non sécurisés
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
                // Échec ponctuel ; WorkManager réessaiera selon la politique de backoff
                Result.retry()
            }
        }
    }

    /**
     * Construit et envoie une notification sommaire pour signaler le(s) réseau(x) non sécurisé(s).
     * Vous pouvez personnaliser le style ou le texte (par exemple lister les SSID).
     */
    private fun sendNotification(countInsecure: Int) {
        val notifManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1) Créer le channel si nécessaire (Android 8+)
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

        // 2) Construire la notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning) // ou votre icône d’application
            .setContentTitle("Réseau Wi-Fi non sécurisé détecté")
            .setContentText("$countInsecure réseau(s) en accès libre trouvé(s).")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // 3) Afficher la notification
        notifManager.notify(NOTIF_ID, builder.build())
    }
}