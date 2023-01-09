import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.binding
import junit.framework.AssertionFailedError
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
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
            Assertions.assertInstanceOf(Ok::class.java, response)
        }

        @Test
        fun `create deletion job via async typed api succeeds`() = withServerAndRetrofit { server, retrofit ->
            server.enqueue(MockResponse().setResponseCode(201))

            val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

            runBlocking {
                val response = deletionJobClient.createDeletionJobAsyncTyped()
                Assertions.assertInstanceOf(Ok::class.java, response)
            }
        }

        @Test
        fun `create deletion job via async typed api fails on error`() = withServerAndRetrofit { server, retrofit ->
            server.enqueue(MockResponse().setResponseCode(503))

            val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

            runBlocking {
                val response = deletionJobClient.createDeletionJobAsyncTyped()
                Assertions.assertInstanceOf(Err::class.java, response)
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
            Assertions.assertInstanceOf(Ok::class.java, response)
        }

        @Test
        fun `cancel deletion job via async typed api succeeds`() = withServerAndRetrofit { server, retrofit ->
            server.enqueue(MockResponse().setResponseCode(200).setBody("""{"cancelled":"true"}"""))

            val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

            runBlocking {
                val response = deletionJobClient.cancelDeletionJobAsyncTyped()
                Assertions.assertInstanceOf(Ok::class.java, response)
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
                    async { deletionJobClient.createDeletionJobAsyncTyped().bind() }
                    val b = async { deletionJobClient.cancelDeletionJobAsyncTyped().bind() }
                    b.await()
                }
                Assertions.assertInstanceOf(Ok::class.java, result)
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
                Assertions.assertInstanceOf(Err::class.java, result)
                when(val error = (result as Err).error) {
                    is HttpError -> assertEquals(error.code, 503)
                    is NetworkError -> AssertionFailedError("Expected an HttpError result but got NetworkError")
                    is UnknownApiError -> AssertionFailedError("Expected an HttpError result but got NetworkError")
                }
            }
        }
    }
}