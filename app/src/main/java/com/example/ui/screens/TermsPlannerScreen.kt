package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeFormatter
import com.example.data.model.SchoolFeePlan
import com.example.data.model.TermFee
import com.example.data.model.TermStatus
import com.example.ui.components.CustomCard
import com.example.ui.components.ProgressBarWithLabel
import com.example.ui.components.RupeeText
import com.example.ui.components.StatusBadge
import com.example.ui.dialogs.EditPlanDialog
import com.example.ui.dialogs.EditTermDialog
import com.example.ui.dialogs.RecordPaymentDialog
import com.example.ui.theme.CardSurface
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenProgress
import com.example.ui.theme.GreenProgressDark
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy50
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.OrangeDeadline
import com.example.ui.theme.OrangeDeadlineDark
import com.example.ui.theme.OrangeLight
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TermUiModel

@Composable
fun TermsPlannerScreen(
    plan: SchoolFeePlan,
    termsUiList: List<TermUiModel>,
    onUpdatePlanDetails: (parent: String, child: String, school: String, year: String, monthlyGoal: Double) -> Unit,
    onUpdateTermDetails: (termId: Int, fee: Double, dueDate: String, subtitle: String) -> Unit,
    onAddSavingsForTerm: (termId: Int) -> Unit,
    onMarkTermPaid: (termId: Int, amount: Double, method: String, ref: String, notes: String, date: String) -> Unit,
    onOpenCalculatorForTerm: (termId: Int) -> Unit
) {
    var showEditPlanDialog by remember { mutableStateOf(false) }
    var termToEdit by remember { mutableStateOf<TermFee?>(null) }
    var termToPay by remember { mutableStateOf<TermFee?>(null) }

    if (showEditPlanDialog) {
        EditPlanDialog(
            currentPlan = plan,
            onDismiss = { showEditPlanDialog = false },
            onConfirm = { parent, child, school, year, goal ->
                onUpdatePlanDetails(parent, child, school, year, goal)
                showEditPlanDialog = false
            }
        )
    }

    termToEdit?.let { term ->
        EditTermDialog(
            term = term,
            onDismiss = { termToEdit = null },
            onConfirm = { fee, dueDate, subtitle ->
                onUpdateTermDetails(term.id, fee, dueDate, subtitle)
                termToEdit = null
            }
        )
    }

    termToPay?.let { term ->
        RecordPaymentDialog(
            term = term,
            onDismiss = { termToPay = null },
            onConfirm = { amount, method, ref, notes, date ->
                onMarkTermPaid(term.id, amount, method, ref, notes, date)
                termToPay = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("terms_planner_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Plan Overview Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Navy900),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Three-Term Fee Schedule", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${plan.schoolName} (${plan.academicYear})", color = Navy100, fontSize = 12.sp)
                        }
                        IconButton(onClick = { showEditPlanDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Details", tint = PureWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Student", color = Navy100, fontSize = 11.sp)
                            Text(plan.childName, color = PureWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Column {
                            Text("Parent / Guardian", color = Navy100, fontSize = 11.sp)
                            Text(plan.parentName, color = PureWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Annual Fees", color = Navy100, fontSize = 11.sp)
                            RupeeText(amount = plan.totalAnnualFee, color = PureWhite, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Term Schedule & Status",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Navy900
            )
        }

        // Each of the 3 terms
        items(termsUiList) { termModel ->
            val term = termModel.term
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("term_card_${term.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Navy50,
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "T${term.id}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Navy900
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = term.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                                Text(
                                    text = term.subtitle,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(status = termModel.status)
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { termToEdit = term },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Term", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Fee Amount", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            RupeeText(amount = term.feeAmount, fontSize = 16.sp, color = Navy900)
                        }
                        Column {
                            Text("Amount Saved", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            RupeeText(amount = termModel.savedAmount, fontSize = 16.sp, color = GreenProgressDark)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Remaining", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            RupeeText(
                                amount = termModel.remainingAmount,
                                fontSize = 16.sp,
                                color = if (termModel.remainingAmount > 0) OrangeDeadlineDark else GreenProgressDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Due Date Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Navy50, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Navy900, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Due Date: ${term.dueDate}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Navy900)
                        }
                        Text(
                            text = if (term.isPaid) "Paid on ${term.paidDate ?: "-"}" else if (termModel.daysUntilDue >= 0) "${termModel.daysUntilDue} days remaining" else "Overdue",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (term.isPaid) GreenProgressDark else if (termModel.isDueSoon) OrangeDeadlineDark else TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress
                    ProgressBarWithLabel(
                        progress = termModel.progressFraction,
                        label = if (term.isPaid) "Term Paid & Completed" else "Savings Target Progress",
                        color = if (term.isPaid || termModel.isTargetMet) GreenProgressDark else GreenProgress
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!term.isPaid) {
                            Button(
                                onClick = { onAddSavingsForTerm(term.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenProgress),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_savings_term_${term.id}")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Savings", fontSize = 11.sp, color = PureWhite)
                            }

                            FilledTonalButton(
                                onClick = { termToPay = term },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mark_paid_term_${term.id}")
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mark Paid", fontSize = 11.sp)
                            }
                        } else {
                            Surface(
                                color = GreenLight,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenProgressDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Payment Completed: Ref ${term.paidReference ?: "N/A"}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenProgressDark
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { onOpenCalculatorForTerm(term.id) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(width = 46.dp, height = 38.dp)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = "Calculate", tint = Navy900, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}
