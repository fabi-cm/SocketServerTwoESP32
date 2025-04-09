package com.fabioucb.features

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.LocalDateTime

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
            when (part) {
                "tled:0", "tled:1" -> true
                "yled:0", "yled:1" -> true
                "gled:0", "gled:1" -> true
                else -> false
            }
        }
    }
}

fun Route.actuatorRoutes() {
    route("/actuator") {
        get {
            ESP32Status.actuatorConnected = true
            ESP32Status.lastActuatorUpdate = LocalDateTime.now()
            ESP32Status.addLog("Actuador consultó estado actual")
            call.respondText(LedState.currentCommand)
        }

        post {
            try {
                val command = call.receiveText()
                if (!LedState.validateCommandInternal(command)) {
                    throw IllegalArgumentException("Formato de comando inválido")
                }

                ESP32Status.actuatorConnected = true
                ESP32Status.lastActuatorUpdate = LocalDateTime.now()
                ESP32Status.addLog("Comando actualizado: $command")

                LedState.updateCommand(command)
                call.respondText("Comando actualizado: $command")
            } catch (e: Exception) {
                ESP32Status.addLog("Error al actualizar comando: ${e.message}")
                call.respondText("Error: ${e.message}", status = HttpStatusCode.BadRequest)
            }
        }

        webSocket("/ws") {
            try {
                ESP32Status.actuatorConnected = true
                ESP32Status.lastActuatorUpdate = LocalDateTime.now()
                ESP32Status.addLog("Actuador conectado via WebSocket")

                send(LedState.currentCommand)
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        ESP32Status.lastActuatorUpdate = LocalDateTime.now()
                        if (LedState.validateCommandInternal(text)) {
                            LedState.updateCommand(text)
                            ESP32Status.addLog("Comando WebSocket actualizado: $text")
                            send("ACK: $text")
                        } else {
                            ESP32Status.addLog("Comando WebSocket inválido recibido: $text")
                            send("ERROR: Comando inválido")
                        }
                    }
                }
            } catch (e: Exception) {
                ESP32Status.actuatorConnected = false
                ESP32Status.addLog("Actuador WebSocket desconectado: ${e.message}")
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, e.message ?: "Error desconocido"))
            } finally {
                ESP32Status.actuatorConnected = false
                ESP32Status.addLog("Actuador WebSocket desconectado")
            }
        }
    }
}