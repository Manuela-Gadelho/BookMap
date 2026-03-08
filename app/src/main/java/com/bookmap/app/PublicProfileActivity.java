package com.bookmap.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.User;
import com.bookmap.app.model.UserBook;
import com.bookmap.app.util.SessionManager;

/**
 * PublicProfileActivity - displays the public profile of another user.
 * Constraint 2: ViewNearby <<include>> ViewProfile
 */
public class PublicProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "user_id";

    private DatabaseHelper dbHelper;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile);

        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        long userId = getIntent().getLongExtra(EXTRA_USER_ID, -1);
        if (userId == -1) {
            finish();
            return;
        }

        User user = dbHelper.getUserById(userId);
        if (user == null) {
            finish();
            return;
        }

        // Header
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // User info
        TextView tvName = findViewById(R.id.tvName);
        TextView tvBio = findViewById(R.id.tvBio);
        TextView tvGenres = findViewById(R.id.tvGenres);
        tvName.setText(user.getName());
        tvBio.setText(user.getBio() != null && !user.getBio().isEmpty() ? user.getBio() : "Sem bio");
        tvGenres.setText(user.getFavoriteGenres() != null && !user.getFavoriteGenres().isEmpty()
                ? user.getFavoriteGenres() : "Nenhum genero");

        // Current reading
        LinearLayout layoutCurrentReading = findViewById(R.id.layoutCurrentReading);
        TextView tvCurrentBookTitle = findViewById(R.id.tvCurrentBookTitle);
        TextView tvCurrentBookAuthor = findViewById(R.id.tvCurrentBookAuthor);
        ProgressBar progressCurrentBook = findViewById(R.id.progressCurrentBook);
        TextView tvNoReading = findViewById(R.id.tvNoReading);

        UserBook currentReading = dbHelper.getCurrentReading(userId);
        if (currentReading != null) {
            layoutCurrentReading.setVisibility(View.VISIBLE);
            tvNoReading.setVisibility(View.GONE);
            tvCurrentBookTitle.setText(currentReading.getBookTitle());
            tvCurrentBookAuthor.setText(currentReading.getBookAuthor());
            progressCurrentBook.setProgress(currentReading.getProgress());
        } else {
            layoutCurrentReading.setVisibility(View.GONE);
            tvNoReading.setVisibility(View.VISIBLE);
        }

        // Report button
        Button btnReport = findViewById(R.id.btnReport);
        if (session.isLoggedIn()) {
            btnReport.setOnClickListener(v -> {
                Intent intent = new Intent(this, ReportActivity.class);
                intent.putExtra(ReportActivity.EXTRA_REPORTED_USER_ID, userId);
                startActivity(intent);
            });
        } else {
            btnReport.setVisibility(View.GONE);
        }
    }
}
