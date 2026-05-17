package com.bookmap.app;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import com.google.firebase.FirebaseApp;

public class BookMapApplication extends Application {
    private static final String TAG = "BookMapApp";

    @Override
    public void onCreate() {
        super.onCreate();
        
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                String stackTrace = android.util.Log.getStackTraceString(throwable);
                getSharedPreferences("CrashLog", MODE_PRIVATE)
                    .edit()
                    .putString("last_crash", stackTrace)
                    .commit(); 
            } catch (Exception e) {
                Log.e(TAG, "Failed to save crash log", e);
            }
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        });

        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.w(TAG, "Firebase initialization failed - running in offline mode", e);
        }
    }
}
