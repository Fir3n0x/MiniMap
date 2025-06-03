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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import com.example.minimap.autowide
import com.example.minimap.data.preferences.SettingsKeys
import com.example.minimap.data.preferences.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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
        ParamOption(
            key = "vibration",
            title = "Device Vibration",
            description = "Enable device vibration when discovering a new wifi"
        )
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


    // Instancier le SettingsRepository une seule fois
    val context = LocalContext.current
    val settingsRepo = remember { SettingsRepository(context) }

    // On lit en Flow les préférences persistées pour AutoScan et Notifications
    val autoScanEnabledState by settingsRepo.autoScanEnabledFlow.collectAsState(initial = false)
    val notificationEnabledState by settingsRepo.notificationEnabledFlow.collectAsState(initial = false)
    val vibrationEnabledState by settingsRepo.vibrationEnabledFlow.collectAsState(initial = false)

    // Pour planifier ou annuler le WorkManager
    val workManager = androidx.work.WorkManager.getInstance(context)



    // 6) États locaux pour le reste des options qui ne sont pas persistées (maps clé → Boolean)
    //    Ici, on persiste uniquement « saveResults », « silentMode », « showVersion », « enableLogs » en mémoire.
    val localOptionStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            // Initialiser toutes les clés non gérées par DataStore à false
            (scanOptions + notificationOptions + aboutOptions).forEach { opt ->
                if (opt.key != SettingsKeys.AUTO_SCAN_ENABLED.name &&
                    opt.key != SettingsKeys.NOTIFICATION_ENABLED.name &&
                    opt.key != SettingsKeys.VIBRATION_ENABLED.name
                ) {
                    this[opt.key] = false
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ────────────────────────────────────────────────────────
        // A) Colonne de gauche : bouton « < » + onglets verticaux
        // ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .background(Color(0xFF222222))
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bouton de retour vers Home
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("home") }
                    .padding(vertical = 12.dp)
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

            // Boucle sur les onglets (ParamTab.values()), format visuel et sélection
            ParamTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            if (isSelected) Color(0xFF333333) else Color.Transparent,
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
        // B) Colonne de droite : contenu dynamique selon l’onglet sélectionné
        // ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                ParamTab.Scan -> {
                    // ==== Onglet “Scan” ====
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Paramètres Scan",
                            color = Color.Green,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // 1) Auto Scan (DataStore + WorkManager)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = autoScanEnabledState,
                                onCheckedChange = { checked ->
                                    // Met à jour DataStore
                                    CoroutineScope(Dispatchers.IO).launch {
                                        settingsRepo.setAutoScanEnabled(checked)
                                    }
                                    // Planifie ou annule le Work
                                    if (checked) {
                                        val request =
                                            PeriodicWorkRequestBuilder<WifiScanWorker>(
                                                /* repeatInterval= */ 20,
                                                TimeUnit.MINUTES
                                            )
                                                .setInitialDelay(0, TimeUnit.MINUTES)
                                                .addTag("wifi_auto_scan")
                                                .build()
                                        workManager.enqueueUniquePeriodicWork(
                                            "WifiAutoScanWork",
                                            ExistingPeriodicWorkPolicy.REPLACE,
                                            request
                                        )
                                    } else {
                                        workManager.cancelAllWorkByTag("wifi_auto_scan")
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = scanOptions[0].title,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = scanOptions[0].description,
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Divider(color = Color.DarkGray, thickness = 1.dp)

                        // 2) Sauvegarder les résultats (état local)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = localOptionStates[scanOptions[1].key] == true,
                                onCheckedChange = { checked ->
                                    localOptionStates[scanOptions[1].key] = checked
                                },
                                colors = CheckboxDefaults.colors(
                                    checkmarkColor = Color.Black,
                                    uncheckedColor = Color.Gray,
                                    checkedColor = Color.Green
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = scanOptions[1].title,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = scanOptions[1].description,
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Divider(color = Color.DarkGray, thickness = 1.dp)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = vibrationEnabledState,
                                onCheckedChange = { checked ->
                                    CoroutineScope(Dispatchers.IO).launch {
                                        settingsRepo.setVibrationEnabled(checked)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = scanOptions[2].title, // "Device Vibration"
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = scanOptions[2].description,
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                ParamTab.Notification -> {
                    // ==== Onglet “Notification” ====
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Paramètres Notification",
                            color = Color.Green,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // 1) Notifications Push (DataStore)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = notificationEnabledState,
                                onCheckedChange = { checked ->
                                    // Met à jour DataStore
                                    CoroutineScope(Dispatchers.IO).launch {
                                        settingsRepo.setNotificationEnabled(checked)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = notificationOptions[0].title,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = notificationOptions[0].description,
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Divider(color = Color.DarkGray, thickness = 1.dp)

                        // 2) Mode silencieux (état local)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = localOptionStates[notificationOptions[1].key] == true,
                                onCheckedChange = { checked ->
                                    localOptionStates[notificationOptions[1].key] = checked
                                },
                                colors = CheckboxDefaults.colors(
                                    checkmarkColor = Color.Black,
                                    uncheckedColor = Color.Gray,
                                    checkedColor = Color.Green
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = notificationOptions[1].title,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = notificationOptions[1].description,
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                ParamTab.About -> {
                    // ==== Onglet “About” ====
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "À propos",
                            color = Color.Green,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // 1) Afficher la version (état local)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = localOptionStates[aboutOptions[0].key] == true,
                                onCheckedChange = { checked ->
                                    localOptionStates[aboutOptions[0].key] = checked
                                },
                                colors = CheckboxDefaults.colors(
                                    checkmarkColor = Color.Black,
                                    uncheckedColor = Color.Gray,
                                    checkedColor = Color.Green
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = aboutOptions[0].title,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = aboutOptions[0].description,
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Divider(color = Color.DarkGray, thickness = 1.dp)

                        // 2) Activer les logs (état local)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = localOptionStates[aboutOptions[1].key] == true,
                                onCheckedChange = { checked ->
                                    localOptionStates[aboutOptions[1].key] = checked
                                },
                                colors = CheckboxDefaults.colors(
                                    checkmarkColor = Color.Black,
                                    uncheckedColor = Color.Gray,
                                    checkedColor = Color.Green
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = aboutOptions[1].title,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = aboutOptions[1].description,
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
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
