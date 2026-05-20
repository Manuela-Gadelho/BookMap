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
import android.widget.EditText;

public class AddBookActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 3002;
    private EditText editTitle, editAuthor, editSynopsis, editIsbn;
    private Spinner spinnerGenre;
    private RadioGroup radioStatus;
    private ImageView imgCover;
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private PhotoHelper photoHelper;
    private String coverPath = "";
    private long bookId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        photoHelper = new PhotoHelper(this);
        if (savedInstanceState != null) {
            String savedPath = savedInstanceState.getString("photo_path");
            if (savedPath != null) {
                photoHelper.setCurrentPhotoPath(savedPath);
            }
            coverPath = savedInstanceState.getString("cover_path", "");
        }
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
        String[] genres = com.bookmap.app.util.GenreUtil.getGenresArray();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, genres);
        spinnerGenre.setAdapter(adapter);
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveBook());
        btnAddCover.setOnClickListener(v -> showCoverOptions());
        imgCover.setOnClickListener(v -> showCoverOptions());

        bookId = getIntent().getLongExtra("book_id", -1);
        if (bookId != -1) {
            com.bookmap.app.model.Book book = dbHelper.getBookById(bookId);
            if (book != null) {
                editTitle.setText(book.getTitle());
                editAuthor.setText(book.getAuthor());
                editSynopsis.setText(book.getSynopsis());
                editIsbn.setText(book.getIsbn());
                coverPath = book.getCoverPath() != null ? book.getCoverPath() : "";
                if (book.getGenre() != null) {
                    for (int i = 0; i < genres.length; i++) {
                        if (genres[i].equalsIgnoreCase(book.getGenre())) {
                            spinnerGenre.setSelection(i);
                            break;
                        }
                    }
                }
                radioStatus.setVisibility(View.GONE);
                TextView tvStatusLabel = findViewById(R.id.tvStatusLabel);
                if (tvStatusLabel != null) {
                    tvStatusLabel.setVisibility(View.GONE);
                }
                TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
                if (tvHeaderTitle != null) {
                    tvHeaderTitle.setText("Editar Livro");
                }
                btnSave.setText("Salvar Alterações");
            }
        }

        if (!coverPath.isEmpty()) {
            PhotoHelper.loadImageIntoView(imgCover, coverPath);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (photoHelper.getCurrentPhotoPath() != null) {
            outState.putString("photo_path", photoHelper.getCurrentPhotoPath());
        }
        outState.putString("cover_path", coverPath);
    }

    private void showCoverOptions() {
        String[] options = { "Tirar Foto", "Escolher da Galeria" };
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.CAMERA }, PERMISSION_REQUEST_CAMERA);
            return;
        }
        Intent cameraIntent = photoHelper.createCameraIntent();
        if (cameraIntent != null) {
            startActivityForResult(cameraIntent, PhotoHelper.REQUEST_CAMERA);
        } else {
            Toast.makeText(this, "Erro ao abrir a câmera.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Permissão da câmera negada.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
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
            Toast.makeText(this, "Título e autor são obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bookId != -1) {
            boolean success = dbHelper.updateBook(bookId, title, author, synopsis, coverPath, genre, isbn);
            if (success) {
                com.bookmap.app.model.Book updatedBook = dbHelper.getBookById(bookId);
                if (updatedBook != null) {
                    com.bookmap.app.database.FirebaseSyncHelper.getInstance(this).syncBookToCloud(updatedBook);
                }
                Toast.makeText(this, "Livro atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erro ao atualizar o livro.", Toast.LENGTH_SHORT).show();
            }
        } else {
            int selectedId = radioStatus.getCheckedRadioButtonId();
            String status = "QUERO_LER";
            if (selectedId != -1) {
                RadioButton selected = findViewById(selectedId);
                String selectedText = selected.getText().toString();
                if (selectedText.contains("Lendo"))
                    status = "LENDO";
                else if (selectedText.contains("Lido"))
                    status = "LIDO";
                else
                    status = "QUERO_LER";
            }
            long newBookId = dbHelper.insertBook(title, author, synopsis, coverPath, genre, isbn,
                    session.isLoggedIn() ? session.getUserId() : 0);
            if (newBookId > 0) {
                com.bookmap.app.model.Book newBook = dbHelper.getBookById(newBookId);
                if (newBook != null) {
                    com.bookmap.app.database.FirebaseSyncHelper.getInstance(this).syncBookToCloud(newBook);
                }
                if (session.isLoggedIn()) {
                    dbHelper.insertUserBook(session.getUserId(), newBookId, status, 0);
                }
                Toast.makeText(this, "Livro adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erro ao adicionar o livro.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
