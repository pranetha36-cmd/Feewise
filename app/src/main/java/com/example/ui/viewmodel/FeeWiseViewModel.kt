package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FeeReminder
import com.example.data.model.HouseholdBudget
import com.example.data.model.PaymentRecord
import com.example.data.model.SavingsContribution
import com.example.data.model.SchoolFeePlan
import com.example.data.model.TermFee
import com.example.data.model.TermStatus
import com.example.data.repository.FeeWiseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TermUiModel(
    val term: TermFee,
    val savedAmount: Double,
    val remainingAmount: Double,
    val progressFraction: Float,
    val status: TermStatus,
    val daysUntilDue: Int,
    val isDueSoon: Boolean,
    val isTargetMet: Boolean
)

data class DashboardUiState(
    val plan: SchoolFeePlan,
    val totalAnnualFee: Double,
    val totalSaved: Double,
    val totalRemaining: Double,
    val totalPaid: Double,
    val savingsProgressFraction: Float,
    val nextUpcomingTerm: TermUiModel?,
    val monthlySavingsTarget: Double,
    val termsList: List<TermUiModel>,
    val unreadRemindersCount: Int,
    val isBudgetAlertActive: Boolean,
    val budgetSurplus: Double
)

class FeeWiseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FeeWiseRepository(application.applicationContext)

    val feePlan: StateFlow<SchoolFeePlan> = repository.feePlan
    val contributions: StateFlow<List<SavingsContribution>> = repository.contributions
    val budget: StateFlow<HouseholdBudget> = repository.budget
    val paymentRecords: StateFlow<List<PaymentRecord>> = repository.paymentRecords
    val reminders: StateFlow<List<FeeReminder>> = repository.reminders

    // ----------------------------------------------------
    // Calculator State
    // ----------------------------------------------------
    private val _calcFeeAmount = MutableStateFlow("12000")
    val calcFeeAmount: StateFlow<String> = _calcFeeAmount.asStateFlow()

    private val _calcSavedAmount = MutableStateFlow("3000")
    val calcSavedAmount: StateFlow<String> = _calcSavedAmount.asStateFlow()

    private val _calcMonthsRemaining = MutableStateFlow(4)
    val calcMonthsRemaining: StateFlow<Int> = _calcMonthsRemaining.asStateFlow()

    private val _calcWeeksRemaining = MutableStateFlow(17)
    val calcWeeksRemaining: StateFlow<Int> = _calcWeeksRemaining.asStateFlow()

    private val _calcSelectedTermId = MutableStateFlow<Int?>(1)
    val calcSelectedTermId: StateFlow<Int?> = _calcSelectedTermId.asStateFlow()

    // ----------------------------------------------------
    // CSV Export State
    // ----------------------------------------------------
    private val _exportDateRange = MutableStateFlow(FeeWiseRepository.DateRangeFilter.ALL_TIME)
    val exportDateRange: StateFlow<FeeWiseRepository.DateRangeFilter> = _exportDateRange.asStateFlow()

    private val _exportIncludePlan = MutableStateFlow(true)
    val exportIncludePlan: StateFlow<Boolean> = _exportIncludePlan.asStateFlow()

    private val _exportIncludeContribs = MutableStateFlow(true)
    val exportIncludeContribs: StateFlow<Boolean> = _exportIncludeContribs.asStateFlow()

    private val _exportIncludeBudget = MutableStateFlow(true)
    val exportIncludeBudget: StateFlow<Boolean> = _exportIncludeBudget.asStateFlow()

    private val _exportIncludePayments = MutableStateFlow(true)
    val exportIncludePayments: StateFlow<Boolean> = _exportIncludePayments.asStateFlow()

    private val _exportedCsvPreview = MutableStateFlow("")
    val exportedCsvPreview: StateFlow<String> = _exportedCsvPreview.asStateFlow()

    // ----------------------------------------------------
    // Combined Dashboard State
    // ----------------------------------------------------
    val dashboardUiState: StateFlow<DashboardUiState> = combine(
        feePlan,
        contributions,
        budget,
        reminders
    ) { plan, contribs, budget, remindersList ->
        computeDashboardState(plan, contribs, budget, remindersList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = computeDashboardState(
            feePlan.value,
            contributions.value,
            budget.value,
            reminders.value
        )
    )

    private fun computeDashboardState(
        plan: SchoolFeePlan,
        contribs: List<SavingsContribution>,
        budget: HouseholdBudget,
        remindersList: List<FeeReminder>
    ): DashboardUiState {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = Date()

        val termsUiList = plan.terms.map { term ->
            val saved = contribs.filter { it.termId == term.id }.sumOf { it.amount }
            val remaining = (term.feeAmount - saved).coerceAtLeast(0.0)
            val progress = if (term.feeAmount > 0) {
                (saved / term.feeAmount).toFloat().coerceIn(0f, 1f)
            } else 0f

            var daysUntil = 999
            try {
                val due = dateFormat.parse(term.dueDate)
                if (due != null) {
                    val diff = due.time - today.time
                    daysUntil = (diff / (1000 * 60 * 60 * 24)).toInt()
                }
            } catch (_: Exception) {}

            val isDueSoon = !term.isPaid && daysUntil in 0..14
            val isOverdue = !term.isPaid && daysUntil < 0
            val isTargetMet = saved >= term.feeAmount

            val status = when {
                term.isPaid -> TermStatus.PAID
                isOverdue -> TermStatus.OVERDUE
                isDueSoon -> TermStatus.DUE_SOON
                saved > 0 -> TermStatus.SAVING
                else -> TermStatus.UPCOMING
            }

            TermUiModel(
                term = term,
                savedAmount = saved,
                remainingAmount = remaining,
                progressFraction = progress,
                status = status,
                daysUntilDue = daysUntil,
                isDueSoon = isDueSoon,
                isTargetMet = isTargetMet
            )
        }

        val totalAnnualFee = plan.totalAnnualFee
        val totalSaved = contribs.sumOf { it.amount }
        val totalPaid = termsUiList.filter { it.term.isPaid }.sumOf { it.term.feeAmount }
        val totalRemaining = (totalAnnualFee - totalSaved).coerceAtLeast(0.0)
        val overallProgress = if (totalAnnualFee > 0) {
            (totalSaved / totalAnnualFee).toFloat().coerceIn(0f, 1f)
        } else 0f

        // Next upcoming unpaid term
        val nextTerm = termsUiList
            .filter { !it.term.isPaid }
            .minByOrNull { it.daysUntilDue }

        val unreadReminders = remindersList.count { !it.isDismissed }
        val isBudgetAlert = plan.targetMonthlySavings > budget.monthlySurplus

        return DashboardUiState(
            plan = plan,
            totalAnnualFee = totalAnnualFee,
            totalSaved = totalSaved,
            totalRemaining = totalRemaining,
            totalPaid = totalPaid,
            savingsProgressFraction = overallProgress,
            nextUpcomingTerm = nextTerm,
            monthlySavingsTarget = plan.targetMonthlySavings,
            termsList = termsUiList,
            unreadRemindersCount = unreadReminders,
            isBudgetAlertActive = isBudgetAlert,
            budgetSurplus = budget.monthlySurplus
        )
    }

    // ----------------------------------------------------
    // Fee Plan Operations
    // ----------------------------------------------------

    fun updatePlanDetails(parentName: String, childName: String, schoolName: String, academicYear: String, monthlyGoal: Double) {
        val current = feePlan.value
        repository.updateFeePlan(
            current.copy(
                parentName = parentName.trim(),
                childName = childName.trim(),
                schoolName = schoolName.trim(),
                academicYear = academicYear.trim(),
                targetMonthlySavings = monthlyGoal
            )
        )
    }

    fun updateTermDetails(termId: Int, fee: Double, dueDate: String, subtitle: String) {
        repository.updateTerm(termId, fee, dueDate, subtitle)
    }

    // ----------------------------------------------------
    // Savings Operations
    // ----------------------------------------------------

    fun addSavingsContribution(termId: Int, amount: Double, note: String, date: String? = null) {
        val dateToUse = date ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        repository.addContribution(termId, amount, dateToUse, note)
    }

    fun deleteSavingsContribution(id: String) {
        repository.deleteContribution(id)
    }

    // ----------------------------------------------------
    // Budget Operations
    // ----------------------------------------------------

    fun updateHouseholdBudget(income: Double, rent: Double, groceries: Double, transport: Double, other: Double) {
        repository.updateBudget(
            HouseholdBudget(
                monthlyIncome = income,
                rentAndBills = rent,
                groceries = groceries,
                transport = transport,
                otherEssentials = other
            )
        )
    }

    // ----------------------------------------------------
    // Payment Operations
    // ----------------------------------------------------

    fun markTermPaid(termId: Int, amount: Double, paymentMethod: String, referenceNo: String, notes: String, paymentDate: String? = null) {
        val dateToUse = paymentDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        repository.recordPayment(termId, amount, dateToUse, paymentMethod, referenceNo, notes)
    }

    // ----------------------------------------------------
    // Calculator Operations
    // ----------------------------------------------------

    fun setCalcFeeAmount(value: String) {
        _calcFeeAmount.value = value
    }

    fun setCalcSavedAmount(value: String) {
        _calcSavedAmount.value = value
    }

    fun setCalcMonths(months: Int) {
        _calcMonthsRemaining.value = months.coerceIn(1, 24)
        _calcWeeksRemaining.value = (months * 4.33).toInt().coerceAtLeast(1)
    }

    fun setCalcWeeks(weeks: Int) {
        _calcWeeksRemaining.value = weeks.coerceIn(1, 104)
        _calcMonthsRemaining.value = (weeks / 4.33).toInt().coerceAtLeast(1)
    }

    fun selectTermForCalculator(termId: Int?) {
        _calcSelectedTermId.value = termId
        if (termId != null) {
            val term = feePlan.value.terms.find { it.id == termId }
            if (term != null) {
                val saved = repository.getSavedAmountForTerm(termId)
                _calcFeeAmount.value = term.feeAmount.toInt().toString()
                _calcSavedAmount.value = saved.toInt().toString()

                // Calculate approximate months from due date
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val due = sdf.parse(term.dueDate)
                    if (due != null) {
                        val diffDays = ((due.time - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
                        val months = (diffDays / 30).coerceIn(1, 12)
                        setCalcMonths(months)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun applyCalculatorTargetAsGoal(monthlyTarget: Double) {
        val current = feePlan.value
        repository.updateFeePlan(current.copy(targetMonthlySavings = monthlyTarget))
        Toast.makeText(getApplication(), "Updated monthly goal to ₹${monthlyTarget.toInt()}", Toast.LENGTH_SHORT).show()
    }

    // ----------------------------------------------------
    // CSV Export Operations
    // ----------------------------------------------------

    fun setExportDateRange(range: FeeWiseRepository.DateRangeFilter) {
        _exportDateRange.value = range
        refreshCsvPreview()
    }

    fun toggleExportPlan(include: Boolean) {
        _exportIncludePlan.value = include
        refreshCsvPreview()
    }

    fun toggleExportContribs(include: Boolean) {
        _exportIncludeContribs.value = include
        refreshCsvPreview()
    }

    fun toggleExportBudget(include: Boolean) {
        _exportIncludeBudget.value = include
        refreshCsvPreview()
    }

    fun toggleExportPayments(include: Boolean) {
        _exportIncludePayments.value = include
        refreshCsvPreview()
    }

    fun refreshCsvPreview() {
        val csv = repository.generateCsvReport(
            dateRange = _exportDateRange.value,
            includeFeePlan = _exportIncludePlan.value,
            includeContributions = _exportIncludeContribs.value,
            includeBudget = _exportIncludeBudget.value,
            includePayments = _exportIncludePayments.value
        )
        _exportedCsvPreview.value = csv
    }

    fun shareCsvFile(context: Context) {
        val csv = repository.generateCsvReport(
            dateRange = _exportDateRange.value,
            includeFeePlan = _exportIncludePlan.value,
            includeContributions = _exportIncludeContribs.value,
            includeBudget = _exportIncludeBudget.value,
            includePayments = _exportIncludePayments.value
        )
        val uri: Uri = repository.saveCsvToCache(csv)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "FeeWise Financial Export (${_exportDateRange.value.label})")
            putExtra(Intent.EXTRA_TEXT, "Here is your FeeWise School Fee Plan and financial report exported in CSV format.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Download or Share FeeWise CSV")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun copyCsvToClipboard(context: Context) {
        val csv = repository.generateCsvReport(
            dateRange = _exportDateRange.value,
            includeFeePlan = _exportIncludePlan.value,
            includeContributions = _exportIncludeContribs.value,
            includeBudget = _exportIncludeBudget.value,
            includePayments = _exportIncludePayments.value
        )
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("FeeWise Financial CSV", csv)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    // ----------------------------------------------------
    // Reminders
    // ----------------------------------------------------

    fun dismissReminder(id: String) {
        repository.dismissReminder(id)
    }

    fun restoreReminders() {
        repository.restoreReminders()
        Toast.makeText(getApplication(), "Reminders restored", Toast.LENGTH_SHORT).show()
    }

    // ----------------------------------------------------
    // Reset Data
    // ----------------------------------------------------

    fun resetToSampleData() {
        repository.resetToSampleData()
        selectTermForCalculator(1)
        refreshCsvPreview()
        Toast.makeText(getApplication(), "Reset to sample 3-term plan (₹36,000)", Toast.LENGTH_SHORT).show()
    }
}
