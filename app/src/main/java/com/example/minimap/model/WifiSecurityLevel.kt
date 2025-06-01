package com.example.minimap.model

import android.content.Context
import androidx.compose.ui.graphics.Color
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

enum class WifiSecurityLevel {
    SAFE, MEDIUM, DANGEROUS;

    fun getColor(): Color {
        return when (this) {
            SAFE -> Color.Green
            MEDIUM -> Color.Yellow
            DANGEROUS -> Color.Red
        }
    }
}



fun getColor(securityLevel: WifiSecurityLevel): Color {
    return securityLevel.getColor()
}
