package com.github.dimitark.ktorannotations.processor.routing

enum class RouteFunParameter(val typeName: String, val callExpression: String, val import: Pair<String, String>?) {
    KtorContext(typeName = "com.github.dimitark.ktorannotations.KtorContext", "this", import = null),
    PipelineContext(typeName = "io.ktor.util.pipeline.PipelineContext", callExpression = "this", import = null),
    Call(typeName = "io.ktor.server.application.ApplicationCall", callExpression = "call", import = "io.ktor.server.application" to "call")
}

val RouteFunParameters = RouteFunParameter.values().associateBy { it.typeName }
