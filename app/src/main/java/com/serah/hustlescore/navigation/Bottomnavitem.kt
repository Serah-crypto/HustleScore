package com.serah.hustlescore.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

val userBottomNavItems = listOf(
    BottomNavItem(
        label = "Home",
        icon = Icons.Filled.Home,
        route = Routes.Home.route,
    ),
    BottomNavItem(
        label = "Credit",
        icon = Icons.Filled.Star,
        route = Routes.CreditReport.route,
    ),
    BottomNavItem(
        label = "Alerts",
        icon = Icons.Filled.Notifications,
        route = Routes.Notifications.route,
    ),
    BottomNavItem(
        label = "Profile",
        icon = Icons.Filled.Person,
        route = Routes.UserProfile.route,
    ),
)

val adminBottomNavItems = listOf(
    BottomNavItem(
        label = "Dashboard",
        icon = Icons.Filled.Dashboard,
        route = Routes.AdminDashboard.route,
    ),
    BottomNavItem(
        label = "Users",
        icon = Icons.Filled.Group,
        route = Routes.UsersList.route,
    ),
    BottomNavItem(
        label = "Logs",
        icon = Icons.Filled.Description,
        route = Routes.ScoringLogs.route,
    ),
    BottomNavItem(
        label = "Algorithm",
        icon = Icons.Filled.Settings,
        route = Routes.AlgorithmWeight.route,
    ),
)