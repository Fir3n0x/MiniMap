package com.example.minimap.ui.theme

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minimap.model.BluetoothScannerView
import com.google.maps.android.compose.*


@Composable
fun BluetoothScanScreen(viewModel: BluetoothScannerView = viewModel()) {
    val devices = viewModel.devices

    LaunchedEffect(Unit) {
        viewModel.startScan()
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
            devices.forEach { device ->
                Text(text = device, color = Color.Green, fontSize = 14.sp)
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
                Text("🐾", fontSize = 40.sp)
            }
        }
    }
}



@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun BluetoothScanScreenPreview() {
    BluetoothScanScreen()
}