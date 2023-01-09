import retrofit2.Call
import retrofit2.http.Headers

interface DeletionJobClient {
    @Headers("Content-Type: application/json")
    fun createDeletionJob(): Call<Unit>
}