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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavingsContribution
import com.example.data.model.SchoolFeePlan
import com.example.ui.components.ProgressBarWithLabel
import com.example.ui.components.RupeeText
import com.example.ui.theme.CardSurface
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenProgress
import com.example.ui.theme.GreenProgressDark
import com.example.ui.theme.GreenSoftBg
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy50
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.OrangeDeadlineDark
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RedOverdue
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TermUiModel

@Composable
fun SavingsTrackerScreen(
    plan: SchoolFeePlan,
    termsUiList: List<TermUiModel>,
    contributions: List<SavingsContribution>,
    onAddContributionClick: () -> Unit,
    onDeleteContribution: (id: String) -> Unit
) {
    var selectedFilterTermId by remember { mutableIntStateOf(0) } // 0 = All

    val filteredContributions = if (selectedFilterTermId == 0) {
        contributions
    } else {
        contributions.filter { it.termId == selectedFilterTermId }
    }

    val totalSaved = contributions.sumOf { it.amount }
    val anyTermCompleted = termsUiList.any { it.isTargetMet }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("savings_tracker_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
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
                            Text("Total Savings Accumulated", color = Navy100, fontSize = 12.sp)
                            RupeeText(amount = totalSaved, fontSize = 26.sp, color = PureWhite)
                        }
                        Button(
                            onClick = onAddContributionClick,
                            colors = ButtonDefaults.buttonColors(containerColor = GreenProgress),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("tracker_add_contribution_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Savings", color = PureWhite, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Celebration Banner if any term reached 100%
        if (anyTermCompleted) {
            item {
                Surface(
                    color = GreenSoftBg,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GreenProgress.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = GreenLight,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GreenProgressDark, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "🎉 School Fee Target Reached!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = GreenProgressDark
                            )
                            val completedNames = termsUiList.filter { it.isTargetMet }.joinToString(", ") { it.term.name }
                            Text(
                                text = "Congratulations! You have fully funded $completedNames. No last-minute stress!",
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Term Progress Bars Summary
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Term-by-Term Savings Progress", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Navy900)
                    Spacer(modifier = Modifier.height(12.dp))
                    termsUiList.forEachIndexed { index, termModel ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(termModel.term.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "₹${termModel.savedAmount.toInt()} / ₹${termModel.term.feeAmount.toInt()}",
                                    fontSize = 12.sp,
                                    color = if (termModel.isTargetMet) GreenProgressDark else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            ProgressBarWithLabel(
                                progress = termModel.progressFraction,
                                label = if (termModel.isTargetMet) "Target Reached (100%)" else "Remaining: ₹${termModel.remainingAmount.toInt()}",
                                color = if (termModel.isTargetMet) GreenProgressDark else GreenProgress
                            )
                        }
                        if (index < termsUiList.size - 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        // Contributions History Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Savings Contribution History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Navy900
                )
                Text(
                    text = "${filteredContributions.size} entries",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedFilterTermId == 0,
                    onClick = { selectedFilterTermId = 0 },
                    label = { Text("All Terms", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Navy900,
                        selectedLabelColor = PureWhite
                    )
                )
                plan.terms.forEach { term ->
                    FilterChip(
                        selected = selectedFilterTermId == term.id,
                        onClick = { selectedFilterTermId = term.id },
                        label = { Text(term.name, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Navy900,
                            selectedLabelColor = PureWhite
                        )
                    )
                }
            }
        }

        if (filteredContributions.isEmpty()) {
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
                        Icon(Icons.Default.Savings, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Savings Recorded Yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Tap 'Add Savings' above to start tracking your school fee fund.", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        } else {
            items(filteredContributions, key = { it.id }) { contrib ->
                val term = plan.terms.find { it.id == contrib.termId }
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = GreenLight,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Savings, contentDescription = null, tint = GreenProgressDark, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = term?.name ?: "Term ${contrib.termId}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Navy900
                                )
                                RupeeText(
                                    amount = contrib.amount,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenProgressDark
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = contrib.note,
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = contrib.date,
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        IconButton(
                            onClick = { onDeleteContribution(contrib.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}
