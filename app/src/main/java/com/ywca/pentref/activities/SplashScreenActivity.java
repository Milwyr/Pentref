package com.ywca.pentref.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ywca.pentref.R;
import com.ywca.pentref.common.Utility;

/**
 * This Activity is the first Activity to be displayed to the user.
 */
public class SplashScreenActivity extends AppCompatActivity {
    private boolean mIsFirstTimeInstalled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Customise status bar colour
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        // Read the flag from shared preferences in background thread
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.pref_file_name_local), MODE_PRIVATE);
                mIsFirstTimeInstalled = sharedPreferences.getBoolean(
                        Utility.PREF_KEY_IS_FIRST_TIME_INSTALLED, true);
                return null;
            }
        }.execute();

        // Navigate to LaunchingActivity after the specified time delay
        final int DISPLAY_LENGTH = 1500; // milliseconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, ChoosePageActivity.class);
                intent.putExtra(Utility.PREF_KEY_IS_FIRST_TIME_INSTALLED, mIsFirstTimeInstalled);
                startActivity(intent);
                finish();
            }
        }, DISPLAY_LENGTH);
    }
}