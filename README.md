# Usage

## Gradle configuration
The following plugin is needed in your gradle build script
```
plugins {
    ...
    id("com.google.devtools.ksp") version "1.8.0-1.0.9"
    ...
}
```

And the following dependencies are needed
```
repositories {
    ...
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    ...
    ksp("com.github.dimitark.ktor-annotations:processor:0.0.2")
    implementation("com.github.dimitark.ktor-annotations:annotations:0.0.2")
    ...
}
```

## Code
To use the Ktor annotation configuration, you need to define RouteControllers. You do that by annotating classes with the `@RouteController` annotation. 
And then in the controller, you can define your endpoints, like in the example below.
```kotlin
@RouteController
class SampleController {
    @Get("/home")
    suspend fun koinLess(context: KtorContext) {
        context.call.respondText("Home content...")
    }
}
```

This will generate a Ktor Application extension function (`com.github.dimitark.ktor.routing.ktorRoutingAnnotationConfig`), that defines all the routes that are annotated. 

To start using them, you must call that function from within the Ktor Application.

```kotlin
...
import com.github.dimitark.ktor.routing.ktorRoutingAnnotationConfig
...

fun main() {
    embeddedServer(Netty, port = 8000) {
        ...
        
        ktorRoutingAnnotationConfig()
    }.start(wait = true)
}
```

# Options
By default, the processor generates code that depends on Koin and Ktor's Authentication. 
To disable those, you need to pass some arguments to kps. In you gradle build script add:
```
ksp {
    arg("ktor-annotations-auth", "disabled")
    arg("ktor-annotations-koin", "disabled")
}
```

# Debugging the KSP processor

Run the Gradle daemon with:
`./gradlew clean build -Dorg.gradle.debug=true -Dkotlin.daemon.jvm.options="-Xdebug,-Xrunjdwp:transport=dt_socket\,address=5005\,server=y\,suspend=n" --info`

And in IntelliJ IDEA, create a "Remote JVM Debug" configuration with the default options and run it. The build process started above, will suspend until IntelliJ IDEA attaches to the debugger. 