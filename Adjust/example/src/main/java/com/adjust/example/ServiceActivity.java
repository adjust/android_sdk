package com.adjust.example;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class ServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
    }

    public void onServiceClick(View v) {
        Intent intent = new Intent(this, ServiceExample.class);
        startService(intent);
    }

    public void onReturnClick(View v) {
        finish();
    }
}
