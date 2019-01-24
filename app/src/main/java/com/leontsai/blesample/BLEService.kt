package com.leontsai.blesample

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class BLEService : Service() {
    private val mBinder = LocalBinder()
    private lateinit var mBluetoothManager: BluetoothManager
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var mBluetoothLeScanner: BluetoothLeScanner
    private lateinit var mScanCallback: ScanCallback
    private lateinit var mBluetoothGatt: BluetoothGatt
    private lateinit var mBluetoothDevice: BluetoothDevice
    private var mBluetoothDeviceFound = false

    private val mBluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
        }

    }


    fun initBleInternal(): Boolean {
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        }
        if (mBluetoothManager == null) return false
        if (mBluetoothAdapter == null)
            mBluetoothAdapter = mBluetoothManager.adapter
        if (mBluetoothAdapter == null) return false
        if (mBluetoothLeScanner == null) mBluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner
        if (mBluetoothLeScanner == null) return false
        if (mScanCallback == null)
            mScanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)

                    if (mBluetoothDeviceFound || result == null || result.device == null || result.scanRecord == null || result.device.name == null) {
                        return
                    }

                    var device = result.device
//                    if (device.name.startsWith(GattAttributes.))
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                }
            }
        return mBluetoothAdapter.isEnabled
    }

    fun scanDevice(enable: Boolean) {
        if (enable) {
            mBluetoothLeScanner.startScan(mScanCallback)
        } else {
            mBluetoothLeScanner.stopScan(mScanCallback)
        }
    }

    fun connect() {

    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    inner class LocalBinder : Binder() {

        fun getService(): BLEService {
            return this@BLEService
        }
    }


}