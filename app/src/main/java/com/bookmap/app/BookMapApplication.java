package com.bookmap.app;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.FirebaseApp;

/**
 * Application class for BookMap.
 * Initializes Firebase and global configuration.
 * Includes a global exception handler to prevent permanent crashes.
 */
public class BookMapApplication extends Application {

    private static final String TAG = "BookMapApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Set up global exception handler to prevent "cached crash" loops
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Uncaught exception", throwable);
            try {
                // Clear any bad state by relaunching the app at LoginActivity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to restart app after crash", e);
            }
            // Let the default handler finish the process
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
