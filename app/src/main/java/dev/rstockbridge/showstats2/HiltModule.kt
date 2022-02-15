package dev.rstockbridge.showstats2

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.rstockbridge.showstats2.api.DataFetcher
import dev.rstockbridge.showstats2.api.SetlistfmApi

@Module
@InstallIn(SingletonComponent::class)
abstract class MyModule {

    @Binds
    abstract fun bindCoroutinesContextProvider(
        productionCoroutineContextProvider: ProductionCoroutineContextProvider
    ): CoroutineContextProvider

    @Binds
    abstract fun bindDataFetcher(
        setlistfmApi: SetlistfmApi
    ): DataFetcher
}
