package com.example.minimap.model

import kotlinx.serialization.Serializable

@Serializable
data class WifiNetworkInfo (
    val ssid : String,
    val rssi : Int, // Signal power (Received Signal Strength Indicator)
    // 0 perfect signal (impossible) else, -30 / -90
    val capabilities: String,
    val bssid: String,
    val channel: Int,
    val frequency: Int
)