package com.adeven.adjustio.test;

import java.util.Map;

import com.adeven.adjustio.AdjustIo;

import android.os.Bundle;
import android.app.Activity;
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
        //AdjustIo.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //AdjustIo.onPause();
    }

    protected void trackEvent(String eventToken) {
        //AdjustIo.trackEvent(eventToken);
    }

    protected void trackEvent(String eventToken, Map<String,String> parameters) {
        //AdjustIo.trackEvent(eventToken, parameters);
    }
}
