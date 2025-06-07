package com.example.minimap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.*
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.minimap.ui.theme.*
import com.example.minimap.R
import com.example.minimap.data.preferences.SettingsRepository
import com.example.minimap.model.Screen
import com.example.minimap.model.WorkerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


val autowide = FontFamily(
    Font(R.font.audiowide_regular, FontWeight.Normal)
)


class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            schedulePeriodicWifiScan(this@MainActivity)
        } else {
            Toast.makeText(
                this,
                "Les permissions sont nécessaires pour la fonctionnalité de scan Wi-Fi",
                Toast.LENGTH_LONG
            ).show()
        }
    }



    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                if (checkPermissions(context)) {
                    schedulePeriodicWifiScan(context)
                } else {
                    requestPermissions(context)
                }
            }

            NavHost(navController = navController, startDestination = Screen.Home.route) {
                composable(Screen.Home.route){
                    HomeScreen(navController)
                }

                composable(Screen.WifiScan.route){
                    val context = LocalContext.current
                    WifiScanScreen(context, navController)
                }
                composable(Screen.FileViewer.route){
                    FileViewerScreen(navController = navController)
                }

                composable(Screen.ParameterViewer.route){
                    ParameterViewerScreen(navController = navController)
                }
            }
        }
    }

    private fun checkPermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions(context: Context) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()

        requestPermissionLauncher.launch(permissions)
    }
}



@Composable
fun TerminalButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF003300), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF00FF00),
            fontFamily = autowide,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


fun schedulePeriodicWifiScan(context: Context) {
    val settingsRepo = SettingsRepository(context)
    CoroutineScope(Dispatchers.IO).launch {
        val enabled = settingsRepo.autoScanEnabledFlow.first()
        WorkerManager.scheduleWifiScan(context, enabled)
    }
}



@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun MiniMapHomeScreenPreview() {
    val context = LocalContext.current
    val navController = TestNavHostController(context)
    HomeScreen(navController = navController)
}
