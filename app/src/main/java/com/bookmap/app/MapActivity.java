package com.bookmap.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.adapter.UserAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.database.FirebaseSyncHelper;
import com.bookmap.app.model.User;
import com.bookmap.app.util.LocationHelper;
import com.bookmap.app.util.SessionManager;
import java.util.ArrayList;
import java.util.List;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private LocationHelper locationHelper;
    private RecyclerView recyclerUsers;
    private UserAdapter userAdapter;
    private TextView tvDistance, tvNoUsers, tvLocationStatus;
    private TextView tvGenreSelection;
    private String selectedGenre = "Todos";
    private SeekBar seekDistance;
    private SwitchCompat switchLocationVisible;
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    private int currentDistance = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        locationHelper = new LocationHelper(this);
        recyclerUsers = findViewById(R.id.recyclerUsers);
        tvDistance = findViewById(R.id.tvDistance);
        tvNoUsers = findViewById(R.id.tvNoUsers);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);
        seekDistance = findViewById(R.id.seekDistance);
        switchLocationVisible = findViewById(R.id.switchLocationVisible);
        tvGenreSelection = findViewById(R.id.tvGenreSelection);
        
        List<String> genreList = new java.util.ArrayList<>();
        genreList.add("Todos");
        genreList.addAll(com.bookmap.app.util.GenreUtil.getGenres());
        String[] genres = genreList.toArray(new String[0]);
        
        tvGenreSelection.setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Filtrar por Gênero");
            
            int checkedItem = 0;
            for (int i = 0; i < genres.length; i++) {
                if (genres[i].equalsIgnoreCase(selectedGenre)) {
                    checkedItem = i;
                    break;
                }
            }
            
            builder.setSingleChoiceItems(genres, checkedItem, (dialog, which) -> {
                selectedGenre = genres[which];
                tvGenreSelection.setText(selectedGenre);
                loadNearbyUsers();
                dialog.dismiss();
            });
            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });
        
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        
        seekDistance.setMax(100);
        seekDistance.setProgress(currentDistance);
        tvDistance.setText(currentDistance + " km");
        seekDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentDistance = Math.max(1, progress);
                tvDistance.setText(currentDistance + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                loadNearbyUsers();
            }
        });
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadNearbyUsers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        switchLocationVisible.setChecked(locationHelper.isLocationVisible());
        switchLocationVisible.setOnCheckedChangeListener((buttonView, isChecked) -> {
            locationHelper.setLocationVisible(isChecked);
            if (isChecked) {
                tvLocationStatus.setText("Sua localização está visível para outros leitores");
                updateUserLocationInDb();
            } else {
                tvLocationStatus.setText("Sua localização está oculta");
                clearUserLocationInDb();
            }
        });
        setupBottomNav();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        requestLocationAndLoad();
        try {
            FirebaseSyncHelper.getInstance(this).pullUsersFromCloud(success -> {
                if (success) {
                    runOnUiThread(this::loadNearbyUsers);
                }
            });
        } catch (Exception e) {
            Log.w("MapActivity", "Could not start user cloud pull", e);
        }
    }

    private void requestLocationAndLoad() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                tvLocationStatus.setText("Permissão de localização negada");
                loadNearbyUsers();
            }
        }
    }

    private void getCurrentLocation() {
        locationHelper.requestLastLocation(new LocationHelper.LocationUpdateListener() {
            @Override
            public void onLocationUpdated(double latitude, double longitude) {
                currentLat = latitude;
                currentLng = longitude;
                tvLocationStatus
                        .setText(String.format(java.util.Locale.US, "Localização: %.4f, %.4f", latitude, longitude));
                updateUserLocationInDb();
                loadNearbyUsers();
            }

            @Override
            public void onLocationError(String error) {
                tvLocationStatus.setText("Usando localização aproximada");
                currentLat = locationHelper.getLastLatitude();
                currentLng = locationHelper.getLastLongitude();
                if (currentLat == 0.0 && currentLng == 0.0) {
                    currentLat = -23.4626;
                    currentLng = -46.5322;
                }
                loadNearbyUsers();
            }
        });
    }

    private void updateUserLocationInDb() {
        if (session.isLoggedIn() && locationHelper.isLocationVisible()) {
            User user = dbHelper.getUserById(session.getUserId());
            if (user != null) {
                user.setLatitude(currentLat);
                user.setLongitude(currentLng);
                dbHelper.updateUser(user);
                FirebaseSyncHelper syncHelper = FirebaseSyncHelper.getInstance(this);
                syncHelper.updateUserLocationInCloud(session.getUserId(), currentLat, currentLng);
            }
        }
    }

    private void clearUserLocationInDb() {
        if (session.isLoggedIn()) {
            User user = dbHelper.getUserById(session.getUserId());
            if (user != null) {
                user.setLatitude(0.0);
                user.setLongitude(0.0);
                dbHelper.updateUser(user);
                FirebaseSyncHelper syncHelper = FirebaseSyncHelper.getInstance(this);
                syncHelper.updateUserLocationInCloud(session.getUserId(), 0.0, 0.0);
            }
        }
    }

    private void loadNearbyUsers() {
        String genre = selectedGenre;
        if ("Todos".equalsIgnoreCase(genre))
            genre = null;
        List<User> users = dbHelper.getNearbyUsers(currentLat, currentLng, currentDistance, genre, null);
        if (session.isLoggedIn()) {
            long currentUserId = session.getUserId();
            List<User> filtered = new ArrayList<>();
            for (User u : users) {
                if (u.getId() != currentUserId && (u.getLatitude() != 0.0 || u.getLongitude() != 0.0)) {
                    filtered.add(u);
                }
            }
            users = filtered;
        }
        if (users.isEmpty()) {
            tvNoUsers.setVisibility(View.VISIBLE);
            recyclerUsers.setVisibility(View.GONE);
        } else {
            tvNoUsers.setVisibility(View.GONE);
            recyclerUsers.setVisibility(View.VISIBLE);
        }
        userAdapter = new UserAdapter(users, user -> {
            try {
                Intent intent = new Intent(this, PublicProfileActivity.class);
                intent.putExtra(PublicProfileActivity.EXTRA_USER_ID, user.getId());
                startActivity(intent);
            } catch (Exception e) {
                Log.e("MapActivity", "Error opening PublicProfile", e);
            }
        }, false);
        recyclerUsers.setAdapter(userAdapter);
        updateMapMarkers(users);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loadNearbyUsers(); // initial update if location was already ready
    }

    private void updateMapMarkers(List<User> users) {
        if (mMap == null)
            return;
        mMap.clear();

        if (currentLat != 0.0 && currentLng != 0.0) {
            LatLng myLocation = new LatLng(currentLat, currentLng);
            mMap.addMarker(new MarkerOptions().position(myLocation).title("Você"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 12f));
        }

        if (users != null) {
            for (User u : users) {
                if (u.getLatitude() != 0.0 && u.getLongitude() != 0.0) {
                    LatLng pos = new LatLng(u.getLatitude(), u.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(pos).title(u.getName()));
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationHelper != null) {
            locationHelper.stopLocationUpdates();
        }
    }

    private void setupBottomNav() {
        TextView navShelf = findViewById(R.id.navShelf);
        TextView navMap = findViewById(R.id.navMap);
        TextView navFeed = findViewById(R.id.navFeed);
        TextView navClubs = findViewById(R.id.navClubs);
        TextView navProfile = findViewById(R.id.navProfile);
        navMap.setTextColor(getResources().getColor(R.color.blue_primary));
        navMap.setTypeface(null, android.graphics.Typeface.BOLD);
        navShelf.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            } catch (Exception e) {
                Log.e("MapActivity", "Error navigating to HomeActivity", e);
            }
        });
        navFeed.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, FeedActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            } catch (Exception e) {
                Log.e("MapActivity", "Error navigating to FeedActivity", e);
            }
        });
        navClubs.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, ClubListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            } catch (Exception e) {
                Log.e("MapActivity", "Error navigating to ClubListActivity", e);
            }
        });
        navProfile.setOnClickListener(v -> {
            try {
                if (session.isLoggedIn()) {
                    startActivity(new Intent(this, ProfileActivity.class));
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }
            } catch (Exception e) {
                Log.e("MapActivity", "Error navigating to ProfileActivity", e);
            }
        });
    }
}
