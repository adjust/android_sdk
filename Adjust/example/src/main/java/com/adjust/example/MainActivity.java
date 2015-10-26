package com.adjust.example;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustEvent;

public class MainActivity extends ActionBarActivity {
    private static final String EVENT_TOKEN_SIMPLE      = "{YourEventToken}";
    private static final String EVENT_TOKEN_REVENUE     = "{YourEventToken}";
    private static final String EVENT_TOKEN_CALLBACK    = "{YourEventToken}";
    private static final String EVENT_TOKEN_PARTNER     = "{YourEventToken}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Uri data = intent.getData();
        Adjust.appWillOpenUrl(data);

        // Adjust UI according to SDK state.
        Button btnEnableDisableSDK = (Button)findViewById(R.id.btnEnableDisableSDK);

        if (Adjust.isEnabled()) {
            btnEnableDisableSDK.setText(R.string.txt_disable_sdk);
        } else {
            btnEnableDisableSDK.setText(R.string.txt_enable_sdk);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Adjust.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Adjust.onPause();
    }

    public void onTrackSimpleEventClick(View v) {
        AdjustEvent event = new AdjustEvent(EVENT_TOKEN_SIMPLE);

        Adjust.trackEvent(event);
    }

    public void onTrackRevenueEventClick(View v) {
        AdjustEvent event = new AdjustEvent(EVENT_TOKEN_REVENUE);

        // Add revenue 1 cent of an euro.
        event.setRevenue(0.01, "EUR");

        Adjust.trackEvent(event);
    }

    public void onTrackCallbackEventClick(View v) {
        AdjustEvent event = new AdjustEvent(EVENT_TOKEN_CALLBACK);

        // Add callback parameters to this parameter.
        event.addCallbackParameter("key", "value");

        Adjust.trackEvent(event);
    }

    public void onTrackPartnerEventClick(View v) {
        AdjustEvent event = new AdjustEvent(EVENT_TOKEN_PARTNER);

        // Add partner parameters to this parameter.
        event.addPartnerParameter("foo", "bar");

        Adjust.trackEvent(event);
    }

    public void onEnableDisableOfflineModeClick(View v) {
        if (((Button)v).getText().equals(
                getApplicationContext().getResources().getString(R.string.txt_enable_offline_mode))) {
            Adjust.setOfflineMode(true);
            ((Button)v).setText(R.string.txt_disable_offline_mode);
        } else {
            Adjust.setOfflineMode(false);
            ((Button)v).setText(R.string.txt_enable_offline_mode);
        }
    }

    public void onEnableDisableSDKClick(View v) {
        if (Adjust.isEnabled()) {
            Adjust.setEnabled(false);
            ((Button)v).setText(R.string.txt_enable_sdk);
        } else {
            Adjust.setEnabled(true);
            ((Button)v).setText(R.string.txt_disable_sdk);
        }
    }

    public void onIsSDKEnabledClick(View v) {
        if (Adjust.isEnabled()) {
            Toast.makeText(getApplicationContext(), R.string.txt_sdk_is_enabled,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.txt_sdk_is_disabled,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
