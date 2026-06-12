package com.bookmap.app;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.adapter.BookAdapter;
import com.bookmap.app.adapter.UserAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.Book;
import com.bookmap.app.model.User;
import java.util.Collections;
import java.util.List;
public class SearchActivity extends AppCompatActivity {
    private EditText editSearch;
    private RecyclerView recyclerResults;
    private TextView tvNoResults;
    private Button btnTabBooks, btnTabUsers;
    private DatabaseHelper dbHelper;
    private boolean showingBooks = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        dbHelper = DatabaseHelper.getInstance(this);
        editSearch = findViewById(R.id.editSearch);
        Button btnSearch = findViewById(R.id.btnSearch);
        recyclerResults = findViewById(R.id.recyclerResults);
        tvNoResults = findViewById(R.id.tvNoResults);
        btnTabBooks = findViewById(R.id.btnTabBooks);
        btnTabUsers = findViewById(R.id.btnTabUsers);
        TextView btnBack = findViewById(R.id.btnBack);
        recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        btnBack.setOnClickListener(v -> finish());
        btnSearch.setOnClickListener(v -> performSearch());
        btnTabBooks.setOnClickListener(v -> {
            showingBooks = true;
            updateTabUI();
            performSearch();
        });
        btnTabUsers.setOnClickListener(v -> {
            showingBooks = false;
            updateTabUI();
            performSearch();
        });
        updateTabUI();
        performSearch(); 
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.bookmap.app.database.FirebaseSyncHelper.getInstance(this).setUsersCallback(success -> {
            if (success) {
                runOnUiThread(() -> performSearch());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        com.bookmap.app.database.FirebaseSyncHelper.getInstance(this).setUsersCallback(null);
    }

    private void updateTabUI() {
        btnTabBooks.setTextColor(getResources().getColor(
                showingBooks ? R.color.blue_primary : R.color.gray_text));
        btnTabUsers.setTextColor(getResources().getColor(
                showingBooks ? R.color.gray_text : R.color.blue_primary));
    }
    private void performSearch() {
        String query = editSearch.getText().toString().trim();
        if (showingBooks) {
            if (query.isEmpty()) {
                searchBooks(""); 
            } else {
                searchBooks(query);
            }
        } else {
            if (query.isEmpty()) {
                searchUsers("");
            } else {
                searchUsers(query);
            }
        }
    }
    private void searchBooks(String query) {
        List<Book> books;
        if (query.isEmpty()) {
            books = dbHelper.getAllBooks(); 
        } else {
            books = dbHelper.searchBooks(query);
        }
        
        if (books.isEmpty()) {
            tvNoResults.setVisibility(View.VISIBLE);
            recyclerResults.setVisibility(View.GONE);
        } else {
            tvNoResults.setVisibility(View.GONE);
            recyclerResults.setVisibility(View.VISIBLE);
            BookAdapter adapter = new BookAdapter(books, book -> {
                try {
                    Intent intent = new Intent(this, BookDetailsActivity.class);
                    intent.putExtra(BookDetailsActivity.EXTRA_BOOK_ID, book.getId());
                    startActivity(intent);
                } catch (Exception e) {
                    android.util.Log.e("SearchActivity", "Error opening BookDetails", e);
                }
            });
            recyclerResults.setAdapter(adapter);
        }
    }
    private void searchUsers(String query) {
        List<User> users;
        if (query.isEmpty()) {
            users = dbHelper.getAllUsers();
            Collections.sort(users, (u1, u2) -> u1.getName().compareToIgnoreCase(u2.getName()));
        } else {
            users = dbHelper.searchUsers(query);
        }
        if (users.isEmpty()) {
            tvNoResults.setVisibility(View.VISIBLE);
            recyclerResults.setVisibility(View.GONE);
        } else {
            tvNoResults.setVisibility(View.GONE);
            recyclerResults.setVisibility(View.VISIBLE);
            UserAdapter adapter = new UserAdapter(users, user -> {
                try {
                    Intent intent = new Intent(this, PublicProfileActivity.class);
                    intent.putExtra(PublicProfileActivity.EXTRA_USER_ID, user.getId());
                    startActivity(intent);
                } catch (Exception e) {
                    android.util.Log.e("SearchActivity", "Error opening PublicProfile", e);
                }
            }, false);
            recyclerResults.setAdapter(adapter);
        }
    }
}
