package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.FeeWiseBottomBar
import com.example.ui.components.FeeWiseTopBar
import com.example.ui.components.NavDestination
import com.example.ui.dialogs.AddContributionDialog
import com.example.ui.dialogs.EditPlanDialog
import com.example.ui.dialogs.RemindersCenterDialog
import com.example.ui.screens.BudgetPlannerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExportCsvScreen
import com.example.ui.screens.PaymentRecordsScreen
import com.example.ui.screens.ReportsInsightsScreen
import com.example.ui.screens.SavingsCalculatorScreen
import com.example.ui.screens.SavingsTrackerScreen
import com.example.ui.screens.TermsPlannerScreen
import com.example.ui.theme.FeeWiseTheme
import com.example.ui.viewmodel.FeeWiseViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FeeWiseTheme {
                FeeWiseApp()
            }
        }
    }
}

@Composable
fun FeeWiseApp(viewModel: FeeWiseViewModel = viewModel()) {
    var currentScreen by rememberSaveable { mutableStateOf(NavDestination.DASHBOARD) }

    // Dialog states
    var showAddSavingsDialog by remember { mutableStateOf(false) }
    var addSavingsTargetTermId by remember { mutableIntStateOf(1) }
    var showEditPlanDialog by remember { mutableStateOf(false) }
    var showRemindersDialog by remember { mutableStateOf(false) }

    // Collect viewmodel flows
    val dashboardState by viewModel.dashboardUiState.collectAsStateWithLifecycle()
    val feePlan by viewModel.feePlan.collectAsStateWithLifecycle()
    val contributions by viewModel.contributions.collectAsStateWithLifecycle()
    val budget by viewModel.budget.collectAsStateWithLifecycle()
    val paymentRecords by viewModel.paymentRecords.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()

    // Calculator flows
    val calcFeeStr by viewModel.calcFeeAmount.collectAsStateWithLifecycle()
    val calcSavedStr by viewModel.calcSavedAmount.collectAsStateWithLifecycle()
    val calcMonths by viewModel.calcMonthsRemaining.collectAsStateWithLifecycle()
    val calcWeeks by viewModel.calcWeeksRemaining.collectAsStateWithLifecycle()
    val calcSelectedTermId by viewModel.calcSelectedTermId.collectAsStateWithLifecycle()

    // CSV export flows
    val exportDateRange by viewModel.exportDateRange.collectAsStateWithLifecycle()
    val exportIncludePlan by viewModel.exportIncludePlan.collectAsStateWithLifecycle()
    val exportIncludeContribs by viewModel.exportIncludeContribs.collectAsStateWithLifecycle()
    val exportIncludeBudget by viewModel.exportIncludeBudget.collectAsStateWithLifecycle()
    val exportIncludePayments by viewModel.exportIncludePayments.collectAsStateWithLifecycle()
    val csvPreview by viewModel.exportedCsvPreview.collectAsStateWithLifecycle()

    // Initialize CSV preview on first display
    LaunchedEffect(Unit) {
        viewModel.refreshCsvPreview()
    }

    // Hardware back button navigation
    BackHandler(enabled = currentScreen != NavDestination.DASHBOARD) {
        currentScreen = NavDestination.DASHBOARD
    }

    Scaffold(
        topBar = {
            FeeWiseTopBar(
                currentScreen = currentScreen,
                unreadReminders = dashboardState.unreadRemindersCount,
                onNotificationsClick = { showRemindersDialog = true },
                onNavigate = { currentScreen = it },
                onResetData = { viewModel.resetToSampleData() }
            )
        },
        bottomBar = {
            FeeWiseBottomBar(
                currentScreen = currentScreen,
                onNavigate = { currentScreen = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                NavDestination.DASHBOARD -> {
                    DashboardScreen(
                        state = dashboardState,
                        onNavigate = { currentScreen = it },
                        onAddSavingsClick = {
                            addSavingsTargetTermId = dashboardState.nextUpcomingTerm?.term?.id ?: 1
                            showAddSavingsDialog = true
                        },
                        onEditPlanClick = { showEditPlanDialog = true },
                        onOpenRemindersClick = { showRemindersDialog = true }
                    )
                }

                NavDestination.TERMS -> {
                    TermsPlannerScreen(
                        plan = feePlan,
                        termsUiList = dashboardState.termsList,
                        onUpdatePlanDetails = { parent, child, school, year, goal ->
                            viewModel.updatePlanDetails(parent, child, school, year, goal)
                        },
                        onUpdateTermDetails = { termId, fee, dueDate, subtitle ->
                            viewModel.updateTermDetails(termId, fee, dueDate, subtitle)
                        },
                        onAddSavingsForTerm = { termId ->
                            addSavingsTargetTermId = termId
                            showAddSavingsDialog = true
                        },
                        onMarkTermPaid = { termId, amount, method, ref, notes, date ->
                            viewModel.markTermPaid(termId, amount, method, ref, notes, date)
                        },
                        onOpenCalculatorForTerm = { termId ->
                            viewModel.selectTermForCalculator(termId)
                            currentScreen = NavDestination.CALCULATOR
                        }
                    )
                }

                NavDestination.CALCULATOR -> {
                    SavingsCalculatorScreen(
                        plan = feePlan,
                        budget = budget,
                        feeAmountStr = calcFeeStr,
                        savedAmountStr = calcSavedStr,
                        monthsRemaining = calcMonths,
                        weeksRemaining = calcWeeks,
                        selectedTermId = calcSelectedTermId,
                        onFeeAmountChange = { viewModel.setCalcFeeAmount(it) },
                        onSavedAmountChange = { viewModel.setCalcSavedAmount(it) },
                        onMonthsChange = { viewModel.setCalcMonths(it) },
                        onWeeksChange = { viewModel.setCalcWeeks(it) },
                        onSelectTerm = { viewModel.selectTermForCalculator(it) },
                        onApplyGoal = { viewModel.applyCalculatorTargetAsGoal(it) }
                    )
                }

                NavDestination.SAVINGS -> {
                    SavingsTrackerScreen(
                        plan = feePlan,
                        termsUiList = dashboardState.termsList,
                        contributions = contributions,
                        onAddContributionClick = {
                            addSavingsTargetTermId = 1
                            showAddSavingsDialog = true
                        },
                        onDeleteContribution = { viewModel.deleteSavingsContribution(it) }
                    )
                }

                NavDestination.BUDGET -> {
                    BudgetPlannerScreen(
                        plan = feePlan,
                        budget = budget,
                        onSaveBudget = { income, rent, groceries, transport, other ->
                            viewModel.updateHouseholdBudget(income, rent, groceries, transport, other)
                        }
                    )
                }

                NavDestination.REPORTS -> {
                    ReportsInsightsScreen(
                        state = dashboardState,
                        budget = budget
                    )
                }

                NavDestination.PAYMENTS -> {
                    PaymentRecordsScreen(
                        plan = feePlan,
                        termsUiList = dashboardState.termsList,
                        payments = paymentRecords,
                        onRecordPayment = { termId, amount, method, ref, notes, date ->
                            viewModel.markTermPaid(termId, amount, method, ref, notes, date)
                        }
                    )
                }

                NavDestination.EXPORT -> {
                    ExportCsvScreen(
                        dateRange = exportDateRange,
                        includePlan = exportIncludePlan,
                        includeContribs = exportIncludeContribs,
                        includeBudget = exportIncludeBudget,
                        includePayments = exportIncludePayments,
                        csvPreview = csvPreview,
                        onSelectDateRange = { viewModel.setExportDateRange(it) },
                        onTogglePlan = { viewModel.toggleExportPlan(it) },
                        onToggleContribs = { viewModel.toggleExportContribs(it) },
                        onToggleBudget = { viewModel.toggleExportBudget(it) },
                        onTogglePayments = { viewModel.toggleExportPayments(it) },
                        onShareCsv = { ctx -> viewModel.shareCsvFile(ctx) },
                        onCopyCsv = { ctx -> viewModel.copyCsvToClipboard(ctx) }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showAddSavingsDialog) {
        AddContributionDialog(
            terms = feePlan.terms,
            initialTermId = addSavingsTargetTermId,
            onDismiss = { showAddSavingsDialog = false },
            onConfirm = { termId, amount, note, date ->
                viewModel.addSavingsContribution(termId, amount, note, date)
                showAddSavingsDialog = false
            }
        )
    }

    if (showEditPlanDialog) {
        EditPlanDialog(
            currentPlan = feePlan,
            onDismiss = { showEditPlanDialog = false },
            onConfirm = { parent, child, school, year, goal ->
                viewModel.updatePlanDetails(parent, child, school, year, goal)
                showEditPlanDialog = false
            }
        )
    }

    if (showRemindersDialog) {
        RemindersCenterDialog(
            reminders = reminders,
            onDismissRequest = { showRemindersDialog = false },
            onDismissReminder = { viewModel.dismissReminder(it) },
            onRestoreReminders = { viewModel.restoreReminders() }
        )
    }
}
