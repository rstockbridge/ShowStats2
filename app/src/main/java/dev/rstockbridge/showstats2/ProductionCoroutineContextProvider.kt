package dev.rstockbridge.showstats2

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class ProductionCoroutineContextProvider @Inject constructor() : CoroutineContextProvider {
    override val IO: CoroutineDispatcher by lazy { Dispatchers.IO }
}
