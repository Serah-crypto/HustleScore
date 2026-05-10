package com.serah.hustlescore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.hustlescore.ui.theme.HustleScoreTheme
import com.serah.hustlescore.navigation.AppBottomNavBar
import com.serah.hustlescore.navigation.AppNavHost
import com.serah.hustlescore.navigation.Routes
import com.serah.hustlescore.navigation.adminBottomNavItems
import com.serah.hustlescore.navigation.userBottomNavItems
import com.serah.hustlescore.ui.screens.SplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        // MUST come before super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HustleScoreTheme {

                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                var startDestination by remember { mutableStateOf<String?>(null) }

                // Firebase Auth Listener
                DisposableEffect(Unit) {
                    val auth = FirebaseAuth.getInstance()
                    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                        startDestination =
                            if (firebaseAuth.currentUser != null) "home" else "login"
                    }

                    auth.addAuthStateListener(listener)

                    onDispose {
                        auth.removeAuthStateListener(listener)
                    }
                }

                // UI Control
                when (val destination = startDestination) {

                    null -> {

                        SplashScreen(navController = navController)
                    }

                    else -> {

                        val showUserBottomNav =
                            currentRoute in Routes.UserBottomNavRoutes

                        val showAdminBottomNav =
                            currentRoute in Routes.AdminBottomNavRoutes


                        Scaffold(
                            bottomBar = {
                                when {
                                    showUserBottomNav -> AppBottomNavBar(
                                        navController = navController,
                                        items = userBottomNavItems
                                    )

                                    showAdminBottomNav -> AppBottomNavBar(
                                        navController = navController,
                                        items = adminBottomNavItems
                                    )
                                }
                            }
                        ) { innerPadding ->

                            AppNavHost(
                                navController = navController,
                                startDestination = destination, // ✅ non-null safe
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}