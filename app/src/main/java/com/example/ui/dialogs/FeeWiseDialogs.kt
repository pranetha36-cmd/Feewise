package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeReminder
import com.example.data.model.ReminderType
import com.example.data.model.SchoolFeePlan
import com.example.data.model.TermFee
import com.example.ui.theme.CardSurface
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenProgress
import com.example.ui.theme.GreenProgressDark
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy50
import com.example.ui.theme.Navy900
import com.example.ui.theme.OrangeDeadline
import com.example.ui.theme.OrangeLight
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RedLight
import com.example.ui.theme.RedOverdue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddContributionDialog(
    terms: List<TermFee>,
    initialTermId: Int = 1,
    onDismiss: () -> Unit,
    onConfirm: (termId: Int, amount: Double, note: String, date: String) -> Unit
) {
    var selectedTermId by remember { mutableIntStateOf(initialTermId) }
    var amountText by remember { mutableStateOf("1000") }
    var noteText by remember { mutableStateOf("Monthly savings contribution") }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    var dateText by remember { mutableStateOf(todayStr) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val quickAmounts = listOf(500, 1000, 2000, 3000, 5000)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = GreenProgress,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Savings Contribution", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Select School Term:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                terms.forEach { term ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTermId = term.id }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = selectedTermId == term.id,
                            onClick = { selectedTermId = term.id }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${term.name} (Fee: ₹${term.feeAmount.toInt()})",
                            fontSize = 14.sp,
                            fontWeight = if (selectedTermId == term.id) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Contribution Amount (₹):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorText = null
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("e.g. 2000") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contribution_amount_input")
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text("Quick Add:", fontSize = 11.sp, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickAmounts.forEach { amt ->
                        FilterChip(
                            selected = amountText == amt.toString(),
                            onClick = { amountText = amt.toString() },
                            label = { Text("+₹$amt", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenLight,
                                selectedLabelColor = GreenProgressDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Contribution Date (YYYY-MM-DD):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    singleLine = true,
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Note / Source of Funds:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    singleLine = true,
                    placeholder = { Text("e.g. Salary savings, Diwali bonus") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorText!!, color = RedOverdue, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorText = "Please enter a valid amount greater than 0"
                        return@Button
                    }
                    onConfirm(selectedTermId, amount, noteText, dateText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenProgress),
                modifier = Modifier.testTag("save_contribution_button")
            ) {
                Text("Save Contribution", color = PureWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditPlanDialog(
    currentPlan: SchoolFeePlan,
    onDismiss: () -> Unit,
    onConfirm: (parent: String, child: String, school: String, year: String, monthlyGoal: Double) -> Unit
) {
    var parentName by remember { mutableStateOf(currentPlan.parentName) }
    var childName by remember { mutableStateOf(currentPlan.childName) }
    var schoolName by remember { mutableStateOf(currentPlan.schoolName) }
    var academicYear by remember { mutableStateOf(currentPlan.academicYear) }
    var monthlyGoalText by remember { mutableStateOf(currentPlan.targetMonthlySavings.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit School Fee Plan", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = parentName,
                    onValueChange = { parentName = it },
                    label = { Text("Parent / Guardian Name(s)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = childName,
                    onValueChange = { childName = it },
                    label = { Text("Child Name & Grade") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = schoolName,
                    onValueChange = { schoolName = it },
                    label = { Text("School Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = academicYear,
                    onValueChange = { academicYear = it },
                    label = { Text("Academic Year") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = monthlyGoalText,
                    onValueChange = { monthlyGoalText = it },
                    label = { Text("Monthly Savings Target Goal") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val goal = monthlyGoalText.toDoubleOrNull() ?: 3000.0
                    onConfirm(parentName, childName, schoolName, academicYear, goal)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Navy900)
            ) {
                Text("Update Plan", color = PureWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditTermDialog(
    term: TermFee,
    onDismiss: () -> Unit,
    onConfirm: (fee: Double, dueDate: String, subtitle: String) -> Unit
) {
    var feeText by remember { mutableStateOf(term.feeAmount.toInt().toString()) }
    var dueDateText by remember { mutableStateOf(term.dueDate) }
    var subtitleText by remember { mutableStateOf(term.subtitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit ${term.name} Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = subtitleText,
                    onValueChange = { subtitleText = it },
                    label = { Text("Term Description / Subtitle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it },
                    label = { Text("Fee Amount") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dueDateText,
                    onValueChange = { dueDateText = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fee = feeText.toDoubleOrNull() ?: term.feeAmount
                    onConfirm(fee, dueDateText, subtitleText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Navy900)
            ) {
                Text("Save Changes", color = PureWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RecordPaymentDialog(
    term: TermFee,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, method: String, ref: String, notes: String, date: String) -> Unit
) {
    var amountText by remember { mutableStateOf(term.feeAmount.toInt().toString()) }
    var methodText by remember { mutableStateOf("UPI / NetBanking") }
    var refText by remember { mutableStateOf("TXN-${System.currentTimeMillis().toString().takeLast(6)}") }
    var notesText by remember { mutableStateOf("School counter fee receipt acknowledged") }
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    var dateText by remember { mutableStateOf(today) }

    val methods = listOf("UPI / NetBanking", "Cheque", "Credit / Debit Card", "Cash / Counter")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = Navy900)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mark ${term.name} as Paid", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    color = Navy50,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Recording this marks the term as paid in your personal FeeWise tracker for financial bookkeeping. (FeeWise does not process real money).",
                        fontSize = 11.sp,
                        color = Navy900,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Paid") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Payment Method:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                methods.forEach { m ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { methodText = m }
                    ) {
                        RadioButton(selected = methodText == m, onClick = { methodText = m })
                        Text(m, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = refText,
                    onValueChange = { refText = it },
                    label = { Text("Receipt / Transaction Reference") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Payment Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Bookkeeping Notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: term.feeAmount
                    onConfirm(amount, methodText, refText, notesText, dateText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenProgressDark),
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("Record Payment", color = PureWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RemindersCenterDialog(
    reminders: List<FeeReminder>,
    onDismissRequest: () -> Unit,
    onDismissReminder: (id: String) -> Unit,
    onRestoreReminders: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = OrangeDeadline)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fee Reminders", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    color = Navy50,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "FeeWise generates in-app reminders based on your fee deadlines and household budget. No external SMS/email spam.",
                        fontSize = 11.sp,
                        color = Navy900,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val activeReminders = reminders.filter { !it.isDismissed }
                if (activeReminders.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenProgress, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("All Caught Up!", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("No active reminders or pending alerts.", fontSize = 12.sp, color = TextSecondary)
                    }
                } else {
                    activeReminders.forEach { reminder ->
                        val (icon, color, bg) = when (reminder.type) {
                            ReminderType.DEADLINE_APPROACHING -> Triple(Icons.Default.Warning, OrangeDeadline, OrangeLight)
                            ReminderType.MONTHLY_TARGET -> Triple(Icons.Default.Savings, GreenProgress, GreenLight)
                            ReminderType.MISSED_SAVINGS -> Triple(Icons.Default.Warning, RedOverdue, RedLight)
                            ReminderType.UNDERFUNDED -> Triple(Icons.Default.Info, Navy900, Navy50)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    color = bg,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(reminder.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(reminder.message, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        color = bg,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(reminder.dateText, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                                IconButton(
                                    onClick = { onDismissReminder(reminder.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onRestoreReminders) {
                    Text("Restore Dismissed", fontSize = 12.sp)
                }
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900)
                ) {
                    Text("Done")
                }
            }
        }
    )
}
