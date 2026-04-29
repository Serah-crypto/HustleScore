package com.serah.hustlescore.navigation

import android.R.attr.type
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hustlescore.screens.auth.LoginScreen
import com.hustlescore.screens.user.ScoreBreakdownScreen
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

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.Register.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ── Auth ───────────────────────────────────────────────────────────
        composable(Routes.Login.route) {
            LoginScreen(navController)
        }
        composable(Routes.Register.route) {
            RegisterScreen(navController)
        }
        composable(Routes.ForgotPassword.route) {
            ForgotPasswordScreen(navController)
        }

        // ── User ───────────────────────────────────────────────────────────
        composable(Routes.Home.route) {
            HomeScreen(navController)
        }
        composable(Routes.UserDashboard.route) {
            DashboardScreen(navController)
        }
        composable(Routes.CreditReport.route) {
            CreditReportScreen(navController)
        }
        composable(Routes.FinancialAdvice.route) {
            FinancialAdviceScreen(navController)
        }
        composable(Routes.Notifications.route) {
            NotificationsScreen(navController)
        }
        composable(Routes.Profile.route) {
            ProfileScreen(navController)
        }
        composable(Routes.ScoreBreakdown.route) {
            ScoreBreakdownScreen(navController)
        }
        composable(Routes.UploadSms.route) {
            UploadSMSScreen(navController)
        }
        composable(Routes.AddTransaction.route) {
            AddTransactionScreen(navController)
        }

        // ── Admin ──────────────────────────────────────────────────────────
        composable(Routes.AdminDashboard.route) {
            AdminDashboardScreen(navController)
        }
        composable(Routes.AlgorithmWeight.route) {
            AlgorithmWeightScreen(navController)
        }
        composable(Routes.ScoringLogs.route) {
            ScoringLogsScreen(navController)
        }
        composable(Routes.UsersList.route) {
            UsersListScreen(navController)
        }

        // ── Parameterized ──────────────────────────────────────────────────
        composable(
            route = Routes.UserDetail.route,
            arguments = listOf(
                navArgument(Routes.UserDetail.ARG_USER_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments
                ?.getString(Routes.UserDetail.ARG_USER_ID)
                ?: return@composable
            UserDetailScreen(
                navController = navController,
                userId = userId
            )
        }
    }
}

@Composable
fun ProfileScreen(x0: NavHostController) {
    TODO("Not yet implemented")
}