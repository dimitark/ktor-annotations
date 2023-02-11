package com.github.dimitark.ktorannotations.annotations

import kotlin.annotation.AnnotationTarget.*
import kotlin.annotation.AnnotationRetention.*

@Target(CLASS, FUNCTION)
@Retention(SOURCE)
annotation class ProtectedRoute(val value: String = "")
