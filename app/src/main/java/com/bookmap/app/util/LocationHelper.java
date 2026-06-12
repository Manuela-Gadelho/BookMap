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
        return prefs.getBoolean(KEY_LOCATION_VISIBLE, false);
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
            if (listener != null)
                listener.onLocationError("Permissao de localização não concedida");
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
                        Log.w(TAG, "Failed to get last location, trying fallback", e);
                        useLocationManagerFallback(listener);
                    });
        } catch (Exception e) {
            Log.w(TAG, "Exception getting location, trying fallback", e);
            useLocationManagerFallback(listener);
        }
    }

    public void requestLocationUpdates(LocationUpdateListener listener) {
        this.listener = listener;
        if (!hasLocationPermission()) {
            if (listener != null)
                listener.onLocationError("Permissao de localização não concedida");
            return;
        }
        try {
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
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.getMainLooper());
        } catch (Exception e) {
            Log.w(TAG, "Exception requesting location updates, trying fallback", e);
            useLocationManagerFallback(listener);
        }
    }

    
    public void startContinuousUpdates(LocationUpdateListener listener) {
        this.listener = listener;
        stopLocationUpdates(); 
        if (!hasLocationPermission()) {
            if (listener != null)
                listener.onLocationError("Permissao de localização não concedida");
            return;
        }
        try {
            LocationRequest locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000)
                    .setMinUpdateIntervalMillis(5000)
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
                }
            };
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.getMainLooper());
        } catch (Exception e) {
            Log.w(TAG, "Exception starting continuous updates", e);
            useLocationManagerFallback(listener);
        }
    }

    private void useLocationManagerFallback(LocationUpdateListener listener) {
        try {
            android.location.LocationManager locationManager = (android.location.LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                boolean isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
                boolean isNetworkEnabled = locationManager
                        .isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);

                android.location.Location location = null;
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(context,
                                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        location = locationManager
                                .getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
                    }
                }
                if (location == null && isGpsEnabled) {
                    if (ActivityCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        location = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
                    }
                }

                if (location != null) {
                    saveLastLocation(location.getLatitude(), location.getLongitude());
                    if (listener != null) {
                        listener.onLocationUpdated(location.getLatitude(), location.getLongitude());
                    }
                    return;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "LocationManager fallback failed", e);
        }

        if (listener != null) {
            listener.onLocationError("Usando localização aproximada");
        }
    }

    public void stopLocationUpdates() {
        if (locationCallback != null) {
            try {
                fusedLocationClient.removeLocationUpdates(locationCallback);
            } catch (Exception e) {
                Log.w(TAG, "Exception removing location updates", e);
            }
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

