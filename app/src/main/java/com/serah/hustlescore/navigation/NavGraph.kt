package com.serah.hustlescore.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.hustlescore.screens.auth.LoginScreen
import com.hustlescore.screens.user.ScoreBreakdownScreen
import com.serah.hustlescore.ui.screens.admin.AdminDashboardScreen
import com.serah.hustlescore.ui.screens.admin.AlgorithmWeightsScreen
import com.serah.hustlescore.ui.screens.admin.ScoringLogsScreen
import com.serah.hustlescore.ui.screens.admin.UserDetailScreen
import com.serah.hustlescore.ui.screens.admin.UsersListScreen
import com.serah.hustlescore.ui.screens.auth.ForgotPasswordScreen
import com.serah.hustlescore.ui.screens.auth.RegisterScreen
import com.serah.hustlescore.ui.screens.user.CreditReportScreen
import com.serah.hustlescore.ui.screens.user.DashboardScreen
import com.serah.hustlescore.ui.screens.user.FinancialAdviceScreen
import com.serah.hustlescore.ui.screens.user.NotificationsScreen
import com.serah.hustlescore.ui.screens.user.ProfileScreen
import com.serah.hustlescore.ui.screens.user.UploadSMSScreen


sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Upload : Screen("upload")
    object ScoreBreakdown : Screen("score")
    object CreditReport : Screen("report")
    object Advice : Screen("advice")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
    object ForgotPassword : Screen("forgot_password")
    object AdminDashboard : Screen("admin_dashboard")
    object AdminUsers : Screen("admin_users")
    object AdminUserDetail : Screen("admin_user/{userId}") {
        fun createRoute(userId: String) = "admin_user/$userId"
    }
    object AdminLogs : Screen("admin_logs")
    object AdminWeights : Screen("admin_weights")
    object AddTransaction : Screen("add_transaction")
}

@Composable
fun NavGraph(navController: NavHostController, isAdmin: Boolean, startDestination: String) {
    NavHost(navController = navController, startDestination = Screen.Register.route) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController,) }
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }
        composable(Screen.Upload.route) { UploadSMSScreen(navController) }
        composable(Screen.ScoreBreakdown.route) { ScoreBreakdownScreen(navController) }
        composable(Screen.CreditReport.route) { CreditReportScreen(navController) }
        composable(Screen.Advice.route) { FinancialAdviceScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.Notifications.route) { NotificationsScreen(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }
        composable(Screen.AdminDashboard.route) { AdminDashboardScreen(navController) }
        composable(Screen.AdminUsers.route) { UsersListScreen(navController) }
        composable(
            Screen.AdminUserDetail.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            UserDetailScreen(navController, backStackEntry.arguments?.getString("userId") ?: "")
        }
        composable(Screen.AdminLogs.route) { ScoringLogsScreen(navController) }
        composable(Screen.AdminWeights.route) { AlgorithmWeightsScreen(navController) }
    }
}

class UploadSMSScreen(navController: NavHostController) {

}
