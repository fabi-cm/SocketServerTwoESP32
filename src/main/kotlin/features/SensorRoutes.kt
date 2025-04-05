package com.fabioucb.features

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSensorRoutes() {
    routing {
        route("/sensor") {
            post {
                val distance = call.receiveText().toIntOrNull() ?: -1
                val interval = calculateInterval(distance)

                // Aquí podrías enviar comandos a los actuadores
                // basado en el intervalo detectado

                call.respondText("Interval: $interval")
            }

            get("/intervals") {
                call.respondText("0,0,10;1,10,20;2,20,30;-1")
            }
        }
    }
}

private fun calculateInterval(distance: Int): Int {
    return when {
        distance in 0..10 -> 0
        distance in 11..20 -> 1
        distance in 21..30 -> 2
        else -> -1
    }
}