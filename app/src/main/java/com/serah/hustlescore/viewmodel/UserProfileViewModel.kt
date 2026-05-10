package com.serah.hustlescore.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.serah.hustlescore.models.UserProfile

class UserProfileViewModel : ViewModel() {

    val profile = mutableStateOf(UserProfile())

    val isLoading = mutableStateOf(false)

    val error = mutableStateOf<String?>(null)

    fun fetchProfile() {

        isLoading.value = true

        profile.value = UserProfile(
            fullName = "Serah Ngure",
            email = "serah@gmail.com",
            phone = "0712345678",
            county = "Nyeri",
            occupation = "Business Owner",
            bio = "HustleScore user profile"
        )

        isLoading.value = false
    }
}