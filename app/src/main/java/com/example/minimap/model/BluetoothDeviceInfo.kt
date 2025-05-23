package com.example.minimap.model

import java.util.UUID

data class BluetoothDeviceInfo(
    val name: String?,
    val address: String,
    val rssi: Int,
    val deviceClass: String?,
    val uuidList: List<UUID?> = emptyList()
)