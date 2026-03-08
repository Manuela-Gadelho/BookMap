package com.bookmap.app;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AddBookActivity - add a new book to the catalog and user shelf.
 */
public class AddBookActivity extends AppCompatActivity {

    private TextInputEditText editTitle, editAuthor, editSynopsis, editIsbn;
    private Spinner spinnerGenre;
    private RadioGroup radioStatus;
    private DatabaseHelper dbHelper;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        editTitle = findViewById(R.id.editTitle);
        editAuthor = findViewById(R.id.editAuthor);
        editSynopsis = findViewById(R.id.editSynopsis);
        editIsbn = findViewById(R.id.editIsbn);
        spinnerGenre = findViewById(R.id.spinnerGenre);
        radioStatus = findViewById(R.id.radioStatus);

        Button btnSave = findViewById(R.id.btnSaveBook);
        TextView btnBack = findViewById(R.id.btnBack);

        // Genre spinner
        String[] genres = {"Fantasia", "Terror", "Romance", "Ficcao Cientifica",
                "Tecnologia", "Literatura Brasileira", "Historia", "Autoajuda", "Outro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, genres);
        spinnerGenre.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveBook());
    }

    private void saveBook() {
        String title = editTitle.getText().toString().trim();
        String author = editAuthor.getText().toString().trim();
        String synopsis = editSynopsis.getText().toString().trim();
        String isbn = editIsbn.getText().toString().trim();
        String genre = spinnerGenre.getSelectedItem().toString();

        if (title.isEmpty() || author.isEmpty()) {
            Toast.makeText(this, "Titulo e autor sao obrigatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected status
        int selectedId = radioStatus.getCheckedRadioButtonId();
        String status = "QUERO_LER"; // default
        if (selectedId != -1) {
            RadioButton selected = findViewById(selectedId);
            String selectedText = selected.getText().toString();
            if (selectedText.contains("Lendo")) status = "LENDO";
            else if (selectedText.contains("Lido")) status = "LIDO";
            else status = "QUERO_LER";
        }

        // Insert book
        long bookId = dbHelper.insertBook(title, author, synopsis, "", genre, isbn);
        if (bookId > 0) {
            // Add to user shelf
            if (session.isLoggedIn()) {
                dbHelper.insertUserBook(session.getUserId(), bookId, status, 0);
            }
            Toast.makeText(this, "Livro adicionado com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erro ao adicionar livro", Toast.LENGTH_SHORT).show();
        }
    }
}
