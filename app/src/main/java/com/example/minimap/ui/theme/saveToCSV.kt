package com.example.minimap.ui.theme

import android.content.Context
import android.util.Log
import com.example.minimap.model.WifiNetworkInfo
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun readKnownWifiKeys(context: Context, fileName: String): MutableSet<String> {
    val file = File(context.filesDir, fileName)
    val knownKeys = mutableSetOf<String>()

    if (!file.exists()) return knownKeys

    file.forEachLine { line ->
        val parts = line.split(";")
        if (parts.size >= 5) {
            val key = "${parts[0]}:${parts[1]}"
            knownKeys.add(key)
        }
    }
    return knownKeys
}


fun readWifiNetworksFromCsv(context: Context, fileName: String): List<WifiNetworkInfo> {
    val file = File(context.filesDir, fileName)
    Log.d("DEBUG", "Looking for file at: ${file.absolutePath}")


    if (!file.exists()) {
        Log.d("DEBUG", "File does not exist!")
        return emptyList()
    }

    return file.readLines()
        .mapNotNull { line ->
            val parts = line.split(";")

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            if (parts.size >= 6) {
                try {
                    val ssid = parts[0]
                    val bssid = parts[1]
                    val rssi = parts[2].toIntOrNull() ?: return@mapNotNull null
                    val frequency = parts[3].toIntOrNull() ?: return@mapNotNull null
                    val capabilities = parts[4]
                    val date = dateFormat.parse(parts[5]) ?: return@mapNotNull null
                    val timestamp = date.time

                    WifiNetworkInfo(ssid, bssid, rssi, frequency, capabilities, timestamp)
                } catch (e: Exception) {
                    Log.e("CSV", "Error parsing line: $line", e)
                    null
                }
            } else {
                null
            }
        }
}




fun appendNewWifisToCsv(context: Context, fileName: String, newNetworks: List<WifiNetworkInfo>) {
    val file = File(context.filesDir, fileName)
    val knownKeys = readKnownWifiKeys(context, fileName)

    file.appendText("") // create if doesn't exist

    //val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    //val currentTime = sdf.format(Date())
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val currentTime = System.currentTimeMillis()
    val formattedTime = sdf.format(Date(currentTime))

    newNetworks.forEach { network ->
        val key = "${network.ssid}:${network.bssid}"
        if (!knownKeys.contains(key)) {
            val line = "${network.ssid};${network.bssid};${network.rssi};${network.frequency};${network.capabilities};${currentTime}\n"
            file.appendText(line)
            knownKeys.add(key)
        }
    }
}