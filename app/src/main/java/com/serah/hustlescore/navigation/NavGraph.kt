package com.serah.hustlescore.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.hustlescore.screens.user.ScoreBreakdownScreen
import com.hustlescore.ui.screens.auth.LoginScreen
import com.serah.hustlescore.ui.screens.OnboardingScreen
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
import com.serah.hustlescore.ui.screens.user.NotificationsScreen
import com.serah.hustlescore.ui.screens.user.UploadSMSScreen
import com.serah.hustlescore.ui.screens.user.UserDetailFormScreen
import com.serah.hustlescore.ui.screens.user.UserProfileScreen
import com.serah.hustlescore.ui.theme.ThemeViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    isAdmin: Boolean,
    startDestination: String,
    themeViewModel: ThemeViewModel
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // ── Splash ─────────────────────────────────────────────────────
        composable(Routes.SplashScreen.route) {
            SplashScreen(navController)
        }
        composable(Routes.Onboarding.route) {
            OnboardingScreen(navController)
        }


        // ── Auth ───────────────────────────────────────────────────────
        composable(Routes.Login.route) {
            LoginScreen(navController)
        }
        composable(Routes.Register.route) {
            RegisterScreen(navController)
        }
        composable(Routes.ForgotPassword.route) {
            ForgotPasswordScreen(navController)
        }

        // ── User ───────────────────────────────────────────────────────
        composable(Routes.UserProfile.route) {
            UserProfileScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )
        }
        composable(Routes.Home.route) {
            HomeScreen(navController = navController,
            themeViewModel = themeViewModel)
        }
        composable(Routes.UserDashboard.route) {
            DashboardScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )
        }
        composable(Routes.UploadSms.route) {
            UploadSMSScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )
        }
        composable(Routes.ScoreBreakdown.route) {
            ScoreBreakdownScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )
        }
        composable(Routes.CreditReport.route) {
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
        composable(Routes.Notifications.route) {
            NotificationsScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )
        }
        composable(Routes.AddTransaction.route) {          // ✅ was missing
            AddTransactionScreen(navController)
        }
        composable(Routes.UserDetailForm.route) {
            UserDetailFormScreen(navController)
        }

        // ── Admin ──────────────────────────────────────────────────────
        composable(Routes.AdminDashboard.route) {
            AdminDashboardScreen(navController)
        }
        composable(Routes.UsersList.route) {
            UsersListScreen(navController)
        }
        composable(Routes.ScoringLogs.route) {
            ScoringLogsScreen(navController)
        }
        composable(Routes.AlgorithmWeight.route) {
            AlgorithmWeightScreen(navController)
        }

        // ── Parameterized ──────────────────────────────────────────────
        composable(
            route = Routes.UserDetail.route,
            arguments = listOf(navArgument(Routes.UserDetail.ARG_USER_ID) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            UserDetailScreen(
                navController,
                backStackEntry.arguments?.getString(Routes.UserDetail.ARG_USER_ID) ?: ""
            )
        }
    }
}