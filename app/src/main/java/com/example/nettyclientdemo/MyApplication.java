package com.example.nettyclientdemo;

import android.app.Application;

/**
 * Created by lei.zhang on 2017/6/5.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
}
