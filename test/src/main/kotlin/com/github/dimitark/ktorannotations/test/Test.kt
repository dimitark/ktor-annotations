package com.github.dimitark.ktorannotations.test

import com.github.dimitark.ktor.routing.ktorRoutingAnnotationConfig
import com.github.dimitark.ktorannotations.KtorContext
import com.github.dimitark.ktorannotations.annotations.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(Netty, port = 8000) {
        install(Koin) {
            modules(
                module {
                    single { Service() }
                }
            )
        }

        ktorRoutingAnnotationConfig()
    }.start(wait = true)
}


@RouteController
class TestController(private val service: Service) {

    @Get("/in-controller")
    suspend fun inController(context: KtorContext) {
        context.call.respondText("In Controller... ${service.test()}")
    }

    @Post("/{test}")
    suspend fun inControllerPost(context: KtorContext) {
        context.call.respondText("In Controller Post... ${service.test()} - ${context.call.parameters["test"]}")
    }
}

class Service {
    fun test() = "Service Koin Test"
}