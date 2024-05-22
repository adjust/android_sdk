package com.adjust.examples

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast

import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.OnIsEnabledListener

class MainActivity : AppCompatActivity() {

    private var btnEnableDisableSDK: Button? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = intent
        val data = intent.data
        Adjust.appWillOpenUrl(data, applicationContext)

        // Adjust UI according to SDK state.
        btnEnableDisableSDK = findViewById<View>(R.id.btnEnableDisableSDK) as Button
    }

    public override fun onResume() {
        super.onResume()

        Adjust.isEnabled(this, OnIsEnabledListener {
            if (it) {
                btnEnableDisableSDK!!.setText(R.string.txt_disable_sdk)
            } else {
                btnEnableDisableSDK!!.setText(R.string.txt_enable_sdk)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }

    fun onTrackSimpleEventClick(v: View) {
        val event = AdjustEvent(EVENT_TOKEN_SIMPLE)

        // Assign custom identifier to event which will be reported in success/failure callbacks.
        event.setCallbackId("PrettyRandomIdentifier")

        Adjust.trackEvent(event)
    }

    fun onTrackRevenueEventClick(v: View) {
        val event = AdjustEvent(EVENT_TOKEN_REVENUE)

        // Add revenue 1 cent of an euro.
        event.setRevenue(0.01, "EUR")

        Adjust.trackEvent(event)
    }

    fun onTrackCallbackEventClick(v: View) {
        val event = AdjustEvent(EVENT_TOKEN_CALLBACK)

        // Add callback parameters to this parameter.
        event.addCallbackParameter("key", "value")

        Adjust.trackEvent(event)
    }

    fun onTrackPartnerEventClick(v: View) {
        val event = AdjustEvent(EVENT_TOKEN_PARTNER)

        // Add partner parameters to this parameter.
        event.addPartnerParameter("foo", "bar")

        Adjust.trackEvent(event)
    }

    fun onEnableDisableOfflineModeClick(v: View) {
        if ((v as Button).text == applicationContext.resources.getString(R.string.txt_enable_offline_mode)) {
            Adjust.setOfflineMode(true)
            v.setText(R.string.txt_disable_offline_mode)
        } else {
            Adjust.setOfflineMode(false)
            v.setText(R.string.txt_enable_offline_mode)
        }
    }

    fun onEnableDisableSDKClick(v: View) {
        Adjust.isEnabled(this, OnIsEnabledListener {
            if (it) {
                Adjust.setEnabled(false)
                (v as Button).setText(R.string.txt_enable_sdk)
            } else {
                Adjust.setEnabled(true)
                (v as Button).setText(R.string.txt_disable_sdk)
            }
        })
    }

    fun onIsSDKEnabledClick(v: View) {
        Adjust.isEnabled(this, OnIsEnabledListener {
            if (it) {
                Toast.makeText(applicationContext, R.string.txt_sdk_is_enabled,
                        Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, R.string.txt_sdk_is_disabled,
                        Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun onFireIntentClick(v: View) {
        val intent = Intent("com.android.vending.INSTALL_REFERRER")
        intent.setPackage("com.adjust.examples")
        intent.putExtra("referrer", "utm_source=test&utm_medium=test&utm_term=test&utm_content=test&utm_campaign=test")
        sendBroadcast(intent)
    }

    fun onServiceActivityClick(v: View) {
        val intent = Intent(this, ServiceActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private val EVENT_TOKEN_SIMPLE = "g3mfiw"
        private val EVENT_TOKEN_REVENUE = "a4fd35"
        private val EVENT_TOKEN_CALLBACK = "34vgg9"
        private val EVENT_TOKEN_PARTNER = "w788qs"
    }
}