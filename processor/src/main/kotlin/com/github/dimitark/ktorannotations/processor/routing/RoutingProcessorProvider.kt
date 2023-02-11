package com.github.dimitark.ktorannotations.processor.routing

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class RoutingProcessorProvider: SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = RoutingProcessor(
        logger = environment.logger,
        codeGenerator = environment.codeGenerator
    )
}
