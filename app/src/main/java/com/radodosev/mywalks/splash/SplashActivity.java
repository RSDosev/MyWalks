package com.radodosev.mywalks.splash;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.radodosev.mywalks.walktracking.WalkTrackingActivity;

/**
 * Created by blue on 13.7.2017 г.
 * For aesthetics only
 */
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed(() -> {
            startActivity(WalkTrackingActivity.getIntent(SplashActivity.this));
            finish();
        }, 1_000);
    }
}
