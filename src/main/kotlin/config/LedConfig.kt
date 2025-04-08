package com.fabioucb.config

import kotlinx.serialization.Serializable

@Serializable
data class Thresholds(val x: Float, val y: Float, val z: Float)

@Serializable
data class ColorResponse(
    val distance: Float,
    val color: String,
    val thresholds: Thresholds
)

@Serializable
data class ThresholdsUpdateResponse(
    val message: String,
    val thresholds: Thresholds
)

object LedConfig {
    private var currentThresholds = Thresholds(10f, 20f, 30f)

    fun getCurrentThresholds(): Thresholds = currentThresholds

    fun updateThresholds(newThresholds: Thresholds) {
        require(newThresholds.x < newThresholds.y && newThresholds.y < newThresholds.z) {
            "Thresholds must satisfy: x < y < z"
        }
        currentThresholds = newThresholds
    }

    fun getColor(distance: Float): ColorResponse {
        val color = when {
            distance < currentThresholds.x -> "RED"
            distance < currentThresholds.y -> "YELLOW"
            distance < currentThresholds.z -> "GREEN"
            else -> "OFF"
        }
        return ColorResponse(distance, color, currentThresholds)
    }
}