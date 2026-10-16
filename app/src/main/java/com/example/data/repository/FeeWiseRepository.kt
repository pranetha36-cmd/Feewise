package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.FeeReminder
import com.example.data.model.HouseholdBudget
import com.example.data.model.PaymentRecord
import com.example.data.model.ReminderType
import com.example.data.model.SavingsContribution
import com.example.data.model.SchoolFeePlan
import com.example.data.model.TermFee
import com.example.data.model.TermStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FeeWiseRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("feewise_prefs", Context.MODE_PRIVATE)

    private val _feePlan = MutableStateFlow(loadFeePlan())
    val feePlan: StateFlow<SchoolFeePlan> = _feePlan.asStateFlow()

    private val _contributions = MutableStateFlow(loadContributions())
    val contributions: StateFlow<List<SavingsContribution>> = _contributions.asStateFlow()

    private val _budget = MutableStateFlow(loadBudget())
    val budget: StateFlow<HouseholdBudget> = _budget.asStateFlow()

    private val _paymentRecords = MutableStateFlow(loadPaymentRecords())
    val paymentRecords: StateFlow<List<PaymentRecord>> = _paymentRecords.asStateFlow()

    private val _reminders = MutableStateFlow(loadReminders())
    val reminders: StateFlow<List<FeeReminder>> = _reminders.asStateFlow()

    init {
        // Re-evaluate reminders whenever state is initialized
        refreshReminders()
    }

    // ----------------------------------------------------
    // Fee Plan Operations
    // ----------------------------------------------------

    fun updateFeePlan(updatedPlan: SchoolFeePlan) {
        _feePlan.value = updatedPlan
        saveFeePlan(updatedPlan)
        refreshReminders()
    }

    fun updateTerm(termId: Int, newFee: Double, newDueDate: String, subtitle: String? = null) {
        val current = _feePlan.value
        val updatedTerms = current.terms.map { term ->
            if (term.id == termId) {
                term.copy(
                    feeAmount = newFee,
                    dueDate = newDueDate,
                    subtitle = subtitle ?: term.subtitle
                )
            } else term
        }
        updateFeePlan(current.copy(terms = updatedTerms))
    }

    // ----------------------------------------------------
    // Savings Contributions
    // ----------------------------------------------------

    fun addContribution(termId: Int, amount: Double, date: String, note: String): SavingsContribution {
        val newContrib = SavingsContribution(
            id = UUID.randomUUID().toString(),
            termId = termId,
            amount = amount,
            date = date,
            note = note.ifBlank { "Savings contribution" },
            timestamp = System.currentTimeMillis()
        )
        val updatedList = listOf(newContrib) + _contributions.value
        _contributions.value = updatedList
        saveContributions(updatedList)
        refreshReminders()
        return newContrib
    }

    fun deleteContribution(id: String) {
        val updatedList = _contributions.value.filterNot { it.id == id }
        _contributions.value = updatedList
        saveContributions(updatedList)
        refreshReminders()
    }

    fun getSavedAmountForTerm(termId: Int): Double {
        return _contributions.value
            .filter { it.termId == termId }
            .sumOf { it.amount }
    }

    fun getTotalSaved(): Double {
        return _contributions.value.sumOf { it.amount }
    }

    // ----------------------------------------------------
    // Household Budget
    // ----------------------------------------------------

    fun updateBudget(updatedBudget: HouseholdBudget) {
        _budget.value = updatedBudget
        saveBudget(updatedBudget)
        refreshReminders()
    }

    // ----------------------------------------------------
    // Payment Records
    // ----------------------------------------------------

    fun recordPayment(
        termId: Int,
        amount: Double,
        paymentDate: String,
        paymentMethod: String,
        referenceNo: String,
        notes: String
    ): PaymentRecord {
        val currentPlan = _feePlan.value
        val term = currentPlan.terms.find { it.id == termId }
        val termName = term?.name ?: "Term $termId"

        val newRecord = PaymentRecord(
            id = UUID.randomUUID().toString(),
            termId = termId,
            termName = termName,
            amount = amount,
            paymentDate = paymentDate,
            paymentMethod = paymentMethod,
            referenceNo = referenceNo,
            notes = notes
        )

        // Mark term as paid in fee plan
        val updatedTerms = currentPlan.terms.map {
            if (it.id == termId) {
                it.copy(
                    isPaid = true,
                    paidDate = paymentDate,
                    paidReference = referenceNo,
                    paymentMethod = paymentMethod
                )
            } else it
        }
        updateFeePlan(currentPlan.copy(terms = updatedTerms))

        val updatedRecords = listOf(newRecord) + _paymentRecords.value
        _paymentRecords.value = updatedRecords
        savePaymentRecords(updatedRecords)
        refreshReminders()
        return newRecord
    }

    // ----------------------------------------------------
    // Reminders & Alerts
    // ----------------------------------------------------

    fun dismissReminder(id: String) {
        val updated = _reminders.value.map {
            if (it.id == id) it.copy(isDismissed = true) else it
        }
        _reminders.value = updated
        saveReminders(updated)
    }

    fun restoreReminders() {
        refreshReminders(forceRecreate = true)
    }

    private fun refreshReminders(forceRecreate: Boolean = false) {
        val plan = _feePlan.value
        val budget = _budget.value
        val existingDismissedIds = if (forceRecreate) emptySet() else {
            _reminders.value.filter { it.isDismissed }.map { it.id }.toSet()
        }

        val generated = mutableListOf<FeeReminder>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = Date()

        for (term in plan.terms) {
            val saved = getSavedAmountForTerm(term.id)
            val remaining = (term.feeAmount - saved).coerceAtLeast(0.0)

            if (!term.isPaid) {
                // Check due date proximity
                try {
                    val dueDateObj = dateFormat.parse(term.dueDate)
                    if (dueDateObj != null) {
                        val diffMillis = dueDateObj.time - today.time
                        val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

                        if (diffDays in 0..7) {
                            generated.add(
                                FeeReminder(
                                    id = "reminder_due_soon_${term.id}",
                                    title = "Fee Deadline Approaching: ${term.name}",
                                    message = "${term.name} fee of ₹${term.feeAmount.toInt()} is due in $diffDays day(s) on ${term.dueDate}. Remaining: ₹${remaining.toInt()}.",
                                    type = ReminderType.DEADLINE_APPROACHING,
                                    dateText = "Due in $diffDays days",
                                    actionTermId = term.id
                                )
                            )
                        } else if (diffDays < 0) {
                            generated.add(
                                FeeReminder(
                                    id = "reminder_overdue_${term.id}",
                                    title = "Payment Deadline Passed: ${term.name}",
                                    message = "${term.name} fee was due on ${term.dueDate}. Please arrange ₹${remaining.toInt()} to complete this term's fees.",
                                    type = ReminderType.DEADLINE_APPROACHING,
                                    dateText = "Past Due",
                                    actionTermId = term.id
                                )
                            )
                        }
                    }
                } catch (_: Exception) {}

                // Underfunded warning
                if (saved == 0.0) {
                    generated.add(
                        FeeReminder(
                            id = "reminder_underfunded_${term.id}",
                            title = "Upcoming Term Not Started: ${term.name}",
                            message = "No savings recorded yet for ${term.name} (Fee: ₹${term.feeAmount.toInt()}). Start setting aside small weekly amounts.",
                            type = ReminderType.UNDERFUNDED,
                            dateText = "Pending Savings",
                            actionTermId = term.id
                        )
                    )
                }
            }
        }

        // Monthly Target Reminder
        val monthlyTarget = plan.targetMonthlySavings
        generated.add(
            FeeReminder(
                id = "reminder_monthly_target",
                title = "Monthly School Fee Target: ₹${monthlyTarget.toInt()}",
                message = "Keep on track for the school year by contributing ₹${monthlyTarget.toInt()} into your fee fund this month.",
                type = ReminderType.MONTHLY_TARGET,
                dateText = "Monthly Goal"
            )
        )

        // Budget feasibility check reminder
        if (monthlyTarget > budget.monthlySurplus) {
            val deficit = (monthlyTarget - budget.monthlySurplus).toInt()
            generated.add(
                FeeReminder(
                    id = "reminder_budget_alert",
                    title = "Budget Advisory: Target Exceeds Monthly Surplus",
                    message = "Your planned monthly target of ₹${monthlyTarget.toInt()} exceeds your surplus by ₹$deficit. Review non-essential expenses in Budget Planner.",
                    type = ReminderType.MISSED_SAVINGS,
                    dateText = "Budget Warning"
                )
            )
        }

        val finalized = generated.map { reminder ->
            if (existingDismissedIds.contains(reminder.id)) {
                reminder.copy(isDismissed = true)
            } else reminder
        }

        _reminders.value = finalized
        saveReminders(finalized)
    }

    // ----------------------------------------------------
    // CSV Export Generator
    // ----------------------------------------------------

    enum class DateRangeFilter(val label: String) {
        ALL_TIME("All Time"),
        CURRENT_YEAR("Academic Year 2026-2027"),
        LAST_30_DAYS("Last 30 Days"),
        LAST_90_DAYS("Last 90 Days")
    }

    fun generateCsvReport(
        dateRange: DateRangeFilter,
        includeFeePlan: Boolean = true,
        includeContributions: Boolean = true,
        includeBudget: Boolean = true,
        includePayments: Boolean = true
    ): String {
        val plan = _feePlan.value
        val allContribs = _contributions.value
        val budget = _budget.value
        val payments = _paymentRecords.value

        val now = System.currentTimeMillis()
        val filteredContribs = when (dateRange) {
            DateRangeFilter.ALL_TIME -> allContribs
            DateRangeFilter.CURRENT_YEAR -> allContribs // all within current academic year
            DateRangeFilter.LAST_30_DAYS -> {
                val cutoff = now - (30L * 24 * 60 * 60 * 1000)
                allContribs.filter { it.timestamp >= cutoff }
            }
            DateRangeFilter.LAST_90_DAYS -> {
                val cutoff = now - (90L * 24 * 60 * 60 * 1000)
                allContribs.filter { it.timestamp >= cutoff }
            }
        }

        val csv = StringBuilder()
        csv.appendLine("FeeWise - Smart School Fee Planner Export")
        csv.appendLine("Export Date,${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())}")
        csv.appendLine("Date Range Filter,${dateRange.label}")
        csv.appendLine("")

        if (includeFeePlan) {
            csv.appendLine("=== 1. SCHOOL FEE PLAN OVERVIEW ===")
            csv.appendLine("Parent Name,${escapeCsv(plan.parentName)}")
            csv.appendLine("Child Name,${escapeCsv(plan.childName)}")
            csv.appendLine("School Name,${escapeCsv(plan.schoolName)}")
            csv.appendLine("Academic Year,${escapeCsv(plan.academicYear)}")
            csv.appendLine("Total Annual Fee,INR ${plan.totalAnnualFee.toInt()}")
            csv.appendLine("Monthly Savings Goal,INR ${plan.targetMonthlySavings.toInt()}")
            csv.appendLine("")

            csv.appendLine("Term Name,Subtitle,Fee Amount (INR),Due Date,Saved (INR),Remaining (INR),Status,Payment Date,Payment Ref")
            for (term in plan.terms) {
                val saved = getSavedAmountForTerm(term.id)
                val remaining = (term.feeAmount - saved).coerceAtLeast(0.0)
                val status = if (term.isPaid) "PAID" else if (saved >= term.feeAmount) "SAVED (100%)" else "PENDING"
                csv.appendLine("${escapeCsv(term.name)},${escapeCsv(term.subtitle)},${term.feeAmount.toInt()},${term.dueDate},${saved.toInt()},${remaining.toInt()},$status,${term.paidDate ?: "-"},${escapeCsv(term.paidReference ?: "-")}")
            }
            csv.appendLine("")
        }

        if (includeContributions) {
            csv.appendLine("=== 2. SAVINGS CONTRIBUTIONS HISTORY ===")
            csv.appendLine("Date,Term,Amount (INR),Source / Notes")
            if (filteredContribs.isEmpty()) {
                csv.appendLine("No savings contributions recorded for selected range.,,,")
            } else {
                for (c in filteredContribs) {
                    val termName = plan.terms.find { it.id == c.termId }?.name ?: "Term ${c.termId}"
                    csv.appendLine("${c.date},${escapeCsv(termName)},${c.amount.toInt()},${escapeCsv(c.note)}")
                }
            }
            csv.appendLine("Total Filtered Savings,INR ${filteredContribs.sumOf { it.amount }.toInt()},,")
            csv.appendLine("")
        }

        if (includeBudget) {
            csv.appendLine("=== 3. HOUSEHOLD BUDGET PLANNER ===")
            csv.appendLine("Category,Monthly Amount (INR)")
            csv.appendLine("Monthly Income,${budget.monthlyIncome.toInt()}")
            csv.appendLine("Rent & Household Bills,${budget.rentAndBills.toInt()}")
            csv.appendLine("Groceries,${budget.groceries.toInt()}")
            csv.appendLine("Transport,${budget.transport.toInt()}")
            csv.appendLine("Other Essential Expenses,${budget.otherEssentials.toInt()}")
            csv.appendLine("Total Monthly Expenses,${budget.totalExpenses.toInt()}")
            csv.appendLine("Monthly Net Surplus,${budget.monthlySurplus.toInt()}")
            csv.appendLine("Recommended School Fee Allocation,${plan.targetMonthlySavings.toInt()}")
            val budgetStatus = if (plan.targetMonthlySavings <= budget.monthlySurplus) "AFFORDABLE (Within budget)" else "EXCEEDS SURPLUS (Caution)"
            csv.appendLine("Budget Feasibility Status,$budgetStatus")
            csv.appendLine("")
        }

        if (includePayments) {
            csv.appendLine("=== 4. PAYMENT RECORDS REGISTER ===")
            csv.appendLine("Date,Term,Amount Paid (INR),Payment Method,Reference No,Notes")
            if (payments.isEmpty()) {
                csv.appendLine("No payments recorded yet.,,,,,")
            } else {
                for (p in payments) {
                    csv.appendLine("${p.paymentDate},${escapeCsv(p.termName)},${p.amount.toInt()},${escapeCsv(p.paymentMethod)},${escapeCsv(p.referenceNo)},${escapeCsv(p.notes)}")
                }
            }
            csv.appendLine("")
        }

        return csv.toString()
    }

    /**
     * Writes CSV string to app cache and returns shareable URI using FileProvider.
     */
    fun saveCsvToCache(csvContent: String): Uri {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(exportDir, "FeeWise_Report_$timestamp.csv")
        FileOutputStream(file).use { out ->
            out.write(csvContent.toByteArray(Charsets.UTF_8))
        }

        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    // ----------------------------------------------------
    // Reset to Sample Data (3 terms of ₹12,000 = ₹36,000)
    // ----------------------------------------------------

    fun resetToSampleData() {
        val samplePlan = createDefaultSamplePlan()
        val sampleContribs = createDefaultSampleContributions()
        val sampleBudget = createDefaultSampleBudget()
        val samplePayments = emptyList<PaymentRecord>()

        _feePlan.value = samplePlan
        _contributions.value = sampleContribs
        _budget.value = sampleBudget
        _paymentRecords.value = samplePayments

        saveFeePlan(samplePlan)
        saveContributions(sampleContribs)
        saveBudget(sampleBudget)
        savePaymentRecords(samplePayments)

        refreshReminders(forceRecreate = true)
    }

    // ----------------------------------------------------
    // JSON SharedPreferences Storage
    // ----------------------------------------------------

    private fun loadFeePlan(): SchoolFeePlan {
        val jsonStr = prefs.getString("key_fee_plan", null) ?: return createDefaultSamplePlan()
        return try {
            val obj = JSONObject(jsonStr)
            val parent = obj.optString("parentName", "Priya & Rajesh Sharma")
            val child = obj.optString("childName", "Aarav Sharma (Class 6B)")
            val school = obj.optString("schoolName", "Delhi Public School")
            val year = obj.optString("academicYear", "2026 - 2027")
            val monthlyGoal = obj.optDouble("targetMonthlySavings", 3000.0)

            val termsArr = obj.getJSONArray("terms")
            val termsList = mutableListOf<TermFee>()
            for (i in 0 until termsArr.length()) {
                val t = termsArr.getJSONObject(i)
                termsList.add(
                    TermFee(
                        id = t.getInt("id"),
                        name = t.getString("name"),
                        subtitle = t.optString("subtitle", ""),
                        feeAmount = t.getDouble("feeAmount"),
                        dueDate = t.getString("dueDate"),
                        isPaid = t.optBoolean("isPaid", false),
                        paidDate = if (t.has("paidDate") && !t.isNull("paidDate")) t.getString("paidDate") else null,
                        paidReference = if (t.has("paidReference") && !t.isNull("paidReference")) t.getString("paidReference") else null,
                        paymentMethod = if (t.has("paymentMethod") && !t.isNull("paymentMethod")) t.getString("paymentMethod") else null
                    )
                )
            }
            SchoolFeePlan(parent, child, school, year, termsList, monthlyGoal)
        } catch (_: Exception) {
            createDefaultSamplePlan()
        }
    }

    private fun saveFeePlan(plan: SchoolFeePlan) {
        val obj = JSONObject()
        obj.put("parentName", plan.parentName)
        obj.put("childName", plan.childName)
        obj.put("schoolName", plan.schoolName)
        obj.put("academicYear", plan.academicYear)
        obj.put("targetMonthlySavings", plan.targetMonthlySavings)

        val termsArr = JSONArray()
        for (term in plan.terms) {
            val t = JSONObject()
            t.put("id", term.id)
            t.put("name", term.name)
            t.put("subtitle", term.subtitle)
            t.put("feeAmount", term.feeAmount)
            t.put("dueDate", term.dueDate)
            t.put("isPaid", term.isPaid)
            t.put("paidDate", term.paidDate)
            t.put("paidReference", term.paidReference)
            t.put("paymentMethod", term.paymentMethod)
            termsArr.put(t)
        }
        obj.put("terms", termsArr)
        prefs.edit().putString("key_fee_plan", obj.toString()).apply()
    }

    private fun loadContributions(): List<SavingsContribution> {
        val jsonStr = prefs.getString("key_contributions", null) ?: return createDefaultSampleContributions()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<SavingsContribution>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    SavingsContribution(
                        id = obj.getString("id"),
                        termId = obj.getInt("termId"),
                        amount = obj.getDouble("amount"),
                        date = obj.getString("date"),
                        note = obj.optString("note", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            createDefaultSampleContributions()
        }
    }

    private fun saveContributions(list: List<SavingsContribution>) {
        val arr = JSONArray()
        for (c in list) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("termId", c.termId)
            obj.put("amount", c.amount)
            obj.put("date", c.date)
            obj.put("note", c.note)
            obj.put("timestamp", c.timestamp)
            arr.put(obj)
        }
        prefs.edit().putString("key_contributions", arr.toString()).apply()
    }

    private fun loadBudget(): HouseholdBudget {
        val jsonStr = prefs.getString("key_budget", null) ?: return createDefaultSampleBudget()
        return try {
            val obj = JSONObject(jsonStr)
            HouseholdBudget(
                monthlyIncome = obj.getDouble("monthlyIncome"),
                rentAndBills = obj.getDouble("rentAndBills"),
                groceries = obj.getDouble("groceries"),
                transport = obj.getDouble("transport"),
                otherEssentials = obj.getDouble("otherEssentials")
            )
        } catch (_: Exception) {
            createDefaultSampleBudget()
        }
    }

    private fun saveBudget(budget: HouseholdBudget) {
        val obj = JSONObject()
        obj.put("monthlyIncome", budget.monthlyIncome)
        obj.put("rentAndBills", budget.rentAndBills)
        obj.put("groceries", budget.groceries)
        obj.put("transport", budget.transport)
        obj.put("otherEssentials", budget.otherEssentials)
        prefs.edit().putString("key_budget", obj.toString()).apply()
    }

    private fun loadPaymentRecords(): List<PaymentRecord> {
        val jsonStr = prefs.getString("key_payments", null) ?: return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<PaymentRecord>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    PaymentRecord(
                        id = obj.getString("id"),
                        termId = obj.getInt("termId"),
                        termName = obj.getString("termName"),
                        amount = obj.getDouble("amount"),
                        paymentDate = obj.getString("paymentDate"),
                        paymentMethod = obj.getString("paymentMethod"),
                        referenceNo = obj.optString("referenceNo", ""),
                        notes = obj.optString("notes", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun savePaymentRecords(list: List<PaymentRecord>) {
        val arr = JSONArray()
        for (p in list) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("termId", p.termId)
            obj.put("termName", p.termName)
            obj.put("amount", p.amount)
            obj.put("paymentDate", p.paymentDate)
            obj.put("paymentMethod", p.paymentMethod)
            obj.put("referenceNo", p.referenceNo)
            obj.put("notes", p.notes)
            arr.put(obj)
        }
        prefs.edit().putString("key_payments", arr.toString()).apply()
    }

    private fun loadReminders(): List<FeeReminder> {
        val jsonStr = prefs.getString("key_reminders", null) ?: return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<FeeReminder>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    FeeReminder(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        message = obj.getString("message"),
                        type = ReminderType.valueOf(obj.optString("type", ReminderType.DEADLINE_APPROACHING.name)),
                        dateText = obj.getString("dateText"),
                        isDismissed = obj.optBoolean("isDismissed", false),
                        actionTermId = if (obj.has("actionTermId")) obj.getInt("actionTermId") else null
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveReminders(list: List<FeeReminder>) {
        val arr = JSONArray()
        for (r in list) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("title", r.title)
            obj.put("message", r.message)
            obj.put("type", r.type.name)
            obj.put("dateText", r.dateText)
            obj.put("isDismissed", r.isDismissed)
            r.actionTermId?.let { obj.put("actionTermId", it) }
            arr.put(obj)
        }
        prefs.edit().putString("key_reminders", arr.toString()).apply()
    }

    // ----------------------------------------------------
    // Sample Data Factory (Strictly fulfills sample spec)
    // Three terms of ₹12,000 each = ₹36,000 total.
    // ----------------------------------------------------

    private fun createDefaultSamplePlan(): SchoolFeePlan {
        return SchoolFeePlan(
            parentName = "Priya & Rajesh Sharma",
            childName = "Aarav Sharma (Class 6B)",
            schoolName = "Delhi Public School, R.K. Puram",
            academicYear = "2026 - 2027",
            terms = listOf(
                TermFee(
                    id = 1,
                    name = "Term 1",
                    subtitle = "Monsoon Term (Jul - Oct)",
                    feeAmount = 12000.0,
                    dueDate = "2026-11-15", // ~30 days away
                    isPaid = false
                ),
                TermFee(
                    id = 2,
                    name = "Term 2",
                    subtitle = "Winter Term (Nov - Feb)",
                    feeAmount = 12000.0,
                    dueDate = "2027-02-15",
                    isPaid = false
                ),
                TermFee(
                    id = 3,
                    name = "Term 3",
                    subtitle = "Spring Term (Mar - Jun)",
                    feeAmount = 12000.0,
                    dueDate = "2027-05-15",
                    isPaid = false
                )
            ),
            targetMonthlySavings = 3000.0
        )
    }

    private fun createDefaultSampleContributions(): List<SavingsContribution> {
        return listOf(
            SavingsContribution(
                id = "sample_c1",
                termId = 1,
                amount = 2000.0,
                date = "2026-09-28",
                note = "September salary allocation",
                timestamp = System.currentTimeMillis() - (18L * 24 * 3600 * 1000)
            ),
            SavingsContribution(
                id = "sample_c2",
                termId = 1,
                amount = 1000.0,
                date = "2026-10-10",
                note = "Early festival savings",
                timestamp = System.currentTimeMillis() - (6L * 24 * 3600 * 1000)
            ),
            SavingsContribution(
                id = "sample_c3",
                termId = 2,
                amount = 1000.0,
                date = "2026-10-14",
                note = "Term 2 starter buffer",
                timestamp = System.currentTimeMillis() - (2L * 24 * 3600 * 1000)
            )
        )
    }

    private fun createDefaultSampleBudget(): HouseholdBudget {
        return HouseholdBudget(
            monthlyIncome = 75000.0,
            rentAndBills = 22000.0,
            groceries = 14000.0,
            transport = 6000.0,
            otherEssentials = 8000.0
        )
    }
}
