package com.adjust.examples;

import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustDeeplink;

public class ServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        Intent intent = getIntent();
        Uri data = intent.getData();
        AdjustDeeplink adjustDeeplink = new AdjustDeeplink(data);
        Adjust.processDeeplink(adjustDeeplink, getApplicationContext());
    }

    public void onServiceClick(View v) {
        Intent intent = new Intent(this, ServiceExample.class);
        startService(intent);
    }

    public void onReturnClick(View v) {
        finish();
    }
}
