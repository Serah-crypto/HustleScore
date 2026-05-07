package com.serah.hustlescore.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.hustlescore.screens.user.ScoreBreakdownScreen
import com.hustlescore.ui.screens.auth.LoginScreen
import com.serah.hustlescore.ui.screens.SplashScreen  // ← keep only this one
import com.serah.hustlescore.ui.screens.admin.AdminDashboardScreen
import com.serah.hustlescore.ui.screens.admin.AlgorithmWeightScreen
import com.serah.hustlescore.ui.screens.admin.ScoringLogsScreen
import com.serah.hustlescore.ui.screens.admin.UserDetailScreen
import com.serah.hustlescore.ui.screens.admin.UsersListScreen
import com.serah.hustlescore.ui.screens.auth.ForgotPasswordScreen
import com.serah.hustlescore.ui.screens.auth.RegisterScreen
import com.serah.hustlescore.ui.screens.user.CreditReportScreen
import com.serah.hustlescore.ui.screens.user.DashboardScreen
import com.serah.hustlescore.ui.screens.user.FinancialAdviceScreen
import com.serah.hustlescore.ui.screens.user.HomeScreen
import com.serah.hustlescore.ui.screens.user.NotificationsScreen
import com.serah.hustlescore.ui.screens.user.ProfileScreen
import com.serah.hustlescore.ui.screens.user.UploadSMSScreen

@Composable
fun NavGraph(navController: NavHostController, isAdmin: Boolean, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.SplashScreen.route) { SplashScreen(navController) }
        composable(Routes.Login.route) { LoginScreen(navController) }
        composable(Routes.Home.route) { HomeScreen(navController) }
        composable(Routes.Register.route) { RegisterScreen(navController) }
        composable(Routes.UserDashboard.route) { DashboardScreen(navController) }
        composable(Routes.UploadSms.route) { UploadSMSScreen(navController) }
        composable(Routes.ScoreBreakdown.route) { ScoreBreakdownScreen(navController) }
        composable(Routes.CreditReport.route) { CreditReportScreen(navController) }
        composable(Routes.FinancialAdvice.route) { FinancialAdviceScreen(navController) }
        composable(Routes.Profile.route) { ProfileScreen(navController) }
        composable(Routes.Notifications.route) { NotificationsScreen(navController) }
        composable(Routes.ForgotPassword.route) { ForgotPasswordScreen(navController) }
        composable(Routes.AdminDashboard.route) { AdminDashboardScreen(navController) }
        composable(Routes.UsersList.route) { UsersListScreen(navController) }
        composable(
            Routes.UserDetail.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            UserDetailScreen(navController, backStackEntry.arguments?.getString("userId") ?: "")
        }
        composable(Routes.ScoringLogs.route) { ScoringLogsScreen(navController) }
        composable(Routes.AlgorithmWeight.route) { AlgorithmWeightScreen(navController) }
    }
}