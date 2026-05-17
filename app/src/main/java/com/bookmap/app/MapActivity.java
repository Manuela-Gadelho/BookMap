package com.bookmap.app;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
public class MapActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private LocationHelper locationHelper;
    private RecyclerView recyclerUsers;
    private UserAdapter userAdapter;
    private TextView tvDistance, tvNoUsers, tvLocationStatus;
    private Spinner spinnerGenre, spinnerLanguage;
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
        spinnerGenre = findViewById(R.id.spinnerGenre);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        seekDistance = findViewById(R.id.seekDistance);
        switchLocationVisible = findViewById(R.id.switchLocationVisible);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        String[] genres = { "Todos", "Fantasia", "Terror", "Romance", "Ficcao Cientifica",
                "Tecnologia", "Literatura Brasileira" };
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, genres);
        spinnerGenre.setAdapter(genreAdapter);
        String[] languages = { "Todos", "Portugues", "English", "Espanol" };
        ArrayAdapter<String> langAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, languages);
        spinnerLanguage.setAdapter(langAdapter);
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
        spinnerGenre.setOnItemSelectedListener(filterListener);
        spinnerLanguage.setOnItemSelectedListener(filterListener);
        switchLocationVisible.setChecked(locationHelper.isLocationVisible());
        switchLocationVisible.setOnCheckedChangeListener((buttonView, isChecked) -> {
            locationHelper.setLocationVisible(isChecked);
            if (isChecked) {
                tvLocationStatus.setText("Sua localizacao esta visivel para outros leitores");
                updateUserLocationInDb();
            } else {
                tvLocationStatus.setText("Sua localizacao esta oculta");
                clearUserLocationInDb();
            }
        });
        setupBottomNav();
        requestLocationAndLoad();
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
                tvLocationStatus.setText("Permissao de localizacao negada");
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
                tvLocationStatus.setText(String.format("Localizacao: %.4f, %.4f", latitude, longitude));
                updateUserLocationInDb();
                loadNearbyUsers();
            }
            @Override
            public void onLocationError(String error) {
                tvLocationStatus.setText("Usando localizacao aproximada");
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
        String genre = spinnerGenre.getSelectedItem().toString();
        String language = spinnerLanguage.getSelectedItem().toString();
        if ("Todos".equals(genre))
            genre = null;
        if ("Todos".equals(language))
            language = null;
        List<User> users = dbHelper.getNearbyUsers(currentLat, currentLng, currentDistance, genre, language);
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
        TextView navClubs = findViewById(R.id.navClubs);
        TextView navProfile = findViewById(R.id.navProfile);
        navMap.setTextColor(getResources().getColor(R.color.blue_primary));
        navShelf.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                Log.e("MapActivity", "Error navigating to HomeActivity", e);
            }
        });
        navClubs.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, ClubListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
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
