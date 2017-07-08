package com.radodosev.mywalks;

import android.app.Application;
import android.content.Context;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Rado on 7/8/2017.
 */

public class MyWalksApplication extends Application {
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        MyWalksApplication.appContext = this;

        initDBFlow();
        initLeakCanary();
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
