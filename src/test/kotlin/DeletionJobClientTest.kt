import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory


class DeletionJobClientTest {

    @Test
    fun foo() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("hello, world!"))

        val retrofit = createRetrofit(server.url("/").toString())

        val deletionJobClient = retrofit.create(DeletionJobClient::class.java)

        val response = deletionJobClient.createDeletionJob().execute()
        Assertions.assertTrue(response.isSuccessful)
        server.shutdown()
    }

    private fun createRetrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .addConverterFactory(JacksonConverterFactory.create())
        .baseUrl(baseUrl)
        .build()
}