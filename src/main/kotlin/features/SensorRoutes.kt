package com.fabioucb.features

import com.fabioucb.config.LedConfig
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.sensorRoutes() {
    route("/sensor") {
        get("/intervals") {
            val thresholds = LedConfig.getCurrentThresholds()
            call.respondText("0,0.0,${thresholds.x};1,${thresholds.x},${thresholds.y};2,${thresholds.y},${thresholds.z};-1")
        }

        post {
            try {
                val distance = call.receiveText().toFloatOrNull()
                    ?: throw IllegalArgumentException("Formato de distancia inválido")

                val interval = calculateInterval(distance)
                val command = when(interval) {
                    0 -> "tled:1,yled:0,gled:0"   // Rojo
                    1 -> "tled:0,yled:1,gled:0"   // Amarillo
                    2 -> "tled:0,yled:0,gled:1"   // Verde
                    else -> "tled:0,yled:0,gled:0" // Apagado
                }

                LedState.updateCommand(command)
                call.respondText("Interval: $interval, Command: $command")
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = HttpStatusCode.BadRequest)
            }
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