package dev.rstockbridge.showstats2.api.models

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize
import kotlin.math.ceil

data class SetlistData(
    @field:Json(name = "setlist")
    val setlists: List<Setlist>,
    private val itemsPerPage: Int,
    @field:Json(name = "total")
    private val numberOfSetlists: Int
) {
    val numberOfPages: Int
        get() = ceil((numberOfSetlists.toDouble()) / (itemsPerPage.toDouble())).toInt()
}

@Parcelize
data class Setlist(
    val eventDate: String,
    val artist: SetlistArtist,
    val venue: SetlistVenue,
    val url: String
) : Parcelable

@Parcelize
data class SetlistArtist(
    val mbid: String,
    val name: String
) : Parcelable

@Parcelize
data class SetlistVenue(
    val name: String,
    val city: City
) : Parcelable

@Parcelize
data class City(
    val name: String,
    @field:Json(name = "coords")
    val coordinates: Coordinates
) : Parcelable

@Parcelize
data class Coordinates(
    @field:Json(name = "lat")
    val latitude: Double,
    @field:Json(name = "long")
    val longitude: Double
) : Parcelable
