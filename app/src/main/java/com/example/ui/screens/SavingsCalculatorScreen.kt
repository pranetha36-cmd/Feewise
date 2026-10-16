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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeFormatter
import com.example.data.model.HouseholdBudget
import com.example.data.model.SchoolFeePlan
import com.example.ui.components.RupeeText
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DividerColor
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
import com.example.ui.theme.RedLight
import com.example.ui.theme.RedOverdue
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SavingsCalculatorScreen(
    plan: SchoolFeePlan,
    budget: HouseholdBudget,
    feeAmountStr: String,
    savedAmountStr: String,
    monthsRemaining: Int,
    weeksRemaining: Int,
    selectedTermId: Int?,
    onFeeAmountChange: (String) -> Unit,
    onSavedAmountChange: (String) -> Unit,
    onMonthsChange: (Int) -> Unit,
    onWeeksChange: (Int) -> Unit,
    onSelectTerm: (Int?) -> Unit,
    onApplyGoal: (Double) -> Unit
) {
    val feeAmount = feeAmountStr.toDoubleOrNull() ?: 12000.0
    val savedAmount = savedAmountStr.toDoubleOrNull() ?: 3000.0
    val remainingFee = (feeAmount - savedAmount).coerceAtLeast(0.0)

    val monthlyTarget = if (monthsRemaining > 0) remainingFee / monthsRemaining else remainingFee
    val weeklyTarget = if (weeksRemaining > 0) remainingFee / weeksRemaining else remainingFee

    val isAffordable = monthlyTarget <= budget.monthlySurplus
    val surplusDiff = (budget.monthlySurplus - monthlyTarget)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("savings_calculator_screen"),
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
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Smart Savings Calculator",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Automated Monthly & Weekly Fee Target Planner",
                            color = Navy100,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Quick Term Selector Chips
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Load from School Fee Schedule:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        plan.terms.forEach { term ->
                            FilterChip(
                                selected = selectedTermId == term.id,
                                onClick = { onSelectTerm(term.id) },
                                label = { Text(term.name, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Navy900,
                                    selectedLabelColor = PureWhite
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Calculator Inputs Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Upcoming Fee Amount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Navy900
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = feeAmountStr,
                        onValueChange = {
                            onFeeAmountChange(it)
                            onSelectTerm(null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calc_fee_amount_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "2. Amount Already Saved",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Navy900
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = savedAmountStr,
                        onValueChange = {
                            onSavedAmountChange(it)
                            onSelectTerm(null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calc_saved_amount_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Months Remaining Stepper & Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "3. Months Remaining",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Navy900
                            )
                            Text(
                                text = "Or ~$weeksRemaining weeks until deadline",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onMonthsChange((monthsRemaining - 1).coerceAtLeast(1)) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Navy50, CircleShape)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Navy900)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "$monthsRemaining mo",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            IconButton(
                                onClick = { onMonthsChange((monthsRemaining + 1).coerceAtMost(24)) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Navy50, CircleShape)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Navy900)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = monthsRemaining.toFloat(),
                        onValueChange = { onMonthsChange(it.toInt()) },
                        valueRange = 1f..12f,
                        steps = 10,
                        colors = SliderDefaults.colors(
                            thumbColor = Navy900,
                            activeTrackColor = Navy900,
                            inactiveTrackColor = DividerColor
                        )
                    )
                }
            }
        }

        // Calculation Results Card (Primary Highlight)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Navy50),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "CALCULATED SAVINGS TARGET",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(text = "Monthly Target", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            RupeeText(
                                amount = monthlyTarget,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Navy900
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Weekly Target", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            RupeeText(
                                amount = weeklyTarget,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenProgressDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Breakdown Pill
                    Surface(
                        color = PureWhite,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Remaining Fee to Fund:", fontSize = 12.sp, color = TextSecondary)
                                RupeeText(amount = remainingFee, fontSize = 12.sp, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Time Horizon:", fontSize = 12.sp, color = TextSecondary)
                                Text("$monthsRemaining months (~$weeksRemaining weeks)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Budget Affordability Check
                    Surface(
                        color = if (isAffordable) GreenLight else RedLight,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isAffordable) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isAffordable) GreenProgressDark else RedOverdue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isAffordable) "Budget Friendly & Realistic" else "Exceeds Available Budget",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isAffordable) GreenProgressDark else RedOverdue
                                )
                                Text(
                                    text = if (isAffordable) {
                                        "Leaves ₹${surplusDiff.toInt()} surplus buffer in your monthly household budget."
                                    } else {
                                        "Target is ₹${(-surplusDiff).toInt()} higher than your surplus (₹${budget.monthlySurplus.toInt()}). Adjust timeframe or expenses."
                                    },
                                    fontSize = 11.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onApplyGoal(monthlyTarget) },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("apply_calculator_target_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply ₹${monthlyTarget.toInt()}/mo as Plan Goal", color = PureWhite, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Formula & Guidance Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Navy900, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Smart Savings Formula", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Navy900)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Remaining Fee = Total Fee − Amount Already Saved\n• Monthly Savings Target = Remaining Fee ÷ Months Remaining\n• Weekly Savings Target = Remaining Fee ÷ Weeks Remaining\n\nExample: If upcoming fee is ₹12,000, saved is ₹3,000, and 4 months remain, your target is ₹2,250 / month.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}
