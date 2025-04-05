package com.fabioucb.features

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

fun Application.configureActuatorRoutes() {
    routing {
        route("/actuator") {
            post {
                val command = call.receiveText()
                // Ejemplo: "tled:1,yled:0,gled:1"
                processActuatorCommand(command)
                call.respondText("Command executed: $command")
            }

            webSocket("/ws") {
                send("Actuator WebSocket connected")

                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        // Procesar comando y responder
                        send("ACK: $text")
                    }
                }
            }
        }
    }
}

private fun processActuatorCommand(command: String) {
    // Implementar lógica para controlar los LEDs
    println("Processing actuator command: $command")
}