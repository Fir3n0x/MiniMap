package com.example.minimap


class WorkerManagerUnitTest {
//
//    private lateinit var context: Context
//    private lateinit var workManager: WorkManager
//
//    @Before
//    fun setup() {
//
//        val context = ApplicationProvider.getApplicationContext<Context>()
//
//        val config = Configuration.Builder()
//            .setMinimumLoggingLevel(android.util.Log.DEBUG)
//            .build()
//
//        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
//
//        workManager = mockk(relaxed = true)
//
//        mockkStatic(WorkManager::class)
//        every { WorkManager.getInstance(context) } returns workManager
//    }
//
//    @Test
//    fun `test scheduleWifiScan enable true`() {
//        // Act
//        WorkerManager.scheduleWifiScan(context, true)
//
//        // Assert
//        verify(exactly = 1) {
//            workManager.enqueueUniquePeriodicWork(
//                eq("periodicWifiScan"),
//                eq(ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE),
//                any()
//            )
//        }
//
//        verify(exactly = 1) {
//            workManager.enqueue(
//                match<WorkRequest> {
//                    "DEBUG_WORKER" in it.tags
//                }
//            )
//        }
//    }
//
//    @Test
//    fun `test scheduleWifiScan enable false`() {
//        // Act
//        WorkerManager.scheduleWifiScan(context, false)
//
//        // Assert
//        verify(exactly = 1) {
//            workManager.cancelUniqueWork("periodicWifiScan")
//        }
//    }
}
