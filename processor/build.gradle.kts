plugins {
    kotlin("jvm")
    id("maven-publish")
}

group = "com.github.dimitark.ktorannotations"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.8.0-1.0.8")

    implementation(project(":annotations"))

    implementation("com.squareup:kotlinpoet:1.12.0")
    implementation("com.squareup:kotlinpoet-ksp:1.12.0")
}
