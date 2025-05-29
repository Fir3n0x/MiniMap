package com.example.minimap.model

import androidx.compose.ui.graphics.Color

enum class WifiSecurityLevel {
    SAFE, MEDIUM, DANGEROUS
}

fun getSecurityLevel(capabilities: String): WifiSecurityLevel {
    return when {
        capabilities.contains("WPA3") || capabilities.contains("WPA2") -> WifiSecurityLevel.SAFE
        capabilities.contains("WEP") -> WifiSecurityLevel.MEDIUM
        capabilities.isBlank() || !capabilities.contains("WPA") -> WifiSecurityLevel.DANGEROUS
        else -> WifiSecurityLevel.MEDIUM
    }
}

fun getColor(capabilities: String) : Color {
    val color =  when (getSecurityLevel(capabilities)){
        WifiSecurityLevel.SAFE -> Color.Green
        WifiSecurityLevel.MEDIUM -> Color.Yellow
        WifiSecurityLevel.DANGEROUS -> Color.Red
    }
    return color
}

fun getLabel() : WifiSecurityLevel {
    return WifiSecurityLevel.DANGEROUS
}