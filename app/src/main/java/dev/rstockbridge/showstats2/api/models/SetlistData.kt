package dev.rstockbridge.showstats2.api.models

import com.squareup.moshi.Json
import kotlin.math.ceil

data class SetlistData(
    @Json(name = "setlist")
    val setlists: List<Setlist>,
    private val itemsPerPage: Int,
    @Json(name = "total")
    private val numberOfSetlists: Int
) {
    val numberOfPages: Int
        get() = ceil((numberOfSetlists.toDouble()) / (itemsPerPage.toDouble())).toInt()
}

data class Setlist(
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
    val coordinates: Coordinates
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)
