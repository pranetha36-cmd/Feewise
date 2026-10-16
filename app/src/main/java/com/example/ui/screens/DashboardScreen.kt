package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeFormatter
import com.example.data.model.TermStatus
import com.example.ui.components.CustomCard
import com.example.ui.components.NavDestination
import com.example.ui.components.ProgressBarWithLabel
import com.example.ui.components.RupeeText
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CardSurface
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenProgress
import com.example.ui.theme.GreenProgressDark
import com.example.ui.theme.GreenSoftBg
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy50
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.OrangeDeadline
import com.example.ui.theme.OrangeDeadlineDark
import com.example.ui.theme.OrangeLight
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RedLight
import com.example.ui.theme.RedOverdue
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.DashboardUiState
import com.example.ui.viewmodel.TermUiModel

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onNavigate: (NavDestination) -> Unit,
    onAddSavingsClick: () -> Unit,
    onEditPlanClick: () -> Unit,
    onOpenRemindersClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Profile Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Navy900),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Navy800,
                        shape = CircleShape,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.plan.childName,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${state.plan.schoolName} • ${state.plan.academicYear}",
                            color = Navy100,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "Parent: ${state.plan.parentName}",
                            color = Navy100.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                    Surface(
                        color = Navy800,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onEditPlanClick() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Plan",
                                tint = PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // 2. Alert strip if reminders or budget warnings
        if (state.unreadRemindersCount > 0) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.isBudgetAlertActive) RedLight else OrangeLight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenRemindersClick() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (state.isBudgetAlertActive) Icons.Default.Warning else Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (state.isBudgetAlertActive) RedOverdue else OrangeDeadlineDark,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (state.isBudgetAlertActive) "Budget Advisory Alert" else "${state.unreadRemindersCount} Active Reminders",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (state.isBudgetAlertActive) RedOverdue else OrangeDeadlineDark
                            )
                            Text(
                                text = if (state.isBudgetAlertActive) {
                                    "Monthly savings target exceeds your household surplus. Tap to review."
                                } else {
                                    "Upcoming fee deadline or savings milestones need attention. Tap to view."
                                },
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 3. Hero Annual Fee Overview Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL ANNUAL SCHOOL FEES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            RupeeText(
                                amount = state.totalAnnualFee,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Navy900
                            )
                        }
                        Surface(
                            color = GreenLight,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${(state.savingsProgressFraction * 100).toInt()}% SAVED",
                                color = GreenProgressDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    ProgressBarWithLabel(
                        progress = state.savingsProgressFraction,
                        label = "Overall 3-Term Savings Progress",
                        color = GreenProgress
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Amount Saved", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            RupeeText(amount = state.totalSaved, fontSize = 16.sp, color = GreenProgressDark)
                        }
                        Column {
                            Text(text = "Remaining to Save", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            RupeeText(amount = state.totalRemaining, fontSize = 16.sp, color = OrangeDeadlineDark)
                        }
                        Column {
                            Text(text = "Fees Paid", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            RupeeText(amount = state.totalPaid, fontSize = 16.sp, color = Navy900)
                        }
                    }
                }
            }
        }

        // 4. Next Upcoming Fee Banner Card
        item {
            val next = state.nextUpcomingTerm
            if (next != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy50),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (next.isDueSoon) OrangeLight else Navy100,
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = if (next.isDueSoon) OrangeDeadlineDark else Navy900,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Next Upcoming Fee",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${next.term.name} (${next.term.subtitle})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Navy900
                                    )
                                }
                            }
                            StatusBadge(status = next.status)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(text = "Due Date", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = next.term.dueDate,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (next.isDueSoon) OrangeDeadlineDark else TextPrimary
                                )
                                Text(
                                    text = if (next.daysUntilDue >= 0) "In ${next.daysUntilDue} days" else "Past Due",
                                    fontSize = 11.sp,
                                    color = if (next.isDueSoon) OrangeDeadlineDark else TextSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Term Fee", fontSize = 11.sp, color = TextSecondary)
                                RupeeText(amount = next.term.feeAmount, fontSize = 18.sp, color = Navy900)
                                Text(
                                    text = "Remaining: ₹${next.remainingAmount.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (next.remainingAmount > 0) OrangeDeadlineDark else GreenProgressDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        ProgressBarWithLabel(
                            progress = next.progressFraction,
                            label = "Saved: ₹${next.savedAmount.toInt()} / ₹${next.term.feeAmount.toInt()}",
                            color = GreenProgress
                        )
                    }
                }
            }
        }

        // 5. Monthly Savings Target & Budget Alignment Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = GreenProgress,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Monthly Savings Target",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Navy900
                            )
                        }
                        RupeeText(
                            amount = state.monthlySavingsTarget,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Household Monthly Surplus: ₹${state.budgetSurplus.toInt()}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        val isSafe = state.monthlySavingsTarget <= state.budgetSurplus
                        Surface(
                            color = if (isSafe) GreenLight else RedLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isSafe) "Safe & Manageable" else "Exceeds Surplus",
                                color = if (isSafe) GreenProgressDark else RedOverdue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Quick Action Buttons
        item {
            Text(
                text = "Quick Actions",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Navy900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddSavingsClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenProgress),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_savings_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Savings", fontSize = 12.sp, color = PureWhite)
                }

                FilledTonalButton(
                    onClick = { onNavigate(NavDestination.TERMS) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_fee_plan_button")
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Fee Plan", fontSize = 12.sp)
                }

                FilledTonalButton(
                    onClick = { onNavigate(NavDestination.BUDGET) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_budget_button")
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Budget", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onNavigate(NavDestination.CALCULATOR) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_calculator_button")
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp), tint = Navy900)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Calculator", fontSize = 12.sp, color = Navy900)
                }

                OutlinedButton(
                    onClick = { onNavigate(NavDestination.EXPORT) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_export_csv_button")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Navy900)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export CSV", fontSize = 12.sp, color = Navy900)
                }
            }
        }

        // 7. Three-Term Glance Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Three-Term Fee Overview",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Navy900
                )
                TextButton(onClick = { onNavigate(NavDestination.TERMS) }) {
                    Text("View All", fontSize = 12.sp, color = Navy900, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(state.termsList) { termModel ->
            DashboardTermCard(
                termModel = termModel,
                onAddSavings = onAddSavingsClick,
                onViewDetails = { onNavigate(NavDestination.TERMS) }
            )
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
fun DashboardTermCard(
    termModel: TermUiModel,
    onAddSavings: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = termModel.term.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Text(
                        text = termModel.term.subtitle,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                StatusBadge(status = termModel.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Due: ${termModel.term.dueDate}",
                    fontSize = 12.sp,
                    color = if (termModel.isDueSoon) OrangeDeadlineDark else TextSecondary,
                    fontWeight = if (termModel.isDueSoon) FontWeight.Bold else FontWeight.Normal
                )
                RupeeText(amount = termModel.term.feeAmount, fontSize = 14.sp, color = Navy900)
            }

            Spacer(modifier = Modifier.height(8.dp))

            ProgressBarWithLabel(
                progress = termModel.progressFraction,
                label = "Saved ₹${termModel.savedAmount.toInt()} (Remaining: ₹${termModel.remainingAmount.toInt()})",
                color = if (termModel.isTargetMet) GreenProgressDark else GreenProgress
            )

            if (termModel.isTargetMet && !termModel.term.isPaid) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenProgressDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "100% Target Reached! Ready for fee deadline.",
                        fontSize = 11.sp,
                        color = GreenProgressDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
