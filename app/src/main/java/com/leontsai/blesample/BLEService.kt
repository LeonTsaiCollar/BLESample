package com.leontsai.blesample

import android.app.Service
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
        val WRITE_SUCCESS = 804
        val RECEIVE_DEVICE_MSG = 805
        val DESCRIPTION_WRITE = 806
    }

    private val mBinder = LocalBinder()
    @Volatile
    private var mBluetoothDeviceFound = false

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
                WRITE_SUCCESS -> {
                    if (!BLEHelper.INSTANCE.mResendRunnable.mIsSent) {
                        BLEHelper.INSTANCE.mResendRunnable.mIsSent = true
                        mHandler.removeCallbacks(BLEHelper.INSTANCE.mResendRunnable)
                    }
                }

                RECEIVE_DEVICE_MSG -> {
                    //TODO("根据硬件返回的结果判断此次开门是否成功")
                    stopSelf()
                }

                else -> {

                }
            }
            return@Handler true
        }
    }


    fun initBle(context: Context): Boolean {
        return BLEHelper.initBLE(context, mHandler)
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
        BLEHelper.INSTANCE.closeGatt()
        mHandler.removeCallbacksAndMessages(null)
    }

}