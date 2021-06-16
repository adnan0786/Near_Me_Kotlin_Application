package com.example.nearmekotlindemo.utility

sealed class State<T> {
    class Loading<T>(val flag: T) : State<T>()
    data class Success<T>(val data: T) : State<T>()
    data class Failed<T>(val error: String) : State<T>()

    companion object {
        fun <T> loading(flag: T) = Loading(flag)
        fun <T> success(data: T) = Success(data)
        fun <T> failed(error: String) = Failed<T>(error)
    }
}
