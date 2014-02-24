package com.adjust.sdk.test;

import java.util.Map;

import com.adjust.sdk.test.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;


public class UnitTestActivity extends Activity {

    public UnitTestActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Adjust.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Adjust.onPause();
    }

    protected void trackEvent(String eventToken) {
        //Adjust.trackEvent(eventToken);
    }

    protected void trackEvent(String eventToken, Map<String,String> parameters) {
        //Adjust.trackEvent(eventToken, parameters);
    }
}
