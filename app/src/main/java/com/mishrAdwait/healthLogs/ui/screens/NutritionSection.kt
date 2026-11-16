package com.mishrAdwait.healthLogs.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mishrAdwait.healthLogs.data.MealEntity
import com.mishrAdwait.healthLogs.model.ChatbotOption
import com.mishrAdwait.healthLogs.ui.components.AiMacroDialogState
import com.mishrAdwait.healthLogs.ui.components.AiMacroInputDialog
import com.mishrAdwait.healthLogs.ui.components.AiMacroOptionsDialog
import com.mishrAdwait.healthLogs.ui.components.launchAskAiPrompt
import com.mishrAdwait.healthLogs.ui.components.parseAndApplyAiJson
import com.mishrAdwait.healthLogs.ui.theme.MyApplicationTheme
import com.mishrAdwait.healthLogs.ui.theme.ThemePreviews
import java.text.NumberFormat
import java.util.Locale

@Composable
fun NutritionSection(
    totalCalories: Int,
    targetCalories: Int?,
    totalProtein: Double,
    totalCarbs: Double,
    totalFat: Double,
    todayMeals: List<MealEntity>,
    onLogMealClicked: () -> Unit,
    selectedChatbot: ChatbotOption = ChatbotOption.GENERIC_SHARE,
    onUpdateMeal: (MealEntity) -> Unit = {},
    onHeaderClick: () -> Unit
) {
    val context = LocalContext.current
    var aiDialogState by remember { mutableStateOf(AiMacroDialogState.CLOSED) }

    val isProteinIncomplete = todayMeals.any { it.protein == null }
    val isCarbsIncomplete = todayMeals.any { it.carbs == null }
    val isFatIncomplete = todayMeals.any { it.fat == null }
    val isIncomplete = isProteinIncomplete || isCarbsIncomplete || isFatIncomplete

    val calorieFormatter = NumberFormat.getNumberInstance(Locale.getDefault())
    val formattedTarget = targetCalories?.let { calorieFormatter.format(it) }

    if (aiDialogState == AiMacroDialogState.OPTIONS) {
        AiMacroOptionsDialog(
            onDismissRequest = { aiDialogState = AiMacroDialogState.CLOSED },
            onAskAiClicked = {
                val launched = launchAskAiPrompt(context, todayMeals, selectedChatbot)
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
                val error = parseAndApplyAiJson(jsonText, todayMeals, onUpdateMeal)
                if (error == null) {
                    Toast.makeText(context, "Meals updated successfully!", Toast.LENGTH_SHORT).show()
                }
                error
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onHeaderClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nutrition Tracker",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = { aiDialogState = AiMacroDialogState.OPTIONS },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SmartToy,
                                contentDescription = "AI Assistant",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Surface(
                        onClick = onLogMealClicked,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
                        color = Color.Transparent,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = "Log Meal",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(18.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            // Nutrition Tracker Content
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Target calorie tracker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "CALORIES",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = NumberFormat.getNumberInstance(Locale.getDefault()).format(totalCalories),
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (formattedTarget != null) {
                                    Text(
                                        text = " / $formattedTarget kcal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                                    )
                                } else {
                                    Text(
                                        text = " kcal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                                    )
                                }
                            }
                        }
                        if (targetCalories != null && targetCalories > 0) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.background, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { (totalCalories / targetCalories.toFloat()).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.secondary,
                                    strokeWidth = 4.dp,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                                Text(
                                    text = "${((totalCalories / targetCalories.toFloat()) * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    MacroProgressBar("Protein", totalProtein, 160.0, MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    MacroProgressBar("Carbs", totalCarbs, 250.0, MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    MacroProgressBar("Fat", totalFat, 70.0, MaterialTheme.colorScheme.tertiary)

                    if (isIncomplete && todayMeals.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "* Some entries are missing macro data.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (todayMeals.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fastfood,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No meals logged today",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun NutritionSectionPreview() {
    MyApplicationTheme {
        NutritionSection(
            totalCalories = 1200,
            targetCalories = 2400,
            totalProtein = 80.0,
            totalCarbs = 150.0,
            totalFat = 40.0,
            todayMeals = listOf(
                MealEntity(
                    mealName = "Lunch",
                    calories = 600,
                    protein = 30.0,
                    carbs = 70.0,
                    fat = 20.0,
                    date = System.currentTimeMillis()
                )
            ),
            onLogMealClicked = {},
            onHeaderClick = {}
        )
    }
}
