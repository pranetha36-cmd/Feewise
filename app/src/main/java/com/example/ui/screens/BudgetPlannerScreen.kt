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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
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
import com.example.ui.theme.RedSoftBg
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BudgetPlannerScreen(
    plan: SchoolFeePlan,
    budget: HouseholdBudget,
    onSaveBudget: (income: Double, rent: Double, groceries: Double, transport: Double, other: Double) -> Unit
) {
    var incomeStr by remember(budget.monthlyIncome) { mutableStateOf(budget.monthlyIncome.toInt().toString()) }
    var rentStr by remember(budget.rentAndBills) { mutableStateOf(budget.rentAndBills.toInt().toString()) }
    var groceriesStr by remember(budget.groceries) { mutableStateOf(budget.groceries.toInt().toString()) }
    var transportStr by remember(budget.transport) { mutableStateOf(budget.transport.toInt().toString()) }
    var otherStr by remember(budget.otherEssentials) { mutableStateOf(budget.otherEssentials.toInt().toString()) }

    val income = incomeStr.toDoubleOrNull() ?: 0.0
    val rent = rentStr.toDoubleOrNull() ?: 0.0
    val groceries = groceriesStr.toDoubleOrNull() ?: 0.0
    val transport = transportStr.toDoubleOrNull() ?: 0.0
    val other = otherStr.toDoubleOrNull() ?: 0.0

    val totalExpenses = rent + groceries + transport + other
    val monthlySurplus = (income - totalExpenses).coerceAtLeast(0.0)

    val plannedSavingsTarget = plan.targetMonthlySavings
    val isManageable = plannedSavingsTarget <= monthlySurplus
    val deficitOrSurplus = monthlySurplus - plannedSavingsTarget

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("budget_planner_screen"),
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
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = PureWhite, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Household Budget Planner", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Balance household expenses with school fee goals", color = Navy100, fontSize = 12.sp)
                    }
                }
            }
        }

        // Budget Summary & Surplus Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("MONTHLY FINANCIAL OVERVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Income", fontSize = 11.sp, color = TextSecondary)
                            RupeeText(amount = income, fontSize = 16.sp, color = Navy900)
                        }
                        Column {
                            Text("Total Expenses", fontSize = 11.sp, color = TextSecondary)
                            RupeeText(amount = totalExpenses, fontSize = 16.sp, color = RedOverdue)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Net Surplus", fontSize = 11.sp, color = TextSecondary)
                            RupeeText(amount = monthlySurplus, fontSize = 18.sp, color = GreenProgressDark)
                        }
                    }
                }
            }
        }

        // School Fee Target Feasibility Card (Crucial Prompt Requirement)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isManageable) GreenLight else RedSoftBg
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isManageable) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isManageable) GreenProgressDark else RedOverdue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isManageable) "Realistic & Manageable Plan" else "⚠️ Target Exceeds Available Surplus",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isManageable) GreenProgressDark else RedOverdue
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isManageable) {
                        Text(
                            text = "Your planned school fee target of ₹${plannedSavingsTarget.toInt()} is comfortably within your ₹${monthlySurplus.toInt()} monthly surplus. After setting aside school fees, you still retain ₹${deficitOrSurplus.toInt()} as an emergency household buffer.",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 17.sp
                        )
                    } else {
                        val shortfall = -deficitOrSurplus
                        Text(
                            text = "Your monthly school fee savings target of ₹${plannedSavingsTarget.toInt()} exceeds your surplus (₹${monthlySurplus.toInt()}) by ₹${shortfall.toInt()}.\n\nFeeWise Recommendation: Do NOT resort to high-interest borrowing or unrealistic targets. Instead, adjust non-essential categories below or start saving 2-3 months earlier to lower your required monthly installment.",
                            fontSize = 12.sp,
                            color = RedOverdue,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Monthly Income Input
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. Monthly Household Income", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Navy900)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Net salary, business income, or combined earnings", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = incomeStr,
                        onValueChange = { incomeStr = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_income_input")
                    )
                }
            }
        }

        // Monthly Essential Expenses
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("2. Monthly Household Expenses", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Navy900)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Enter major expense categories to calculate realistic surplus", fontSize = 11.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(14.dp))

                    BudgetExpenseRow(
                        title = "Rent & Household Bills",
                        subtitle = "House rent, electricity, water, internet",
                        icon = Icons.Default.Home,
                        value = rentStr,
                        onValueChange = { rentStr = it }
                    )

                    Divider(color = DividerColor, modifier = Modifier.padding(vertical = 10.dp))

                    BudgetExpenseRow(
                        title = "Groceries & Food",
                        subtitle = "Provisions, vegetables, dairy, cooking essentials",
                        icon = Icons.Default.ShoppingCart,
                        value = groceriesStr,
                        onValueChange = { groceriesStr = it }
                    )

                    Divider(color = DividerColor, modifier = Modifier.padding(vertical = 10.dp))

                    BudgetExpenseRow(
                        title = "Transport & Fuel",
                        subtitle = "School van, petrol/diesel, public transport",
                        icon = Icons.Default.DirectionsCar,
                        value = transportStr,
                        onValueChange = { transportStr = it }
                    )

                    Divider(color = DividerColor, modifier = Modifier.padding(vertical = 10.dp))

                    BudgetExpenseRow(
                        title = "Other Essential Expenses",
                        subtitle = "Medical, insurance, emergencies, utilities",
                        icon = Icons.Default.Receipt,
                        value = otherStr,
                        onValueChange = { otherStr = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onSaveBudget(income, rent, groceries, transport, other)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_budget_button")
                    ) {
                        Text("Save Household Budget", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
fun BudgetExpenseRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Navy900, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
        }
        Text(subtitle, fontSize = 11.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
