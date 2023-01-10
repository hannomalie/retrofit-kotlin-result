import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.Type

// Taken from https://proandroiddev.com/retrofit-calladapter-for-either-type-2145781e1c20
// and adjusted to Result<T, R>

sealed interface ApiError
data class HttpError(val code: Int, val body: String?) : ApiError
data class NetworkError(val throwable: Throwable) : ApiError
object CircuitBreakerOpen: ApiError
data class UnknownApiError(val throwable: Throwable) : ApiError

internal class ResultCall<R>(
    private val delegate: Call<R>,
    private val successType: Type,
) : Call<Result<R, ApiError>> {

    override fun enqueue(callback: Callback<Result<R, ApiError>>) = delegate.enqueue(
        object : Callback<R> {
            override fun onResponse(call: Call<R>, response: Response<R>) {
                callback.onResponse(this@ResultCall, Response.success(response.toResult(successType)))
            }

            override fun onFailure(call: Call<R>, throwable: Throwable) {
                val error = when (throwable) {
                    is CallNotPermittedException -> CircuitBreakerOpen
                    is IOException -> NetworkError(throwable)
                    else -> UnknownApiError(throwable)
                }
                callback.onResponse(this@ResultCall, Response.success(Err(error)))
            }
        }
    )

    override fun clone(): Call<Result<R, ApiError>> = ResultCall(delegate, successType)
    override fun execute(): Response<Result<R, ApiError>> = try {
        Response.success(delegate.execute().toResult(successType))
    } catch (e: CallNotPermittedException) {
        Response.success(Err(CircuitBreakerOpen))
    }
    override fun isExecuted() = delegate.isExecuted

    override fun cancel() {
        delegate.cancel()
    }

    override fun isCanceled() = delegate.isCanceled
    override fun request(): Request = delegate.request()
    override fun timeout(): Timeout = delegate.timeout()


}

private fun <R> Response<R>.toResult(successType: Type): Result<R, ApiError> {
    // Http error response (4xx - 5xx)
    if (!isSuccessful) {
        val errorBody = errorBody()?.string()
        return Err(HttpError(code(), errorBody))
    }

    // Http success response with body
    body()?.let { body -> return Ok(body) }

    // if we defined Unit as success type it means we expected no response body
    // e.g. in case of 204 No Content
    return if (successType == Unit::class.java) {
        @Suppress("UNCHECKED_CAST")
        Ok(Unit) as Result<R, ApiError>
    } else {
        @Suppress("UNCHECKED_CAST")
        Err(UnknownError("Response body was null")) as Result<R, ApiError>
    }
}