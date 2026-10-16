package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeFormatter
import com.example.data.model.TermStatus
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DividerColor
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenProgress
import com.example.ui.theme.GreenProgressDark
import com.example.ui.theme.GreenSoftBg
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy50
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.OrangeDeadline
import com.example.ui.theme.OrangeDeadlineDark
import com.example.ui.theme.OrangeLight
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RedLight
import com.example.ui.theme.RedOverdue
import com.example.ui.theme.RedOverdueDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class NavDestination(val route: String, val title: String, val icon: ImageVector) {
    DASHBOARD("dashboard", "Dashboard", Icons.Default.Dashboard),
    TERMS("terms", "Terms", Icons.Default.CalendarMonth),
    CALCULATOR("calculator", "Calculator", Icons.Default.Calculate),
    SAVINGS("savings", "Savings", Icons.Default.Savings),
    BUDGET("budget", "Budget", Icons.Default.AccountBalanceWallet),
    REPORTS("reports", "Insights", Icons.Default.Analytics),
    PAYMENTS("payments", "Payments", Icons.Default.ReceiptLong),
    EXPORT("export", "Export CSV", Icons.Default.FileDownload)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeWiseTopBar(
    currentScreen: NavDestination,
    unreadReminders: Int,
    onNotificationsClick: () -> Unit,
    onNavigate: (NavDestination) -> Unit,
    onResetData: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "FeeWise",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = GreenProgress,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "SMART PLANNER",
                            color = PureWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = when (currentScreen) {
                        NavDestination.DASHBOARD -> "School Fee & Savings Dashboard"
                        NavDestination.TERMS -> "Three-Term School Fee Planner"
                        NavDestination.CALCULATOR -> "Smart Savings Calculator"
                        NavDestination.SAVINGS -> "Savings Contributions & History"
                        NavDestination.BUDGET -> "Household Budget & Feasibility"
                        NavDestination.REPORTS -> "Financial Reports & Visual Insights"
                        NavDestination.PAYMENTS -> "Official Term Payment Records"
                        NavDestination.EXPORT -> "Export Financial Data (CSV)"
                    },
                    color = Navy100,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        },
        actions = {
            // Reminders / Notification Bell with Badge
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.testTag("notifications_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadReminders > 0) {
                            Badge(
                                containerColor = OrangeDeadline,
                                contentColor = PureWhite
                            ) {
                                Text("$unreadReminders")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Reminders and Alerts",
                        tint = PureWhite
                    )
                }
            }

            // Export CSV shortcut
            IconButton(
                onClick = { onNavigate(NavDestination.EXPORT) },
                modifier = Modifier.testTag("export_csv_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Export CSV Data",
                    tint = PureWhite
                )
            }

            // Overflow Menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("overflow_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = PureWhite
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Reports & Insights") },
                        leadingIcon = { Icon(Icons.Default.Analytics, contentDescription = null, tint = Navy900) },
                        onClick = {
                            menuExpanded = false
                            onNavigate(NavDestination.REPORTS)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Payment Records") },
                        leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Navy900) },
                        onClick = {
                            menuExpanded = false
                            onNavigate(NavDestination.PAYMENTS)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Export CSV Data") },
                        leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null, tint = Navy900) },
                        onClick = {
                            menuExpanded = false
                            onNavigate(NavDestination.EXPORT)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reset to Sample Plan") },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = OrangeDeadlineDark) },
                        onClick = {
                            menuExpanded = false
                            onResetData()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Navy900,
            titleContentColor = PureWhite,
            actionIconContentColor = PureWhite
        )
    )
}

@Composable
fun FeeWiseBottomBar(
    currentScreen: NavDestination,
    onNavigate: (NavDestination) -> Unit
) {
    val items = listOf(
        NavDestination.DASHBOARD,
        NavDestination.TERMS,
        NavDestination.CALCULATOR,
        NavDestination.SAVINGS,
        NavDestination.BUDGET
    )

    NavigationBar(
        containerColor = PureWhite,
        tonalElevation = 8.dp
    ) {
        items.forEach { dest ->
            val selected = currentScreen == dest
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(dest) },
                icon = {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.title
                    )
                },
                label = {
                    Text(
                        text = dest.title,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PureWhite,
                    selectedTextColor = Navy900,
                    indicatorColor = Navy900,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                modifier = Modifier.testTag("nav_item_${dest.route}")
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: TermStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        TermStatus.PAID -> Pair(GreenLight, GreenProgressDark)
        TermStatus.DUE_SOON -> Pair(OrangeLight, OrangeDeadlineDark)
        TermStatus.SAVING -> Pair(Navy50, Navy900)
        TermStatus.UPCOMING -> Pair(Navy50, TextSecondary)
        TermStatus.OVERDUE -> Pair(RedLight, RedOverdueDark)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = status.displayName.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun RupeeText(
    amount: Double,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    Text(
        text = FeeFormatter.formatRupee(amount),
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
    )
}

@Composable
fun CustomCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = CardSurface,
    elevation: Dp = 2.dp,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        modifier = modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
fun ProgressBarWithLabel(
    progress: Float,
    label: String = "Savings Progress",
    modifier: Modifier = Modifier,
    color: Color = GreenProgress
) {
    val percent = (progress * 100).toInt().coerceIn(0, 100)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$percent%",
                fontSize = 12.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = DividerColor
        )
    }
}
