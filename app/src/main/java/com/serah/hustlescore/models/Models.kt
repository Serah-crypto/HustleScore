package com.serah.hustlescore.models


data class Transaction(
    val id: String = "",
    val userId: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: Double = 0.0,
    val date: String = "",
    val description: String = "",
    val category: String = "",
    val mpesaRef: String = "",
    val rawSms: String = ""
)

enum class TransactionType {
    INCOME,
    EXPENSE,
    SAVINGS,
    LOAN_REPAYMENT
}



data class HustleScore(
    val totalScore: Int = 0,
    val incomeScore: Int = 0,
    val savingsScore: Int = 0,
    val expenseScore: Int = 0,
    val activityScore: Int = 0,
    val debtScore: Int = 0,
    val grade: ScoreGrade = ScoreGrade.POOR,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalSavings: Double = 0.0
)

enum class ScoreGrade(val label: String, val min: Int) {
    EXCELLENT("Excellent", 750),
    GOOD("Good", 550),
    FAIR("Fair", 350),
    POOR("Poor", 0)
}

data class FinancialAdvice(
    val title: String,
    val description: String,
    val icon: String,
    val priority: Priority
)

enum class Priority { HIGH, MEDIUM, LOW }

data class AlgorithmWeights(
    val incomeWeight: Double = 0.30,
    val savingsWeight: Double = 0.25,
    val expenseWeight: Double = 0.20,
    val activityWeight: Double = 0.15,
    val debtWeight: Double = 0.10
)

data class AppUser(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "user",
    val phone: String = "",
    val county: String = "",
    val occupation: String = ""
)


data class AppNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "system",
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)

