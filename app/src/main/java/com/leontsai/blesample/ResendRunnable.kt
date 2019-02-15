package com.leontsai.blesample

import java.util.*

class ResendRunnable(var mResendPacket: ByteArray) : Runnable {

    @Volatile
    var mIsSent: Boolean = false

    override fun run() {
        if (!mIsSent) {
            mIsSent = true
            BLEHelper.INSTANCE.writeCharacteristic(
                UUID.fromString(GATTATTRIBUTES_SERVICE_OPEN_DOOR)
                , UUID.fromString(GATTATTRIBUTES_CHARACTERISTIC_SEND_COMMAND)
                , mResendPacket
            )
        }
    }
}