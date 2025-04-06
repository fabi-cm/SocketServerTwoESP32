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
                val distanceText = call.receiveText()
                val distance = distanceText.toFloatOrNull()
                    ?: throw IllegalArgumentException("Invalid distance format")

                val interval = calculateInterval(distance)

                call.respondText(
                    text = "Interval: $interval",
                    status = HttpStatusCode.OK
                )
            } catch (e: Exception) {
                call.respondText(
                    text = "Error: ${e.message}",
                    status = HttpStatusCode.BadRequest
                )
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