package com.example.minimap.ui.theme


import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.minimap.model.WifiClassifier
import com.example.minimap.model.WifiNetworkInfo
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission", "ServiceCast")
@Composable
fun WifiScanScreen(context: Context, navController: NavController) {


    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//    val wifiNetworks = remember { mutableStateMapOf<String, WifiNetwork>() }
    val wifiNetworks = remember { mutableStateListOf<WifiNetworkInfo>() }


    var isRunning by remember { mutableStateOf(true) }


    // Initialize classifier
    val wifiClassifier = remember { WifiClassifier(context) }


    LaunchedEffect(isRunning) {
        while (isRunning) {
            wifiManager.startScan()
            delay(1000)
            val results = wifiManager.scanResults



            val uniqueNetworks = mutableMapOf<String, WifiNetworkInfo>()

            for(result in results){

                //Retrieve features
                val features = extractFeatures(result)

                //Predict security level with model
                val securityLevel = wifiClassifier.predictSecurityLevel(features)

                val ssid = result.SSID
                val rssi = result.level
                val bssid = result.BSSID
                val capabilities = result.capabilities
                val channel = result.channelWidth
                val frequency = result.frequency
                val centerFreq0 = result.centerFreq0
                val centerFreq1 = result.centerFreq1
                val timestamp = result.timestamp
                val operatorFriendlyName = result.operatorFriendlyName
                val venueName = result.venueName
                val isPasspointNetwork = result.isPasspointNetwork
                val is80211mcResponder = result.is80211mcResponder

                if (ssid.isBlank()) continue // ignore empty ssid

                val existing = uniqueNetworks[ssid]
                if (existing == null) {
                    // no include -> add up
                    uniqueNetworks[ssid] = WifiNetworkInfo(
                        ssid = ssid, bssid = bssid, rssi = rssi, frequency = frequency, capabilities = capabilities, timestamp = timestamp, label = securityLevel
                    )
                } else {
                    // already available → compare rssi
                    val diff = kotlin.math.abs(existing.rssi - rssi)
                    if (diff > 5) {
                        // Significant difference → keep the highest (close to 0)
                        if (rssi > existing.rssi) {
                            uniqueNetworks[ssid] = WifiNetworkInfo(
                                ssid = ssid, bssid = bssid, rssi = rssi, frequency = frequency, capabilities = capabilities, timestamp = timestamp, label = securityLevel
                            )
                        }
                    }
                    // else, ignored bc redundancies
                }
            }


            appendNewWifisToCsv(context, "wifis_dataset.csv", uniqueNetworks.values.toList())



            wifiNetworks.clear()
            wifiNetworks.addAll(uniqueNetworks.values)
            delay(2000)

        }
    }

//    WifiRadarView(networks = wifiNetworks.values.toList(), modifier = Modifier.fillMaxSize())
    WifiRadarDetection(navController = navController, networks = wifiNetworks.toList(), isRunning = isRunning, onToggleRunning = {isRunning = !isRunning}, modifier = Modifier.fillMaxSize())
}



@Composable
@Preview(showBackground = true, backgroundColor = 0xFF000000)
fun WifiScanPreview() {
    val mockNetworks = listOf(
        WifiNetworkInfo("INSA_WIFI", "9", 9, 4, "4", 4),
    )

    var isRunning: Boolean = true

    WifiRadarDetection(navController = rememberNavController(), networks = mockNetworks, isRunning = isRunning, onToggleRunning = {isRunning = !isRunning}, modifier = Modifier.fillMaxSize())
}


// Function to retrieve features from a ScanResult
private fun extractFeatures(scan: ScanResult): FloatArray {
    val caps = scan.capabilities.lowercase()

    return floatArrayOf(
        // is_open
        if (caps.contains("ess") && !(caps.contains("wpa") || caps.contains("rsn"))) 1f else 0f,
        // uses_wep
        if (caps.contains("wep")) 1f else 0f,
        // uses_wpa
        if (caps.contains("tkip")) 1f else 0f,
        // uses_wpa2_ccmp
        if (caps.contains("wpa2") && caps.contains("ccmp")) 1f else 0f,
        // uses_wpa3
        if (caps.contains("sae")) 1f else 0f,
        // wps_enabled
        if (caps.contains("wps")) 1f else 0f,
        // rssi_class
        when {
            scan.level >= -60 -> 0f  // Fort
            scan.level <= -80 -> 2f  // Faible
            else -> 1f               // Moyen
        },
        // is_5ghz
        if (scan.frequency > 4000) 1f else 0f,
        // is_hidden
        if (scan.SSID.matches(Regex(".*[0-9A-Fa-f]{4}$"))) 1f else 0f,
        // is_public
        if (listOf("eduroam", "citywifi", "public", "free").any {
                scan.SSID.contains(it, ignoreCase = true)
            }) 1f else 0f
    )
}