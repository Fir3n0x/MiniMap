package com.example.minimap.model

sealed class Screen(val route : String){
    object Home : Screen("home")
    object WifiScan : Screen("wifi_scan")
    object FileViewer : Screen("fileViewer")
    object ParameterViewer : Screen("parameterViewer")
}