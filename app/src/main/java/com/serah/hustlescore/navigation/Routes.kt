package com.serah.hustlescore.navigation

sealed class Routes(val route: String) {

    // ── Splash ─────────────────────────────────────────────────────────────
    object Splash : Routes("splashscreen")          // ✅ alias used in SplashScreen.kt
    object SplashScreen : Routes("splashscreen")    // ✅ keeps NavGraph working

    // ── Auth ───────────────────────────────────────────────────────────────
    object Login          : Routes("login")
    object Register       : Routes("register")
    object ForgotPassword : Routes("forgot_password")

    // ── User ───────────────────────────────────────────────────────────────
    object Home           : Routes("home")
    object UserDashboard  : Routes("user_dashboard")
    object CreditReport   : Routes("credit_report")
    object FinancialAdvice: Routes("financial_advice")
    object Notifications  : Routes("notifications")
    object ScoreBreakdown : Routes("score_breakdown")
    object UploadSms      : Routes("upload_sms")
    object AddTransaction : Routes("add_transaction")   // ✅ was missing
    object UserProfile    : Routes("profile")
    object UserDetailForm : Routes("user_detail_form")

    // ── Admin ───────────────────────────────────────────────────────────────
    object AdminDashboard  : Routes("admin_dashboard")
    object AlgorithmWeight : Routes("algorithm_weight")
    object ScoringLogs     : Routes("scoring_logs")
    object UsersList       : Routes("users_list")

    // ── Parameterized ───────────────────────────────────────────────────────
    object UserDetail : Routes("user_detail/{userId}") {
        fun createRoute(userId: String) = "user_detail/$userId"
        const val ARG_USER_ID = "userId"
    }

    companion object {
        val UserBottomNavRoutes = setOf(
            Home.route,
            CreditReport.route,
            Notifications.route,
            UserProfile.route,
        )

        val AdminBottomNavRoutes = setOf(
            AdminDashboard.route,
            UsersList.route,
            ScoringLogs.route,
            AlgorithmWeight.route,
        )
    }
}