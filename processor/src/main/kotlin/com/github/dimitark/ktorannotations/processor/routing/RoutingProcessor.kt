package com.github.dimitark.ktorannotations.processor.routing

import com.github.dimitark.ktorannotations.annotations.*
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import kotlin.reflect.KClass

val AnnotationClasses = setOf(RouteController::class, Get::class, Post::class, Put::class, Delete::class)

class RoutingProcessor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator, options: Map<String, String>): SymbolProcessor {

    private val authEnabled = options["ktor-annotations-auth"] != "disabled"
    private val koinEnabled = options["ktor-annotations-koin"] != "disabled"

    private val visitor = RouteVisitor(logger = logger, koinEnabled = koinEnabled, authEnabled = authEnabled)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("Ktor Annotation Routing processor...")
        logger.info("Ktor Annotations: auth enabled = ${authEnabled}; koin enabled = $koinEnabled")

        AnnotationClasses.forEach { resolver.visitAnnotated(it) }
        return emptyList()
    }

    override fun finish() {
        RouteConfigCodeGenerator.generate(visitor, codeGenerator)
    }

    private fun Resolver.visitAnnotated(annotationClass: KClass<out Annotation>) {
        annotationClass.qualifiedName?.let { annotation -> getSymbolsWithAnnotation(annotation).forEach { it.accept(visitor, annotationClass) } }
    }
}
