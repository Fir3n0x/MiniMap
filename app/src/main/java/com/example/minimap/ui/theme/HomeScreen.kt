package com.example.minimap.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.minimap.TerminalButton
import com.example.minimap.autowide
import com.example.minimap.model.Screen


@Composable
fun HomeScreen(navController : NavController) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ParameterButton(
                onClick = {
                    navController.navigate("parameterViewer")
                },
                modifier = Modifier
            )

            Spacer(modifier = Modifier.weight(1f))

            StoredButton(
                onClick = {
                    navController.navigate("fileViewer")
                },
                modifier = Modifier
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "MiniMap",
                color = Color(0xFF00FF00),
                fontFamily = autowide,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            TerminalButton("WiFi Detection") {
                navController.navigate(Screen.WifiScan.route)
            }
        }
    }
}




@Composable
fun StoredButton(onClick: () -> Unit, modifier: Modifier) {
    Text(
        text = "|||\\",
        color = Color.Green,
        fontFamily = autowide,
        fontSize = 14.sp,
        modifier = modifier
            .background(Color(0xFF232222), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() }
    )
}

@Composable
fun ParameterButton(onClick: () -> Unit, modifier: Modifier){
    Text(
        text = "ooo",
        color = Color.Green,
        fontFamily = autowide,
        fontSize = 14.sp,
        modifier = modifier
            .background(Color(0xFF232222), RoundedCornerShape(8.dp))
            .padding(horizontal = 2.dp, vertical = 6.dp)
            .clickable { onClick() }
    )
}