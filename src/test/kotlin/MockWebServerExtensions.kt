import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.retrofit.CircuitBreakerCallAdapter
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

fun withServerAndRetrofit(block: (MockWebServer, Retrofit) -> Unit) {
    MockWebServer().use { server ->
        block(server, createRetrofit(server.url("/").toString()))
    }
}

private fun createRetrofit(baseUrl: String): Retrofit = Retrofit.Builder()
    .addConverterFactory(JacksonConverterFactory.create(ObjectMapper().findAndRegisterModules().registerKotlinModule()))
    .addCallAdapterFactory(
        CircuitBreakerCallAdapter.of(
            CircuitBreaker.of(
                "default",
                CircuitBreakerConfig.custom()
                    .failureRateThreshold(50f)
                    .slidingWindowSize(10)
                    .build()
            )
        )
    )
    .addCallAdapterFactory(ResultCallAdapterFactory())
    .baseUrl(baseUrl)
    .build()