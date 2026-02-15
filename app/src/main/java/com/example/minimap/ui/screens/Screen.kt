package com.example.minimap.ui.screens

sealed class Screen(val route : String){
    object Home : Screen("home")
    object WifiScan : Screen("wifi_scan")
    object FileViewer : Screen("fileViewer")
    object ParameterViewer : Screen("parameterViewer")
    object MapViewer : Screen("mapViewer")
}