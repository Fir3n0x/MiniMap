package com.example.minimap.ui.theme


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.minimap.TerminalButton
import com.example.minimap.autowide
import com.example.minimap.model.Screen
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableIntStateOf
import com.example.minimap.model.WifiNetworkInfo
import kotlinx.serialization.json.Json
import java.io.File


@Composable
fun FileViewerScreen(navController: NavController) {
    val context = LocalContext.current
    val tabs = listOf("WiFi Detection", "Bluetooth Scan")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var jsonContent by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<File?>(null) }

    var jsonFiles by remember {
        mutableStateOf(
            context.filesDir?.listFiles { file ->
                file.extension == "json"
            }?.toList() ?: emptyList()
        )
    }

    if (showDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Text(
                    text = "Close",
                    modifier = Modifier
                        .clickable { showDialog = false }
                        .padding(8.dp),
                    color = Color.Green
                )
            },
            title = {
                Text("WIFI Info", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Box(modifier = Modifier.padding(top = 8.dp)) {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        item {
                            Text(
                                text = jsonContent,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            containerColor = Color.DarkGray
        )
    }


    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                Text(
                    text = "Yes",
                    modifier = Modifier
                        .clickable {
                            fileToDelete?.delete()
                            jsonFiles = context.filesDir?.listFiles { file -> file.extension == "json" }?.toList()
                                ?: emptyList()
                            showDeleteDialog = false
                        }
                        .padding(8.dp),
                    color = Color.Red
                )
            },
            dismissButton = {
                Text(
                    text = "No",
                    modifier = Modifier
                        .clickable { showDeleteDialog = false }
                        .padding(8.dp),
                    color = Color.Green
                )
            },
            title = {
                Text("Confirm Deletion", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this file?",
                    color = Color.White,
                    fontSize = 14.sp
                )
            },
            containerColor = Color.DarkGray
        )
    }




    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {

            Box(
                modifier = Modifier
                    .clickable {
                        navController.navigate("home")
                    }
                    .padding(start = 16.dp, end = 32.dp) // space from border
            ) {
                Text(
                    text = "<",
                    color = Color.Green,
                    fontFamily = autowide,
                    fontSize = 35.sp
                )
            }

            tabs.forEachIndexed { index, title ->
                Text(
                    text = title,
                    fontFamily = autowide,
                    color = if (index == selectedTabIndex) Color.Green else Color.Gray,
                    modifier = Modifier
                        .clickable { selectedTabIndex = index }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Scrollable list .json
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(jsonFiles) { file ->
                FileItem(
                    fileName = file.name,
                    onClick = {
                        try {
                            val content = file.readText()
                            jsonContent = content
                            showDialog = true
                        } catch (e: Exception) {
                            Log.e("FileViewer", "Error reading file: ${e.message}")
                        }
                    },
                    onDelete = {
                        fileToDelete = file
                        showDeleteDialog = true
                    }
                )
            }
        }
    }
}

@Composable
fun FileItem(fileName: String, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFF1F1F1F), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = fileName,
            color = Color.White,
            fontFamily = autowide,
            fontSize = 16.sp,
            modifier = Modifier.clickable { onClick() }
        )

        Text(
            text = "|x|",
            color = Color.Red,
            fontSize = 18.sp,
            modifier = Modifier
                .clickable { onDelete() }
                .padding(start = 12.dp)
        )
    }
}
