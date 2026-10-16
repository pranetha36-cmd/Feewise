package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeFormatter
import com.example.data.model.HouseholdBudget
import com.example.data.model.SchoolFeePlan
import com.example.ui.components.ProgressBarWithLabel
import com.example.ui.components.RupeeText
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
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RedOverdue
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.DashboardUiState
import com.example.ui.viewmodel.TermUiModel

@Composable
fun ReportsInsightsScreen(
    state: DashboardUiState,
    budget: HouseholdBudget
) {
    val totalFee = state.totalAnnualFee
    val totalSaved = state.totalSaved
    val totalPaid = state.totalPaid
    val totalRemaining = state.totalRemaining

    // Financial preparedness index
    val preparednessScore = if (totalFee > 0) {
        val savedWeight = (totalSaved / totalFee) * 50
        val paidWeight = (totalPaid / totalFee) * 30
        val budgetWeight = if (state.monthlySavingsTarget <= budget.monthlySurplus) 20 else 5
        (savedWeight + paidWeight + budgetWeight).toInt().coerceIn(10, 100)
    } else 100

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("reports_insights_screen"),
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
                            Icon(Icons.Default.Analytics, contentDescription = null, tint = PureWhite, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Financial Reports & Insights", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Annual trends, comparison charts, and preparedness", color = Navy100, fontSize = 12.sp)
                    }
                }
            }
        }

        // Readiness Score Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
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
                        color = if (preparednessScore >= 70) GreenLight else Navy50,
                        shape = CircleShape,
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$preparednessScore%",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = if (preparednessScore >= 70) GreenProgressDark else Navy900
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Financial Preparedness Index", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Navy900)
                        Text(
                            text = if (preparednessScore >= 70) {
                                "Excellent Planning! You are consistently setting aside funds ahead of fee deadlines."
                            } else {
                                "On Track. Keep allocating your monthly savings goal to avoid payment bottlenecks."
                            },
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Summary Metric Grid
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ANNUAL SCHOOL FEE SUMMARY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Planned", fontSize = 11.sp, color = TextSecondary)
                            RupeeText(amount = totalFee, fontSize = 16.sp, color = Navy900)
                        }
                        Column {
                            Text("Total Accumulated", fontSize = 11.sp, color = TextSecondary)
                            RupeeText(amount = totalSaved, fontSize = 16.sp, color = GreenProgressDark)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Remaining", fontSize = 11.sp, color = TextSecondary)
                            RupeeText(amount = totalRemaining, fontSize = 16.sp, color = OrangeDeadlineDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    ProgressBarWithLabel(
                        progress = state.savingsProgressFraction,
                        label = "Annual Target Progress (${(state.savingsProgressFraction * 100).toInt()}%)",
                        color = GreenProgress
                    )
                }
            }
        }

        // Visual Comparison Chart: Planned vs Saved vs Paid for all 3 Terms
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Term-Wise Comparison Chart", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Navy900)
                    Text("Planned Fee vs Actual Savings vs Paid", fontSize = 11.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ChartLegendItem(color = Navy900, label = "Planned Fee")
                        ChartLegendItem(color = GreenProgress, label = "Actual Saved")
                        ChartLegendItem(color = OrangeDeadline, label = "Paid")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Compose Canvas Chart
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val termsCount = state.termsList.size.coerceAtLeast(1)
                        val maxFee = (state.termsList.maxOfOrNull { it.term.feeAmount } ?: 12000.0).coerceAtLeast(1.0)

                        val groupWidth = canvasWidth / termsCount
                        val barWidth = 18.dp.toPx()
                        val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())

                        // Base line
                        drawLine(
                            color = Color(0xFFCBD5E1),
                            start = Offset(0f, canvasHeight - 20f),
                            end = Offset(canvasWidth, canvasHeight - 20f),
                            strokeWidth = 2f
                        )

                        state.termsList.forEachIndexed { i, termModel ->
                            val groupCenterX = i * groupWidth + (groupWidth / 2)

                            val feeHeight = ((termModel.term.feeAmount / maxFee) * (canvasHeight - 40f)).toFloat()
                            val savedHeight = ((termModel.savedAmount / maxFee) * (canvasHeight - 40f)).toFloat()
                            val paidAmount = if (termModel.term.isPaid) termModel.term.feeAmount else 0.0
                            val paidHeight = ((paidAmount / maxFee) * (canvasHeight - 40f)).toFloat()

                            val groundY = canvasHeight - 20f

                            // Bar 1: Planned Fee (Navy)
                            val b1X = groupCenterX - barWidth * 1.5f - 2.dp.toPx()
                            drawRoundRect(
                                color = Color(0xFF0F2C59),
                                topLeft = Offset(b1X, groundY - feeHeight),
                                size = Size(barWidth, feeHeight),
                                cornerRadius = cornerRadius
                            )

                            // Bar 2: Saved (Green)
                            val b2X = groupCenterX - barWidth * 0.5f
                            drawRoundRect(
                                color = Color(0xFF10B981),
                                topLeft = Offset(b2X, groundY - savedHeight),
                                size = Size(barWidth, savedHeight.coerceAtLeast(4f)),
                                cornerRadius = cornerRadius
                            )

                            // Bar 3: Paid (Orange)
                            val b3X = groupCenterX + barWidth * 0.5f + 2.dp.toPx()
                            drawRoundRect(
                                color = Color(0xFFF59E0B),
                                topLeft = Offset(b3X, groundY - paidHeight),
                                size = Size(barWidth, paidHeight.coerceAtLeast(4f)),
                                cornerRadius = cornerRadius
                            )
                        }
                    }

                    // Term Labels under Chart
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        state.termsList.forEach { termModel ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(termModel.term.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Navy900)
                                Text("₹${termModel.term.feeAmount.toInt()}", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Practical Financial Tips for Parents
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = OrangeDeadline, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Parent Financial Planning Recommendations", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Navy900)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "1. Set aside monthly contributions right after salary credit.\n2. Keep your school fee fund separated from daily household expense accounts.\n3. Complete savings at least 15 days before the official due date to avoid banking delays.\n4. Review and adjust your Household Budget regularly if incidental expenses rise.",
                        fontSize = 11.sp,
                        color = TextPrimary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
fun ChartLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = color, shape = RoundedCornerShape(2.dp), modifier = Modifier.size(10.dp)) {}
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}
