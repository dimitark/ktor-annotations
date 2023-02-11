plugins {
    kotlin("jvm")
    id("maven-publish")
    `java-library`
}

group = "com.github.dimitark.ktorannotations"
version = "0.0.1"

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.dimitark.ktorannotations"
            artifactId = "annotations"
            version = "0.0.1"

            from(components["java"])
        }
    }
}

dependencies {
    compileOnly("io.ktor:ktor-server-core-jvm:2.2.3")
}