package com.leontsai.blesample

import android.content.ComponentName
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var mService: BLEService

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BLEService.LocalBinder
            mService=binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Toast.makeText(this@MainActivity,"onServiceDisconnected",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
