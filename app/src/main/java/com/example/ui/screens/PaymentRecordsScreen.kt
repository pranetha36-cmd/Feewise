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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.PaymentRecord
import com.example.data.model.SchoolFeePlan
import com.example.data.model.TermFee
import com.example.ui.components.RupeeText
import com.example.ui.dialogs.RecordPaymentDialog
import com.example.ui.theme.CardSurface
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenProgress
import com.example.ui.theme.GreenProgressDark
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy50
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.OrangeLight
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TermUiModel

@Composable
fun PaymentRecordsScreen(
    plan: SchoolFeePlan,
    termsUiList: List<TermUiModel>,
    payments: List<PaymentRecord>,
    onRecordPayment: (termId: Int, amount: Double, method: String, ref: String, notes: String, date: String) -> Unit
) {
    var termToRecord by remember { mutableStateOf<TermFee?>(null) }

    termToRecord?.let { term ->
        RecordPaymentDialog(
            term = term,
            onDismiss = { termToRecord = null },
            onConfirm = { amount, method, ref, notes, date ->
                onRecordPayment(term.id, amount, method, ref, notes, date)
                termToRecord = null
            }
        )
    }

    val totalPaid = payments.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("payment_records_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
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
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = PureWhite, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Term Payment Register", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Track official fee payment receipts and history", color = Navy100, fontSize = 12.sp)
                    }
                }
            }
        }

        // Disclaimer Banner
        item {
            Surface(
                color = Navy50,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Navy900, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FeeWise is a financial planner and record keeper. It does not process or transfer actual banking funds. Use this screen to record payment receipts and keep track of completed terms.",
                        fontSize = 11.sp,
                        color = Navy900,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Three Term Payment Status Checklist
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Academic Year Terms Checklist", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Navy900)
                    Spacer(modifier = Modifier.height(12.dp))

                    termsUiList.forEach { termModel ->
                        val term = termModel.term
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (term.isPaid) Icons.Default.CheckCircle else Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = if (term.isPaid) GreenProgressDark else TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(term.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Navy900)
                                    Text("Fee: ₹${term.feeAmount.toInt()} • Due ${term.dueDate}", fontSize = 11.sp, color = TextSecondary)
                                }
                            }

                            if (term.isPaid) {
                                Surface(
                                    color = GreenLight,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "PAID (${term.paidDate ?: "Completed"})",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenProgressDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { termToRecord = term },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenProgress),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Mark Paid", fontSize = 11.sp, color = PureWhite)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recorded Payments History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment History Log",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Navy900
                )
                Text(
                    text = "Total Paid: ₹${totalPaid.toInt()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenProgressDark
                )
            }
        }

        if (payments.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Payments Recorded Yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("When you pay a term at the school, tap 'Mark Paid' above to record the receipt.", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        } else {
            items(payments, key = { it.id }) { payment ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = GreenLight,
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenProgressDark, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(payment.termName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Navy900)
                                    Text(payment.paymentDate, fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            RupeeText(amount = payment.amount, fontSize = 16.sp, color = GreenProgressDark)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Navy50, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Method: ${payment.paymentMethod}", fontSize = 11.sp, color = TextPrimary)
                            Text("Ref: ${payment.referenceNo}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Navy900)
                        }

                        if (payment.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(payment.notes, fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}
