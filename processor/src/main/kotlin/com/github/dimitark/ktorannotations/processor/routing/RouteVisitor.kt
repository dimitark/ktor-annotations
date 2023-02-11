package com.github.dimitark.ktorannotations.processor.routing

import com.github.dimitark.ktorannotations.annotations.*
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

class RouteVisitor(private val logger: KSPLogger, val koinEnabled: Boolean, val authEnabled: Boolean): KSEmptyVisitor<KClass<out Annotation>, Unit>() {
    private val atomicInt = AtomicInteger()

    private val controllers = mutableSetOf<ControllerDef>()
    private val functions = mutableListOf<RouteFun>()
    private val dependencyFiles = mutableSetOf<KSFile>()

    fun dependencies() = dependencyFiles

    fun resolve(): Map<ControllerDef, List<RouteFun>> = functions
        .groupBy { it.parent }
        .filter { entry -> entry.key in controllers }
        .mapKeys { entry -> controllers.first { it == entry.key } }

    override fun defaultHandler(node: KSNode, data: KClass<out Annotation>) {}

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: KClass<out Annotation>) {
        if (data != RouteController::class) { return }

        if (!koinEnabled && classDeclaration.primaryConstructor?.parameters?.isNotEmpty() == true) {
            logger.error("RouteController parameters only supported when Koin is enabled", classDeclaration)
        }

        if (!authEnabled && classDeclaration.authProvider() != null) {
            logger.error("Protected routes only supported when Auth is enabled", classDeclaration)
        }

        controllers.add(
            ControllerDef(
                uuid = atomicInt.getAndIncrement(),
                clazz = classDeclaration
            )
        )

        classDeclaration.containingFile?.let { dependencyFiles.add(it) }
    }

    @OptIn(KspExperimental::class)
    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: KClass<out Annotation>) {
        val (method, path) = when(data) {
            Get::class -> "get" to function.getAnnotationsByType(Get::class).first().value
            Post::class -> "post" to function.getAnnotationsByType(Post::class).first().value
            Put::class -> "put" to function.getAnnotationsByType(Put::class).first().value
            Delete::class -> "delete" to function.getAnnotationsByType(Delete::class).first().value
            else -> return
        }

        val parent = function.parent?.let { parent -> controllers.firstOrNull { it.clazz == parent } } ?: return
        val authProvider = function.authProvider() ?: parent.clazz.authProvider()

        if (!authEnabled && authProvider != null) {
            logger.error("Protected routes only supported when Auth is enabled", function)
        }

        functions.add(
            RouteFun(
                path = path,
                method = method,
                funPackage = function.qualifiedName?.getQualifier() ?: "",
                funName = function.simpleName.getShortName(),
                parent = parent,
                authenticationProvider = authProvider
            )
        )

        function.containingFile?.let { dependencyFiles.add(it) }
    }

    @OptIn(KspExperimental::class)
    private fun KSDeclaration.authProvider() = getAnnotationsByType(ProtectedRoute::class).firstOrNull()?.value
}

data class ControllerDef(val uuid: Int, val clazz: KSClassDeclaration)

data class RouteFun(
    val path: String,
    val method: String,
    val funPackage: String,
    val funName: String,
    val parent: ControllerDef,
    val authenticationProvider: String? = null
)
