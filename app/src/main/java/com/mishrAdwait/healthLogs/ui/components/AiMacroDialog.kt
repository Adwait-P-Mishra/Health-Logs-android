package com.mishrAdwait.healthLogs.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mishrAdwait.healthLogs.data.MealEntity
import com.mishrAdwait.healthLogs.model.ChatbotOption
import com.mishrAdwait.healthLogs.ui.theme.MyApplicationTheme
import com.mishrAdwait.healthLogs.ui.theme.ThemePreviews
import org.json.JSONArray

enum class AiMacroDialogState {
    CLOSED,
    OPTIONS,
    INPUT_RESPONSE
}

@Composable
fun AiMacroOptionsDialog(
    onDismissRequest: () -> Unit,
    onAskAiClicked: () -> Unit,
    onInputMacrosClicked: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("AI Macro Assistant", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                IconButton(onClick = onDismissRequest, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Estimate calories and macronutrients for your meals using your preferred AI chatbot.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onAskAiClicked,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ask AI for Macros")
                }
                OutlinedButton(
                    onClick = onInputMacrosClicked,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Input AI Macros")
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun AiMacroInputDialog(
    onDismissRequest: () -> Unit,
    onApplyMacros: (String) -> String?
) {
    var responseText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Input AI Macros", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                IconButton(onClick = onDismissRequest, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Paste the JSON response copied from your chatbot below:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = responseText,
                    onValueChange = {
                        responseText = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    placeholder = { Text("[{\"mealName\": \"...\", \"calories\": ...}]") },
                    maxLines = 8,
                    shape = RoundedCornerShape(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData = clipboard.primaryClip
                            if (clipData != null && clipData.itemCount > 0) {
                                val pasted = clipData.getItemAt(0).text?.toString() ?: ""
                                if (pasted.isNotEmpty()) {
                                    responseText = pasted
                                    errorMessage = null
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Paste Clipboard")
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val err = onApplyMacros(responseText)
                    if (err != null) {
                        errorMessage = err
                    } else {
                        onDismissRequest()
                    }
                },
                enabled = responseText.isNotBlank()
            ) {
                Text("Apply Macros")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

fun launchAskAiPrompt(
    context: Context,
    todayMeals: List<MealEntity>,
    selectedChatbot: ChatbotOption
): Boolean {
    val mealsNeedingMacros = todayMeals.filter {
        it.calories == 0 || it.protein == null || it.carbs == null || it.fat == null
    }.ifEmpty { todayMeals }

    if (mealsNeedingMacros.isEmpty()) {
        Toast.makeText(context, "No meals found to estimate.", Toast.LENGTH_SHORT).show()
        return false
    }

    val mealListString = mealsNeedingMacros.joinToString("\n") { "- ${it.mealName}" }
    val promptText = """
Calculate estimated calories and macronutrients for these meals. Respond ONLY with a valid JSON array matching this exact format, with no markdown code blocks or extra text outside the array:
[
  {"mealName": "Meal Name", "calories": 350, "protein": 12, "carbs": 65, "fat": 5}
]

Meals to estimate:
$mealListString
    """.trimIndent()

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("AI Macro Prompt", promptText)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Prompt copied to clipboard!", Toast.LENGTH_SHORT).show()

    try {
        when (selectedChatbot) {
            ChatbotOption.GENERIC_SHARE -> {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, promptText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Send prompt to AI Chatbot"))
            }
            else -> {
                val appPackage = selectedChatbot.appPackage
                var launched = false
                if (!appPackage.isNullOrEmpty()) {
                    val appIntent = context.packageManager.getLaunchIntentForPackage(appPackage)
                    if (appIntent != null) {
                        context.startActivity(appIntent)
                        launched = true
                    }
                }
                if (!launched && selectedChatbot.webUrl.isNotEmpty()) {
                    val encodedPrompt = Uri.encode(promptText)
                    val url = if (selectedChatbot == ChatbotOption.CHATGPT) {
                        "https://chatgpt.com/?q=$encodedPrompt"
                    } else if (selectedChatbot == ChatbotOption.PERPLEXITY) {
                        "https://www.perplexity.ai/search?q=$encodedPrompt"
                    } else {
                        selectedChatbot.webUrl
                    }
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(webIntent)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Opening chatbot failed. Prompt is in clipboard.", Toast.LENGTH_SHORT).show()
    }
    return true
}

fun parseAndApplyAiJson(
    jsonText: String,
    todayMeals: List<MealEntity>,
    onUpdateMeal: (MealEntity) -> Unit
): String? {
    try {
        var cleanJson = jsonText.trim()
        if (cleanJson.contains("```")) {
            cleanJson = cleanJson.replace("```json", "").replace("```", "").trim()
        }

        // Unescape escaped brackets like \[ and \] from markdown text copies
        cleanJson = cleanJson.replace("\\[", "[").replace("\\]", "]")

        val startIdx = cleanJson.indexOf('[')
        val endIdx = cleanJson.lastIndexOf(']')
        if (startIdx == -1 || endIdx == -1 || endIdx < startIdx) {
            return "Invalid format. Expected a JSON array starting with '[' and ending with ']'."
        }
        cleanJson = cleanJson.substring(startIdx, endIdx + 1)

        val jsonArray = JSONArray(cleanJson)
        if (jsonArray.length() == 0) {
            return "JSON array is empty."
        }

        var updateCount = 0

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val name = obj.optString("mealName", obj.optString("name", obj.optString("meal", "")))
            val calories = obj.optInt("calories", obj.optInt("kcal", 0))
            val protein = if (obj.has("protein")) obj.optDouble("protein", 0.0) else null
            val carbs = if (obj.has("carbs")) obj.optDouble("carbs", 0.0) else null
            val fat = if (obj.has("fat")) obj.optDouble("fat", 0.0) else null

            val targetMeal = todayMeals.find {
                it.mealName.trim().equals(name.trim(), ignoreCase = true)
            } ?: todayMeals.getOrNull(i)

            if (targetMeal != null) {
                val updated = targetMeal.copy(
                    calories = if (calories > 0) calories else targetMeal.calories,
                    protein = protein ?: targetMeal.protein,
                    carbs = carbs ?: targetMeal.carbs,
                    fat = fat ?: targetMeal.fat
                )
                onUpdateMeal(updated)
                updateCount++
            }
        }

        if (updateCount == 0) {
            return "No matching meals found to update."
        }

        return null
    } catch (e: Exception) {
        e.printStackTrace()
        return "Failed to parse JSON: ${e.localizedMessage ?: "Syntax error"}"
    }
}

@ThemePreviews
@Composable
fun AiMacroOptionsDialogPreview() {
    MyApplicationTheme {
        AiMacroOptionsDialog(
            onDismissRequest = {},
            onAskAiClicked = {},
            onInputMacrosClicked = {}
        )
    }
}

@ThemePreviews
@Composable
fun AiMacroInputDialogPreview() {
    MyApplicationTheme {
        AiMacroInputDialog(
            onDismissRequest = {},
            onApplyMacros = { null }
        )
    }
}
