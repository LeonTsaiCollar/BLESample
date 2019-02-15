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
                    mHandler.postDelayed({ connect() }, 200)
                }
                DISCOVERY -> {
                    setCharacteristicNotification(
                        UUID.fromString(GATTATTRIBUTES_SERVICE_OPEN_DOOR)
                        , UUID.fromString(GATTATTRIBUTES_CHARACTERISTIC_RETURN_COMMAND)
                    )
                }
                DESCRIPTION_WRITE -> {
                    BLEHelper.INSTANCE.sendCommand()
                    mHandler.postDelayed(BLEHelper.INSTANCE.mResendRunnable, 500)
                }
                WRITE -> {
                    if (!BLEHelper.INSTANCE.mResendRunnable.mIsSent) {
                        BLEHelper.INSTANCE.mResendRunnable.mIsSent = true
                        mHandler.removeCallbacks(BLEHelper.INSTANCE.mResendRunnable)
                    }
                }


                else -> {

                }
            }
            return@Handler true
        }
    }


    fun initBle(context: Context): Boolean {
        return BLEHelper.initBLE(context)
    }

    fun scanDevice(enable: Boolean) {
        BLEHelper.INSTANCE.scanDevice(enable)
    }

    fun connect(): Boolean {
        return BLEHelper.INSTANCE.connect()
    }

    private fun setCharacteristicNotification(serviceUUID: UUID, characteristicUUID: UUID): Boolean {
        return BLEHelper.INSTANCE.setCharacteristicNotification(serviceUUID, characteristicUUID)
    }

    private fun writeCharacteristic(serviceUUID: UUID, characteristicUUID: UUID, value: ByteArray): Boolean {
        return BLEHelper.INSTANCE.writeCharacteristic(serviceUUID, characteristicUUID, value)
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