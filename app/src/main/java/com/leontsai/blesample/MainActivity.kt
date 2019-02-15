package com.leontsai.blesample

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mService: BLEService

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BLEService.LocalBinder
            mService = binder.getService()
            mService.initBle(this@MainActivity)
            mService.scanDevice(true)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Toast.makeText(this@MainActivity, "onServiceDisconnected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bleOpenDoor.setOnClickListener {
            val intent = Intent(this@MainActivity, BLEService::class.java)
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        }
    }
}
