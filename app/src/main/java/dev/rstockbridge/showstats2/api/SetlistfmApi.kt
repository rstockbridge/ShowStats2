package dev.rstockbridge.showstats2.api

import dev.rstockbridge.showstats2.api.models.SetlistData
import java.io.IOException
import javax.inject.Inject

class SetlistfmApi @Inject constructor() : DataFetcher {

    private val service: SetlistfmService =
        RetrofitWrapper.retrofit.create(SetlistfmService::class.java)

    override suspend fun getSetlistData(userId: String, pageIndex: Int): Response<SetlistData> {
        return try {
            val response = service.getSetlistData(userId, pageIndex)

            if (response.isSuccessful) {
                Response.Success(response.body()!!)
            } else {
                Response.Error
            }
        } catch (e: IOException) {
            Response.Failure(e)
        }
    }
}
