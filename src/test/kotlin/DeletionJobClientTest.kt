import com.github.michaelbull.result.Ok
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory


class DeletionJobClientTest {

    @Test
    fun `create deletion job via regular api succeeds`() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(201))

        val retrofit = createRetrofit(server.url("/").toString())

        val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

        val response = deletionJobClient.createDeletionJob().execute()
        Assertions.assertTrue(response.isSuccessful)

        server.shutdown()
    }

    @Test
    fun `create deletion job via typed api succeeds`() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(201))

        val retrofit = createRetrofit(server.url("/").toString())

        val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

        val response = deletionJobClient.createDeletionJobTyped().execute().body()
        Assertions.assertInstanceOf(Ok::class.java, response)

        server.shutdown()
    }

    private fun createRetrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .addConverterFactory(JacksonConverterFactory.create())
        .addCallAdapterFactory(ResultCallAdapterFactory())
        .baseUrl(baseUrl)
        .build()
}