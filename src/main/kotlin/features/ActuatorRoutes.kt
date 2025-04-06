package com.fabioucb.features

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

fun Route.actuatorRoutes() {
    route("/actuator") {
        post {
            try {
                val command = call.receiveText()
                validateCommand(command)

                println("Comando válido recibido: $command")
                call.respondText(
                    text = "Command processed: $command",
                    status = HttpStatusCode.OK
                )
            } catch (e: Exception) {
                call.respondText(
                    text = "Error: ${e.message}",
                    status = HttpStatusCode.BadRequest
                )
            }
        }

        webSocket("/ws") {
            try {
                send("Actuator WebSocket connected")

                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        validateCommand(text)
                        send("ACK: $text")
                    }
                }
            } catch (e: Exception) {
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, e.message ?: "Unknown error"))
            }
        }
    }
}

private fun validateCommand(command: String) {
    val parts = command.split(",")
    if (parts.size != 3) {
        throw IllegalArgumentException("Formato de comando inválido. Use 'tled:X,yled:Y,gled:Z'")
    }

    parts.forEach { part ->
        val keyValue = part.split(":")
        if (keyValue.size != 2 || !listOf("tled", "yled", "gled").contains(keyValue[0])) {
            throw IllegalArgumentException("Formato de comando inválido en parte: $part")
        }

        val value = keyValue[1].toIntOrNull()
        if (value == null || (value != 0 && value != 1)) {
            throw IllegalArgumentException("Valor inválido (debe ser 0 o 1) en: $part")
        }
    }
}