package com.fabioucb

import io.ktor.server.application.*
import com.fabioucb.features.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
//    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSockets()
    configureHTTP()
    configureSerialization()
    configureMonitoring()
    configureRouting()
}
