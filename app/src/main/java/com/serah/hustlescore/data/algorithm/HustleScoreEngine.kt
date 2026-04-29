package com.serah.hustlescore.data.algorithm


import com.serah.hustlescore.models.AlgorithmWeights
import com.serah.hustlescore.models.FinancialAdvice
import com.serah.hustlescore.models.HustleScore
import com.serah.hustlescore.models.Priority
import com.serah.hustlescore.models.ScoreGrade
import com.serah.hustlescore.models.Transaction
import com.serah.hustlescore.models.TransactionType
import kotlin.math.*
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Color

object HustleScoreEngine {





        fun getScoreColor(score: Int): Color {
            return when (score) {
                in 0..300 -> Color.Red
                in 301..600 -> Color.Yellow
                in 601..1000 -> Color.Green
                else -> Color.Gray
            }
        }


    fun calculate(
        transactions: List<Transaction>,
        weights: AlgorithmWeights = AlgorithmWeights()
    ): HustleScore {
        if (transactions.isEmpty()) return HustleScore()

        val income = transactions.filter { it.type == TransactionType.INCOME }
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val savings = transactions.filter { it.type == TransactionType.SAVINGS }
        val loans = transactions.filter { it.type == TransactionType.LOAN_REPAYMENT }

        val totalIncome = income.sumOf { it.amount }
        val totalExpenses = expenses.sumOf { it.amount }
        val totalSavings = savings.sumOf { it.amount }
        val totalLoanRepaid = loans.sumOf { it.amount }

        // 1. Income Stability Score
        val monthlyIncome = groupByMonth(income)
        val incomeValues = monthlyIncome.values.map { list -> list.sumOf { it.amount } }
        val avgIncome = if (incomeValues.isNotEmpty()) incomeValues.average() else 0.0
        val variance = if (incomeValues.size > 1)
            sqrt(incomeValues.map { (it - avgIncome).pow(2.0) }.average()) else TODO()
        val consistency = if (avgIncome > 0) max(0.0, 1.0 - (variance / avgIncome)) else 0.0
        val incomeScore = min(1000.0, (consistency * 600) + (min(totalIncome / 50000, 1.0) * 400))

        // 2. Savings Score
        val savingsRatio = if (totalIncome > 0) totalSavings / totalIncome else 0.0
        val savingsScore = min(1000.0, savingsRatio * 4000)

        // 3. Expense Control Score
        val expenseRatio = if (totalIncome > 0) totalExpenses / totalIncome else 1.0
        val expenseScore = min(1000.0, max(0.0, (1 - expenseRatio) * 1250))

        // 4. Transaction Activity Score
        val allMonths = groupByMonth(transactions)
        val txPerMonth = transactions.size.toDouble() / max(1, allMonths.size)
        val activityScore = min(1000.0, txPerMonth * 50)

        // 5. Debt Behavior Score
        val debtScore = if (loans.isNotEmpty())
            min(1000.0, (totalLoanRepaid / max(totalExpenses * 0.2, 1.0)) * 500)
        else 500.0

        val total = (incomeScore * weights.incomeWeight +
                savingsScore * weights.savingsWeight +
                expenseScore * weights.expenseWeight +
                activityScore * weights.activityWeight +
                debtScore * weights.debtWeight).roundToInt().coerceIn(0, 1000)

        val grade = when {
            total >= 750 -> ScoreGrade.EXCELLENT
            total >= 550 -> ScoreGrade.GOOD
            total >= 350 -> ScoreGrade.FAIR
            else -> ScoreGrade.POOR
        }

        return HustleScore(
            totalScore = total,
            incomeScore = incomeScore.roundToInt(),
            savingsScore = savingsScore.roundToInt(),
            expenseScore = expenseScore.roundToInt(),
            activityScore = activityScore.roundToInt(),
            debtScore = debtScore.roundToInt(),
            grade = grade,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            totalSavings = totalSavings
        )
    }

