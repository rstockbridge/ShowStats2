package dev.rstockbridge.showstats2

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class ProductionCoroutineContextProvider: CoroutineContextProvider {
    override val IO: CoroutineDispatcher by lazy { Dispatchers.IO}
}
