package dev.rstockbridge.showstats2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.rstockbridge.showstats2.api.DataFetcher
import dev.rstockbridge.showstats2.api.Response
import dev.rstockbridge.showstats2.api.models.Setlist
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class FetchDataViewModel(
    private val contextProvider: CoroutineContextProvider,
    private val dataFetcher: DataFetcher
) : ViewModel() {

    data class UserMessage(
        val uniqueId: String = UUID.randomUUID().toString(),
        val message: String
    )

    data class ViewState(
        val shows: List<Setlist>?,
        val displayProgressBar: Boolean,
        val userMessages: List<UserMessage> = emptyList()
    )

    sealed class DataResponse {
        data class Success(val shows: List<Setlist>) : DataResponse()
        object Error : DataResponse()
    }

    private val _viewState = MutableStateFlow(ViewState(null, false))
    val viewState = _viewState.asStateFlow()

    fun onSetlistfmIdSubmitted(setlistfmId: String) {
        fetchData(setlistfmId)
    }

    private fun fetchData(setlistfmId: String) {
        viewModelScope.launch {
            _viewState.value = ViewState(
                shows = null,
                displayProgressBar = true,
            )

            when (val response = makeNetworkRequest(setlistfmId)) {
                is DataResponse.Success -> {
                    _viewState.update { currentViewState ->
                        currentViewState.copy(
                            shows = response.shows,
                            displayProgressBar = true,
                        )
                    }
                }
                DataResponse.Error -> {
                    _viewState.update { currentViewState ->
                        currentViewState.copy(
                            shows = null,
                            displayProgressBar = false,
                            userMessages = currentViewState.userMessages + UserMessage(message = "Something has gone wrong!")
                        )
                    }
                }
            }
        }
    }

    private suspend fun makeNetworkRequest(setlistfmId: String): DataResponse {
        return withContext(contextProvider.IO) {

            val shows: MutableList<Setlist> = mutableListOf()
            val page1Response = dataFetcher.getSetlistData(setlistfmId, 1)

            if (page1Response is Response.Success) {
                shows.addAll(page1Response.body.setlists)
                val numberOfPages = page1Response.body.numberOfPages

                for (i in 2..numberOfPages) {
                    delay(63L)
                    val pageResponse = dataFetcher.getSetlistData(setlistfmId, i)

                    if (pageResponse is Response.Success) {
                        shows.addAll(pageResponse.body.setlists)
                    } else {
                        return@withContext DataResponse.Error
                    }
                }

                return@withContext DataResponse.Success(shows)
            } else {
                return@withContext DataResponse.Error
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

class UserNameViewModelFactory(
    private val contextProvider: CoroutineContextProvider,
    private val dataFetcher: DataFetcher
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FetchDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FetchDataViewModel(contextProvider, dataFetcher) as T
        }
        throw IllegalArgumentException("Unable to construct UserNameViewModel")
    }
}
