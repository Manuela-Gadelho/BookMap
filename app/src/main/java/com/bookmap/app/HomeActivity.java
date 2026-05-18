package com.bookmap.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.adapter.UserBookAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.UserBook;
import com.bookmap.app.util.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private RecyclerView recyclerBooks;
    private UserBookAdapter adapter;
    private List<UserBook> userBooks = new ArrayList<>();
    private String currentFilter = null;
    private Button btnLendo, btnQueroLer, btnLidos;
    private LinearLayout layoutCurrentReading;
    private TextView tvCurrentTitle, tvCurrentAuthor, tvCurrentProgress;
    private ProgressBar progressCurrent;
    private TextView tvEmptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            android.content.SharedPreferences crashPrefs = getSharedPreferences("CrashLog", MODE_PRIVATE);
            String lastCrash = crashPrefs.getString("last_crash", null);
            if (lastCrash != null) {
                crashPrefs.edit().remove("last_crash").apply();
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Relatório de Erro (Crash Log)")
                        .setMessage("O aplicativo crashou no último teste com o seguinte erro:\n\n" + lastCrash)
                        .setPositiveButton("Ok", null)
                        .setNeutralButton("Copiar Erro", (dialog, which) -> {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(
                                    android.content.Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("Crash Log",
                                    lastCrash);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(this, "Erro copiado!", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            }
        } catch (Exception e) {
            Log.e("HomeActivity", "Failed to check or show crash log", e);
        }

        setContentView(R.layout.activity_home);
        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        btnLendo = findViewById(R.id.btnLendo);
        btnQueroLer = findViewById(R.id.btnQueroLer);
        btnLidos = findViewById(R.id.btnLidos);
        layoutCurrentReading = findViewById(R.id.layoutCurrentReading);
        tvCurrentTitle = findViewById(R.id.tvCurrentTitle);
        tvCurrentAuthor = findViewById(R.id.tvCurrentAuthor);
        tvCurrentProgress = findViewById(R.id.tvCurrentProgress);
        progressCurrent = findViewById(R.id.progressCurrent);
        recyclerBooks = findViewById(R.id.recyclerBooks);
        recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserBookAdapter(userBooks, userBook -> {
            try {
                Intent intent = new Intent(this, BookDetailsActivity.class);
                intent.putExtra(BookDetailsActivity.EXTRA_BOOK_ID, userBook.getBookId());
                startActivity(intent);
            } catch (Exception e) {
                Log.e("HomeActivity", "Error opening book details", e);
                Toast.makeText(this, "Erro ao abrir detalhes do livro.", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerBooks.setAdapter(adapter);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        findViewById(R.id.fabAddBook).setOnClickListener(v -> {
            try {
                if (session.isLoggedIn()) {
                    startActivity(new Intent(this, AddBookActivity.class));
                } else {
                    Toast.makeText(this, "Faça login para adicionar livros.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("HomeActivity", "Error opening AddBookActivity", e);
                Toast.makeText(this, "Erro ao abrir tela de adicionar livro.", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, SearchActivity.class));
            } catch (Exception e) {
                Log.e("HomeActivity", "Error opening SearchActivity", e);
                Toast.makeText(this, "Erro ao abrir a busca.", Toast.LENGTH_SHORT).show();
            }
        });
        btnLendo.setOnClickListener(v -> setFilter("LENDO"));
        btnQueroLer.setOnClickListener(v -> setFilter("QUERO_LER"));
        btnLidos.setOnClickListener(v -> setFilter("LIDO"));
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
            currentFilter = null;
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
            tvEmptyMessage.setText(R.string.login_required_shelf);
            recyclerBooks.setVisibility(View.GONE);
            return;
        }
        List<UserBook> books = dbHelper.getUserBooksByStatus(session.getUserId(), currentFilter);
        adapter.updateData(books);
        if (books.isEmpty()) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText(R.string.empty_shelf);
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
            tvCurrentProgress.setText(current.getProgress() + "% Concluído");
            layoutCurrentReading.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, BookDetailsActivity.class);
                    intent.putExtra(BookDetailsActivity.EXTRA_BOOK_ID, current.getBookId());
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("HomeActivity", "Error opening book details from current reading", e);
                }
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
            try {
                Intent intent = new Intent(this, MapActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            } catch (Exception e) {
                Log.e("HomeActivity", "Error navigating to MapActivity", e);
                Toast.makeText(this, "Erro ao abrir o mapa.", Toast.LENGTH_SHORT).show();
            }
        });
        navClubs.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, ClubListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            } catch (Exception e) {
                Log.e("HomeActivity", "Error navigating to ClubListActivity", e);
                Toast.makeText(this, "Erro ao abrir clubes.", Toast.LENGTH_SHORT).show();
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
                Log.e("HomeActivity", "Error navigating to ProfileActivity", e);
                Toast.makeText(this, "Erro ao abrir perfil.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
