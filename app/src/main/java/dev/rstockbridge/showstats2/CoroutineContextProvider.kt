package dev.rstockbridge.showstats2

import kotlinx.coroutines.CoroutineDispatcher

interface CoroutineContextProvider {
    val IO: CoroutineDispatcher
}
