package com.example.minimap.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.location.LocationManager
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.minimap.autowide
import com.example.minimap.data.handler.readWifiNetworksFromCsv
import com.example.minimap.model.WifiNetworkInfo
import com.example.minimap.model.WifiSecurityLevel
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

// Paris coordinates (fallback by default)
private val PARIS_LOCATION = GeoPoint(48.8566, 2.3522)

@Composable
fun MapViewerScreen(navController: NavController) {
    val context = LocalContext.current

    // Retrieve system insets
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    // State to store MapView
    var mapView by remember { mutableStateOf<MapView?>(null) }

    // Configuration OSMDroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Read date from csv
    val wifiNetworks = remember {
        readWifiNetworksFromCsv(context, "wifis_dataset.csv")
    }

    // Only filter wifi with valid coordinates
    val validWifiNetworks = remember(wifiNetworks) {
        wifiNetworks.filter {
            it.latitude != 0.0 && it.longitude != 0.0
        }
    }

    // Group wifi with the same position
    val wifiByPosition = remember(validWifiNetworks) {
        validWifiNetworks.groupBy { wifi ->
            "${wifi.latitude},${wifi.longitude}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(systemBarsPadding)
    ) {
        // FIX HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.Black)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "<",
                color = Color.Green,
                fontSize = 35.sp,
                fontFamily = autowide,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { navController.navigate("home") }
            )

            Text(
                text = "Map (${validWifiNetworks.size} WiFi)",
                color = Color.Green,
                fontSize = 24.sp,
                fontFamily = autowide,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // MAP IN A FIX FRAME
        if (validWifiNetworks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No WiFi networks with location data.\nScan WiFi networks first!",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = autowide,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            // Box container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Color.DarkGray) // Background if map won't load
            ) {
                // Map in the box
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize() // Fill parent Box
                        .clip(RoundedCornerShape(12.dp)), // To fix with angle (cut angle)
                    factory = { ctx ->
                        MapView(ctx).apply {
                            mapView = this
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)

                            isHorizontalMapRepetitionEnabled = false
                            isVerticalMapRepetitionEnabled = false
                            setScrollableAreaLimitLatitude(
                                MapView.getTileSystem().maxLatitude,
                                MapView.getTileSystem().minLatitude,
                                0
                            )

                            // Get current location
                            val initialCenter = getUserLocation(ctx, validWifiNetworks)

                            controller.setZoom(15.0)
                            controller.setCenter(initialCenter)

                            // Overlay user location
                            // Display a blue point for user location
                            val locationOverlay =
                                MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                            locationOverlay.enableMyLocation()
                            locationOverlay.enableFollowLocation() // Follow position if moving
                            overlays.add(locationOverlay)



                            // CLUSTERING CUSTOM
                            val clusterManager = object : RadiusMarkerClusterer(ctx) {
                                fun getClusterIcon(pCluster: MutableList<Marker>): BitmapDrawable {
                                    // Count TOTAL number of wifi
                                    val totalWifiCount = pCluster.sumOf { marker ->
                                        val title = marker.title
                                        if (title.contains("WiFi networks here")) {
                                            title.split(" ")[0].toIntOrNull() ?: 1
                                        } else {
                                            1
                                        }
                                    }

                                    // Find the most dangerous security
                                    val worstSecurity = pCluster.maxOfOrNull { marker ->
                                        val snippet = marker.snippet
                                        when {
                                            snippet.contains("DANGEROUS") -> 3
                                            snippet.contains("MEDIUM") -> 2
                                            snippet.contains("SAFE") -> 1
                                            else -> 0
                                        }
                                    } ?: 0

                                    val clusterColor = when (worstSecurity) {
                                        3 -> AndroidColor.RED
                                        2 -> AndroidColor.YELLOW
                                        1 -> AndroidColor.GREEN
                                        else -> AndroidColor.GRAY
                                    }

                                    return createClusterIcon(ctx, totalWifiCount, clusterColor)
                                }
                            }

                            clusterManager.setRadius(100)

                            // Add markers
                            wifiByPosition.forEach { (_, wifisAtSameLocation) ->
                                if (wifisAtSameLocation.size == 1) {
                                    val wifi = wifisAtSameLocation[0]
                                    val marker = createWifiMarker(ctx, this, wifi)
                                    clusterManager.add(marker)
                                } else {
                                    val marker = createGroupedWifiMarker(ctx, this, wifisAtSameLocation)
                                    clusterManager.add(marker)
                                }
                            }

                            overlays.add(clusterManager)
                            invalidate()
                        }
                    }
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDetach()
        }
    }
}

