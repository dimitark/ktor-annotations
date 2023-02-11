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
    compileOnly("io.ktor:ktor-server-core-jvm:2.2.3")
}