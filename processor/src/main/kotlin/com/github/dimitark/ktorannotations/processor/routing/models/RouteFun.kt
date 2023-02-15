package com.github.dimitark.ktorannotations.processor.routing.models

import com.github.dimitark.ktorannotations.processor.routing.RouteFunParameter

data class RouteFun(
    val path: String,
    val method: String,
    val funPackage: String,
    val funName: String,
    val params: List<RouteFunParameter>,
    val parent: ControllerDef,
    val authenticationProvider: String? = null
)
