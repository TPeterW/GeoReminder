package com.peter.georeminder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import static android.view.View.*;

/**
 * Created by Peter on 10/11/15.
 * After launch, this screen will appear for 1.5 seconds, then head off to MainScreen and destroy itself
 */
public class SplashScreen extends Activity {
    private static int SPLASH_TIME_OUT = 2000;

    private ImageView splashLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION; //TODO: try remove the word "layout"
        decorView.setSystemUiVisibility(uiOptions);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        splashLogo = (ImageView) findViewById(R.id.splash_img);

        splashLogo.getLayoutParams().width = (int)(metrics.widthPixels * 0.5);
        splashLogo.getLayoutParams().height = (int)(metrics.heightPixels * 0.4);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                splashLogo.setVisibility(VISIBLE);
            }
        }, 500);

        YoYo.with(Techniques.FadeInUp)
                .delay(200)
                .duration(1300)
                .playOn(splashLogo);

        new Handler().postDelayed(new Runnable() {
            // Showing splash screen with a timer.
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreen.this, MainScreen.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
