package com.github.dimitark.ktorannotations.processor.routing

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.writeTo

object RouteConfigCodeGenerator {
    private const val RoutingConfigPackageName = "com.github.dimitark.ktor.routing"
    private const val RoutingConfigFileName = "KtorRoutingAnnotationConfig"
    private const val RoutingConfigFunName = "ktorRoutingAnnotationConfig"

    private const val KtorApplicationClassName = "io.ktor.server.application.Application"
    private const val KtorRoutingPackageName = "io.ktor.server.routing"

    fun generate(visitor: RouteVisitor, codeGenerator: CodeGenerator) {
        val fileBuilder = FileSpec.builder(RoutingConfigPackageName, RoutingConfigFileName)
        val routes = visitor.resolve()

        // Add the standard imports
        if (visitor.authEnabled) {
            fileBuilder.addImport("io.ktor.server.auth", "authenticate")
        }

        if (visitor.koinEnabled) {
            fileBuilder
                .addImport("org.koin.dsl", "module")
                .addImport("org.koin.ktor.ext", "inject")
                .addImport("org.koin.core.context", "loadKoinModules")
        }

        // Add the imports for all the http methods
        val uniqueMethods = routes.values.flatten().map { it.method }.toSet().toTypedArray()
        fileBuilder.addImport(KtorRoutingPackageName, *(arrayOf("routing") + uniqueMethods))

        // Add the routes config fun
        fileBuilder.addFunction(routes.generateConfigFun(visitor))

        // Write the file
        fileBuilder.build().writeTo(codeGenerator, Dependencies(aggregating = true, *visitor.dependencies().toTypedArray()))
    }

    private fun Map<ControllerDef, List<RouteFun>>.generateConfigFun(visitor: RouteVisitor): FunSpec {
        val (koinDef, controllersInject) = controllers(visitor)

        val funCode = CodeBlock.builder()

        if (visitor.koinEnabled) {
            funCode.add(generateKoinModuleDefinition(koinDef))
        }

        funCode.add(controllersInject).add(generateRoutingConfig())

        return FunSpec
            .builder(RoutingConfigFunName)
            .receiver(ClassName.bestGuess(KtorApplicationClassName))
            .addCode(funCode.build())
            .build()
    }

    private fun generateKoinModuleDefinition(koinDef: CodeBlock): CodeBlock = CodeBlock.builder()
        .addStatement("""loadKoinModules(module {""")
        .add(koinDef)
        .addStatement("""})""")
        .build()

    private fun Map<ControllerDef, List<RouteFun>>.controllers(visitor: RouteVisitor): Pair<CodeBlock, CodeBlock> {
        val controllersKoin = CodeBlock.builder()
        val controllersCode = CodeBlock.builder()

        keys.forEach { controller ->
            val fullName = controller.clazz.fullName()
            val constructorParams = (controller.clazz.primaryConstructor?.parameters ?: emptyList()).joinToString(", ") { "get()" }
            controllersKoin.addStatement("""single { ${fullName}($constructorParams) }""")

            if (visitor.koinEnabled) {
                controllersCode.addStatement("""val controller${controller.uuid} by inject<${fullName}>()""")
            } else {
                controllersCode.addStatement("""val controller${controller.uuid} = ${fullName}()""")
            }
        }

        return controllersKoin.build() to controllersCode.build()
    }

    private fun Map<ControllerDef, List<RouteFun>>.generateRoutingConfig(): CodeBlock {
        val funCode = CodeBlock.builder().addStatement("routing {")

        // Group them by authentication provider
        val groupedByAuthProvider = entries
            .flatMap { it.value.map { value -> value to it.key } }
            .groupBy { it.first.authenticationProvider }

        groupedByAuthProvider.forEach { (authProvider, functions) ->

            if (authProvider != null) {
                funCode.addStatement("""authenticate${authProvider.takeIf { it.isNotBlank() }?.inBracketsAndQuotes() ?: ""} {""")
            }

            functions.forEach { (function, controller) ->
                funCode.add(
                    CodeBlock.builder()
                        .addStatement("""   ${function.method}("${function.path}") {""")
                        .functionCall(controller, function)
                        .addStatement("""   }""")
                        .build()
                )
            }

            if (authProvider != null) {
                funCode.addStatement("""}""")
            }
        }

        return funCode.addStatement("}").build()
    }

    private fun CodeBlock.Builder.functionCall(controller: ControllerDef, function: RouteFun) = apply {
        addStatement("""controller${controller.uuid}.${function.funName}(this)""")
    }

    private fun KSClassDeclaration.fullName() = listOf(packageName.getQualifier(), packageName.getShortName(), simpleName.getShortName()).joinToString(".")

    private fun String.inBracketsAndQuotes() = """("$this")"""
}
