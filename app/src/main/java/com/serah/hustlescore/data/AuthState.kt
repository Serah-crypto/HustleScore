package com.serah.hustlescore.data


sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Registered : AuthState()
    data class LoggedIn(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
