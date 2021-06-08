package com.alight.android.aoa_launcher.application;

import android.app.Application;
import android.content.Context;

public class Myapplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }
}
