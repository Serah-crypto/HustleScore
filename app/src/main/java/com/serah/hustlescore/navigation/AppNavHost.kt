package com.serah.hustlescore.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hustlescore.screens.user.ScoreBreakdownScreen
import com.hustlescore.ui.screens.auth.LoginScreen
import com.serah.hustlescore.ui.screens.SplashScreen
import com.serah.hustlescore.ui.screens.admin.AdminDashboardScreen
import com.serah.hustlescore.ui.screens.admin.AlgorithmWeightScreen
import com.serah.hustlescore.ui.screens.admin.ScoringLogsScreen
import com.serah.hustlescore.ui.screens.admin.UserDetailScreen
import com.serah.hustlescore.ui.screens.admin.UsersListScreen
import com.serah.hustlescore.ui.screens.auth.ForgotPasswordScreen
import com.serah.hustlescore.ui.screens.auth.RegisterScreen
import com.serah.hustlescore.ui.screens.user.AddTransactionScreen
import com.serah.hustlescore.ui.screens.user.CreditReportScreen
import com.serah.hustlescore.ui.screens.user.DashboardScreen
import com.serah.hustlescore.ui.screens.user.FinancialAdviceScreen
import com.serah.hustlescore.ui.screens.user.HomeScreen
import com.serah.hustlescore.ui.screens.user.UploadSMSScreen
import com.serah.hustlescore.ui.screens.user.UserDetailFormScreen
import com.serah.hustlescore.ui.screens.user.UserProfileScreen
import com.serah.hustlescore.ui.theme.ThemeViewModel
import com.serah.hustlescore.ui.screens.user.NotificationsScreen


@Composable
fun AppNavHost(
    modifier         : Modifier         = Modifier,
    navController    : NavHostController = rememberNavController(),
    startDestination : String            = Routes.Register.route,
    themeViewModel   : ThemeViewModel                            // ✅ passed from MainActivity
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination,
        modifier         = modifier
    ) {
        composable(Routes.Login.route)          { LoginScreen(navController) }
        composable(Routes.UserProfile.route) {
            UserProfileScreen(
                navController = navController,
                themeViewModel = themeViewModel  // pass it in
            )
        }
        composable(Routes.Register.route)       { RegisterScreen(navController) }
        composable(Routes.ForgotPassword.route) { ForgotPasswordScreen(navController) }
        composable(Routes.SplashScreen.route)   { SplashScreen(navController) }

        composable(Routes.Home.route)            {
            HomeScreen(
                navController = navController,
                themeViewModel = themeViewModel)
        }

        composable(Routes.UserDashboard.route)   {
            DashboardScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )

        }
        composable(Routes.CreditReport.route)  {
            CreditReportScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )
        }

        composable(Routes.FinancialAdvice.route) {
            FinancialAdviceScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )
        }

        composable(Routes.Notifications.route)   {
            NotificationsScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )
        }

        composable(Routes.ScoreBreakdown.route)  {
            ScoreBreakdownScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )
        }

        composable(Routes.UploadSms.route)       {
            UploadSMSScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )
        }

        composable(Routes.AddTransaction.route) {
            AddTransactionScreen(navController = navController, themeViewModel = themeViewModel)
        }
        composable(Routes.UserDetailForm.route) {
            UserDetailFormScreen(navController = navController, themeViewModel = themeViewModel)
        }

        // ✅ UserProfileScreen receives themeViewModel so it can show the toggle
        composable(Routes.UserProfile.route) {
            UserProfileScreen(navController = navController, themeViewModel = themeViewModel)
        }


        composable(Routes.AdminDashboard.route)  { AdminDashboardScreen(navController) }
        composable(Routes.AlgorithmWeight.route) { AlgorithmWeightScreen(navController) }
        composable(Routes.ScoringLogs.route)     { ScoringLogsScreen(navController) }
        composable(Routes.UsersList.route)       { UsersListScreen(navController) }

        composable(
            route     = Routes.UserDetail.route,
            arguments = listOf(navArgument(Routes.UserDetail.ARG_USER_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(Routes.UserDetail.ARG_USER_ID) ?: return@composable
            UserDetailScreen(navController = navController, userId = userId)
        }
    }
}