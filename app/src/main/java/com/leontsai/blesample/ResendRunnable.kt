package com.leontsai.blesample

class ResendRunnable(var mResendPacket: ByteArray) : Runnable {

    private var mIsSent: Boolean = false

    override fun run() {
        if (!mIsSent) {
            mIsSent = true
        }
    }
}