    fun parseMpesaSms(smsText: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = smsText.split("\n").filter { it.isNotBlank() }

        val receivedPattern = Regex("""([A-Z0-9]+) Confirmed\. You have received Ksh([\d,]+\.?\d*) from (.+?) on (\d+/\d+/\d+)""", RegexOption.IGNORE_CASE)
        val sentPattern = Regex("""([A-Z0-9]+) Confirmed\. Ksh([\d,]+\.?\d*) sent to (.+?) on (\d+/\d+/\d+)""", RegexOption.IGNORE_CASE)
        val paybillPattern = Regex("""([A-Z0-9]+) Confirmed\. Ksh([\d,]+\.?\d*) paid to (.+?) on (\d+/\d+/\d+)""", RegexOption.IGNORE_CASE)
        val depositPattern = Regex("""([A-Z0-9]+) Confirmed\. Ksh([\d,]+\.?\d*) deposited to (.+?) on (\d+/\d+/\d+)""", RegexOption.IGNORE_CASE)
        val loanPattern = Regex("""([A-Z0-9]+) Confirmed\. Ksh([\d,]+\.?\d*) loan repayment to (.+?) on (\d+/\d+/\d+)""", RegexOption.IGNORE_CASE)

        for (line in lines) {
            val (match, type) = listOf(
                receivedPattern.find(line) to TransactionType.INCOME,
                depositPattern.find(line) to TransactionType.SAVINGS,
                loanPattern.find(line) to TransactionType.LOAN_REPAYMENT,
                sentPattern.find(line) to TransactionType.EXPENSE,
                paybillPattern.find(line) to TransactionType.EXPENSE,
            ).firstOrNull { it.first != null } ?: continue

            match ?: continue
            val amount = match.groupValues[2].replace(",", "").toDoubleOrNull() ?: continue
            val desc = match.groupValues[3]
            val rawDate = match.groupValues[4]
            val parts = rawDate.split("/")
            val isoDate = if (parts.size == 3) "${parts[2]}-${parts[1].padStart(2,'0')}-${parts[0].padStart(2,'0')}" else ""

            transactions.add(Transaction(
                id = match.groupValues[1],
                type = type,
                amount = amount,
                description = desc,
                date = isoDate,
                mpesaRef = match.groupValues[1],
                rawSms = line,
                category = detectCategory(desc, type)
            ))
        }
        return transactions
    }

    private fun detectCategory(desc: String, type: TransactionType): String {
        val d = desc.lowercase()
        return when {
            d.contains("kplc") || d.contains("power") || d.contains("water") -> "utilities"
            d.contains("safaricom") || d.contains("airtel") -> "airtime"
            d.contains("school") || d.contains("college") -> "education"
            d.contains("hospital") || d.contains("pharmacy") -> "healthcare"
            d.contains("supermarket") || d.contains("market") -> "food"
            type == TransactionType.SAVINGS -> "savings"
            type == TransactionType.LOAN_REPAYMENT -> "loan"
            else -> "other"
        }
    }



    fun getAdvice(score: HustleScore): List<FinancialAdvice> {
        val advice = mutableListOf<FinancialAdvice>()
        if (score.incomeScore < 500) advice.add(FinancialAdvice("Diversify Income Streams", "Consider adding a second income source like M-Pesa float, small business, or gig work.", "💰", Priority.HIGH))
        if (score.savingsScore < 400) advice.add(FinancialAdvice("Start a Savings Habit", "Set aside at least 10-20% of every income using M-Pesa savings or a SACCO.", "🏦", Priority.HIGH))
        if (score.expenseScore < 400) advice.add(FinancialAdvice("Control Your Expenses", "Track your spending and reduce non-essential expenses.", "📊", Priority.MEDIUM))
        if (score.activityScore < 400) advice.add(FinancialAdvice("Increase Transaction Activity", "More consistent M-Pesa usage builds a stronger financial profile.", "📱", Priority.MEDIUM))
        if (score.debtScore < 400) advice.add(FinancialAdvice("Prioritize Loan Repayments", "Consistent repayments significantly boost your credit score.", "✅", Priority.HIGH))
        advice.add(FinancialAdvice("Join a SACCO", "SACCOs offer affordable loans to members with good savings history.", "🤝", Priority.LOW))
        return advice
    }

    private fun groupByMonth(txs: List<Transaction>): Map<String, List<Transaction>> =
        txs.groupBy { it.date.take(7) }


}


