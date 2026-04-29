package com.serah.hustlescore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.hustlescore.ui.theme.HustleScoreTheme
import com.serah.hustlescore.navigation.AppBottomNavBar
import com.serah.hustlescore.navigation.AppNavHost
import com.serah.hustlescore.navigation.Routes
import com.serah.hustlescore.navigation.Screen
import com.serah.hustlescore.navigation.adminBottomNavItems
import com.serah.hustlescore.navigation.userBottomNavItems


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HustleScoreTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                val showUserBottomNav = currentRoute in Routes.userBottomNavRoutes
                val showAdminBottomNav = currentRoute in Routes.adminBottomNavRoutes

                val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
                    Screen.Dashboard.route
                } else {
                    Screen.Login.route
                }

                Scaffold(
                    bottomBar = {
                        when {
                            showUserBottomNav -> AppBottomNavBar(
                                navController = navController,
                                items = userBottomNavItems,
                            )
                            showAdminBottomNav -> AppBottomNavBar(
                                navController = navController,
                                items = adminBottomNavItems,
                            )
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}