package com.fabioucb.features

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.cors.routing.*
import org.slf4j.event.Level

fun Application.configureCommonFeatures() {
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }

    install(CallLogging) {
        level = Level.INFO
    }
}