package com.example.minimap.model

// Handle "+1" animation
data class PlusOneAnimation(
    val id: String,
    var shouldAnimate: Boolean = true,
    var progress: Float = 0f,
    var alpha: Float = 1f
)