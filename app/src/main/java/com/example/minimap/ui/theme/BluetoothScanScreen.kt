package com.example.minimap.ui.theme

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.minimap.autowide
import com.example.minimap.model.BluetoothDeviceInfo
import com.example.minimap.model.BluetoothScannerView
import com.google.maps.android.compose.*

@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION])
@Composable
fun BluetoothScanScreen(
    viewModel: BluetoothScannerView = viewModel(),
    navController: NavController
) {
    // Collect devices as State
    val devices by viewModel.devices.collectAsState()

    // Start scanning when screen appears
    LaunchedEffect(Unit) {
        viewModel.startPeriodicScan()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with back button and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Text(
                        text = "<",
                        color = Color.Green,
                        fontFamily = autowide,
                        fontSize = 35.sp
                    )
                }

                // Title
                Text(
                    text = "Bluetooth Analysis",
                    color = Color.Green,
                    fontFamily = autowide,
                    fontSize = 24.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                // Empty space to balance the row
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Devices list
            Text("Bluetooth Deveices Detected :", color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            if (devices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun appareil détecté", color = Color.White)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = devices,
                        key = { it.address }  // Use MAC address as unique key
                    ) { device ->
                        DeviceItem(device = device)
                    }
                }
            }

            // Bottom animation box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Visualization", color = Color.White, fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun DeviceItem(device: BluetoothDeviceInfo) {  // Replace with your actual device class
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
    ) {
        Text("Nom: ${device.name ?: "Inconnu"}", color = Color.Green)
        Text(
            "Adresse MAC: ${device.address}",
            color = Color.LightGray,
            fontSize = 12.sp
        )
        Text("RSSI: ${device.rssi} dBm", color = Color.Cyan, fontSize = 12.sp)
        device.deviceClass?.let {
            Text("Classe: $it", color = Color.Magenta, fontSize = 12.sp)
        }
        if (device.uuidList.isNotEmpty()) {
            Column {
                Text("UUIDs :", color = Color.Red, fontSize = 12.sp)
                device.uuidList.forEach { uuid ->
                    Text("• $uuid", color = Color.Red, fontSize = 12.sp)
                }
            }
        }
    }
}



@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION])
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun BluetoothScanScreenPreview() {
    BluetoothScanScreen(navController = rememberNavController())
}

