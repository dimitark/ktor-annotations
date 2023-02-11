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
dependencies {
    ...
    ksp(project(":processor"))
    implementation(project(":annotations"))
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

# Debugging the KSP processor

Run the Gradle daemon with:
`./gradlew clean build -Dorg.gradle.debug=true -Dkotlin.daemon.jvm.options="-Xdebug,-Xrunjdwp:transport=dt_socket\,address=5005\,server=y\,suspend=n" --info`

And in IntelliJ IDEA, create a "Remote JVM Debug" configuration with the default options and run it. The build process started above, will suspend until IntelliJ IDEA attaches to the debugger. 