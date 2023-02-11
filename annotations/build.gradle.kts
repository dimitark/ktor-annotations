plugins {
    kotlin("jvm")
    id("maven")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.ktor:ktor-server-core-jvm:2.2.3")
}