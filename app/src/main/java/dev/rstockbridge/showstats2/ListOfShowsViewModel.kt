package dev.rstockbridge.showstats2

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.rstockbridge.showstats2.api.DataFetcher
import dev.rstockbridge.showstats2.api.Response
import dev.rstockbridge.showstats2.api.models.Show
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ListOfShowsViewModel(
    private val contextProvider: CoroutineContextProvider,
    private val dataFetcher: DataFetcher
) : ViewModel() {

    data class UserMessage(
        val uniqueId: String = UUID.randomUUID().toString(),
        @StringRes val message: Int
    )

    data class ListOfShowsViewState(
        val shows: List<Show>?,
        val networkCallInProgress: Boolean,
        val userMessages: List<UserMessage> = emptyList()
    )

    sealed class ListOfShowsResponse {
        data class Success(val listOfShows: List<Show>) : ListOfShowsResponse()
        object Error : ListOfShowsResponse()
    }

    private val _viewState = MutableStateFlow(ListOfShowsViewState(null, false))
    val viewState = _viewState.asStateFlow()

    fun fetchData(setlistfmId: String) {
        viewModelScope.launch {
            _viewState.value = ListOfShowsViewState(null, true)

            when (val response = makeNetworkRequest(setlistfmId)) {
                is ListOfShowsResponse.Success -> {
                    _viewState.update { currentViewState ->
                        currentViewState.copy(
                            shows = response.listOfShows,
                            networkCallInProgress = false
                        )
                    }
                }
                ListOfShowsResponse.Error -> {
                    _viewState.update { currentViewState ->
                        currentViewState.copy(
                            shows = null,
                            networkCallInProgress = false,
                            userMessages = currentViewState.userMessages + UserMessage(message = R.string.error_message)
                        )
                    }
                }
            }
        }
    }

    private suspend fun makeNetworkRequest(setlistfmId: String): ListOfShowsResponse {
        return withContext(contextProvider.IO) {

            val showData: ArrayList<Show> = arrayListOf()
            val page1Response = dataFetcher.getSetlistData(setlistfmId, 1)

            if (page1Response is Response.Success) {
                showData.addAll(page1Response.body.shows)
                val numberOfPages = page1Response.body.numberOfPages

                for (i in 2..numberOfPages) {
                    delay(63L)
                    val pageResponse = dataFetcher.getSetlistData(setlistfmId, i)

                    if (pageResponse is Response.Success) {
                        showData.addAll(pageResponse.body.shows)
                    } else {
                        return@withContext ListOfShowsResponse.Error
                    }
                }

                return@withContext ListOfShowsResponse.Success(showData)
            } else {
                return@withContext ListOfShowsResponse.Error
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

class ListOfShowsViewModelFactory(
    private val contextProvider: CoroutineContextProvider,
    private val dataFetcher: DataFetcher
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListOfShowsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListOfShowsViewModel(contextProvider, dataFetcher) as T
        }
        throw IllegalArgumentException("Unable to construct ListOfShowsViewModel")
    }
}
