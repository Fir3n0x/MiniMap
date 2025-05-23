package com.example.minimap.model


import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.ParcelUuid
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

//class BluetoothScannerView(application : Application) : AndroidViewModel(application){
//    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
//    val devices = mutableStateListOf<String>()
//
//    private val context = getApplication<Application>().applicationContext
//
//    private val receiver = object : BroadcastReceiver() {
//        @SuppressLint("MissingPermission")
//        override fun onReceive(context: Context?, intent: Intent?) {
//            when (intent?.action) {
//                BluetoothDevice.ACTION_FOUND -> {
//                    val device: BluetoothDevice? =
//                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
//                    device?.let {
//                        val name = it.name ?: "Unknown"
//                        val address = it.address
//                        val entry = "$name ($address)"
//                        if (!devices.contains(entry)) {
//                            devices.add(entry)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    init {
//        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//        context.registerReceiver(receiver, filter)
//    }
//
//    @SuppressLint("MissingPermission")
//    fun startScan() {
//        if (bluetoothAdapter?.isDiscovering == true) {
//            bluetoothAdapter.cancelDiscovery()
//        }
//        bluetoothAdapter?.startDiscovery()
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        context.unregisterReceiver(receiver)
//    }
//}



class BluetoothScannerView(application: Application) : AndroidViewModel(application) {

    private val _devices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val devices: StateFlow<List<BluetoothDeviceInfo>> = _devices

    @SuppressLint("ServiceCast")
    private val bluetoothAdapter: BluetoothAdapter? =
        (application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    private val receiver = object : BroadcastReceiver() {

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val rssi: Short =
                    intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                val deviceClass = device?.bluetoothClass?.deviceClass?.toString()

                if (device != null) {

                    val info = BluetoothDeviceInfo(
                        name = device.name,
                        address = device.address,
                        rssi = rssi.toInt(),
                        deviceClass = deviceClass
                    )

                    viewModelScope.launch {
//                        val currentList = _devices.value
//                        if (info.address !in currentList.map { it.address }) {
//                            _devices.value = currentList + info
//                        }

                        val currentList = _devices.value.toMutableList()
                        val existingIndex = currentList.indexOfFirst { it.address == info.address }
                        if (existingIndex >= 0) {
                            currentList[existingIndex] = info  // replace older by new
                        } else {
                            currentList.add(info)
                        }
                        _devices.value = currentList
                    }
                }

                device?.fetchUuidsWithSdp()


            }
        }
    }


    val uuidReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            val uuidList = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)
                ?.mapNotNull { it as? ParcelUuid }
                ?.map { it.uuid }

            if (device != null && uuidList != null) {
                viewModelScope.launch {
                    _devices.value = _devices.value.map {
                        if (it.address == device.address) it.copy(uuidList = uuidList)
                        else it
                    }
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startPeriodicScan() {
        val context = getApplication<Application>().applicationContext
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, filter)

        val uuidFilter = IntentFilter(BluetoothDevice.ACTION_UUID)
        context.registerReceiver(uuidReceiver, uuidFilter)

        viewModelScope.launch {
            while (true) {
                bluetoothAdapter?.cancelDiscovery() // Stop the previous one if still enable
                bluetoothAdapter?.startDiscovery()
                delay(3000) // release every 10 seconds
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScan() {
        val context = getApplication<Application>().applicationContext
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, filter)

        val uuidFilter = IntentFilter(BluetoothDevice.ACTION_UUID)
        context.registerReceiver(uuidReceiver, uuidFilter)

        bluetoothAdapter?.startDiscovery()
    }

    override fun onCleared() {
        super.onCleared()
        val context = getApplication<Application>().applicationContext
        context.unregisterReceiver(receiver)
        context.unregisterReceiver(uuidReceiver)
    }
}
