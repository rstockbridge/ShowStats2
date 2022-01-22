package dev.rstockbridge.showstats2.api

import dev.rstockbridge.showstats2.BuildConfig.SETLISTFM_KEY
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitWrapper {
    private const val BASE_URL = "https://api.setlist.fm/rest/1.0/user/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(getClient())
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private fun getClient(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()

        val headerInterceptor = Interceptor { chain: Interceptor.Chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("x-api-key", SETLISTFM_KEY)
                .header("Accept", "application/json")
                .method(original.method, original.body)
                .build()

            chain.proceed(request)
        }

        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return httpClient
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(headerInterceptor)
            .connectTimeout(200, TimeUnit.SECONDS)
            .readTimeout(200, TimeUnit.SECONDS)
            .writeTimeout(200, TimeUnit.SECONDS)
            .build()
    }
}
