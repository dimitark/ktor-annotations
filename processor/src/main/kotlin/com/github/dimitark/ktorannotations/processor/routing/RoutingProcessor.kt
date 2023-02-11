package com.github.dimitark.ktorannotations.processor.routing

import com.github.dimitark.ktorannotations.annotations.*
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import kotlin.reflect.KClass

val AnnotationClasses = setOf(RouteController::class, Get::class, Post::class, Put::class, Delete::class)

class RoutingProcessor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator): SymbolProcessor {

    private val visitor = RouteVisitor()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("Ktor Routing processor...")
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
