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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay



@Composable
fun HomeScreen(navController: NavController) {

    var showTitle by remember { mutableStateOf(false) }
    var showScanButton by remember { mutableStateOf(false) }
    var showBottomButtons by remember { mutableStateOf(false) }

    var showRobot by remember { mutableStateOf(false) }
    var robotWaving by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        // Delay before displaying title
        delay(100)
        showTitle = true

        // Delay before displaying SCAN button (after title)
        delay(400)
        showScanButton = true

        // Delay before displaying bottom buttons
        delay(400)
        showBottomButtons = true


        // Delay before showing robot (after other elements)
        delay(400)
        showRobot = true
        robotWaving = true
        delay(1000) // Wave duration
        robotWaving = false
        delay(500)
        showRobot = false
    }


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
        AnimatedVisibility(
            visible = showTitle,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut()
        ) {
            TerminalTitle()
        }

        // SCAN button with animation
        AnimatedVisibility(
            visible = showScanButton,
            enter = fadeIn() + expandVertically(),
            modifier = Modifier.fillMaxSize(),
            exit = fadeOut()
        ) {
            Box(contentAlignment = Alignment.Center) {
                ScanButton(navController)
            }
        }

        AnimatedVisibility(
            visible = showRobot,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            RobotAnimation(isWaving = robotWaving)
        }


        // Bottom button with animation
        AnimatedVisibility(
            visible = showBottomButtons,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            modifier = Modifier.align(Alignment.BottomCenter),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 32.dp, end = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton("ooo") { navController.navigate("parameterViewer") }
                IconButton("|||\\") { navController.navigate("fileViewer") }
            }
        }
    }
}

@Composable
private fun ScanButton(navController: NavController) {

    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(200.dp)
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            }
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

    // Written animation + cursor
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
        // Measure length full text
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

        // Container with fixed length
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
fun RobotAnimation(isWaving: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val waveRotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val antennaPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {

        // Antenna point (with pulsing animation)
        Box(
            modifier = Modifier
                .size(10.dp)
                .graphicsLayer {
                    scaleX = antennaPulse
                    scaleY = antennaPulse
                }
                .background(Color.Green, CircleShape)
                .offset(y = (-4).dp)
        )
        // Antenna wire (spiral)
        Canvas(modifier = Modifier.size(20.dp, 30.dp)) {
            drawPath(
                path = Path().apply {
                    moveTo(size.width / 2, 0f)
                    cubicTo(
                        size.width * 0.8f, size.height * 0.2f,
                        size.width * 0.2f, size.height * 0.4f,
                        size.width / 2, size.height * 0.6f
                    )
                    cubicTo(
                        size.width * 0.8f, size.height * 0.8f,
                        size.width * 0.2f, size.height,
                        size.width / 2, size.height
                    )
                },
                color = Color.Green,
                style = Stroke(width = 2.dp.toPx())
            )
        }


        // head
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF444444), CircleShape)
                .border(2.dp, Color.Green, CircleShape)
        ) {
            // eyes
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Green, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Green, CircleShape)
                )
            }

            // smile
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.Green,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.25f, size.height * 0.5f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.5f, size.height * 0.3f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // robot body
        Box(
            modifier = Modifier
                .size(60.dp, 80.dp)
                .background(Color(0xFF333333), RoundedCornerShape(8.dp))
                .border(2.dp, Color.Green, RoundedCornerShape(8.dp))
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // left arm
                Box(
                    modifier = Modifier
                        .size(12.dp, 40.dp)
                        .graphicsLayer {
                            rotationZ = if (isWaving) waveRotation else 0f
                            transformOrigin = TransformOrigin(0.5f, 0f)
                        }
                        .background(Color(0xFF444444))
                        .offset(y = 20.dp)
                )

                // right arm
                Box(
                    modifier = Modifier
                        .size(12.dp, 40.dp)
                        .graphicsLayer {
                            rotationZ = if (isWaving) -waveRotation else 0f
                            transformOrigin = TransformOrigin(0.5f, 0f)
                        }
                        .background(Color(0xFF444444))
                        .offset(y = 20.dp)
                )
            }
        }

        // Text "Hello there !"
        AnimatedVisibility(
            visible = isWaving,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut()
        ) {
            Text(
                text = "Hello there !",
                color = Color.Green,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
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


