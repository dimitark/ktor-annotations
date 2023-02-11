package com.github.dimitark.ktorannotations.test

import com.github.dimitark.ktor.routing.ktorRoutingAnnotationConfig
import com.github.dimitark.ktorannotations.KtorContext
import com.github.dimitark.ktorannotations.annotations.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(Netty, port = 8000) {
        install(Authentication) {
            basic { }
            basic("auth-provider") {  }
        }
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
class KoinLessController {
    @Get("/koin-less")
    suspend fun koinLess(context: KtorContext) {
        context.call.respondText("Koin-less controller...")
    }
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

    @ProtectedRoute
    @Get("/protected-0")
    suspend fun protected0(context: KtorContext) {
        context.call.respondText("Protected 0")
    }

    @ProtectedRoute
    @Get("/protected-1")
    suspend fun protected1(context: KtorContext) {
        context.call.respondText("Protected 1")
    }

    @ProtectedRoute("auth-provider")
    @Get("/protected-named-0")
    suspend fun protectedNamed0(context: KtorContext) {
        context.call.respondText("Protected named 0")
    }

    @ProtectedRoute("auth-provider")
    @Get("/protected-named-1")
    suspend fun protectedNamed1(context: KtorContext) {
        context.call.respondText("Protected named 1")
    }
}

class Service {
    fun test() = "Service Koin Test"
}