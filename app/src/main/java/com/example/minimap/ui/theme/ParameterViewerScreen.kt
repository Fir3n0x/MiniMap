package com.example.minimap.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.minimap.autowide

// 1) Enum for tabs
private enum class ParamTab(val label: String, val icon: @Composable () -> Unit) {
    Scan(
        label = "Scan",
        icon = { Icon(Icons.Default.Search, contentDescription = "Scan", tint = Color.Green) }
    ),
    Notification(
        label = "Notification",
        icon = { Icon(Icons.Default.Notifications, contentDescription = "Notification", tint = Color.Green) }
    ),
    About(
        label = "About",
        icon = { Icon(Icons.Default.Info, contentDescription = "About", tint = Color.Green) }
    )
}

// 2) Data class which represents an option with title, description and state
private data class ParamOption(
    val key: String,
    val title: String,
    val description: String
)

@Composable
fun ParameterViewerScreen(navController: NavController) {
    // State for current checked tab
    var selectedTab by rememberSaveable { mutableStateOf(ParamTab.Scan) }

    // For each tab, we define an option list
    val scanOptions = listOf(
        ParamOption(
            key = "autoScan",
            title = "Auto Scan",
            description = "Automatically scans networks each 30 minutes when the application is closed"
        ),
        ParamOption(
            key = "saveResults",
            title = "Auto save Wifi",
            description = "Automatically saves the results of the WiFi scan locally on the device."
        ),
    )
    val notificationOptions = listOf(
        ParamOption(
            key = "pushNotifications",
            title = "Notifications Push",
            description = "Show a notification when a particular wifi is detected after \"Auto Scan\". Require \"Auto Scan\" to work."
        ),
        ParamOption(
            key = "silentMode",
            title = "Silent Mode",
            description = "Disables sounds and vibrations during notifications."
        )
    )
    val aboutOptions = listOf(
        ParamOption(
            key = "showVersion",
            title = "Show current Version",
            description = "Shows the current version of the application on the main screen."
        ),
        ParamOption(
            key = "enableLogs",
            title = "Enable logs",
            description = "Saves an extended history for debugging (reboot required)."
        )
    )

    // Map to store checked state of each option (each key -> MutableState<Boolean>)
    // Initialize every state as false
    val checkboxStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            (scanOptions + notificationOptions + aboutOptions).forEach {
                this[it.key] = false
            }
        }
    }



    // Global content
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ────────────────────────────────────────────────────────
        // 1) Left column : vertical tabs
        // ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .background(Color(0xFF222222))
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // -- Button "<" at the top to get back "HomeScreen" --
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("home") }
                    .padding(vertical = 12.dp),
            ) {
                Text(
                    text = "<",
                    color = Color.Green,
                    fontSize = 24.sp,
                    fontFamily = autowide,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // For each tab of ParamTab, we retrieve value from values()
            ParamTab.entries.forEach { tab ->
                val isSelected = (tab == selectedTab)

                // Each tab is a clickable box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            // Lighter color is selected
                            if (isSelected) Color(0xFF333333)
                            else Color.Transparent,
                            shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                        )
                        .clickable { selectedTab = tab }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Icon tab (green if selected, else gray)
                        CompositionLocalProvider(
                            LocalContentColor provides if (isSelected) Color.Green else Color.Gray
                        ) {
                            tab.icon()
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tab.label,
                            color = if (isSelected) Color.White else Color.LightGray,
                            fontSize = 12.sp,
                            fontFamily = autowide,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ────────────────────────────────────────────────────────
        // 2) Right column : content (options) according to current tab
        // ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                ParamTab.Scan -> {
                    TabContent(options = scanOptions, checkboxStates = checkboxStates)
                }
                ParamTab.Notification -> {
                    TabContent(options = notificationOptions, checkboxStates = checkboxStates)
                }
                ParamTab.About -> {
                    TabContent(options = aboutOptions, checkboxStates = checkboxStates)
                }
            }
        }
    }
}

/**
 * Reusable composable to display a list of options checkbox with description.
 *
 * @param options         List of ParamOption (each option has a key, title and description).
 * @param checkboxStates  Shared map where each key (ParamOption.key)   Boolean indicates the status checked.
 */
@Composable
private fun TabContent(
    options: List<ParamOption>,
    checkboxStates: SnapshotStateMap<String, Boolean>
) {
    // If long list, we can scroll down thanks to LazyColumn
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(options) { option ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkbox (state read from checkboxStates)
                    Checkbox(
                        checked = checkboxStates[option.key] == true,
                        onCheckedChange = { checked ->
                            checkboxStates[option.key] = checked
                        },
                        colors = CheckboxDefaults.colors(
                            checkmarkColor = Color.Black,
                            uncheckedColor = Color.Gray,
                            checkedColor = Color.Green
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Option title
                    Text(
                        text = option.title,
                        color = Color.White,
                        fontFamily = autowide,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Color gray for description
                Text(
                    text = option.description,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontFamily = autowide,
                    modifier = Modifier.padding(start = 40.dp) // Align under text, not under checkbox
                )
            }
        }
    }
}
