package dev.rstockbridge.showstats2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.rstockbridge.showstats2.api.DataFetcher
import dev.rstockbridge.showstats2.api.Response
import dev.rstockbridge.showstats2.api.models.City
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MapViewModel(
    private val contextProvider: CoroutineContextProvider,
    private val dataFetcher: DataFetcher
) : ViewModel() {

    data class UserMessage(
        val uniqueId: String = UUID.randomUUID().toString(),
        val message: String
    )

    data class CitiesViewState(
        val cities: Set<City>?,
        val networkCallInProgress: Boolean,
        val userMessages: List<UserMessage> = emptyList()
    )

    sealed class CitiesResponse {
        data class Success(val cities: Set<City>) : CitiesResponse()
        object Error : CitiesResponse()
    }

    private val _viewState = MutableStateFlow(CitiesViewState(null, false))
    val viewState = _viewState.asStateFlow()

    fun fetchData(setlistfmId: String) {
        viewModelScope.launch {
            _viewState.value = CitiesViewState(null, true)

            when (val response = makeNetworkRequest(setlistfmId)) {
                is CitiesResponse.Success -> {
                    _viewState.update { currentViewState ->
                        currentViewState.copy(
                            cities = response.cities,
                            networkCallInProgress = false
                        )
                    }
                }
                CitiesResponse.Error -> {
                    _viewState.update { currentViewState ->
                        currentViewState.copy(
                            cities = null,
                            networkCallInProgress = false,
                            userMessages = currentViewState.userMessages + UserMessage(message = "Something has gone wrong!")
                        )
                    }
                }
            }
        }
    }

    private suspend fun makeNetworkRequest(setlistfmId: String): CitiesResponse {
        return withContext(contextProvider.IO) {

            val cities: MutableSet<City> = mutableSetOf()
            val page1Response = dataFetcher.getSetlistData(setlistfmId, 1)

            if (page1Response is Response.Success) {
                for (show in page1Response.body.shows) {
                    cities.add(show.venue.city)
                }
                val numberOfPages = page1Response.body.numberOfPages

                for (i in 2..numberOfPages) {
                    delay(63L)
                    val pageResponse = dataFetcher.getSetlistData(setlistfmId, i)

                    if (pageResponse is Response.Success) {
                        for (show in pageResponse.body.shows) {
                            cities.add(show.venue.city)
                        }
                    } else {
                        return@withContext CitiesResponse.Error
                    }
                }

                return@withContext CitiesResponse.Success(cities)
            } else {
                return@withContext CitiesResponse.Error
            }
        }
    }

    fun userMessageShown(messageId: String) {
        _viewState.update { currentViewState ->
            val messages = currentViewState.userMessages.filterNot { it.uniqueId == messageId }
            currentViewState.copy(userMessages = messages)
        }
    }
}

class MapViewModelFactory(
    private val contextProvider: CoroutineContextProvider,
    private val dataFetcher: DataFetcher
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(contextProvider, dataFetcher) as T
        }
        throw IllegalArgumentException("Unable to construct ListOfShowsViewModel")
    }
}
