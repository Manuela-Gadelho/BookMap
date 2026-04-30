package com.bookmap.app;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

/**
 * Application class for BookMap.
 * Initializes Firebase and global configuration.
 */
public class BookMapApplication extends Application {

    private static final String TAG = "BookMapApp";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.w(TAG, "Firebase initialization failed - running in offline mode", e);
        }
    }
}
