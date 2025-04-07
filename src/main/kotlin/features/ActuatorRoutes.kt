package com.fabioucb.features

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

// Objeto compartido para almacenar el estado
object LedState {
    var currentCommand = "tled:0,yled:0,gled:0"
        private set

    fun updateCommand(newCommand: String) {
        if (validateCommandInternal(newCommand)) {
            currentCommand = newCommand
        }
    }

    fun validateCommandInternal(command: String): Boolean {
        val parts = command.split(",")
        if (parts.size != 3) return false

        return parts.all { part ->
            when {
                part == "tled:0" || part == "tled:1" -> true
                part == "yled:0" || part == "yled:1" -> true
                part == "gled:0" || part == "gled:1" -> true
                else -> false
            }
        }
    }
}

fun Route.actuatorRoutes() {
    route("/actuator") {
        get {
            call.respondText(LedState.currentCommand)
        }

        post {
            try {
                val command = call.receiveText()
                if (!LedState.validateCommandInternal(command)) {
                    throw IllegalArgumentException("Formato de comando inválido")
                }
                LedState.updateCommand(command)
                call.respondText("Comando actualizado: $command")
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = HttpStatusCode.BadRequest)
            }
        }

        webSocket("/ws") {
            try {
                send(LedState.currentCommand)
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        if (LedState.validateCommandInternal(text)) {
                            LedState.updateCommand(text)
                            send("ACK: $text")
                        } else {
                            send("ERROR: Comando inválido")
                        }
                    }
                }
            } catch (e: Exception) {
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, e.message ?: "Error desconocido"))
            }
        }
    }
}