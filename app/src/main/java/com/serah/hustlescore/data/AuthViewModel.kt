package com.serah.hustlescore.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.serah.sokohub.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Expose role so the UI can decide where to navigate
    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be blank")
            return
        }
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Passwords do not match")
            return
        }

        _authState.value = AuthState.Loading

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = mAuth.currentUser?.uid ?: run {
                        _authState.value = AuthState.Error("User ID not found")
                        return@addOnCompleteListener
                    }
                    // ✅ Never store raw password
                    val userdata = User(username, email, uid = uid, role = "user")
                    FirebaseDatabase.getInstance().getReference("Users/$uid")
                        .setValue(userdata)
                        .addOnCompleteListener { result ->
                            _authState.value = if (result.isSuccessful) {

                                AuthState.Registered
                            } else {
                                AuthState.Error(result.exception?.message ?: "Failed to save user")
                            }
                        }
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.message ?: "Registration failed"
                    )
                }
            }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be blank")
            return
        }

        _authState.value = AuthState.Loading

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = mAuth.currentUser?.uid ?: run {
                        _authState.value = AuthState.Error("User ID not found")
                        return@addOnCompleteListener
                    }
                    FirebaseDatabase.getInstance().getReference("Users/$uid")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val role = snapshot.child("role").value?.toString() ?: "user"
                            _userRole.value = role
                            _authState.value = AuthState.LoggedIn(role)

                        }
                        .addOnFailureListener {
                            _authState.value = AuthState.Error(it.message ?: "Failed to fetch role")
                        }
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.message ?: "Login failed"
                    )
                }
            }
    }

    fun logout() {
        mAuth.signOut()
        _userRole.value = null
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}