package dev.rstockbridge.showstats2.api.models

import com.squareup.moshi.Json
import kotlin.math.ceil

data class SetlistData(
    @field:Json(name = "setlist")
    val shows: List<Show>,
    private val itemsPerPage: Int,
    @field:Json(name = "total")
    private val numberOfSetlists: Int
) {
    val numberOfPages: Int
        get() = ceil((numberOfSetlists.toDouble()) / (itemsPerPage.toDouble())).toInt()
}

data class Show(
    val eventDate: String,
    val artist: SetlistArtist,
    val venue: SetlistVenue,
    val url: String
)

data class SetlistArtist(
    val name: String
)

data class SetlistVenue(
    val name: String,
    val city: City
)

data class City(
    val name: String,
    @field:Json(name = "coords")
    val coordinates: Coordinates
)

data class Coordinates(
    @field:Json(name = "lat")
    val latitude: Double,
    @field:Json(name = "long")
    val longitude: Double
)
