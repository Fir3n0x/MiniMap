package com.example.minimap.model

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.minimap.ui.theme.WifiScanWorker
import java.util.concurrent.TimeUnit

object WorkerManager {
    fun scheduleWifiScan(context: Context, enable: Boolean) {
        val workManager = WorkManager.getInstance(context)

        if (enable) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<WifiScanWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "periodicWifiScan",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        } else {
            workManager.cancelUniqueWork("periodicWifiScan")
        }
    }
}