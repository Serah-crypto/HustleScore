package com.serah.hustlescore.data

import com.google.firebase.database.FirebaseDatabase
import com.serah.hustlescore.models.HustleScore

object NotificationHelper {

    private fun send(uid: String, title: String, message: String, type: String) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("notifications/$uid")
        val key = ref.push().key ?: return
        val notif = mapOf(
            "id"        to key,
            "title"     to title,
            "message"   to message,
            "type"      to type,
            "isRead"    to false,
            "createdAt" to System.currentTimeMillis()
        )
        ref.child(key).setValue(notif)
    }

    fun scoreCalculated(uid: String, score: HustleScore) {
        send(
            uid     = uid,
            title   = "HustleScore Updated 🎯",
            message = "Your score is ${score.totalScore}/1000 — Grade: ${score.grade.label}. " +
                    when {
                        score.totalScore >= 750 -> "Excellent work! You're in top financial shape."
                        score.totalScore >= 550 -> "Good progress! Keep saving consistently."
                        score.totalScore >= 350 -> "Fair score. Focus on reducing expenses."
                        else                    -> "Let's work on improving your financial habits."
                    },
            type    = "score_update"
        )

        val weakest = listOf(
            "income"   to score.incomeScore,
            "savings"  to score.savingsScore,
            "expenses" to score.expenseScore
        ).minByOrNull { it.second }

        when (weakest?.first) {
            "income"   -> send(uid, "💰 Tip: Boost Your Income",
                "Consider adding a side hustle like M-Pesa float or freelance work to increase income consistency.", "tip")
            "savings"  -> send(uid, "🏦 Tip: Save More",
                "Try saving at least 10% of every income you receive. M-Shwari or a SACCO are great options.", "tip")
            "expenses" -> send(uid, "📊 Tip: Cut Expenses",
                "Your expense ratio is high. Track spending and reduce non-essential purchases.", "tip")
        }
    }

    fun profileCompleted(uid: String) {
        send(
            uid     = uid,
            title   = "Profile Complete ✅",
            message = "Your profile has been saved successfully. You can now get a full credit report.",
            type    = "system"
        )
    }

    fun lowScoreAlert(uid: String, score: Int) {
        if (score < 350) {
            send(
                uid     = uid,
                title   = "⚠ Low HustleScore Alert",
                message = "Your score of $score is below average. Upload more SMS data and focus on savings to improve.",
                type    = "alert"
            )
        }
    }
}