package com.example.minimap.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.minimap.ui.screens.FileViewerScreen
import com.example.minimap.ui.screens.HomeScreen
import com.example.minimap.ui.screens.MapViewerScreen
import com.example.minimap.ui.screens.ParameterViewerScreen
import com.example.minimap.ui.screens.Screen
import com.example.minimap.ui.screens.WifiScanScreen

@SuppressLint("MissingPermission")
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

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

        composable(Screen.MapViewer.route) {
            MapViewerScreen(navController = navController)
        }
    }
}