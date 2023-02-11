package com.github.dimitark.ktorannotations

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

typealias KtorContext = PipelineContext<Unit, ApplicationCall>
