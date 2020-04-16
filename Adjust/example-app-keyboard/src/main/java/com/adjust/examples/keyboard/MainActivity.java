package com.adjust.examples.keyboard;

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

public class MainActivity extends AppCompatActivity {
    private static final String EVENT_TOKEN_SIMPLE = "g3mfiw";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Uri data = intent.getData();
        Adjust.appWillOpenUrl(data, getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onTrackSimpleEventClick(View v) {
        AdjustEvent event = new AdjustEvent(EVENT_TOKEN_SIMPLE);

        // Assign custom identifier to event which will be reported in success/failure callbacks.
        event.setCallbackId("PrettyRandomIdentifier");

        Adjust.trackEvent(event);
    }
}
