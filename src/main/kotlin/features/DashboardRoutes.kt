package com.fabioucb.features

import com.fabioucb.config.LedConfig
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Objeto para manejar el estado de conexión de los ESP32
object ESP32Status {
    var sensorConnected = false
    var actuatorConnected = false
    var lastSensorUpdate: LocalDateTime? = null
    var lastActuatorUpdate: LocalDateTime? = null
    var lastDistance: Float? = null

    private val logs = mutableListOf<String>()
    private val maxLogs = 50

    fun addLog(message: String) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        logs.add(0, "[$timestamp] $message") // Agregar al inicio para tener los más recientes primero
        if (logs.size > maxLogs) {
            logs.removeAt(logs.size - 1)
        }
    }

    fun getRecentLogs(): List<String> = logs.take(10) // Mostrar solo los 10 más recientes
    fun checkConnectionStatus() {
        val now = LocalDateTime.now()
        val timeoutMinutes = 1L // Considerar desconectado después de 1 minuto sin actividad

        sensorConnected = lastSensorUpdate?.let {
            now.minusMinutes(timeoutMinutes).isBefore(it)
        } ?: false

        actuatorConnected = lastActuatorUpdate?.let {
            now.minusMinutes(timeoutMinutes).isBefore(it)
        } ?: false
    }
}

fun Application.configureDashboardRoutes() {
    routing {
        get("/") {
            ESP32Status.checkConnectionStatus()
            val currentCommand = LedState.currentCommand.split(",").associate {
                val parts = it.split(":")
                parts[0] to (parts[1] == "1")
            }

            call.respond(FreeMarkerContent(
                "dashboard.ftl",
                mapOf(
                    "thresholds" to LedConfig.getCurrentThresholds(),
                    "sensorStatus" to ESP32Status.sensorConnected,
                    "actuatorStatus" to ESP32Status.actuatorConnected,
                    "lastDistance" to ESP32Status.lastDistance,
                    "currentInterval" to ESP32Status.lastDistance?.let { distance ->
                        calculateInterval(distance)
                    },
                    "ledStatus" to currentCommand,
                    "logs" to ESP32Status.getRecentLogs()
                )
            ))
        }
    }
}

private fun calculateInterval(distance: Float): Int {
    val thresholds = LedConfig.getCurrentThresholds()
    return when {
        distance < 0 -> -1
        distance <= thresholds.x -> 0
        distance <= thresholds.y -> 1
        distance <= thresholds.z -> 2
        else -> -1
    }
}