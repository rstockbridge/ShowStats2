package dev.rstockbridge.showstats2.api

import dev.rstockbridge.showstats2.api.models.SetlistData

interface DataFetcher {
    suspend fun getSetlistData(userId: String, pageIndex: Int): Response<SetlistData>
}
