package com.leontsai.blesample

import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Message
import java.util.*

class BLEHelper private constructor() {

    private var mContext: Context? = null
    lateinit var mHandler: Handler

    lateinit var mResendRunnable: ResendRunnable

    companion object {

        val INSTANCE: BLEHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            BLEHelper()
        }

        fun initBLE(context: Context, handler: Handler): Boolean {
            INSTANCE.mContext = context
            INSTANCE.mHandler = handler
            return INSTANCE.initBleInternal()
        }
    }

    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothLeScanner: BluetoothLeScanner? = null
    private var mScanCallback: ScanCallback? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mBluetoothDevice: BluetoothDevice? = null
    @Volatile
    private var mBluetoothDeviceFound = false
    @Volatile
    private var mDeviceConnected = false

    private val mBluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            if (mDeviceConnected) return

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mDeviceConnected = true
                mBluetoothGatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mDeviceConnected = false
                mHandler.post { connect() }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            mHandler.sendEmptyMessage(BLEService.DISCOVERY)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            mHandler.sendEmptyMessage(BLEService.WRITE_SUCCESS)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            val msg = Message.obtain()
            msg.what = BLEService.RECEIVE_DEVICE_MSG
            msg.obj = characteristic?.value
            mHandler.sendMessage(msg)
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            mHandler.sendEmptyMessage(BLEService.DESCRIPTION_WRITE)
        }
    }

    fun initBleInternal(): Boolean {
        if (mBluetoothManager == null) {
            mBluetoothManager = mContext?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
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
                        mHandler.sendEmptyMessage(BLEService.FOUND_DEVICE)
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
        mBluetoothGatt = mBluetoothDevice?.connectGatt(mContext, false, mBluetoothGattCallback)
        if (mBluetoothGatt == null) return false
        return true
    }

    fun setCharacteristicNotification(serviceUUID: UUID, characteristicUUID: UUID): Boolean {
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

    fun writeCharacteristic(serviceUUID: UUID, characteristicUUID: UUID, value: ByteArray): Boolean {
        val bluetoothGattService = mBluetoothGatt?.getService(serviceUUID)

        if (bluetoothGattService == null) return false

        val bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(characteristicUUID)

        if (bluetoothGattCharacteristic == null) return false

        bluetoothGattCharacteristic.value = value
        return mBluetoothGatt?.writeCharacteristic(bluetoothGattCharacteristic) ?: false
    }


    fun sendCommand() {
        val command = byteArrayOf(0xd4.toByte(), 0xa4.toByte())
        mResendRunnable = ResendRunnable(command)
        writeCharacteristic(
            UUID.fromString(GATTATTRIBUTES_SERVICE_OPEN_DOOR)
            , UUID.fromString(GATTATTRIBUTES_CHARACTERISTIC_SEND_COMMAND), command
        )
    }

    fun closeGatt() {
        if (mBluetoothGatt == null) return
        mBluetoothDevice = null
        mBluetoothGatt?.disconnect()
        mBluetoothGatt?.close()
        mBluetoothGatt = null
    }
}