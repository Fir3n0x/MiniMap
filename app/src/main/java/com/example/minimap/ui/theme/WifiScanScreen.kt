package com.example.minimap.ui.theme


import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.minimap.model.WifiNetworkInfo
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission", "ServiceCast")
@Composable
fun WifiScanScreen(context: Context, navController: NavController) {
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//    val wifiNetworks = remember { mutableStateMapOf<String, WifiNetwork>() }
    val wifiNetworks = remember { mutableStateListOf<WifiNetworkInfo>() }

    LaunchedEffect(Unit) {
        while (true) {
            wifiManager.startScan()
            delay(1000)
            val results = wifiManager.scanResults


            val uniqueNetworks = mutableMapOf<String, WifiNetworkInfo>()

            for(result in results){
                val ssid = result.SSID
                val rssi = result.level
                val bssid = result.BSSID
                val capa = result.capabilities
                val channel = result.channelWidth
                val frequency = result.frequency

                if (ssid.isBlank()) continue // ignore empty ssid

                val existing = uniqueNetworks[ssid]
                if (existing == null) {
                    // no include -> add up
                    uniqueNetworks[ssid] = WifiNetworkInfo(ssid, rssi, capa, bssid, channel, frequency)
                } else {
                    // already available → compare rssi
                    val diff = kotlin.math.abs(existing.rssi - rssi)
                    if (diff > 5) {
                        // Significant difference → keep the highest (close to 0)
                        if (rssi > existing.rssi) {
                            uniqueNetworks[ssid] = WifiNetworkInfo(ssid, rssi, capa, bssid, channel, frequency)
                        }
                    }
                    // else, ignored bc redundancies
                }
            }


            wifiNetworks.clear()
            wifiNetworks.addAll(uniqueNetworks.values)
            delay(2000)

        }
    }

//    WifiRadarView(networks = wifiNetworks.values.toList(), modifier = Modifier.fillMaxSize())
    WifiRadarDetection(navController = navController, networks = wifiNetworks.toList(), modifier = Modifier.fillMaxSize())
}



@Composable
@Preview(showBackground = true, backgroundColor = 0xFF000000)
fun WifiScanPreview() {
    val mockNetworks = listOf(
        WifiNetworkInfo("INSA_WIFI", -0, "", "", 0, 0),
        WifiNetworkInfo("INSA_WIFI", -3, "",  "", 0, 0),
        WifiNetworkInfo("INSA_IFI", -10, "",  "", 0, 0),
        WifiNetworkInfo("INSA_WII", -20, "",  "", 0, 0),
        WifiNetworkInfo("INSA_WIFI", -50, "",  "", 0, 0),
        WifiNetworkInfo("Freebox", -70, "",  "", 0, 0),
        WifiNetworkInfo("Hidden Network", -90, "",  "", 0, 0)
    )

    WifiRadarDetection(navController = rememberNavController(), networks = mockNetworks, modifier = Modifier.fillMaxSize())
}
