package com.fabioucb.features

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.sensorRoutes() {
    route("/sensor") {
        get("/intervals") {
            call.respondText("0,0.0,10.0;1,10.0,20.0;2,20.0,30.0;-1")
        }

        post {
            try {
                val distance = call.receiveText().toFloatOrNull()
                    ?: throw IllegalArgumentException("Formato de distancia inválido")

                val interval = calculateInterval(distance)
                val command = when(interval) {
                    0 -> "tled:1,yled:0,gled:0"
                    1 -> "tled:0,yled:1,gled:0"
                    2 -> "tled:0,yled:0,gled:1"
                    else -> "tled:0,yled:0,gled:0"
                }

                // Actualiza el estado compartido
                LedState.updateCommand(command)

                call.respondText("Interval: $interval, Command: $command")
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = HttpStatusCode.BadRequest)
            }
        }
    }
}

private fun calculateInterval(distance: Float): Int {
    return when {
        distance < 0 -> -1
        distance <= 10.0f -> 0
        distance <= 20.0f -> 1
        distance <= 30.0f -> 2
        else -> -1
    }
}