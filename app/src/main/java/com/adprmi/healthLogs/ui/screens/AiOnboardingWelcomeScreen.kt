package com.adprmi.healthLogs.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adprmi.healthLogs.ui.theme.MyApplicationTheme
import com.adprmi.healthLogs.ui.theme.PrimaryFixed
import com.adprmi.healthLogs.ui.theme.PrimaryFixedDim
import com.adprmi.healthLogs.ui.theme.SecondaryFixed
import com.adprmi.healthLogs.ui.theme.ThemePreviews

@Composable
fun AiOnboardingWelcomeScreen(
    onSetUpAi: () -> Unit,
    onSkip: () -> Unit
) {
    MyApplicationTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .safeDrawingPadding()
        ) {
            // Decorative Ambient Glow
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 100.dp)
                    .blur(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Section: Visual / Illustration
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AiIllustration()
                }

                // Middle Section: Typography
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "AI-Powered Precision",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Want to set up AI-powered calorie estimation? You'll need an API key from a provider like Google Gemini or OpenAI.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = PrimaryFixedDim,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Bottom Section: Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onSetUpAi,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "SET UP AI",
                            style = MaterialTheme.typography.labelMedium,
                            letterSpacing = 2.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryFixed
                        )
                    ) {
                        Text(
                            text = "SKIP FOR NOW",
                            style = MaterialTheme.typography.labelMedium,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val opacity by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "opacity"
    )

    Box(
        modifier = Modifier
            .size(192.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = opacity
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer abstract rings
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, PrimaryFixed.copy(alpha = 0.2f), CircleShape)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .border(1.dp, PrimaryFixed.copy(alpha = 0.4f), CircleShape)
        )

        // Inner Icon Container (Glassmorphism effect)
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
                .border(1.dp, PrimaryFixed.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Row {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp),
                    tint = SecondaryFixed
                )
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp),
                    tint = PrimaryFixed
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun AiOnboardingWelcomeScreenPreview() {
    MyApplicationTheme {
        AiOnboardingWelcomeScreen(
            onSetUpAi = {},
            onSkip = {}
        )
    }
}
