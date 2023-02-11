package com.github.dimitark.ktorannotations.processor.routing

import com.google.devtools.ksp.processing.CodeGenerator
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
        fileBuilder
            .addImport("org.koin.dsl", "module")
            .addImport("org.koin.ktor.ext", "inject")
            .addImport("org.koin.core.context", "loadKoinModules")

        // Add the imports for all the http methods
        val uniqueMethods = routes.values.flatten().map { it.method }.toSet().toTypedArray()
        fileBuilder.addImport(KtorRoutingPackageName, *(arrayOf("routing") + uniqueMethods))

        // Add the routes config fun
        fileBuilder.addFunction(routes.generateConfigFun())

        // Write the file
        fileBuilder.build().writeTo(codeGenerator, aggregating = false)
    }

    private fun Map<ControllerDef, List<RouteFun>>.generateConfigFun(): FunSpec {
        val (koinDef, controllersInject) = controllers()

        return FunSpec
            .builder(RoutingConfigFunName)
            .receiver(ClassName.bestGuess(KtorApplicationClassName))
            .addCode(
                CodeBlock
                    .builder()
                    .add(generateKoinModuleDefinition(koinDef))
                    .add(controllersInject)
                    .add(generateRoutingConfig())
                    .build()
            )
            .build()
    }

    private fun generateKoinModuleDefinition(koinDef: CodeBlock): CodeBlock = CodeBlock.builder()
        .addStatement("""loadKoinModules(module {""")
        .add(koinDef)
        .addStatement("""})""")
        .build()

    private fun Map<ControllerDef, List<RouteFun>>.controllers(): Pair<CodeBlock, CodeBlock> {
        val controllersKoin = CodeBlock.builder()
        val controllersCode = CodeBlock.builder()

        keys.forEach { controller ->
            val fullName = controller.clazz.fullName()
            val constructorParams = (controller.clazz.primaryConstructor?.parameters ?: emptyList()).joinToString(", ") { "get()" }
            controllersKoin.addStatement("""single { ${fullName}($constructorParams) }""")

            controllersCode.addStatement("""val controller${controller.uuid} by inject<${fullName}>()""")
        }

        return controllersKoin.build() to controllersCode.build()
    }

    private fun Map<ControllerDef, List<RouteFun>>.generateRoutingConfig(): CodeBlock {
        val funCode = CodeBlock.builder().addStatement("routing {")

        entries.forEach { (controller, functions) ->
            functions.forEach { function ->
                funCode.add(
                    CodeBlock.builder()
                        .addStatement("""   ${function.method}("${function.path}") {""")
                        .functionCall(controller, function)
                        .addStatement("""   }""")
                        .build()
                )
            }
        }

        return funCode.addStatement("}").build()
    }

    private fun CodeBlock.Builder.functionCall(controller: ControllerDef, function: RouteFun) = apply {
        addStatement("""controller${controller.uuid}.${function.funName}(this)""")
    }

    private fun KSClassDeclaration.fullName() = listOf(packageName.getQualifier(), packageName.getShortName(), simpleName.getShortName()).joinToString(".")
}
