package com.serah.hustlescore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.hustlescore.ui.theme.HustleScoreTheme
import com.serah.hustlescore.navigation.AppBottomNavBar
import com.serah.hustlescore.navigation.AppNavHost
import com.serah.hustlescore.navigation.Routes
import com.serah.hustlescore.navigation.adminBottomNavItems
import com.serah.hustlescore.navigation.userBottomNavItems
import com.serah.hustlescore.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Get the ViewModel globally
            val themeViewModel: ThemeViewModel = viewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()

            // Pass the state into the theme wrapper
            HustleScoreTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                val showUserBottomNav = currentRoute in Routes.UserBottomNavRoutes
                val showAdminBottomNav = currentRoute in Routes.AdminBottomNavRoutes

                Scaffold(
                    // The Scaffold background will now automatically switch colors
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        when {
                            showUserBottomNav -> AppBottomNavBar(
                                navController = navController,
                                items = userBottomNavItems,
                                themeViewModel = themeViewModel
                            )

                            showAdminBottomNav -> AppBottomNavBar(
                                navController = navController,
                                items = adminBottomNavItems,
                                themeViewModel = themeViewModel
                            )

                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        startDestination = Routes.SplashScreen.route,
                        modifier = Modifier.padding(innerPadding),
                        themeViewModel = themeViewModel
                    )
                }
            }
        }
    }
}