import com.github.michaelbull.result.Result
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

private class ResultCallAdapter<R>(
    private val successType: Type
) : CallAdapter<R, Call<Result<R, ApiError>>> {

    override fun adapt(call: Call<R>): Call<Result<R, ApiError>> = ResultCall(call, successType)

    override fun responseType(): Type = successType
}

internal class ResultCallAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) return null
        check(returnType is ParameterizedType) { "Return type must be a parameterized type." }

        val responseType = getParameterUpperBound(0, returnType)
        if (getRawType(responseType) != Result::class.java) return null
        check(responseType is ParameterizedType) { "Response type must be a parameterized type." }

        val leftType = getParameterUpperBound(1, responseType)
        if (getRawType(leftType) != ApiError::class.java) return null

        val rightType = getParameterUpperBound(0, responseType)
        return ResultCallAdapter<Any>(rightType)
    }
}