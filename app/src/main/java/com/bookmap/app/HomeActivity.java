package com.bookmap.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bookmap.app.adapter.UserBookAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.UserBook;
import com.bookmap.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeActivity - Virtual Shelf (Estante Virtual).
 * Shows books organized by reading status with bottom navigation.
 */
public class HomeActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager session;
    private RecyclerView recyclerBooks;
    private UserBookAdapter adapter;
    private List<UserBook> userBooks = new ArrayList<>();
    private String currentFilter = null; // null = all
    private Button btnLendo, btnQueroLer, btnLidos;
    private LinearLayout layoutCurrentReading;
    private TextView tvCurrentTitle, tvCurrentAuthor, tvCurrentProgress;
    private ProgressBar progressCurrent;
    private TextView tvEmptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        // Filter buttons
        btnLendo = findViewById(R.id.btnLendo);
        btnQueroLer = findViewById(R.id.btnQueroLer);
        btnLidos = findViewById(R.id.btnLidos);

        // Current reading section
        layoutCurrentReading = findViewById(R.id.layoutCurrentReading);
        tvCurrentTitle = findViewById(R.id.tvCurrentTitle);
        tvCurrentAuthor = findViewById(R.id.tvCurrentAuthor);
        tvCurrentProgress = findViewById(R.id.tvCurrentProgress);
        progressCurrent = findViewById(R.id.progressCurrent);

        // Books list
        recyclerBooks = findViewById(R.id.recyclerBooks);
        recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserBookAdapter(userBooks, userBook -> {
            Intent intent = new Intent(this, BookDetailsActivity.class);
            intent.putExtra(BookDetailsActivity.EXTRA_BOOK_ID, userBook.getBookId());
            startActivity(intent);
        });
        recyclerBooks.setAdapter(adapter);

        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        // FAB to add book
        findViewById(R.id.fabAddBook).setOnClickListener(v -> {
            if (session.isLoggedIn()) {
                startActivity(new Intent(this, AddBookActivity.class));
            } else {
                android.widget.Toast.makeText(this, "Faca login para adicionar livros", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // Search button
        findViewById(R.id.btnSearch).setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class)));

        // Filter buttons
        btnLendo.setOnClickListener(v -> setFilter("LENDO"));
        btnQueroLer.setOnClickListener(v -> setFilter("QUERO_LER"));
        btnLidos.setOnClickListener(v -> setFilter("LIDO"));

        // Bottom Navigation
        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBooks();
        loadCurrentReading();
    }

    private void setFilter(String filter) {
        if (filter.equals(currentFilter)) {
            currentFilter = null; // Toggle off
        } else {
            currentFilter = filter;
        }
        updateFilterUI();
        loadBooks();
    }

    private void updateFilterUI() {
        btnLendo.setTextColor(getResources().getColor(
                "LENDO".equals(currentFilter) ? R.color.blue_primary : R.color.gray_text));
        btnQueroLer.setTextColor(getResources().getColor(
                "QUERO_LER".equals(currentFilter) ? R.color.blue_primary : R.color.gray_text));
        btnLidos.setTextColor(getResources().getColor(
                "LIDO".equals(currentFilter) ? R.color.blue_primary : R.color.gray_text));
    }

    private void loadBooks() {
        if (!session.isLoggedIn()) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText("Faca login para ver sua estante");
            recyclerBooks.setVisibility(View.GONE);
            return;
        }

        List<UserBook> books = dbHelper.getUserBooksByStatus(session.getUserId(), currentFilter);
        adapter.updateData(books);

        if (books.isEmpty()) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText("Nenhum livro encontrado. Adicione livros a sua estante!");
            recyclerBooks.setVisibility(View.GONE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            recyclerBooks.setVisibility(View.VISIBLE);
        }
    }

    private void loadCurrentReading() {
        if (!session.isLoggedIn()) {
            layoutCurrentReading.setVisibility(View.GONE);
            return;
        }

        UserBook current = dbHelper.getCurrentReading(session.getUserId());
        if (current != null) {
            layoutCurrentReading.setVisibility(View.VISIBLE);
            tvCurrentTitle.setText(current.getBookTitle());
            tvCurrentAuthor.setText(current.getBookAuthor());
            progressCurrent.setProgress(current.getProgress());
            tvCurrentProgress.setText(current.getProgress() + "% Concluido");
            layoutCurrentReading.setOnClickListener(v -> {
                Intent intent = new Intent(this, BookDetailsActivity.class);
                intent.putExtra(BookDetailsActivity.EXTRA_BOOK_ID, current.getBookId());
                startActivity(intent);
            });
        } else {
            layoutCurrentReading.setVisibility(View.GONE);
        }
    }

    private void setupBottomNav() {
        TextView navShelf = findViewById(R.id.navShelf);
        TextView navMap = findViewById(R.id.navMap);
        TextView navClubs = findViewById(R.id.navClubs);
        TextView navProfile = findViewById(R.id.navProfile);

        navShelf.setTextColor(getResources().getColor(R.color.blue_primary));

        navMap.setOnClickListener(v -> {
            startActivity(new Intent(this, MapActivity.class));
        });
        navClubs.setOnClickListener(v -> {
            startActivity(new Intent(this, ClubListActivity.class));
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
