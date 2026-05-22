package com.bookmap.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bookmap.app.adapter.ReviewAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.Review;
import com.bookmap.app.util.SessionManager;

import java.util.List;

public class FeedActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager session;
    private RecyclerView recyclerFeed;
    private TextView tvEmptyFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        recyclerFeed = findViewById(R.id.recyclerFeed);
        tvEmptyFeed = findViewById(R.id.tvEmptyFeed);
        
        recyclerFeed.setLayoutManager(new LinearLayoutManager(this));

        setupBottomNavigation();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadFeed();
    }

    private void loadFeed() {
        long currentUserId = session.getUserId();
        if (currentUserId == -1) return;

        List<Review> timelineReviews = dbHelper.getTimelineReviews(currentUserId);
        
        if (timelineReviews != null && !timelineReviews.isEmpty()) {
            recyclerFeed.setVisibility(View.VISIBLE);
            tvEmptyFeed.setVisibility(View.GONE);
            ReviewAdapter adapter = new ReviewAdapter(timelineReviews);
            recyclerFeed.setAdapter(adapter);
        } else {
            recyclerFeed.setVisibility(View.GONE);
            tvEmptyFeed.setVisibility(View.VISIBLE);
        }
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navShelf).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            overridePendingTransition(0, 0);
        });
        
        findViewById(R.id.navMap).setOnClickListener(v -> {
            startActivity(new Intent(this, MapActivity.class));
            finish();
            overridePendingTransition(0, 0);
        });

        findViewById(R.id.navClubs).setOnClickListener(v -> {
            startActivity(new Intent(this, ClubListActivity.class));
            finish();
            overridePendingTransition(0, 0);
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            overridePendingTransition(0, 0);
        });
    }
}
