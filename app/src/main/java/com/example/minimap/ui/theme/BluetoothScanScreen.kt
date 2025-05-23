package com.example.minimap.ui.theme

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minimap.model.BluetoothDeviceInfo
import com.example.minimap.model.BluetoothScannerView
import com.google.maps.android.compose.*

@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION])
@Composable
fun BluetoothScanScreen(viewModel: BluetoothScannerView = viewModel()) {


//    val devices = viewModel.devices

    val devices = viewModel.devices.collectAsState()


    LaunchedEffect(Unit){
        viewModel.startPeriodicScan()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ){

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Appareils Bluetooth détectés :", color = Color.White)
            Spacer(Modifier.height(8.dp))
            devices.value.forEach { device ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("Nom: ${device.name ?: "Inconnu"}", color = Color.Green)
                    Text("Adresse MAC: ${device.address}", color = Color.LightGray, fontSize = 12.sp)
                    Text("RSSI: ${device.rssi} dBm", color = Color.Cyan, fontSize = 12.sp)
                    device.deviceClass?.let {
                        Text("Classe: $it", color = Color.Magenta, fontSize = 12.sp)
                    }
                    device.uuidList.let { uuids ->
                        if (uuids.isNotEmpty()) {
                            Column {
                                Text("UUIDs :", color = Color.Red, fontSize = 12.sp)
                                uuids.forEach { uuid ->
                                    Text("• $uuid", color = Color.Red, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Animation mignonne (exemple fixe pour l’instant)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text("box", fontSize = 40.sp)
            }
        }
    }
}


@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION])
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun BluetoothScanScreenPreview() {
    BluetoothScanScreen()
}