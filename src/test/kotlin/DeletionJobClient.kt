import retrofit2.Call
import retrofit2.http.Headers
import retrofit2.http.POST

interface DeletionJobClient {
    @Headers("Content-Type: application/json")
    @POST("/deletionJob")
    fun createDeletionJob(): Call<Unit>
}