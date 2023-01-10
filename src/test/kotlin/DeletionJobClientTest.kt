import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import junit.framework.AssertionFailedError
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import com.github.michaelbull.result.coroutines.binding.binding as asyncBinding


class DeletionJobClientTest {
    @Nested
    inner class Creation {

        @Test
        fun `create deletion job via regular api succeeds`() = withServerAndRetrofit { server, retrofit ->
            server.enqueue(MockResponse().setResponseCode(201))

            val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

            val response = deletionJobClient.createDeletionJob().execute()
            Assertions.assertTrue(response.isSuccessful)
        }

        @Test
        fun `create deletion job via typed api succeeds`() = withServerAndRetrofit { server, retrofit ->
            server.enqueue(MockResponse().setResponseCode(201))

            val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

            val response = deletionJobClient.createDeletionJobTyped().execute().body()
            assertInstanceOf(Ok::class.java, response)
        }

        @Test
        fun `create deletion job via async typed api succeeds`() = withServerAndRetrofit { server, retrofit ->
            server.enqueue(MockResponse().setResponseCode(201))

            val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

            runBlocking {
                val response = deletionJobClient.createDeletionJobAsyncTyped()
                assertInstanceOf(Ok::class.java, response)
            }
        }

        @Test
        fun `create deletion job via async typed api fails on error`() = withServerAndRetrofit { server, retrofit ->
            server.enqueue(MockResponse().setResponseCode(503))

            val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

            runBlocking {
                val response = deletionJobClient.createDeletionJobAsyncTyped()
                assertInstanceOf(Err::class.java, response)
            }
        }
    }

    @Nested
    inner class Cancellation {

        @Test
        fun `cancel deletion job via regular api succeeds`() = withServerAndRetrofit { server, retrofit ->
            server.enqueue(MockResponse().setResponseCode(200).setBody("""{"cancelled":"true"}"""))

            val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

            val response = deletionJobClient.cancelDeletionJob().execute()
            Assertions.assertTrue(response.isSuccessful)
        }

        @Test
        fun `cancel deletion job via typed api succeeds`() = withServerAndRetrofit { server, retrofit ->
            server.enqueue(MockResponse().setResponseCode(200).setBody("""{"cancelled":"true"}"""))

            val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

            val response = deletionJobClient.cancelDeletionJobTyped().execute().body()
            assertInstanceOf(Ok::class.java, response)
        }

        @Test
        fun `cancel deletion job via async typed api succeeds`() = withServerAndRetrofit { server, retrofit ->
            server.enqueue(MockResponse().setResponseCode(200).setBody("""{"cancelled":"true"}"""))

            val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

            runBlocking {
                val response = deletionJobClient.cancelDeletionJobAsyncTyped()
                assertInstanceOf(Ok::class.java, response)
            }
        }
    }

    @Nested
    inner class UseCase {
        @Test
        fun `deletion job lifecycle happy path is nice with comprehensions`() = withServerAndRetrofit { server, retrofit ->
            server.enqueue(MockResponse().setResponseCode(201))
            server.enqueue(MockResponse().setResponseCode(200).setBody("""{"cancelled":"true"}"""))

            val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

            runBlocking {
                val result = asyncBinding {
                    deletionJobClient.createDeletionJobAsyncTyped().bind()
                    deletionJobClient.cancelDeletionJobAsyncTyped().bind()
                }
                assertInstanceOf(Ok::class.java, result)
            }
        }

        @Test
        fun `deletion job creation short circuits nice with comprehensions`() = withServerAndRetrofit { server, retrofit ->
            server.enqueue(MockResponse().setResponseCode(503))
            server.enqueue(MockResponse().setResponseCode(503))

            val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

            runBlocking {
                val result = asyncBinding {
                    async { deletionJobClient.createDeletionJobAsyncTyped().bind() }
                    async { deletionJobClient.cancelDeletionJobAsyncTyped().bind() }.await()
                }
                assertInstanceOf(Err::class.java, result)
                when(val error = (result as Err).error) {
                    is HttpError -> assertEquals(error.code, 503)
                    is NetworkError -> throw AssertionFailedError("Expected an HttpError result but got NetworkError")
                    is UnknownApiError -> throw AssertionFailedError("Expected an HttpError result but got NetworkError")
                    CircuitBreakerOpen -> throw AssertionFailedError("Expected an HttpError result but got CircuitBreakerOpen")
                }
            }
        }
    }

    @Nested
    inner class CircuitBreaker {
        @Test
        fun `open circuitbreaker results in proper error`() = withServerAndRetrofit { server, retrofit ->
            repeat(10) {
                server.enqueue(MockResponse().setResponseCode(403))
            }

            val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

            repeat(10) {
                assertInstanceOf(Err::class.java, deletionJobClient.createDeletionJobTyped().execute().body())
            }
            val result = deletionJobClient.createDeletionJobTyped().execute().body()!!
            assertInstanceOf(Err::class.java, result)

            when(val error = (result as Err<*>).error!!) {
                CircuitBreakerOpen -> { }
                else -> throw AssertionFailedError("Expected an CircuitBreakerOpen result but got ${error.javaClass}")
            }
        }

        @Test
        fun `open circuitbreaker results in proper error using async api`() = withServerAndRetrofit { server, retrofit ->
            repeat(10) { server.enqueue(MockResponse().setResponseCode(403)) }

            val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

            val result = runBlocking {
                repeat(10) {
                    assertInstanceOf(Err::class.java, deletionJobClient.createDeletionJobAsyncTyped())
                }
                deletionJobClient.createDeletionJobAsyncTyped()
            }
            assertInstanceOf(Err::class.java, result)

            when(val error = (result as Err<*>).error!!) {
                CircuitBreakerOpen -> { }
                else -> throw AssertionFailedError("Expected an CircuitBreakerOpen result but got ${error.javaClass}")
            }
        }
    }
}