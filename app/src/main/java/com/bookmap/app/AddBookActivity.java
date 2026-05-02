package com.bookmap.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.util.PhotoHelper;
import com.bookmap.app.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AddBookActivity - add a new book to the catalog and user shelf.
 * Now supports adding a cover photo via camera or gallery.
 */
public class AddBookActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 3002;

    private TextInputEditText editTitle, editAuthor, editSynopsis, editIsbn;
    private Spinner spinnerGenre;
    private RadioGroup radioStatus;
    private ImageView imgCover;
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private PhotoHelper photoHelper;
    private String coverPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        photoHelper = new PhotoHelper(this);

        editTitle = findViewById(R.id.editTitle);
        editAuthor = findViewById(R.id.editAuthor);
        editSynopsis = findViewById(R.id.editSynopsis);
        editIsbn = findViewById(R.id.editIsbn);
        spinnerGenre = findViewById(R.id.spinnerGenre);
        radioStatus = findViewById(R.id.radioStatus);
        imgCover = findViewById(R.id.imgCover);

        Button btnSave = findViewById(R.id.btnSaveBook);
        Button btnAddCover = findViewById(R.id.btnAddCover);
        TextView btnBack = findViewById(R.id.btnBack);

        // Genre spinner
        String[] genres = {"Fantasia", "Terror", "Romance", "Ficcao Cientifica",
                "Tecnologia", "Literatura Brasileira", "Historia", "Autoajuda", "Outro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, genres);
        spinnerGenre.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveBook());
        btnAddCover.setOnClickListener(v -> showCoverOptions());
        imgCover.setOnClickListener(v -> showCoverOptions());
    }

    private void showCoverOptions() {
        String[] options = {"Tirar Foto", "Escolher da Galeria"};
        new AlertDialog.Builder(this)
                .setTitle("Capa do Livro")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            return;
        }
        Intent cameraIntent = photoHelper.createCameraIntent();
        if (cameraIntent != null) {
            startActivityForResult(cameraIntent, PhotoHelper.REQUEST_CAMERA);
        } else {
            Toast.makeText(this, "Erro ao abrir camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent = photoHelper.createGalleryIntent();
        startActivityForResult(galleryIntent, PhotoHelper.REQUEST_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permissao da camera negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        String photoPath = null;

        if (requestCode == PhotoHelper.REQUEST_CAMERA) {
            photoPath = photoHelper.processCameraResult();
        } else if (requestCode == PhotoHelper.REQUEST_GALLERY && data != null) {
            Uri imageUri = data.getData();
            photoPath = photoHelper.processGalleryResult(imageUri);
        }

        if (photoPath != null) {
            coverPath = photoPath;
            PhotoHelper.loadImageIntoView(imgCover, coverPath);
            Toast.makeText(this, "Capa adicionada!", Toast.LENGTH_SHORT).show();
        }
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

        // Insert book with cover path
        long bookId = dbHelper.insertBook(title, author, synopsis, coverPath, genre, isbn);
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
