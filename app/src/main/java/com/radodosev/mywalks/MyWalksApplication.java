package com.radodosev.mywalks;

import android.app.Application;
import android.content.Context;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.squareup.leakcanary.LeakCanary;

import timber.log.Timber;

/**
 * Created by Rado on 7/8/2017.
 */

public class MyWalksApplication extends Application {
    // create reference to the application context, to available later at any time
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        MyWalksApplication.appContext = this;

        initDBFlow();
        initLeakCanary();
        initTimber();
    }

    private void initTimber() {
        Timber.plant(new Timber.DebugTree());
    }

    private void initLeakCanary() {
        if (BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return;
            }

            LeakCanary.install(this);
        }
    }

    private void initDBFlow() {
        FlowManager.init(this);
    }

    public static Context get(){
        return appContext;
    }
}
