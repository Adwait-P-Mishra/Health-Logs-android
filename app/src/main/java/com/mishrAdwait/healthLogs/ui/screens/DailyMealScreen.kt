package com.mishrAdwait.healthLogs.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mishrAdwait.healthLogs.data.MealEntity
import com.mishrAdwait.healthLogs.model.ChatbotOption
import com.mishrAdwait.healthLogs.ui.components.AiMacroDialogState
import com.mishrAdwait.healthLogs.ui.components.AiMacroInputDialog
import com.mishrAdwait.healthLogs.ui.components.AiMacroOptionsDialog
import com.mishrAdwait.healthLogs.ui.components.DeleteConfirmationDialog
import com.mishrAdwait.healthLogs.ui.components.launchAskAiPrompt
import com.mishrAdwait.healthLogs.ui.components.parseAndApplyAiJson
import com.mishrAdwait.healthLogs.ui.theme.MyApplicationTheme
import com.mishrAdwait.healthLogs.ui.theme.ThemePreviews
import com.mishrAdwait.healthLogs.util.DateUtils
import com.mishrAdwait.healthLogs.viewmodel.MealViewModel
import java.util.*

@Composable
fun DailyMealScreen(
    dateMillis: Long,
    viewModel: MealViewModel,
    onBack: () -> Unit,
    onEditTriggered: (MealEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val allLogs by viewModel.allLogs.collectAsState()
    val selectedChatbot by viewModel.selectedChatbot.collectAsState()
    val targetDate = Date(dateMillis)

    val dailyLogs = remember(allLogs, dateMillis) {
        allLogs.filter { DateUtils.isSameDay(Date(it.date), targetDate) }
            .sortedByDescending { it.date }
    }

    DailyMealContent(
        date = targetDate,
        dailyLogs = dailyLogs,
        onBack = onBack,
        onEditTriggered = onEditTriggered,
        onDeleteLog = { viewModel.deleteLog(it) },
        selectedChatbot = selectedChatbot,
        onUpdateMeal = { viewModel.updateMeal(it) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyMealContent(
    date: Date,
    dailyLogs: List<MealEntity>,
    onBack: () -> Unit,
    onEditTriggered: (MealEntity) -> Unit,
    onDeleteLog: (String) -> Unit,
    selectedChatbot: ChatbotOption = ChatbotOption.GENERIC_SHARE,
    onUpdateMeal: (MealEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var aiDialogState by remember { mutableStateOf(AiMacroDialogState.CLOSED) }
    var showDeleteLogConfirm by remember { mutableStateOf<String?>(null) }

    if (aiDialogState == AiMacroDialogState.OPTIONS) {
        AiMacroOptionsDialog(
            onDismissRequest = { aiDialogState = AiMacroDialogState.CLOSED },
            onAskAiClicked = {
                val launched = launchAskAiPrompt(context, dailyLogs, selectedChatbot)
                if (launched) {
                    aiDialogState = AiMacroDialogState.INPUT_RESPONSE
                } else {
                    aiDialogState = AiMacroDialogState.CLOSED
                }
            },
            onInputMacrosClicked = {
                aiDialogState = AiMacroDialogState.INPUT_RESPONSE
            }
        )
    } else if (aiDialogState == AiMacroDialogState.INPUT_RESPONSE) {
        AiMacroInputDialog(
            onDismissRequest = { aiDialogState = AiMacroDialogState.CLOSED },
            onApplyMacros = { jsonText ->
                val error = parseAndApplyAiJson(jsonText, dailyLogs, onUpdateMeal)
                if (error == null) {
                    Toast.makeText(context, "Meals updated successfully!", Toast.LENGTH_SHORT).show()
                }
                error
            }
        )
    }

    if (showDeleteLogConfirm != null) {
        DeleteConfirmationDialog(
            title = "Delete Meal",
            message = "Are you sure you want to delete this meal log?",
            onConfirm = {
                onDeleteLog(showDeleteLogConfirm!!)
                showDeleteLogConfirm = null
            },
            onDismiss = { showDeleteLogConfirm = null }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Meals: ${DateUtils.formatDate(date, "MMM d, yyyy")}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        Surface(
                            onClick = { aiDialogState = AiMacroDialogState.OPTIONS },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                            color = Color.Transparent,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SmartToy,
                                    contentDescription = "AI Assistant",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )
            }
        },
        modifier = modifier.testTag("daily_meal_screen")
    ) { innerPadding ->
        if (dailyLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fastfood,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No meals logged for this day",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .consumeWindowInsets(innerPadding),
                contentPadding = PaddingValues(
                    start = 16.dp + innerPadding.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                    top = 16.dp + innerPadding.calculateTopPadding(),
                    end = 16.dp + innerPadding.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                    bottom = 16.dp + innerPadding.calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dailyLogs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fastfood,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(4.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = log.mealName,
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text("Healthy • Nutrition", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { onEditTriggered(log) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit log",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { showDeleteLogConfirm = log.id },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete log",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${log.calories}",
                                        style = MaterialTheme.typography.displaySmall,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "kcal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }

                            val hasMacros = log.protein != null || log.carbs != null || log.fat != null
                            if (hasMacros) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    log.protein?.let {
                                        MacroColumn(label = "PROTEIN", value = "${it.toInt()}g", color = MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                                    }
                                    log.carbs?.let {
                                        MacroColumn(label = "CARBS", value = "${it.toInt()}g", color = MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                                    }
                                    log.fat?.let {
                                        MacroColumn(label = "FAT", value = "${it.toInt()}g", color = MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                                    }
                                }
                            }

                            if (log.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(
                                    Modifier,
                                    DividerDefaults.Thickness,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Notes: ${log.notes}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroColumn(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(color, RoundedCornerShape(2.dp)))
    }
}

@ThemePreviews
@Composable
fun DailyMealPreview() {
    MyApplicationTheme {
        val sampleLogs = listOf(
            MealEntity(
                mealName = "Oatmeal with Berries",
                calories = 350,
                protein = 12.0,
                carbs = 65.0,
                fat = 5.0,
                date = System.currentTimeMillis(),
                notes = "Added some almond milk"
            ),
            MealEntity(
                mealName = "Grilled Chicken Salad",
                calories = 450,
                protein = 40.0,
                carbs = 10.0,
                fat = 25.0,
                date = System.currentTimeMillis()
            ),
            MealEntity(
                mealName = "Greek Yogurt",
                calories = 150,
                protein = 15.0,
                carbs = 8.0,
                fat = 4.0,
                date = System.currentTimeMillis()
            )
        )

        DailyMealContent(
            date = Date(),
            dailyLogs = sampleLogs,
            onBack = {},
            onEditTriggered = {},
            onDeleteLog = {}
        )
    }
}

@ThemePreviews
@Composable
fun DailyMealEmptyPreview() {
    MyApplicationTheme {
        DailyMealContent(
            date = Date(),
            dailyLogs = emptyList(),
            onBack = {},
            onEditTriggered = {},
            onDeleteLog = {}
        )
    }
}

@ThemePreviews
@Composable
fun MacroColumnPreview() {
    MyApplicationTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MacroColumn(label = "PROTEIN", value = "25g", color = MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            MacroColumn(label = "CARBS", value = "40g", color = MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            MacroColumn(label = "FAT", value = "10g", color = MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
        }
    }
}
