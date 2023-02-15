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
    ksp("com.github.dimitark.ktor-annotations:processor:0.0.3")
    implementation("com.github.dimitark.ktor-annotations:annotations:0.0.3")
    ...
}
```

## Code
To use the Ktor annotation configuration, you need to define RouteControllers. You do that by annotating classes with the `@RouteController` annotation. 
And then in the controller, you can define your endpoints, like in the example below.

**If Koin is not disabled (default behaviour)** you can Autowire instances of classes defined in your Koin modules by defining them in the controller's primary constructor. 

You can define the following parameters in your annotated functions:
* `KtorContext` - which is a `typealias` to `PipelineContext<Unit, ApplicationCall>`
* `PipelineContext<Unit, ApplicationCall>`
* `ApplicationCall`

```kotlin
@RouteController
class TestController(private val service: Service) {

    @Get("/in-controller-context")
    suspend fun inControllerContext(context: KtorContext) {
        context.call.respondText("In Controller Context... ${service.test()}")
    }

    @Get("/in-controller-call")
    suspend fun inControllerCall(call: ApplicationCall) {
        call.respondText("In Controller Call... ${service.test()}")
    }

    @Get("/in-controller-pipeline")
    suspend fun inControllerPipeline(pipeline: PipelineContext<Unit, ApplicationCall>) {
        pipeline.call.respondText("In Controller Pipeline... ${service.test()}")
    }

    @Post("/{test}")
    suspend fun inControllerPost(call: ApplicationCall) {
        call.respondText("In Controller Post... ${service.test()} - ${call.parameters["test"]}")
    }
}
```

### Protected

If the auth is not disabled (default behaviour) - you can annotate your route functions with the `@Protected(authProviderName)` annotation. 
```kotlin
@ProtectedRoute
@Get("/protected")
suspend fun protected(call: ApplicationCall) {
    call.respondText("Protected 1")
}

@ProtectedRoute("jwt-auth-provider")
@Get("/protected-jwt")
suspend fun protectedJwt(call: ApplicationCall) {
    call.respondText("Protected named 0")
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