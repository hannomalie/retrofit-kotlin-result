import com.github.michaelbull.result.Ok
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class DeletionJobClientTest {

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
}