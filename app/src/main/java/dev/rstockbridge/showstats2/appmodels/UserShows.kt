package dev.rstockbridge.showstats2.appmodels

import android.os.Parcelable
import dev.rstockbridge.showstats2.api.models.Setlist
import dev.rstockbridge.showstats2.api.models.SetlistVenue
import kotlinx.parcelize.Parcelize

fun constructShowsFromSetlists(setlists: List<Setlist>): List<Show> {
    val result = mutableListOf<Show>()

    setlists.forEach { setlist ->
        val show = Show(setlist.eventDate, setlist.venue)

        if (!result.contains(show)) {
            result.add(show)
        }

        result[result.indexOf(show)].addArtist(
            setlist.artist.mbid,
            setlist.artist.name,
            setlist.url
        )
    }

    return result
}

@Parcelize
data class Show(
    val eventDate: String,
    val venue: SetlistVenue,
    val artistIdNameMap: HashMap<String, String> = HashMap(),
    val artistNameUrlMap: MutableMap<String, String> = HashMap()
) : Parcelable {
    fun getArtistNames(): List<String> {
        return artistIdNameMap.values.sorted()
    }

    fun getUrls(): List<String> {
        val result = mutableListOf<String>()
        val artistNames = getArtistNames()
        for (artistName in artistNames) {
            result.add(artistNameUrlMap[artistName]!!)
        }
        return result
    }

    fun addArtist(
        artistId: String,
        artist: String,
        artistSetlistUrl: String
    ) {
        if (!artistIdNameMap.containsKey(artistId)) {
            artistIdNameMap[artistId] = artist
        }
        if (!artistNameUrlMap.containsKey(artist)) {
            artistNameUrlMap[artist] = artistSetlistUrl
        }
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val show = o as Show
        return eventDate == show.eventDate &&
                venue.name == show.venue.name
    }

    override fun hashCode(): Int {
        var result = eventDate.hashCode()
        result = 31 * result + venue.hashCode()
        return result
    }
}
