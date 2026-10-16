package com.example.data.model

import java.text.NumberFormat
import java.util.Locale

/**
 * Status of each school term's fee payment and savings.
 */
enum class TermStatus(val displayName: String) {
    PAID("Paid"),
    DUE_SOON("Due Soon"),
    SAVING("Saving"),
    UPCOMING("Upcoming"),
    OVERDUE("Overdue")
}

/**
 * Single Term details: fee amount, due date, payment status.
 */
data class TermFee(
    val id: Int,
    val name: String, // e.g. "Term 1"
    val subtitle: String, // e.g. "Monsoon / Fall Term"
    val feeAmount: Double, // e.g. 12000.0
    val dueDate: String, // ISO date string e.g. "2026-11-15"
    val isPaid: Boolean = false,
    val paidDate: String? = null,
    val paidReference: String? = null,
    val paymentMethod: String? = null
)

/**
 * Overall School Fee Plan for the academic year.
 */
data class SchoolFeePlan(
    val parentName: String,
    val childName: String,
    val schoolName: String,
    val academicYear: String,
    val terms: List<TermFee>,
    val targetMonthlySavings: Double = 3000.0
) {
    val totalAnnualFee: Double
        get() = terms.sumOf { it.feeAmount }
}

/**
 * Individual savings contribution towards a term.
 */
data class SavingsContribution(
    val id: String,
    val termId: Int,
    val amount: Double,
    val date: String,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Household monthly income and expense budget.
 */
data class HouseholdBudget(
    val monthlyIncome: Double,
    val rentAndBills: Double,
    val groceries: Double,
    val transport: Double,
    val otherEssentials: Double
) {
    val totalExpenses: Double
        get() = rentAndBills + groceries + transport + otherEssentials

    val monthlySurplus: Double
        get() = (monthlyIncome - totalExpenses).coerceAtLeast(0.0)
}

/**
 * Types of in-app fee reminders.
 */
enum class ReminderType {
    DEADLINE_APPROACHING,
    MONTHLY_TARGET,
    MISSED_SAVINGS,
    UNDERFUNDED
}

/**
 * In-app notification / reminder item.
 */
data class FeeReminder(
    val id: String,
    val title: String,
    val message: String,
    val type: ReminderType,
    val dateText: String,
    val isDismissed: Boolean = false,
    val actionTermId: Int? = null
)

/**
 * Payment confirmation record.
 */
data class PaymentRecord(
    val id: String,
    val termId: Int,
    val termName: String,
    val amount: Double,
    val paymentDate: String,
    val paymentMethod: String,
    val referenceNo: String,
    val notes: String
)

/**
 * Utility functions for currency and date formatting.
 */
object FeeFormatter {
    private val indianLocale = Locale("en", "IN")

    fun formatRupee(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(indianLocale)
        // Ensure standard ₹ symbol
        return formatter.format(amount).replace("INR", "₹").trim()
    }

    fun formatRupeeShort(amount: Double): String {
        return "₹${String.format(Locale.US, "%,.0f", amount)}"
    }
}
