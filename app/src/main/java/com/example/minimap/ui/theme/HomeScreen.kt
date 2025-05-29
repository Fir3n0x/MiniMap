package com.example.minimap.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.minimap.TerminalButton
import com.example.minimap.autowide
import com.example.minimap.model.Screen
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Shadow
import kotlinx.coroutines.delay



@Composable
fun HomeScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.TopCenter
    ) {

        // Depth effect with gradient circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width/2, size.height/2)
            val maxRadius = size.maxDimension

            // Create concentric circles for depth effect
            for (i in 1..5) {
                val radius = maxRadius * (i/5f)
                drawCircle(
                    color = Color(0xFF111111).copy(alpha = 1f - (i * 0.15f)),
                    center = center,
                    radius = radius,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Title at the top
        TerminalTitle()

        // Main content with perfectly centered scan button
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ScanButton(navController)
        }

        // Bottom navigation
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 32.dp, end = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
//            IconButton("⚙") { navController.navigate("parameterViewer") }
//            IconButton("📁") { navController.navigate("fileViewer") }
            IconButton("ooo") { navController.navigate("parameterViewer") }
            IconButton("|||\\") { navController.navigate("fileViewer") }
        }
    }
}

@Composable
private fun ScanButton(navController: NavController) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(200.dp)
            .background(
                color = Color(0xFF1E2624).copy(alpha = 0.8f),
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = Color(0xFF00FF00).copy(alpha = 0.5f),
                shape = CircleShape
            )
            .clickable { navController.navigate(Screen.WifiScan.route) }
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "SCAN",
                color = Color.Green.copy(alpha = 0.9f),
                fontFamily = autowide,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.shadow(2.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "WiFi Networks",
                color = Color.White.copy(alpha = 0.7f),
                fontFamily = autowide,
                fontSize = 14.sp
            )
        }
    }
}


@Composable
private fun TerminalTitle() {
    var showCursor by remember { mutableStateOf(true) }
    var textToDisplay by remember { mutableStateOf("") }
    val fullText = "MINIMAP"
    val prefix = "$> "
    val textWidth = remember { mutableStateOf(0.dp) }

    // Animation d'écriture + curseur
    LaunchedEffect(Unit) {
        fullText.forEachIndexed { index, _ ->
            textToDisplay = fullText.take(index + 1)
            delay(150)
        }
        while (true) {
            delay(500)
            showCursor = !showCursor
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        // Mesure la largeur du texte complet
        Text(
            text = prefix + fullText,
            color = Color.Transparent,
            fontFamily = autowide,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.onGloballyPositioned {
                textWidth.value = it.size.width.dp
            }
        )

        // Conteneur avec largeur fixe
        Box(
            modifier = Modifier.width(textWidth.value)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = prefix + textToDisplay,
                    color = Color(0xFF00FF00),
                    fontFamily = autowide,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.shadow(4.dp)
                )

                AnimatedVisibility(
                    visible = showCursor,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 48.dp)
                            .background(Color(0xFF00FF00))
                    )
                }
            }
        }
    }
}


@Composable
private fun IconButton(icon: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(60.dp)
            .background(
                color = Color(0xFF232222),
                shape = CircleShape
            )
            .clickable { onClick() }
    ) {
        Text(icon, color = Color.Green, fontSize = 24.sp)
    }
}


