package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.FeeWiseRepository
import com.example.ui.theme.CardSurface
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenProgress
import com.example.ui.theme.GreenProgressDark
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy50
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ExportCsvScreen(
    dateRange: FeeWiseRepository.DateRangeFilter,
    includePlan: Boolean,
    includeContribs: Boolean,
    includeBudget: Boolean,
    includePayments: Boolean,
    csvPreview: String,
    onSelectDateRange: (FeeWiseRepository.DateRangeFilter) -> Unit,
    onTogglePlan: (Boolean) -> Unit,
    onToggleContribs: (Boolean) -> Unit,
    onToggleBudget: (Boolean) -> Unit,
    onTogglePayments: (Boolean) -> Unit,
    onShareCsv: (Context) -> Unit,
    onCopyCsv: (Context) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("export_csv_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
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
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = PureWhite, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Export Financial Data", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Download fee plans, savings, and budget in CSV format", color = Navy100, fontSize = 12.sp)
                    }
                }
            }
        }

        // 1. Select Date Range
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. Select Export Date Range", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Navy900)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Filter transactions and savings entries", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    val options = listOf(
                        FeeWiseRepository.DateRangeFilter.ALL_TIME,
                        FeeWiseRepository.DateRangeFilter.CURRENT_YEAR,
                        FeeWiseRepository.DateRangeFilter.LAST_30_DAYS,
                        FeeWiseRepository.DateRangeFilter.LAST_90_DAYS
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        options.forEach { opt ->
                            FilterChip(
                                selected = dateRange == opt,
                                onClick = { onSelectDateRange(opt) },
                                label = { Text(opt.label, fontSize = 12.sp) },
                                leadingIcon = if (dateRange == opt) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Navy900,
                                    selectedLabelColor = PureWhite
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // 2. Select Sections to Include
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("2. Choose Financial Sections to Include", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Navy900)
                    Spacer(modifier = Modifier.height(10.dp))

                    ExportCheckboxRow(
                        title = "School Fee Plans & Terms",
                        subtitle = "Term 1, Term 2, Term 3 fee amounts, due dates, and status",
                        checked = includePlan,
                        onCheckedChange = onTogglePlan
                    )

                    ExportCheckboxRow(
                        title = "Savings Contributions History",
                        subtitle = "All date-stamped savings entries filtered by date range",
                        checked = includeContribs,
                        onCheckedChange = onToggleContribs
                    )

                    ExportCheckboxRow(
                        title = "Household Budget Details",
                        subtitle = "Monthly income, expenses breakdown, and net surplus",
                        checked = includeBudget,
                        onCheckedChange = onToggleBudget
                    )

                    ExportCheckboxRow(
                        title = "Payment Records Register",
                        subtitle = "Receipt references, payment dates, and payment methods",
                        checked = includePayments,
                        onCheckedChange = onTogglePayments
                    )
                }
            }
        }

        // Primary Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onShareCsv(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenProgress),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("download_share_csv_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download / Share CSV", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { onCopyCsv(context) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_csv_clipboard_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = Navy900)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy CSV", color = Navy900, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Live CSV Preview Box
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Navy50),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Live CSV Preview", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Navy900)
                        Text("${csvPreview.lines().size} lines", fontSize = 11.sp, color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = PureWhite,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = csvPreview,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Info Banner
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
                    Icon(Icons.Default.Info, contentDescription = null, tint = Navy900, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "The generated CSV file is formatted for seamless opening in Google Sheets, Microsoft Excel, or Apple Numbers. All amounts are exported in standard INR currency values.",
                        fontSize = 11.sp,
                        color = Navy900,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
fun ExportCheckboxRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = GreenProgress,
                uncheckedColor = TextMuted
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextSecondary)
        }
    }
}
