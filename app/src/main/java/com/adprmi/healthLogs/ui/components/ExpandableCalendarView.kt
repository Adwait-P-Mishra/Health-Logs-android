package com.adprmi.healthLogs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adprmi.healthLogs.util.DateUtils
import androidx.compose.ui.tooling.preview.Preview
import com.adprmi.healthLogs.ui.theme.MyApplicationTheme
import com.adprmi.healthLogs.ui.theme.ThemePreviews
import java.util.*

@Composable
fun ExpandableCalendarView(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }
    var displayedMonth by remember { mutableStateOf(Date()) }

    // Synchronize displayedMonth when selectedDate changes
    LaunchedEffect(selectedDate) {
        displayedMonth = selectedDate
    }

    val weekDays = remember(selectedDate) {
        DateUtils.getWeekDays(selectedDate)
    }

    val monthDays = remember(displayedMonth) {
        DateUtils.getMonthDays(displayedMonth)
    }

    val currentMonthCalendar = Calendar.getInstance().apply { time = displayedMonth }

    Card(
        modifier = modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (isExpanded) {
                            val cal = Calendar.getInstance().apply {
                                time = displayedMonth
                                add(Calendar.MONTH, -1)
                            }
                            displayedMonth = cal.time
                        } else {
                            val cal = Calendar.getInstance().apply {
                                time = selectedDate
                                add(Calendar.DAY_OF_YEAR, -7)
                            }
                            onDateSelected(cal.time)
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isExpanded = !isExpanded }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("expand_calendar_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DateUtils.formatDate(displayedMonth, "MMMM yyyy"),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onDateSelected(Date()) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Go to Today",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        if (isExpanded) {
                            val cal = Calendar.getInstance().apply {
                                time = displayedMonth
                                add(Calendar.MONTH, 1)
                            }
                            displayedMonth = cal.time
                        } else {
                            val cal = Calendar.getInstance().apply {
                                time = selectedDate
                                add(Calendar.DAY_OF_YEAR, 7)
                            }
                            onDateSelected(cal.time)
                        }
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expandable Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    if (!isExpanded) {
                        // Collapsed (Week Calendar View) - Matching Redesign
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            weekDays.forEach { date ->
                                val isSelected = DateUtils.isSameDay(date, selectedDate)
                                val isToday = DateUtils.isSameDay(date, Date())

                                val bgColor =
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                val textColor =
                                    if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                val labelColor =
                                    if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                                val dayShape = RoundedCornerShape(12.dp)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(2.dp)
                                        .clip(dayShape)
                                        .then(
                                            if (isToday && !isSelected) {
                                                Modifier.border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.primary,
                                                    dayShape
                                                )
                                            } else Modifier
                                        )
                                        .background(bgColor)
                                        .clickable { onDateSelected(date) }
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = DateUtils.formatDate(date, "EEE").uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = labelColor
                                    )
                                    Text(
                                        text = DateUtils.formatDate(date, "d"),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    } else {
                        // Expanded (Month Grid View with Navigation)
                        Column {
                            // 42-day Month Grid
                            Column(modifier = Modifier.fillMaxWidth()) {
                                val chunks = monthDays.chunked(7)
                                chunks.forEach { rowDays ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        rowDays.forEach { date ->
                                            val isSelected = DateUtils.isSameDay(date, selectedDate)
                                            val isToday = DateUtils.isSameDay(date, Date())
                                            val cal = Calendar.getInstance().apply { time = date }
                                            val isCurrentMonth =
                                                cal.get(Calendar.MONTH) == currentMonthCalendar.get(
                                                    Calendar.MONTH
                                                )

                                            val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

                                            val textColor = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                                                else -> MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.3f
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                                    .clip(CircleShape)
                                                    .then(
                                                        if (isToday && !isSelected) {
                                                            Modifier.border(
                                                                1.dp,
                                                                MaterialTheme.colorScheme.primary,
                                                                CircleShape
                                                            )
                                                        } else Modifier
                                                    )
                                                    .background(bgColor)
                                                    .clickable { onDateSelected(date) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = DateUtils.formatDate(date, "d"),
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = textColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun ExpandableCalendarViewPreview() {
    MyApplicationTheme {
        var selectedDate by remember { mutableStateOf(Date()) }
        ExpandableCalendarView(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )
    }
}

@ThemePreviews
@Composable
fun ExpandableCalendarViewExpandedPreview() {
    MyApplicationTheme {
        var selectedDate by remember { mutableStateOf(Date()) }
        ExpandableCalendarView(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            initialExpanded = true
        )
    }
}
