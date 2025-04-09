package com.fabioucb

import com.fabioucb.features.actuatorRoutes
import com.fabioucb.features.configureDashboardRoutes
import com.fabioucb.features.sensorRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRoutes() {
    routing {
        configureDashboardRoutes()
        sensorRoutes()
        actuatorRoutes()
        ledThresholdRoutes()
        // Ruta de salud para pruebas
        get("/health") {
            call.respondText("Server is running")
        }
    }
}
