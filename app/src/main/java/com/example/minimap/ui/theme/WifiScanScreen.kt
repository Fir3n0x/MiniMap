package com.example.minimap.ui.theme


import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.minimap.model.WifiNetwork
import kotlinx.coroutines.delay
import kotlin.collections.addAll
import kotlin.collections.mutableListOf
import kotlin.compareTo
import kotlin.text.clear

@SuppressLint("MissingPermission", "ServiceCast")
@Composable
fun WifiRadarScreen(context: Context) {
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//    val wifiNetworks = remember { mutableStateMapOf<String, WifiNetwork>() }
    val wifiNetworks = remember { mutableStateListOf<WifiNetwork>() }

    LaunchedEffect(Unit) {
        while (true) {
            wifiManager.startScan()
            delay(1000)
            val results = wifiManager.scanResults


            val uniqueNetworks = mutableMapOf<String, WifiNetwork>()

            for(result in results){
                val ssid = result.SSID
                val rssi = result.level
                val bssid = result.BSSID
                val capa = result.capabilities
                val channel = result.channelWidth
                val frequency = result.frequency

                if (ssid.isBlank()) continue // Ignore les SSID vides

                val existing = uniqueNetworks[ssid]
                if (existing == null) {
                    // Pas encore ajouté → on ajoute
                    uniqueNetworks[ssid] = WifiNetwork(ssid, rssi, capa, bssid, channel, frequency)
                } else {
                    // Déjà présent → on compare les RSSI
                    val diff = kotlin.math.abs(existing.rssi - rssi)
                    if (diff > 5) {
                        // Différence significative → on garde le plus fort (proche de 0)
                        if (rssi > existing.rssi) {
                            uniqueNetworks[ssid] = WifiNetwork(ssid, rssi, capa, bssid, channel, frequency)
                        }
                    }
                    // Sinon → ignorer car redondant
                }
            }


            wifiNetworks.clear()
            wifiNetworks.addAll(uniqueNetworks.values)
            delay(2000) // toutes les 500ms

//            results.forEach{
//                val ssid = it.SSID
//                if(ssid.isNotBlank()) {
//                    wifiNetworks[ssid] = WifiNetwork(ssid, it.level)
//                }
//            }
//
//            val detectedSsids = results.map {
//                it.SSID
//            }.toSet()
//
//            val keysToRemove = wifiNetworks.keys - detectedSsids
//            keysToRemove.forEach {
//                wifiNetworks.remove(it)
//            }

//            delay(500)
        }
    }

//    WifiRadarView(networks = wifiNetworks.values.toList(), modifier = Modifier.fillMaxSize())
    WifiRadarDetection(networks = wifiNetworks.toList(), modifier = Modifier.fillMaxSize())
}



@Composable
@Preview(showBackground = true, backgroundColor = 0xFF000000)
fun WifiRadarPreview() {
    val mockNetworks = listOf(
        WifiNetwork("INSA_WIFI", -0, "", "", 0, 0),
        WifiNetwork("INSA_WIFI", -3, "",  "", 0, 0),
        WifiNetwork("INSA_IFI", -10, "",  "", 0, 0),
        WifiNetwork("INSA_WII", -20, "",  "", 0, 0),
        WifiNetwork("INSA_WIFI", -50, "",  "", 0, 0),
        WifiNetwork("Freebox", -70, "",  "", 0, 0),
        WifiNetwork("Hidden Network", -90, "",  "", 0, 0)
    )

    WifiRadarDetection(networks = mockNetworks, modifier = Modifier.fillMaxSize())
}
