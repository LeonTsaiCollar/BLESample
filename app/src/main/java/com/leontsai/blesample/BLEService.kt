package com.leontsai.blesample

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import java.util.*

class BLEService : Service() {

    companion object {
        val FOUND_DEVICE = 802
        val DISCOVERY = 803
        val WRITE = 804
        val RECEIVE_DEVICE_MSG = 805
        val DESCRIPTION_WRITE = 806
    }


    private val mBinder = LocalBinder()
    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothLeScanner: BluetoothLeScanner? = null
    private var mScanCallback: ScanCallback? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mBluetoothDevice: BluetoothDevice? = null
    @Volatile
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

    private val mHandler: Handler by lazy {
        Handler {
            when (it.what) {
                FOUND_DEVICE -> {
                    scanDevice(false)
                    mBluetoothDeviceFound = true
                    mHandler.postDelayed(Runnable { connect() }, 200)
                }
                DISCOVERY -> {

                }

                else -> {

                }
            }
            return@Handler true
        }
    }


    fun initBleInternal(): Boolean {
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        }
        if (mBluetoothManager == null) return false

        if (mBluetoothAdapter == null)
            mBluetoothAdapter = mBluetoothManager?.adapter
        if (mBluetoothAdapter == null) return false
        if (mBluetoothLeScanner == null) mBluetoothLeScanner = mBluetoothAdapter?.bluetoothLeScanner
        if (mBluetoothLeScanner == null) return false
        if (mScanCallback == null)
            mScanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)

                    if (mBluetoothDeviceFound || result == null || result.device == null || result.scanRecord == null || result.device.name == null) {
                        return
                    }

                    val device = result.device
                    if (device.name.startsWith(GATTATTRIBUTES_DEVICE_NAME)) {
                        mBluetoothDevice = device
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                }
            }
        return mBluetoothAdapter!!.isEnabled
    }

    fun scanDevice(enable: Boolean) {
        if (enable) {
            mBluetoothLeScanner?.startScan(mScanCallback)
        } else {
            mBluetoothLeScanner?.stopScan(mScanCallback)
        }
    }

    fun connect(): Boolean {
        if (mBluetoothDevice == null) return false
        mBluetoothGatt = mBluetoothDevice?.connectGatt(this, false, mBluetoothGattCallback)
        if (mBluetoothGatt == null) return false
        return true
    }

    private fun setCharacteristicNotification(serviceUUID: UUID, characteristicUUID: UUID): Boolean {
        val bluetoothGattService = mBluetoothGatt?.getService(serviceUUID)

        if (bluetoothGattService == null) return false

        val bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(characteristicUUID)

        if (bluetoothGattCharacteristic == null) return false

        val descriptors = bluetoothGattCharacteristic.descriptors

        if (descriptors == null) return false

        var result = mBluetoothGatt?.setCharacteristicNotification(bluetoothGattCharacteristic, true) ?: false

        descriptors.forEach {
            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            result = result && (mBluetoothGatt?.writeDescriptor(it) ?: false)
        }

        return result
    }

    private fun writeCharacteristic(serviceUUID: UUID, characteristicUUID: UUID, value: ByteArray): Boolean {
        val bluetoothGattService = mBluetoothGatt?.getService(serviceUUID)

        if (bluetoothGattService == null) return false

        val bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(characteristicUUID)

        if (bluetoothGattCharacteristic == null) return false

        bluetoothGattCharacteristic.value = value
        return mBluetoothGatt?.writeCharacteristic(bluetoothGattCharacteristic) ?: false
    }


    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    inner class LocalBinder : Binder() {

        fun getService(): BLEService {
            return this@BLEService
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
    }

}