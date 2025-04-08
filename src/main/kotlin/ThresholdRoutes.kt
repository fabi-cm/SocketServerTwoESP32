package com.fabioucb

import com.fabioucb.config.LedConfig
import com.fabioucb.config.Thresholds
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class ThresholdsUpdateResponse(
    val message: String,
    val thresholds: Thresholds
)

fun Route.ledThresholdRoutes() {
    route("/led") {
        get("/thresholds") {
            call.respond(LedConfig.getCurrentThresholds())
        }

        post("/thresholds") {
            try {
                val newThresholds = call.receive<Thresholds>()
                LedConfig.updateThresholds(newThresholds)

                val response = ThresholdsUpdateResponse(
                    message = "Thresholds updated successfully",
                    thresholds = LedConfig.getCurrentThresholds()
                )

                call.respond(HttpStatusCode.OK, response)
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Invalid thresholds"))
                )
            } catch (e: Exception) {
                application.log.error("Failed to update thresholds", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Internal server error")
                )
            }
        }

        get("/color") {
            val distance = call.request.queryParameters["distance"]?.toFloatOrNull()
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing or invalid 'distance' parameter")
                )

            try {
                call.respond(LedConfig.getColor(distance))
            } catch (e: Exception) {
                application.log.error("Failed to get color", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to determine color")
                )
            }
        }
    }
}