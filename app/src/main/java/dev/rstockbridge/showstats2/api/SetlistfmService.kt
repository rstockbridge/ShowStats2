package dev.rstockbridge.showstats2.api

import dev.rstockbridge.showstats2.api.models.SetlistData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SetlistfmService {

    @GET("{userId}/attended")
    suspend fun getSetlistData(
        @Path("userId") userId: String,
        @Query("p") pageIndex: Int
    ): Response<SetlistData>
}
