package com.example.minimap.ui.theme

import android.os.SystemClock
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.minimap.autowide
import com.example.minimap.model.WifiNetworkInfo
import com.example.minimap.model.WifiSecurityLevel
import com.example.minimap.model.getColor
import com.example.minimap.model.getSecurityLevel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun WifiRadarDetection(
    navController: NavController,
    networks: List<WifiNetworkInfo>,
    isRunning: Boolean,
    onToggleRunning: () -> Unit,
    modifier: Modifier = Modifier
) {

    var expanded by remember { mutableStateOf(false) }


    val listHeight by animateDpAsState(
        targetValue = if (expanded) 200.dp else 40.dp,
        animationSpec = tween(durationMillis = 300)
    )


    var expandedSsid by remember { mutableStateOf<String?>(null) }


    var selectedSsid by remember { mutableStateOf<String?>(null) }


//    var isRunning by remember { mutableStateOf(true) }





    // infinite animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAnim"
    )


    // propagation effect for radar
    val radarPulse = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarWave"
    )


    val angles = networks.mapIndexed { i, net ->
        net.ssid to (i * (2 * Math.PI / networks.size)).toFloat()
    }.toMap()


    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }

    if (showExportDialog) {
        ExportDialog(
            networks = networks,
            onDismiss = { showExportDialog = false },
            onConfirm = { fileName ->
                exportNetworksToJson(context, networks, fileName)
            }
        )
    }







    Box(modifier = modifier.background(Color.Black)) {


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            // return
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clickable {
                        navController.navigate("home")
                    }
                    .padding(start = 16.dp) // space from border
            ) {
                Text(
                    text = "<",
                    color = Color.Green,
                    fontFamily = autowide,
                    fontSize = 35.sp
                )
            }

            // title
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp) // vertical
            ) {
                Text(
                    text = "WiFi Detection",
                    color = Color.Green,
                    fontFamily = autowide,
                    fontSize = 24.sp
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp)
            ) {
                ExportButton {
                    showExportDialog = true
                }
            }
        }


        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {




                    // Radar length
                    val rawWidth = size.width * 0.8f
                    val rawHeight = size.height * 0.5f


                    val gridStep = 50f
                    // Multiple of gridstep
                    val radarWidth = (rawWidth / gridStep).toInt() * gridStep
                    val radarHeight = (rawHeight / gridStep).toInt() * gridStep


                    val topLeft = Offset((size.width - radarWidth) / 2, (size.height - radarHeight) / 2)
                    val bottomRight = Offset(topLeft.x + radarWidth, topLeft.y + radarHeight)
                    val center = Offset(size.width / 2, size.height / 2)





                    // 1. Radar frame
                    drawRect(
                        color = Color.Black,
                        topLeft = topLeft,
                        size = androidx.compose.ui.geometry.Size(radarWidth, radarHeight)
                    )




                    var x = topLeft.x
                    while (x <= bottomRight.x) {
                        drawLine(
                            color = Color.Green.copy(alpha = 0.3f),
                            start = Offset(x, topLeft.y),
                            end = Offset(x, bottomRight.y)
                        )
                        x += gridStep
                    }

                    var y = topLeft.y
                    while (y <= bottomRight.y) {
                        drawLine(
                            color = Color.Green.copy(alpha = 0.3f),
                            start = Offset(topLeft.x, y),
                            end = Offset(bottomRight.x, y)
                        )
                        y += gridStep
                    }

                    // Central point (user)
                    drawCircle(
                        color = Color.Blue,
                        radius = 10f,
                        center = center
                    )




                    if(isRunning) {
                        with(drawContext.canvas) {
                            save() // save context
                            clipRect(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y)

                            drawCircle(
                                color = Color.Green.copy(alpha = 0.3f * (1 - radarPulse.value)),
                                center = center,
                                radius = radarPulse.value * (radarHeight / 2f)
                            )

                            restore() // restore context
                        }
                    }

                    networks.forEachIndexed { index, network ->

                        val strength = (abs(network.rssi)).coerceIn(0, 100)
                        val maxDistanceH = radarHeight / 2f
                        val maxDistanceW = radarWidth / 2f
                        val distanceH = (strength / 100f) * maxDistanceH
                        val distanceW = (strength / 100f) * maxDistanceW

                        // Random direction for ssid
                        val seed = network.ssid.hashCode()
                        // val angle = remember(seed) { Random(seed).nextFloat() * 2f * Math.PI }.toFloat()
                        val angle = angles[network.ssid] ?: 0f
                        val x = center.x + distanceW * cos(angle)
                        val y = center.y + distanceH * sin(angle)
                        val pos = Offset(x, y)


                        val color = getColor(network.capabilities)

                        // Pulse circle
                        if(isRunning) {
                            drawCircle(
                                color = color.copy(alpha = 0.4f * (1 - pulse.value)),
                                radius = 30f + 30f * pulse.value,
                                center = pos
                            )
                        }

                        // Network point
                        drawCircle(
                            color = if (network.ssid == selectedSsid) Color.White else color,
                            radius = if (network.ssid == selectedSsid) 20f else 10f,
                            center = pos
                        )
                    }
                }
            }


            Text(
                text = if (isRunning) "|| Pause" else "|>| Resume",
                color = Color.Green,
                fontFamily = autowide,
                fontSize = 18.sp,
                modifier = Modifier
                    .background(Color(0xFF232222), RoundedCornerShape(8.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .clickable { onToggleRunning() }
            )

            Spacer(modifier = Modifier.height(listHeight + 20.dp))
//            Spacer(modifier = Modifier.weight(0.2f))  // Takes remaining 40% of space
        }









        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(listHeight)
                .background(Color(0xFF101010))
                .align(Alignment.BottomCenter)
        ) {
            Column {
                // Header toggle state
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Retrieved wifis (${networks.size})" ,
                        fontFamily = autowide,
                        color = Color.Green,
                        modifier = Modifier.weight(1f)
                    )
                    // Indicate state
                    Text(
                        text = if (expanded) "▼" else "▲",
                        color = Color.Green
                    )
                }

                // scrollable list LazyColumn  if expanded
                if (expanded) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // takes all remaining space
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(networks) { network ->

                            var color = getColor(network.capabilities)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        selectedSsid = if (selectedSsid == network.ssid) null else network.ssid
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Canvas(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .padding(end = 8.dp)
                                ){
                                    drawCircle(color = color)
                                }
                                Text(
                                    text = "SSID : ${network.ssid} - RSSI : ${network.rssi} dBm",
                                    color = Color.White
                                )
                                InfoButton {
                                    expandedSsid = if (expandedSsid == network.ssid) null else network.ssid
                                }
                            }

                            if (expandedSsid == network.ssid) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, top = 4.dp, bottom = 8.dp)
                                ) {
                                    Text("BSSID : ${network.bssid}", color = Color.LightGray)
                                    Text("Capabilities : ${network.capabilities}", color = Color.LightGray)
                                    Text("Frequency : ${network.frequency} MHz", color = Color.LightGray)

                                    val bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime()
                                    val scanTimeMillis = bootTime + (network.timestamp / 1000L)
                                    val date = Date(scanTimeMillis)
                                    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                    val readableDate = format.format(date)

                                    Text("Timestamp : $readableDate", color = Color.LightGray)
                                    Text("Security Level : ${network.label}", color = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(Color.DarkGray, shape = androidx.compose.foundation.shape.CircleShape)
            .padding(4.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text("i", color = Color.White)
    }
}

@Composable
fun ExportButton(onClick: () -> Unit) {
    Text(
        text = "[+]",
        color = Color.Green,
        fontFamily = autowide,
        fontSize = 14.sp,
        modifier = Modifier
            .background(Color(0xFF232222), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() }
    )
}
