package com.bookmap.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bookmap.app.adapter.UserAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.User;
import com.bookmap.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * MapActivity - shows nearby readers with filters.
 * Constraint 2: ViewNearby <<include>> ViewProfile - clicking a user opens their profile.
 * Uses a list-based view of nearby users (map integration requires Google Maps API key).
 */
public class MapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private DatabaseHelper dbHelper;
    private SessionManager session;
    private RecyclerView recyclerUsers;
    private UserAdapter userAdapter;
    private TextView tvDistance, tvNoUsers;
    private Spinner spinnerGenre, spinnerLanguage;
    private SeekBar seekDistance;
    private double currentLat = -3.1190; // Default Manaus
    private double currentLng = -60.0217;
    private int currentDistance = 50; // km

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        recyclerUsers = findViewById(R.id.recyclerUsers);
        tvDistance = findViewById(R.id.tvDistance);
        tvNoUsers = findViewById(R.id.tvNoUsers);
        spinnerGenre = findViewById(R.id.spinnerGenre);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        seekDistance = findViewById(R.id.seekDistance);

        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));

        // Genre filter
        String[] genres = {"Todos", "Fantasia", "Terror", "Romance", "Ficcao Cientifica",
                "Tecnologia", "Literatura Brasileira"};
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, genres);
        spinnerGenre.setAdapter(genreAdapter);

        // Language filter
        String[] languages = {"Todos", "Portugues", "English", "Espanol"};
        ArrayAdapter<String> langAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, languages);
        spinnerLanguage.setAdapter(langAdapter);

        // Distance seekbar
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
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                loadNearbyUsers();
            }
        });

        // Filter change listeners
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadNearbyUsers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        spinnerGenre.setOnItemSelectedListener(filterListener);
        spinnerLanguage.setOnItemSelectedListener(filterListener);

        // Bottom navigation
        setupBottomNav();

        // Request location permission
        requestLocationPermission();

        loadNearbyUsers();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Localizacao ativada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadNearbyUsers() {
        String genre = spinnerGenre.getSelectedItem().toString();
        String language = spinnerLanguage.getSelectedItem().toString();

        if ("Todos".equals(genre)) genre = null;
        if ("Todos".equals(language)) language = null;

        List<User> users = dbHelper.getNearbyUsers(currentLat, currentLng, currentDistance, genre, language);

        // Remove current user from list
        if (session.isLoggedIn()) {
            long currentUserId = session.getUserId();
            List<User> filtered = new ArrayList<>();
            for (User u : users) {
                if (u.getId() != currentUserId) {
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

        // Constraint 2: clicking a user opens their public profile
        userAdapter = new UserAdapter(users, user -> {
            Intent intent = new Intent(this, PublicProfileActivity.class);
            intent.putExtra(PublicProfileActivity.EXTRA_USER_ID, user.getId());
            startActivity(intent);
        }, false);
        recyclerUsers.setAdapter(userAdapter);
    }

    private void setupBottomNav() {
        TextView navShelf = findViewById(R.id.navShelf);
        TextView navMap = findViewById(R.id.navMap);
        TextView navClubs = findViewById(R.id.navClubs);
        TextView navProfile = findViewById(R.id.navProfile);

        navMap.setTextColor(getResources().getColor(R.color.blue_primary));

        navShelf.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        navClubs.setOnClickListener(v -> {
            startActivity(new Intent(this, ClubListActivity.class));
            finish();
        });
        navProfile.setOnClickListener(v -> {
            if (session.isLoggedIn()) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
    }
}
