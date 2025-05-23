package com.example.minimap.model

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