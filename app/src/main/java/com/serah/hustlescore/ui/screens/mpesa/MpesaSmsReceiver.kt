package com.serah.hustlescore.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.serah.hustlescore.data.algorithm.HustleScoreEngine
import com.serah.hustlescore.models.Transaction
import java.text.SimpleDateFormat
import java.util.*

class MpesaSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        messages.forEach { sms ->
            val sender = sms.originatingAddress ?: ""
            val body = sms.messageBody ?: ""

            // Only process M-Pesa messages
            if (!sender.contains("MPESA", ignoreCase = true) &&
                !sender.contains("M-PESA", ignoreCase = true) &&
                !body.contains("M-PESA", ignoreCase = true)) return@forEach

            val transaction = parseMpesaSms(body) ?: return@forEach

            val ref = FirebaseDatabase.getInstance()
                .getReference("transactions/$uid")
                .push()

            ref.setValue(transaction.copy(id = ref.key ?: ""))
        }
    }

    private fun parseMpesaSms(body: String): Transaction? {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        return when {
            // Received money: "You have received Ksh1,000.00 from JOHN DOE"
            body.contains("You have received", ignoreCase = true) -> {
                val amount = extractAmount(body) ?: return null
                val desc = extractName(body, "from") ?: "M-Pesa Received"
                Transaction(type = "income", amount = amount, date = dateStr, description = "Received from $desc", rawSms = body)
            }
            // Sent money: "Ksh500.00 sent to JANE DOE"
            body.contains("sent to", ignoreCase = true) -> {
                val amount = extractAmount(body) ?: return null
                val desc = extractName(body, "to") ?: "M-Pesa Sent"
                Transaction(type = "expense", amount = amount, date = dateStr, description = "Sent to $desc", rawSms = body)
            }
            // Payment: "Ksh200.00 paid to KPLC PREPAID"
            body.contains("paid to", ignoreCase = true) -> {
                val amount = extractAmount(body) ?: return null
                val desc = extractName(body, "to") ?: "M-Pesa Payment"
                Transaction(type = "expense", amount = amount, date = dateStr, description = "Paid to $desc", rawSms = body)
            }
            // Withdrawal: "Withdraw Ksh1,000.00 from"
            body.contains("Withdraw", ignoreCase = true) -> {
                val amount = extractAmount(body) ?: return null
                Transaction(type = "expense", amount = amount, date = dateStr, description = "ATM/Agent Withdrawal", rawSms = body)
            }
            // Buy Airtime
            body.contains("airtime", ignoreCase = true) -> {
                val amount = extractAmount(body) ?: return null
                Transaction(type = "expense", amount = amount, date = dateStr, description = "Airtime Purchase", category = "airtime", rawSms = body)
            }
            // Loan repayment: "your loan payment"
            body.contains("loan", ignoreCase = true) -> {
                val amount = extractAmount(body) ?: return null
                Transaction(type = "loan_repayment", amount = amount, date = dateStr, description = "Loan Repayment", rawSms = body)
            }
            // Deposit / savings
            body.contains("deposited", ignoreCase = true) -> {
                val amount = extractAmount(body) ?: return null
                Transaction(type = "savings", amount = amount, date = dateStr, description = "M-Pesa Deposit", rawSms = body)
            }
            else -> null
        }
    }

    private fun extractAmount(body: String): Double? {
        val regex = Regex("""Ksh([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)
        return regex.find(body)?.groupValues?.get(1)
            ?.replace(",", "")?.toDoubleOrNull()
    }

    private fun extractName(body: String, keyword: String): String? {
        val regex = Regex("""$keyword\s+([A-Z][A-Z\s]+?)(?:\s+on|\s+\d|\.|$)""", RegexOption.IGNORE_CASE)
        return regex.find(body)?.groupValues?.get(1)?.trim()
    }
}