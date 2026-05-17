package com.bookmap.app;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.adapter.ClubAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.Club;
import com.bookmap.app.util.SessionManager;
import java.util.List;
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
            try {
                if (session.isLoggedIn()) {
                    startActivity(new Intent(this, CreateClubActivity.class));
                } else {
                    Toast.makeText(this, "Faca login para criar clubes", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("ClubListActivity", "Error opening CreateClubActivity", e);
                Toast.makeText(this, "Erro ao abrir criacao de clube", Toast.LENGTH_SHORT).show();
            }
        });
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
            try {
                Intent intent = new Intent(this, ClubActivity.class);
                intent.putExtra(ClubActivity.EXTRA_CLUB_ID, club.getId());
                startActivity(intent);
            } catch (Exception e) {
                Log.e("ClubListActivity", "Error opening ClubActivity", e);
                Toast.makeText(this, "Erro ao abrir clube", Toast.LENGTH_SHORT).show();
            }
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
            try {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                Log.e("ClubListActivity", "Error navigating to HomeActivity", e);
            }
        });
        navMap.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, MapActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                Log.e("ClubListActivity", "Error navigating to MapActivity", e);
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
                Log.e("ClubListActivity", "Error navigating to ProfileActivity", e);
            }
        });
    }
}
