plugins {
    id("com.google.devtools.ksp") version "1.8.0-1.0.9"
    kotlin("jvm")
}

group = "com.github.dimitark.ktorannotations"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":annotations"))
    ksp(project(":processor"))

    implementation("io.insert-koin:koin-ktor:3.3.1")
    implementation("io.ktor:ktor-server-core-jvm:2.2.3")
    implementation("io.ktor:ktor-server-netty-jvm:2.2.3")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.2.3")
    implementation("io.ktor:ktor-server-default-headers-jvm:2.2.3")
    implementation("io.ktor:ktor-server-auth:2.2.3")
    implementation("ch.qos.logback:logback-classic:1.4.5")
}