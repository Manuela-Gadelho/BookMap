package com.bookmap.app.util;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * Helper class for managing geolocation using FusedLocationProviderClient.
 * Captures user's latitude/longitude for the Literary Map feature.
 * Includes privacy controls for location visibility.
 */
public class LocationHelper {

    private static final String TAG = "LocationHelper";
    private static final String PREFS_LOCATION = "BookMapLocation";
    private static final String KEY_LOCATION_VISIBLE = "locationVisible";
    private static final String KEY_LAST_LAT = "lastLatitude";
    private static final String KEY_LAST_LNG = "lastLongitude";

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private final SharedPreferences prefs;
    private LocationCallback locationCallback;
    private LocationUpdateListener listener;

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.prefs = context.getSharedPreferences(PREFS_LOCATION, Context.MODE_PRIVATE);
    }

    public boolean isLocationVisible() {
        return prefs.getBoolean(KEY_LOCATION_VISIBLE, true);
    }

    public void setLocationVisible(boolean visible) {
        prefs.edit().putBoolean(KEY_LOCATION_VISIBLE, visible).apply();
    }

    public double getLastLatitude() {
        return Double.longBitsToDouble(prefs.getLong(KEY_LAST_LAT, Double.doubleToLongBits(0.0)));
    }

    public double getLastLongitude() {
        return Double.longBitsToDouble(prefs.getLong(KEY_LAST_LNG, Double.doubleToLongBits(0.0)));
    }

    private void saveLastLocation(double lat, double lng) {
        prefs.edit()
                .putLong(KEY_LAST_LAT, Double.doubleToRawLongBits(lat))
                .putLong(KEY_LAST_LNG, Double.doubleToRawLongBits(lng))
                .apply();
    }

    public boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLastLocation(LocationUpdateListener listener) {
        this.listener = listener;

        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted");
            if (listener != null) listener.onLocationError("Permissao de localizacao nao concedida");
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            saveLastLocation(location.getLatitude(), location.getLongitude());
                            if (listener != null) {
                                listener.onLocationUpdated(location.getLatitude(), location.getLongitude());
                            }
                        } else {
                            requestLocationUpdates(listener);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Failed to get last location", e);
                        if (listener != null) listener.onLocationError("Erro ao obter localizacao");
                    });
        } catch (SecurityException e) {
            Log.w(TAG, "Security exception getting location", e);
            if (listener != null) listener.onLocationError("Permissao negada");
        }
    }

    public void requestLocationUpdates(LocationUpdateListener listener) {
        this.listener = listener;

        if (!hasLocationPermission()) {
            if (listener != null) listener.onLocationError("Permissao de localizacao nao concedida");
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, 30000)
                .setMinUpdateIntervalMillis(15000)
                .setMaxUpdates(1)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    saveLastLocation(location.getLatitude(), location.getLongitude());
                    if (listener != null) {
                        listener.onLocationUpdated(location.getLatitude(), location.getLongitude());
                    }
                }
                stopLocationUpdates();
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.w(TAG, "Security exception requesting location updates", e);
            if (listener != null) listener.onLocationError("Permissao negada");
        }
    }

    public void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }

    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    public interface LocationUpdateListener {
        void onLocationUpdated(double latitude, double longitude);
        void onLocationError(String error);
    }
}
