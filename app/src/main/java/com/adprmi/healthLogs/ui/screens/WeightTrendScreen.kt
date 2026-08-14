package com.adprmi.healthLogs.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.adprmi.healthLogs.data.WeightEntity
import com.adprmi.healthLogs.model.WeightUnit
import com.adprmi.healthLogs.util.DateUtils
import com.adprmi.healthLogs.viewmodel.WorkoutViewModel
import com.adprmi.healthLogs.ui.theme.MyApplicationTheme
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightTrendScreen(
    viewModel: WorkoutViewModel,
    onBack: () -> Unit
) {
    val weights by viewModel.allWeights.collectAsState()
    val weightUnit by viewModel.weightUnit.collectAsState()

    WeightTrendContent(
        weights = weights,
        weightUnit = weightUnit,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightTrendContent(
    weights: List<WeightEntity>,
    weightUnit: WeightUnit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weight Trends", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (weights.isEmpty()) {
                EmptyWeightState()
            } else {
                WeightChart(weights.reversed(), weightUnit)
                WeightList(weights, weightUnit)
            }
        }
    }
}

@Composable
fun WeightChart(weights: List<WeightEntity>, unit: WeightUnit) {
    if (weights.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
        val textMeasurer = rememberTextMeasurer()
        val textStyle = MaterialTheme.typography.labelSmall.copy(color = onSurfaceColor, fontSize = 10.sp)
        
        Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val labelHeight = 20.dp.toPx()
                val labelWidth = 32.dp.toPx()
                val graphHeight = size.height - labelHeight
                val graphWidth = size.width - labelWidth

                val maxWeightRaw = weights.maxOf { it.weightKg }
                val minWeightRaw = weights.minOf { it.weightKg }
                
                // Add buffer top and bottom
                val rawRange = (maxWeightRaw - minWeightRaw).coerceAtLeast(1.0)
                val chartMax = maxWeightRaw + (rawRange * 0.15)
                val chartMin = (minWeightRaw - (rawRange * 0.15)).coerceAtLeast(0.0)
                val chartRange = chartMax - chartMin

                // Labels for Y Axis (Weight)
                val yLabelsRaw = listOf(chartMax, (chartMax + chartMin) / 2, chartMin)
                yLabelsRaw.forEach { weightRaw ->
                    val displayWeight = if (unit == WeightUnit.LBS) {
                        (weightRaw * 2.20462 * 10.0).roundToInt() / 10.0
                    } else {
                        (weightRaw * 10.0).roundToInt() / 10.0
                    }
                    val label = "$displayWeight"
                    val labelResult = textMeasurer.measure(label, textStyle)
                    val y = graphHeight - ((weightRaw - chartMin) / chartRange * graphHeight).toFloat()
                    
                    // Draw grid line
                    drawLine(
                        color = onSurfaceColor.copy(alpha = 0.1f),
                        start = Offset(0f, y),
                        end = Offset(graphWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    
                    drawText(
                        textLayoutResult = labelResult,
                        topLeft = Offset(graphWidth + 4.dp.toPx(), y - labelResult.size.height / 2)
                    )
                }

                if (weights.size == 1) {
                    val y = graphHeight / 2
                    val x = graphWidth / 2
                    drawCircle(color = primaryColor, radius = 6.dp.toPx(), center = Offset(x, y))
                    
                    val dateLabel = DateUtils.formatDate(Date(weights[0].date), "MMM d")
                    val labelResult = textMeasurer.measure(dateLabel, textStyle)
                    drawText(
                        textLayoutResult = labelResult,
                        topLeft = Offset(x - labelResult.size.width / 2, graphHeight + 4.dp.toPx())
                    )
                } else {
                    val spacing = graphWidth / (weights.size - 1)
                    val points = weights.mapIndexed { index, weight ->
                        val x = index * spacing
                        val y = graphHeight - ((weight.weightKg - chartMin) / chartRange * graphHeight).toFloat()
                        Offset(x, y)
                    }

                    // Draw path
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    
                    // Draw points
                    points.forEach { point ->
                        drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = point)
                    }

                    // X-Axis Date Labels (Start, Middle, End)
                    val xLabelIndices = if (weights.size > 2) {
                        listOf(0, weights.size / 2, weights.size - 1)
                    } else {
                        listOf(0, weights.size - 1)
                    }

                    xLabelIndices.forEach { index ->
                        val dateLabel = DateUtils.formatDate(Date(weights[index].date), "MMM d")
                        val labelResult = textMeasurer.measure(dateLabel, textStyle)
                        val x = points[index].x
                        
                        // Align start label left, end label right, mid label center
                        val adjustedX = when (index) {
                            0 -> x
                            weights.size - 1 -> x - labelResult.size.width
                            else -> x - labelResult.size.width / 2
                        }.coerceIn(0f, graphWidth - labelResult.size.width)
                        
                        drawText(
                            textLayoutResult = labelResult,
                            topLeft = Offset(adjustedX, graphHeight + 4.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeightList(weights: List<WeightEntity>, unit: WeightUnit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(weights) { weight ->
            WeightListItem(weight, unit)
        }
    }
}

@Composable
fun WeightListItem(weight: WeightEntity, unit: WeightUnit) {
    val displayWeight = if (unit == WeightUnit.LBS) {
        (weight.weightKg * 2.20462 * 10.0).roundToInt() / 10.0
    } else {
        weight.weightKg
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = DateUtils.formatDate(Date(weight.date), "MMM d, yyyy"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$displayWeight ${unit.displayName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingFlat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun EmptyWeightState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(
                Icons.Default.MonitorWeight,
                null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "No weight logs recorded yet.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "You can log your weight from the dashboard or import data from settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
fun WeightTrendScreenPreview() {
    MyApplicationTheme {
        val mockWeights = listOf(
            WeightEntity(date = System.currentTimeMillis(), weightKg = 75.5),
            WeightEntity(date = System.currentTimeMillis() - 86400000 * 1, weightKg = 76.0),
            WeightEntity(date = System.currentTimeMillis() - 86400000 * 2, weightKg = 76.5),
            WeightEntity(date = System.currentTimeMillis() - 86400000 * 3, weightKg = 77.0),
            WeightEntity(date = System.currentTimeMillis() - 86400000 * 4, weightKg = 77.2)
        )
        WeightTrendContent(
            weights = mockWeights,
            weightUnit = WeightUnit.KG,
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeightChartPreview() {
    MyApplicationTheme {
        val mockWeights = listOf(
            WeightEntity(date = System.currentTimeMillis() - 86400000 * 4, weightKg = 77.2),
            WeightEntity(date = System.currentTimeMillis() - 86400000 * 3, weightKg = 77.0),
            WeightEntity(date = System.currentTimeMillis() - 86400000 * 2, weightKg = 76.5),
            WeightEntity(date = System.currentTimeMillis() - 86400000 * 1, weightKg = 76.0),
            WeightEntity(date = System.currentTimeMillis(), weightKg = 75.5)
        )
        WeightChart(weights = mockWeights, unit = WeightUnit.KG)
    }
}

@Preview(showBackground = true)
@Composable
fun WeightListPreview() {
    MyApplicationTheme {
        val mockWeights = listOf(
            WeightEntity(date = System.currentTimeMillis(), weightKg = 75.5),
            WeightEntity(date = System.currentTimeMillis() - 86400000 * 1, weightKg = 76.0)
        )
        WeightList(weights = mockWeights, unit = WeightUnit.KG)
    }
}

@Preview(showBackground = true)
@Composable
fun WeightListItemPreview() {
    MyApplicationTheme {
        WeightListItem(
            weight = WeightEntity(date = System.currentTimeMillis(), weightKg = 75.5),
            unit = WeightUnit.KG
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyWeightStatePreview() {
    MyApplicationTheme {
        EmptyWeightState()
    }
}
