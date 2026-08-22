package com.adprmi.healthLogs.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adprmi.healthLogs.model.HeightUnit
import com.adprmi.healthLogs.model.WeightUnit
import com.adprmi.healthLogs.ui.theme.MyApplicationTheme
import com.adprmi.healthLogs.ui.theme.PrimaryFixed
import com.adprmi.healthLogs.ui.theme.ThemePreviews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeProfileScreen(
    onContinue: (String, Double, Double, WeightUnit, HeightUnit) -> Unit,
    onSkip: () -> Unit
) {
    var gender by remember { mutableStateOf("male") }
    var heightUnit by remember { mutableStateOf(HeightUnit.CM) }
    var weightUnit by remember { mutableStateOf(WeightUnit.KG) }
    
    var heightCm by remember { mutableStateOf("") }
    var heightFt by remember { mutableStateOf("") }
    var heightIn by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    val canContinue = weight.toDoubleOrNull() != null && (
        (heightUnit == HeightUnit.CM && heightCm.toDoubleOrNull() != null) ||
        (heightUnit == HeightUnit.FT_IN && heightFt.toDoubleOrNull() != null && heightIn.toDoubleOrNull() != null)
    )

    MyApplicationTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .safeDrawingPadding()
        ) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text(
                    text = "SKIP",
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 1.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to\n Health Logs",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Profile Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Gender
                        Column {
                            Text("Gender", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("male", "female", "other").forEach { g ->
                                    FilterChip(
                                        selected = gender == g,
                                        onClick = { gender = g },
                                        label = { Text(g.replaceFirstChar { it.uppercase() }) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                            containerColor = Color.Transparent,
                                            labelColor = Color.White.copy(alpha = 0.7f)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = Color.White.copy(alpha = 0.3f),
                                            selectedBorderColor = Color.Transparent,
                                            enabled = true,
                                            selected = gender == g
                                        )
                                    )
                                }
                            }
                        }

                        // Weight
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Weight", color = Color.White, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                                Row(modifier = Modifier.background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))) {
                                    WeightUnit.entries.forEach { unit ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (weightUnit == unit) MaterialTheme.colorScheme.secondary else Color.Transparent)
                                                .clickable { weightUnit = unit }
                                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(unit.displayName, color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = weight,
                                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) weight = it },
                                placeholder = { Text("0.0", color = Color.White.copy(alpha = 0.3f)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    cursorColor = Color.White
                                )
                            )
                        }

                        // Height
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Height", color = Color.White, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                                Row(modifier = Modifier.background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))) {
                                    HeightUnit.entries.forEach { unit ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (heightUnit == unit) MaterialTheme.colorScheme.secondary else Color.Transparent)
                                                .clickable { heightUnit = unit }
                                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(unit.displayName, color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            if (heightUnit == HeightUnit.CM) {
                                OutlinedTextField(
                                    value = heightCm,
                                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) heightCm = it },
                                    placeholder = { Text("cm", color = Color.White.copy(alpha = 0.3f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        cursorColor = Color.White
                                    )
                                )
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = heightFt,
                                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) heightFt = it },
                                        placeholder = { Text("ft", color = Color.White.copy(alpha = 0.3f)) },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color.White,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            cursorColor = Color.White
                                        )
                                    )
                                    OutlinedTextField(
                                        value = heightIn,
                                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) heightIn = it },
                                        placeholder = { Text("in", color = Color.White.copy(alpha = 0.3f)) },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color.White,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            cursorColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        val finalWeightKg = if (weightUnit == WeightUnit.KG) weight.toDouble() else weight.toDouble() * 0.453592
                        val finalHeightCm = if (heightUnit == HeightUnit.CM) {
                            heightCm.toDouble()
                        } else {
                            (heightFt.toDouble() * 30.48) + (heightIn.toDouble() * 2.54)
                        }
                        onContinue(gender, finalWeightKg, finalHeightCm, weightUnit, heightUnit)
                    },
                    enabled = canContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        disabledContainerColor = Color.White.copy(alpha = 0.1f),
                        disabledContentColor = Color.White.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = "CONTINUE",
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 2.sp
                    )
                }
            }

    }
}
}


@ThemePreviews
@Composable
fun WelcomeProfileScreenPreview() {
    MyApplicationTheme {
        WelcomeProfileScreen(
            onContinue = { _, _, _, _, _ -> },
            onSkip = {}
        )
    }
}
