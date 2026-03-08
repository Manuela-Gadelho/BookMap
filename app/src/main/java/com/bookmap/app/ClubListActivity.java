package com.bookmap.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bookmap.app.adapter.ClubAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.Club;
import com.bookmap.app.util.SessionManager;

import java.util.List;

/**
 * ClubListActivity - lists reading clubs (my clubs and all clubs).
 */
public class ClubListActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager session;
    private RecyclerView recyclerClubs;
    private ClubAdapter clubAdapter;
    private TextView tvNoClubs;
    private Button btnMyClubs, btnAllClubs;
    private boolean showingMyClubs = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_list);

        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        recyclerClubs = findViewById(R.id.recyclerClubs);
        tvNoClubs = findViewById(R.id.tvNoClubs);
        btnMyClubs = findViewById(R.id.btnMyClubs);
        btnAllClubs = findViewById(R.id.btnAllClubs);
        Button btnCreateClub = findViewById(R.id.btnCreateClub);

        recyclerClubs.setLayoutManager(new LinearLayoutManager(this));

        btnMyClubs.setOnClickListener(v -> {
            showingMyClubs = true;
            updateTabUI();
            loadClubs();
        });

        btnAllClubs.setOnClickListener(v -> {
            showingMyClubs = false;
            updateTabUI();
            loadClubs();
        });

        btnCreateClub.setOnClickListener(v -> {
            if (session.isLoggedIn()) {
                startActivity(new Intent(this, CreateClubActivity.class));
            } else {
                android.widget.Toast.makeText(this, "Faca login para criar clubes", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // Bottom navigation
        setupBottomNav();
        updateTabUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClubs();
    }

    private void updateTabUI() {
        btnMyClubs.setTextColor(getResources().getColor(
                showingMyClubs ? R.color.blue_primary : R.color.gray_text));
        btnAllClubs.setTextColor(getResources().getColor(
                showingMyClubs ? R.color.gray_text : R.color.blue_primary));
    }

    private void loadClubs() {
        List<Club> clubs;
        if (showingMyClubs && session.isLoggedIn()) {
            clubs = dbHelper.getUserClubs(session.getUserId());
        } else {
            clubs = dbHelper.getAllClubs();
        }

        if (clubs.isEmpty()) {
            tvNoClubs.setVisibility(View.VISIBLE);
            recyclerClubs.setVisibility(View.GONE);
        } else {
            tvNoClubs.setVisibility(View.GONE);
            recyclerClubs.setVisibility(View.VISIBLE);
        }

        clubAdapter = new ClubAdapter(clubs, club -> {
            Intent intent = new Intent(this, ClubActivity.class);
            intent.putExtra(ClubActivity.EXTRA_CLUB_ID, club.getId());
            startActivity(intent);
        });
        recyclerClubs.setAdapter(clubAdapter);
    }

    private void setupBottomNav() {
        TextView navShelf = findViewById(R.id.navShelf);
        TextView navMap = findViewById(R.id.navMap);
        TextView navClubs = findViewById(R.id.navClubs);
        TextView navProfile = findViewById(R.id.navProfile);

        navClubs.setTextColor(getResources().getColor(R.color.blue_primary));

        navShelf.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        navMap.setOnClickListener(v -> {
            startActivity(new Intent(this, MapActivity.class));
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
