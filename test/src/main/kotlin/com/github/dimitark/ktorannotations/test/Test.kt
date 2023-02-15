package com.github.dimitark.ktorannotations.test

import com.github.dimitark.ktor.routing.ktorRoutingAnnotationConfig
import com.github.dimitark.ktorannotations.KtorContext
import com.github.dimitark.ktorannotations.annotations.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(Netty, port = 8000) {
        install(Authentication) {
            basic { }
            basic("jwt-auth-provider") {  }
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

    @Get("/in-controller-context")
    suspend fun inControllerContext(context: KtorContext) {
        context.call.respondText("In Controller Context... ${service.test()}")
    }

    @Get("/in-controller-call")
    suspend fun inControllerCall(call: ApplicationCall) {
        call.respondText("In Controller Call... ${service.test()}")
    }

    @Get("/in-controller-pipeline")
    suspend fun inControllerPipeline(pipeline: PipelineContext<Unit, ApplicationCall>) {
        pipeline.call.respondText("In Controller Pipeline... ${service.test()}")
    }

    @Post("/{test}")
    suspend fun inControllerPost(call: ApplicationCall) {
        call.respondText("In Controller Post... ${service.test()} - ${call.parameters["test"]}")
    }

    @ProtectedRoute
    @Get("/protected")
    suspend fun protected(call: ApplicationCall) {
        call.respondText("Protected 1")
    }

    @ProtectedRoute("jwt-auth-provider")
    @Get("/protected-jwt")
    suspend fun protectedJwt(call: ApplicationCall) {
        call.respondText("Protected named 0")
    }
}

class Service {
    fun test() = "Service Koin Test"
}