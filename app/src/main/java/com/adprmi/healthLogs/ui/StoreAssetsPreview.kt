package com.adprmi.healthLogs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adprmi.healthLogs.R
import com.adprmi.healthLogs.ui.theme.MyApplicationTheme

/**
 * Preview for the Play Store Icon (512x512)
 * Based on the new Nutrition + Training design
 */
@Preview(showBackground = true, widthDp = 512, heightDp = 512)
@Composable
fun PlayStoreIconPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
            Icon(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(400.dp),
            tint = Color.White
        )
    }
}

/**
 * Preview for the Play Store Feature Graphic (1024x500)
 */
@Preview(showBackground = true, widthDp = 1024, heightDp = 500)
@Composable
fun FeatureGraphicPreview() {
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Icon Area
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                   Icon(
                        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(200.dp),
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // App Name
                Text(
                    text = "HEALTH LOGS",
                    color = Color.White,
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 10.sp
                )
                
                Text(
                    text = "NUTRITION • TRAINING • PROGRESS",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 4.sp
                )
            }
        }
    }
}
