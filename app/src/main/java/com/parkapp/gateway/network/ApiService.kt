package com.parkapp.gateway.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("gateway/tasks/next")
    suspend fun getNextTask(@Query("secret") secret: String): NextTaskResponse

    @POST("gateway/tasks/{id}/done")
    suspend fun markDone(
        @Path("id") taskId: Int,
        @Query("secret") secret: String,
    ): Response<Unit>

    @POST("gateway/tasks/{id}/failed")
    suspend fun markFailed(
        @Path("id") taskId: Int,
        @Query("secret") secret: String,
    ): Response<Unit>
}
