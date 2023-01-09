import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

fun withServerAndRetrofit(block: (MockWebServer, Retrofit) -> Unit) {
    MockWebServer().use { server ->
        block(server, createRetrofit(server.url("/").toString()))

    }
}

private fun createRetrofit(baseUrl: String): Retrofit = Retrofit.Builder()
    .addConverterFactory(JacksonConverterFactory.create())
    .addCallAdapterFactory(ResultCallAdapterFactory())
    .baseUrl(baseUrl)
    .build()