package com.github.dimitark.ktorannotations.annotations

import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*

@Target(CLASS)
@Retention(SOURCE)
annotation class RouteController

@Target(FUNCTION)
@Retention(SOURCE)
annotation class Get(val value: String)

@Target(FUNCTION)
@Retention(SOURCE)
annotation class Put(val value: String)

@Target(FUNCTION)
@Retention(SOURCE)
annotation class Post(val value: String)

@Target(FUNCTION)
@Retention(SOURCE)
annotation class Delete(val value: String)
