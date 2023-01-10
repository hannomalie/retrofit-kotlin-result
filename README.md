## kotlin-result Call adapter for Retrofit

Two-file implementation of a call adapter that lets your [Retrofit](https://square.github.io/retrofit/) interface return result objects
from the [kotlin-result](https://github.com/michaelbull/kotlin-result) library.

### TLDR:

```kotlin

sealed interface ApiError
data class HttpError(val code: Int, val body: String?) : ApiError
data class NetworkError(val throwable: Throwable) : ApiError
object CircuitBreakerOpen: ApiError

interface DeletionJobClient {
    @Headers("Content-Type: application/json")
    @POST("/deletionJob")
    suspend fun createDeletionJobAsyncTyped(): Result<Int, ApiError> // Note the return type
}

@Test
fun `deletion job lifecycle unhappy path is nice with comprehensions`() = withServerAndRetrofit { server, retrofit ->
    server.enqueue(MockResponse().setResponseCode(503)) // call will result in error

    val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

    runBlocking {
        val result = asyncBinding { // enables comprehension syntax
            deletionJobClient.createDeletionJobAsyncTyped().bind() // calling a suspending function
            deletionJobClient.cancelDeletionJobAsyncTyped().bind() // calling a suspending function
        }
        when(result) { // compiler error, when not exhaustive
            is Ok -> {}
            is Err<ApiError> -> when(result.error) { // compiler error, when not exhaustive
                CircuitBreakerOpen -> throw AssertionFailedError("Expected HttpError, got CircuitBreakerOpen")
                is HttpError -> { }
                is NetworkError -> throw AssertionFailedError("Expected HttpError, got NetworkError")
                is UnknownApiError -> throw AssertionFailedError("Expected HttpError, got UnknownApiError")
            }
        }
    }
}
```

### Why?

HTTP calls can naturally result in failures of different kinds. Modelling them with exceptions inherits
the typical problems that come with (unchecked) exceptions: they don't show up in the method signatures anymore.
The same goes for other unhappy paths of remote calls that usually happen in distributed systems: 
When the circuitbreaker opens, when rate limiting kicks in etc. pp.
Modelling all states as success or failure and using Kotlin's sealed types for success and/or error results enables
exhaustiveness checks by the compiler. You would never miss a failure state anywhere again.
Kotlin's built-in Result type currently sadly lacks a type parameter for the error type, 
but [kotlin-result](https://github.com/michaelbull/kotlin-result) offers a very lightweight 
implementation of a Result type with two type parameters.
This is a nice fit for results of a Retrofit interface, and luckily Retrofit has an extension API that lets you
implement result type adapters.
Take a look at the example test method above and the comments to see where the compiler can save you from leaving
a hole in your robust client implementations.