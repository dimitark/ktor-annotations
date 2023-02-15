package com.github.dimitark.ktorannotations.processor.routing.models

import com.google.devtools.ksp.symbol.KSClassDeclaration

data class ControllerDef(val uuid: Int, val clazz: KSClassDeclaration)
