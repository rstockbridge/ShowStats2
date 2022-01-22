package dev.rstockbridge.showstats2.api

import java.lang.Exception

sealed class Response<out T> {
    data class Success<T>(val body: T) : Response<T>()
    object Error : Response<Nothing>()
    data class Failure(val e: Exception) : Response<Nothing>()
}
