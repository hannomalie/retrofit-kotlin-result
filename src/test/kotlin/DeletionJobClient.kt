import com.github.michaelbull.result.Result
import retrofit2.Call
import retrofit2.http.Headers
import retrofit2.http.POST

interface DeletionJobClient {
    @Headers("Content-Type: application/json")
    @POST("/deletionJob")
    fun createDeletionJob(): Call<Unit>

    @Headers("Content-Type: application/json")
    @POST("/deletionJob")
    fun createDeletionJobTyped(): Call<Result<Unit, ApiError>>

    @Headers("Content-Type: application/json")
    @POST("/deletionJob")
    suspend fun createDeletionJobAsyncTyped(): Result<Unit, ApiError>
}