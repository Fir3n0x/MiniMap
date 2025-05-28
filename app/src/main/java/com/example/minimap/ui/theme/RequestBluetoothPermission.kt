//package com.example.minimap.ui.theme
//
//import android.app.Activity
//import android.content.Context
//import android.content.pm.PackageManager
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.platform.LocalContext
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//
//@Composable
//fun RequestBluetoothPermissions(
//    onPermissionsGranted: () -> Unit,
//    onPermissionsDenied: () -> Unit
//) {
//    val context = LocalContext.current
//    val activity = context as? Activity
//
//    val requiredPermissions = listOf(
//        Manifest.permission.BLUETOOTH_SCAN,
//        Manifest.permission.BLUETOOTH_CONNECT,
//        Manifest.permission.ACCESS_FINE_LOCATION
//    )
//
//    val permissionStates = remember {
//        requiredPermissions.associateWith { permission ->
//            mutableStateOf(
//                ContextCompat.checkSelfPermission(context, permission) ==
//                        PackageManager.PERMISSION_GRANTED
//            )
//        }
//    }
//
//    val launcher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { permissions ->
//        permissions.forEach { (permission, isGranted) ->
//            permissionStates[permission]?.value = isGranted
//        }
//
//        if (permissionStates.all { it.value.value }) {
//            onPermissionsGranted()
//        } else {
//            onPermissionsDenied()
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        if (permissionStates.all { it.value.value }) {
//            onPermissionsGranted()
//        } else {
//            launcher.launch(requiredPermissions.toTypedArray())
//        }
//    }
//}