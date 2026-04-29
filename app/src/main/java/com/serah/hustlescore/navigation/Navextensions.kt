package com.serah.hustlescore.navigation

import androidx.navigation.NavController

/**
 * Navigate to a destination and clear the back stack up to (but not including) the
 * graph's start destination, preserving state so tabs feel snappy.
 */
fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Navigate back to the Login screen and wipe the entire back stack (logout flow).
 */
fun NavController.navigateToLogin() {
    navigate(Routes.Login.route) {
        popUpTo(0) { inclusive = true }
    }
}

/**
 * Navigate to a user's detail page from the admin users list.
 */
fun NavController.navigateToUserDetail(userId: String) {
    navigate(Routes.UserDetail.createRoute(userId))
}