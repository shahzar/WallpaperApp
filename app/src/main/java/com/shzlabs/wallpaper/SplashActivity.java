package com.shzlabs.wallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by shaz on 3/9/16.
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start Main Activity
        Log.d(TAG, "onCreate: Starting MainActivity");
        startActivity(new Intent(SplashActivity.this, MainActivity.class));

        // Close this activity
        Log.d(TAG, "onCreate: Finishing Splash activity...");
        finish();
    }
}
