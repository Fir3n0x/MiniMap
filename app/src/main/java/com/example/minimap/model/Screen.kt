package com.example.minimap.model

sealed class Screen(val route : String){
    object Home : Screen("home")
    object BluetoothScan : Screen("bluetooth_scan")
    object WifiScan : Screen("wifi_scan")
    object FileViewer : Screen("fileViewer")
}