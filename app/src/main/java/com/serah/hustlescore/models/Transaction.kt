package com.serah.hustlescore.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

// ✅ NOT a data class — plain class so we can have a true no-arg constructor
//    that Firebase can call via reflection, plus a separate enum property for all UI code.
@IgnoreExtraProperties
class Transaction() {

    // ── Firebase-safe fields (all primitives / nullable strings, all defaulted) ──
    var UserId: String? = null
    var id: String? = null
    var amount: Double = 0.0
    var date: String = ""
    var description: String? = null
    var category: String? = null
    var phone: String? = null
    var name: String? = null
    var mpesaRef: String? = null
    var rawSms: String? = null
    var balance: Double? = null
    var timestamp: Long = 0L

    // ✅ Firebase stores & reads this String field — no enum reflection issues
    var typeRaw: String = TransactionType.INCOME.name

    // ✅ All existing code uses `.type` — this is excluded from Firebase serialization
    @get:Exclude
    var type: TransactionType
        get() = runCatching { TransactionType.valueOf(typeRaw) }.getOrDefault(TransactionType.INCOME)
        set(value) { typeRaw = value.name }

    // ── Convenience constructor so all existing call sites keep working unchanged ──
    constructor(
        type: TransactionType = TransactionType.INCOME,
        UserId: String? = null,
        id: String? = null,
        amount: Double = 0.0,
        date: String = "",
        description: String? = null,
        category: String? = null,
        phone: String? = null,
        name: String? = null,
        mpesaRef: String? = null,
        rawSms: String? = null,
        balance: Double? = null,
        timestamp: Long = System.currentTimeMillis()
    ) : this() {
        this.type = type          // calls the setter → sets typeRaw
        this.UserId = UserId
        this.id = id
        this.amount = amount
        this.date = date
        this.description = description
        this.category = category
        this.phone = phone
        this.name = name
        this.mpesaRef = mpesaRef
        this.rawSms = rawSms
        this.balance = balance
        this.timestamp = timestamp
    }

    // ── copy() replacement (data class is gone, so add what the codebase needs) ──
    fun copy(
        type: TransactionType = this.type,
        UserId: String? = this.UserId,
        id: String? = this.id,
        amount: Double = this.amount,
        date: String = this.date,
        description: String? = this.description,
        category: String? = this.category,
        phone: String? = this.phone,
        name: String? = this.name,
        mpesaRef: String? = this.mpesaRef,
        rawSms: String? = this.rawSms,
        balance: Double? = this.balance,
        timestamp: Long = this.timestamp
    ) = Transaction(
        type = type, UserId = UserId, id = id, amount = amount, date = date,
        description = description, category = category, phone = phone,
        name = name, mpesaRef = mpesaRef, rawSms = rawSms,
        balance = balance, timestamp = timestamp
    )
}

enum class TransactionType {
    RECEIVED,
    SENT,
    PAYBILL,
    BUY_GOODS,
    WITHDRAWAL,
    DEPOSIT,
    REVERSAL,
    SAVINGS,
    LOAN_REPAYMENT,
    LOAN_RECEIVED,    // ✅ Added
    INCOME,
    EXPENSE
}