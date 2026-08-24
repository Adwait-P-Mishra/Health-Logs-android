package com.adprmi.healthLogs.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import android.content.res.Configuration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adprmi.healthLogs.model.AI_PRESETS
import com.adprmi.healthLogs.model.AiProviderConfig
import com.adprmi.healthLogs.ui.theme.MyApplicationTheme
import com.adprmi.healthLogs.ui.theme.SurfaceContainerLowest
import com.adprmi.healthLogs.ui.theme.ThemePreviews
import com.adprmi.healthLogs.viewmodel.SettingsViewModel

enum class AiConfigStep {
    CONFIG, SUCCESS
}

@Composable
fun AiConfigScreen(
    viewModel: SettingsViewModel,
    isHosted: Boolean,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val isTesting by viewModel.isTesting.collectAsState()
    val testResult by viewModel.testResult.collectAsState()
    val aiProviderConfig by viewModel.aiProviderConfig.collectAsState()
    var currentStep by remember { mutableStateOf(AiConfigStep.CONFIG) }

    LaunchedEffect(testResult) {
        if (testResult?.isSuccess == true) {
            currentStep = AiConfigStep.SUCCESS
        }
    }

    when (currentStep) {
        AiConfigStep.CONFIG -> {
            AiConfigContent(
                isTesting = isTesting,
                testResult = testResult,
                aiProviderConfig = aiProviderConfig,
                isHosted = isHosted,
                onTestConnection = { viewModel.testConnection(it) },
                onSetAiProviderConfig = { viewModel.setAiProviderConfig(it) },
                onSetAiOnboardingShown = { viewModel.setAiOnboardingShown(it) },
                onBack = onBack
            )
        }
        AiConfigStep.SUCCESS -> {
            AiConnectionSuccessContent(
                onGoToDashboard = {
                    viewModel.resetTestResult()
                    onSuccess()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiConfigContent(
    isTesting: Boolean,
    testResult: Result<Boolean>?,
    aiProviderConfig: AiProviderConfig?,
    isHosted: Boolean,
    onTestConnection: (AiProviderConfig) -> Unit,
    onSetAiProviderConfig: (AiProviderConfig?) -> Unit,
    onSetAiOnboardingShown: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    // Determine initial values based on current config if it matches the hosted/local mode
    val initialPreset = if (isHosted) {
        aiProviderConfig?.let { config ->
            if (!config.isLocal) AI_PRESETS.find { it.baseUrl == config.baseUrl } else null
        } ?: AI_PRESETS.first()
    } else null

    var selectedPreset by remember { mutableStateOf(initialPreset) }
    var name by remember { mutableStateOf(if (aiProviderConfig?.isLocal == !isHosted) aiProviderConfig?.name ?: "" else selectedPreset?.name ?: "") }
    var baseUrl by remember { mutableStateOf(if (aiProviderConfig?.isLocal == !isHosted) aiProviderConfig?.baseUrl ?: "" else selectedPreset?.baseUrl ?: "") }
    var apiKey by remember { mutableStateOf(if (aiProviderConfig?.isLocal == !isHosted) aiProviderConfig?.apiKey ?: "" else selectedPreset?.apiKey ?: "") }
    var model by remember { mutableStateOf(if (aiProviderConfig?.isLocal == !isHosted) aiProviderConfig?.model ?: "" else selectedPreset?.model ?: "") }
    var apiKeyVisible by remember { mutableStateOf(false) }
    
    var showPresetMenu by remember { mutableStateOf(false) }

    LaunchedEffect(testResult) {
        if (testResult?.isSuccess == true) {
            onSetAiProviderConfig(
                AiProviderConfig(name, baseUrl, apiKey, model, !isHosted)
            )
            onSetAiOnboardingShown(true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Health Logs",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var showClearConfirm by remember { mutableStateOf(false) }
                    
                    IconButton(onClick = { showClearConfirm = true }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Clear Configuration", tint = MaterialTheme.colorScheme.error)
                    }
                    
                    if (showClearConfirm) {
                        AlertDialog(
                            onDismissRequest = { showClearConfirm = false },
                            title = { Text("Clear AI Configuration") },
                            text = { Text("Are you sure you want to clear your AI API configuration? This will delete your API key and endpoint settings.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        onSetAiProviderConfig(null)
                                        onSetAiOnboardingShown(false)
                                        showClearConfirm = false
                                        onBack()
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Clear")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showClearConfirm = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column {
                Text(
                    text = if (isHosted) "Hosted Provider Configuration" else "Local Endpoint Configuration",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isHosted) "Configure your connection to a hosted LLM provider." 
                           else "Configure your connection to a local LLM endpoint (e.g., LM Studio, Ollama).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isHosted) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Provider Preset",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box {
                                OutlinedButton(
                                    onClick = { showPresetMenu = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedPreset?.name ?: "Select Provider",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Icon(Icons.Outlined.ExpandMore, contentDescription = null)
                                    }
                                }
                                DropdownMenu(
                                    expanded = showPresetMenu,
                                    onDismissRequest = { showPresetMenu = false },
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                ) {
                                    AI_PRESETS.forEach { preset ->
                                        DropdownMenuItem(
                                            text = { Text(preset.name) },
                                            onClick = {
                                                selectedPreset = preset
                                                name = preset.name
                                                baseUrl = preset.baseUrl
                                                model = preset.model
                                                showPresetMenu = false
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Custom URL") },
                                        onClick = {
                                            selectedPreset = null
                                            name = "Custom"
                                            baseUrl = ""
                                            model = ""
                                            showPresetMenu = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "API Key ${if (!isHosted) "(Optional)" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            placeholder = { Text("Enter your API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                                    Icon(
                                        imageVector = if (apiKeyVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                        contentDescription = if (apiKeyVisible) "Hide API Key" else "Show API Key"
                                    )
                                }
                            }
                        )
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Your key is stored securely on your device using Android Keystore.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Base URL",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = baseUrl,
                            onValueChange = { baseUrl = it },
                            placeholder = { Text("https://...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Model Name",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = model,
                            onValueChange = { model = it },
                            placeholder = { Text("gemini-3.7-flash") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    if (testResult?.isFailure == true) {
                        Text(
                            text = "Error: ${testResult.exceptionOrNull()?.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = {
                            onTestConnection(
                                AiProviderConfig(name, baseUrl, apiKey, model, !isHosted)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        enabled = !isTesting && baseUrl.isNotEmpty() && model.isNotEmpty()
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onSecondary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Test & Save", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiConnectionSuccessContent(
    onGoToDashboard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SuccessAnimation()
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Connection Successful!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Vitality Core is now ready to estimate your calories using AI.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(280.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onGoToDashboard,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Go to Dashboard", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Icon(
            Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.alpha(0.4f)
        )
    }
}

@Composable
fun SuccessAnimation() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(120.dp)
    ) {
        PulseCircle(delay = 0)
        PulseCircle(delay = 500)
        PulseCircle(delay = 1000)
        
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = SurfaceContainerLowest,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PulseCircle(delay: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = delay, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )
    
    val scale = 0.8f + (progress * 0.7f)
    val alpha = 0.8f * (1f - progress)
    
    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale)
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
    )
}

@ThemePreviews
@Composable
fun AiConfigScreenHostedPreview() {
    MyApplicationTheme {
        AiConfigContent(
            isTesting = false,
            testResult = null,
            aiProviderConfig = null,
            isHosted = true,
            onTestConnection = {},
            onSetAiProviderConfig = {},
            onSetAiOnboardingShown = {},
            onBack = {}
        )
    }
}

@ThemePreviews
@Composable
fun AiConnectionSuccessPreview() {
    MyApplicationTheme {
        AiConnectionSuccessContent(onGoToDashboard = {})
    }
}

@ThemePreviews
@Composable
fun AiConfigScreenErrorPreview() {
    MyApplicationTheme {
        AiConfigContent(
            isTesting = false,
            testResult = Result.failure(Exception("Invalid API Key or Endpoint")),
            aiProviderConfig = null,
            isHosted = true,
            onTestConnection = {},
            onSetAiProviderConfig = {},
            onSetAiOnboardingShown = {},
            onBack = {}
        )
    }
}
