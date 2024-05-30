package com.adjust.examples;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.OnIsEnabledListener;

public class MainActivity extends AppCompatActivity {
    private static final String EVENT_TOKEN_SIMPLE = "g3mfiw";
    private static final String EVENT_TOKEN_REVENUE = "a4fd35";
    private static final String EVENT_TOKEN_CALLBACK = "34vgg9";
    private static final String EVENT_TOKEN_PARTNER = "w788qs";

    private Button btnEnableDisableSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Uri data = intent.getData();
        Adjust.processDeeplink(data, getApplicationContext());

        // Adjust UI according to SDK state.
        btnEnableDisableSDK = (Button) findViewById(R.id.btnEnableDisableSDK);
    }

    @Override
    public void onResume() {
        super.onResume();

        Adjust.isEnabled(this, new OnIsEnabledListener() {
            @Override
            public void onIsEnabledRead(boolean isEnabled) {
                if (isEnabled) {
                    btnEnableDisableSDK.setText(R.string.txt_disable_sdk);
                } else {
                    btnEnableDisableSDK.setText(R.string.txt_enable_sdk);
                }
            }
        });

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
        if (((Button) v).getText().equals(
                getApplicationContext().getResources().getString(R.string.txt_enable_offline_mode))) {
            Adjust.switchToOfflineMode();
            ((Button) v).setText(R.string.txt_disable_offline_mode);
        } else {
            Adjust.switchBackToOnlineMode();
            ((Button) v).setText(R.string.txt_enable_offline_mode);
        }
    }

    public void onEnableDisableSDKClick(View v) {
        Adjust.isEnabled(this, new OnIsEnabledListener() {
            @Override
            public void onIsEnabledRead(boolean isEnabled) {
                if (isEnabled) {
                    Adjust.setEnabled(false);
                    ((Button) v).setText(R.string.txt_enable_sdk);
                } else {
                    Adjust.setEnabled(true);
                    ((Button) v).setText(R.string.txt_disable_sdk);
                }
            }
        });

    }

    public void onIsSDKEnabledClick(View v) {

        Adjust.isEnabled(this, new OnIsEnabledListener() {
            @Override
            public void onIsEnabledRead(boolean isEnabled) {
                if (isEnabled) {
                    Toast.makeText(getApplicationContext(), R.string.txt_sdk_is_enabled,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.txt_sdk_is_disabled,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void onFireIntentClick(View v) {
        Intent intent = new Intent("com.android.vending.INSTALL_REFERRER");
        intent.setPackage("com.adjust.examples");
        intent.putExtra("referrer", "utm_source=test&utm_medium=test&utm_term=test&utm_content=test&utm_campaign=test");
        sendBroadcast(intent);
    }

    public void onServiceActivityClick(View v) {
        Intent intent = new Intent(this, ServiceActivity.class);
        startActivity(intent);
    }
}
