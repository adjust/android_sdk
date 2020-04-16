package com.adjust.examples.keyboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
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
    protected void onStart() {
        super.onStart();

        if (!isKeyboardEnabled()) {
            final Context context = this;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.keyboard_not_enbabled);
            builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            builder.setCancelable(false);

            builder.create().show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean isKeyboardEnabled() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            for (final InputMethodInfo imi : imm.getEnabledInputMethodList()) {
                if (imi.getPackageName().equals(getPackageName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e("example", "Exception while checking if keyboard is enabled", e);
        }
        return false;
    }

    public void onTrackSimpleEventClick(View v) {
        AdjustEvent event = new AdjustEvent(EVENT_TOKEN_SIMPLE);

        // Assign custom identifier to event which will be reported in success/failure callbacks.
        event.setCallbackId("PrettyRandomIdentifier");

        Adjust.trackEvent(event);
    }
}
