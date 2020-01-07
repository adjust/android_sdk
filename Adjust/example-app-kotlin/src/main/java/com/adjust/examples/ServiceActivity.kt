package com.adjust.examples

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View

import com.adjust.sdk.Adjust

class ServiceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        val intent = intent
        val data = intent.data
        Adjust.appWillOpenUrl(data, applicationContext)
    }

    fun onServiceClick(v: View) {
        val intent = Intent(this, ServiceExample::class.java)
        startService(intent)
    }

    fun onReturnClick(v: View) {
        finish()
    }
}