// Retrieve user location
private fun getUserLocation(context: Context, validWifiNetworks: List<WifiNetworkInfo>): GeoPoint {
    // Check location permission
    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasLocationPermission) {
        // Not permission -> Fallback on Paris
        return PARIS_LOCATION
    }

    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // First try GPS
        val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (gpsLocation != null) {
            return GeoPoint(gpsLocation.latitude, gpsLocation.longitude)
        }

        // Else try Network
        val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (networkLocation != null) {
            return GeoPoint(networkLocation.latitude, networkLocation.longitude)
        }

        // If valid WiFi, center on the first one
        if (validWifiNetworks.isNotEmpty()) {
            return GeoPoint(validWifiNetworks[0].latitude, validWifiNetworks[0].longitude)
        }

    } catch (e: SecurityException) {
        // Permission refused at runtime
        e.printStackTrace()
    }

    // Final fallback on Paris
    return PARIS_LOCATION
}

// Create a personalized icon cluster
private fun createClusterIcon(context: Context, count: Int, color: Int): BitmapDrawable {
    val size = 120
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Draw circle
    val paint = Paint().apply {
        isAntiAlias = true
        this.color = color
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

    // Draw text (WiFi number)
    val textPaint = Paint().apply {
        isAntiAlias = true
        this.color = AndroidColor.WHITE
        textSize = 50f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    val textY = (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
    canvas.drawText(count.toString(), size / 2f, textY, textPaint)

    return BitmapDrawable(context.resources, bitmap)
}

// Simple marker with visible SSID
private fun createWifiMarker(context: Context, mapView: MapView, wifi: WifiNetworkInfo): Marker {
    return Marker(mapView).apply {
        position = GeoPoint(wifi.latitude, wifi.longitude)
        title = wifi.ssid
        snippet = """
            BSSID: ${wifi.bssid}
            RSSI: ${wifi.rssi} dBm
            Security: ${wifi.label}
            Frequency: ${wifi.frequency} MHz
        """.trimIndent()

        val markerColor = when (wifi.label) {
            WifiSecurityLevel.SAFE -> AndroidColor.GREEN
            WifiSecurityLevel.MEDIUM -> AndroidColor.YELLOW
            WifiSecurityLevel.DANGEROUS -> AndroidColor.RED
            else -> AndroidColor.GRAY
        }

        // Create a personalized icon with complete SSID
        icon = createMarkerIcon(context, wifi.ssid, markerColor)
    }
}

// Create a marker icon with personalized text
private fun createMarkerIcon(context: Context, text: String, backgroundColor: Int): BitmapDrawable {
    val textPaint = Paint().apply {
        isAntiAlias = true
        color = AndroidColor.WHITE
        textSize = 40f
        isFakeBoldText = true
    }

    // Measure text width
    val textWidth = textPaint.measureText(text)
    val width = (textWidth + 40).toInt()
    val height = 80

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background rounded
    val bgPaint = Paint().apply {
        isAntiAlias = true
        color = backgroundColor
        style = Paint.Style.FILL
    }
    canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 20f, 20f, bgPaint)

    // Centered text
    val textY = (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
    canvas.drawText(text, width / 2f, textY, textPaint.apply { textAlign = Paint.Align.CENTER })

    return BitmapDrawable(context.resources, bitmap)
}

// Gathered marker
private fun createGroupedWifiMarker(context: Context, mapView: MapView, wifis: List<WifiNetworkInfo>): Marker {
    val firstWifi = wifis[0]

    return Marker(mapView).apply {
        position = GeoPoint(firstWifi.latitude, firstWifi.longitude)
        title = "${wifis.size} WiFi networks here"

        snippet = buildString {
            wifis.forEachIndexed { index, wifi ->
                append("${index + 1}. ${wifi.ssid}")
                append(" (${wifi.label}, ${wifi.rssi} dBm)")
                if (index < wifis.size - 1) append("\n")
            }
        }

        val worstSecurity = wifis.maxByOrNull { wifi ->
            when (wifi.label) {
                WifiSecurityLevel.DANGEROUS -> 3
                WifiSecurityLevel.MEDIUM -> 2
                WifiSecurityLevel.SAFE -> 1
                else -> 0
            }
        }?.label

        val markerColor = when (worstSecurity) {
            WifiSecurityLevel.DANGEROUS -> AndroidColor.RED
            WifiSecurityLevel.MEDIUM -> AndroidColor.YELLOW
            WifiSecurityLevel.SAFE -> AndroidColor.GREEN
            else -> AndroidColor.GRAY
        }

        // Personalized marker with number
        icon = createMarkerIcon(context, "${wifis.size} WiFi", markerColor)
    }
}