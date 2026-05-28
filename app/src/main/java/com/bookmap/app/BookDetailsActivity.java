package com.bookmap.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.adapter.ReviewAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.database.FirebaseSyncHelper;
import com.bookmap.app.model.Book;
import com.bookmap.app.model.Review;
import com.bookmap.app.model.UserBook;
import com.bookmap.app.util.PhotoHelper;
import com.bookmap.app.util.SessionManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import java.util.ArrayList;
import java.util.List;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BookDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_BOOK_ID = "book_id";
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private long bookId;
    private RecyclerView recyclerReviews;
    private ReviewAdapter reviewAdapter;
    private EditText editReviewText;
    private RatingBar ratingBar;
    private RatingBar ratingBarAverage;
    private TextView tvAverageRating, tvReviewCount;
    private PhotoHelper photoHelper;
    private static final int PERMISSION_REQUEST_CAMERA = 3003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        bookId = getIntent().getLongExtra(EXTRA_BOOK_ID, -1);
        if (bookId == -1) {
            finish();
            return;
        }
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        ratingBarAverage = findViewById(R.id.ratingBarAverage);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvReviewCount = findViewById(R.id.tvReviewCount);
        photoHelper = new PhotoHelper(this);
        if (savedInstanceState != null) {
            String savedPath = savedInstanceState.getString("photo_path");
            if (savedPath != null) {
                photoHelper.setCurrentPhotoPath(savedPath);
            }
        }
        loadBookDetails();
        loadAverageRating();
        recyclerReviews = findViewById(R.id.recyclerReviews);
        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(new ArrayList<>());
        recyclerReviews.setAdapter(reviewAdapter);
        View layoutWriteReview = findViewById(R.id.layoutWriteReview);
        editReviewText = findViewById(R.id.editReviewText);
        ratingBar = findViewById(R.id.ratingBar);
        Button btnSubmitReview = findViewById(R.id.btnSubmitReview);
        if (session.isLoggedIn()) {
            layoutWriteReview.setVisibility(View.VISIBLE);
            btnSubmitReview.setOnClickListener(v -> submitReview());
        } else {
            layoutWriteReview.setVisibility(View.GONE);
        }
        Button btnAddToShelf = findViewById(R.id.btnAddToShelf);
        Button btnUpdateProgress = findViewById(R.id.btnUpdateProgress);
        if (session.isLoggedIn()) {
            UserBook existingUserBook = dbHelper.getUserBook(session.getUserId(), bookId);
            if (existingUserBook != null) {
                btnAddToShelf.setText("Na sua estante");
                btnAddToShelf.setEnabled(false);
                btnUpdateProgress.setVisibility(View.VISIBLE);
                btnUpdateProgress.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(this, UpdateProgressActivity.class);
                        intent.putExtra(UpdateProgressActivity.EXTRA_BOOK_ID, bookId);
                        startActivity(intent);
                    } catch (Exception e) {
                        android.util.Log.e("BookDetailsActivity", "Error opening UpdateProgress", e);
                    }
                });
            } else {
                btnUpdateProgress.setVisibility(View.GONE);
                btnAddToShelf.setOnClickListener(v -> {
                    long result = dbHelper.insertUserBook(session.getUserId(), bookId, "QUERO_LER", 0);
                    if (result > 0) {
                        FirebaseSyncHelper syncHelper = FirebaseSyncHelper.getInstance(this);
                        syncHelper.syncUserBookToCloud(session.getUserId(), bookId, "QUERO_LER", 0);
                        Toast.makeText(this, "Livro adicionado à sua estante!", Toast.LENGTH_SHORT).show();
                        btnAddToShelf.setText("Na sua estante");
                        btnAddToShelf.setEnabled(false);
                        btnUpdateProgress.setVisibility(View.VISIBLE);
                        btnUpdateProgress.setOnClickListener(v2 -> {
                            try {
                                Intent intent = new Intent(this, UpdateProgressActivity.class);
                                intent.putExtra(UpdateProgressActivity.EXTRA_BOOK_ID, bookId);
                                startActivity(intent);
                            } catch (Exception e) {
                                android.util.Log.e("BookDetailsActivity", "Error opening UpdateProgress", e);
                            }
                        });
                    } else {
                        Toast.makeText(this, "Livro já está na sua estante", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            btnUpdateProgress.setVisibility(View.GONE);
            btnAddToShelf.setOnClickListener(
                    v -> Toast.makeText(this, "Faça login para adicionar livros", Toast.LENGTH_SHORT).show());
        }
        loadReviews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookDetails();
        loadReviews();
        loadAverageRating();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (photoHelper.getCurrentPhotoPath() != null) {
            outState.putString("photo_path", photoHelper.getCurrentPhotoPath());
        }
    }

    private void loadBookDetails() {
        Book book = dbHelper.getBookById(bookId);
        if (book == null) {
            finish();
            return;
        }
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvAuthor = findViewById(R.id.tvAuthor);
        TextView tvGenre = findViewById(R.id.tvGenre);
        TextView tvSynopsis = findViewById(R.id.tvSynopsis);
        tvTitle.setText(book.getTitle());
        tvAuthor.setText(book.getAuthor());
        tvGenre.setText(book.getGenre());
        tvSynopsis.setText(book.getSynopsis() != null && !book.getSynopsis().isEmpty()
                ? book.getSynopsis()
                : "Sem sinopse disponível");

        ImageView imgCover = findViewById(R.id.imgBookCover);
        String isbn = book.getIsbn();
        String coverPath = book.getCoverPath();
        String fallbackAssetPath = PhotoHelper.getGenreAssetPath(book.getGenre());
        
        if (coverPath != null && !coverPath.isEmpty()) {
            if (coverPath.startsWith("http") || coverPath.startsWith("asset:")) {
                String glidePath = coverPath.startsWith("asset:") ? coverPath.replaceFirst("^asset:", "file:///android_asset/") : coverPath;
                Glide.with(this)
                    .load(glidePath)
                    .transform(new CenterCrop(), new RoundedCorners(16))
                    .error(Glide.with(this).load(fallbackAssetPath).transform(new CenterCrop(), new RoundedCorners(16)))
                    .into(imgCover);
            } else {
                Glide.with(this)
                    .load(new java.io.File(coverPath))
                    .transform(new CenterCrop(), new RoundedCorners(16))
                    .error(Glide.with(this).load(fallbackAssetPath).transform(new CenterCrop(), new RoundedCorners(16)))
                    .into(imgCover);
            }
        } else if (isbn != null && !isbn.isEmpty()) {
            String url = "https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg?default=false";
            Glide.with(this)
                .load(url)
                .transform(new CenterCrop(), new RoundedCorners(16))
                .error(Glide.with(this).load(fallbackAssetPath).transform(new CenterCrop(), new RoundedCorners(16)))
                .into(imgCover);
        } else {
            Glide.with(this)
                .load(fallbackAssetPath)
                .transform(new CenterCrop(), new RoundedCorners(16))
                .into(imgCover);
        }

        if (session.isLoggedIn() && (book.getCreatorId() == session.getUserId() || session.isAdmin())) {
            imgCover.setOnClickListener(v -> showPhotoOptions(book));
        } else {
            imgCover.setOnClickListener(null);
        }

        View layoutBookCreatorActions = findViewById(R.id.layoutBookCreatorActions);
        Button btnEditBook = findViewById(R.id.btnEditBook);
        Button btnDeleteBook = findViewById(R.id.btnDeleteBook);

        if (session.isLoggedIn() && (book.getCreatorId() == session.getUserId() || session.isAdmin())) {
            layoutBookCreatorActions.setVisibility(View.VISIBLE);
            btnEditBook.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddBookActivity.class);
                intent.putExtra("book_id", bookId);
                startActivity(intent);
            });
            btnDeleteBook.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Excluir Livro")
                        .setMessage("Tem certeza de que deseja excluir este livro?")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            if (dbHelper.deleteBook(bookId)) {
                                com.bookmap.app.database.FirebaseSyncHelper.getInstance(this).deleteBookFromCloud(bookId);
                                Toast.makeText(this, "Livro excluído com sucesso!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(this, "Erro ao excluir o livro.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Não", null)
                        .show();
            });
        } else {
            layoutBookCreatorActions.setVisibility(View.GONE);
        }
    }

    private void loadAverageRating() {
        double avg = dbHelper.getBookAverageRating(bookId);
        int count = dbHelper.getBookReviewCount(bookId);
        if (ratingBarAverage != null) {
            ratingBarAverage.setRating((float) avg);
        }
        if (tvAverageRating != null) {
            tvAverageRating.setText(String.format("%.1f", avg));
        }
        if (tvReviewCount != null) {
            tvReviewCount.setText(count + " avaliação(ões)");
        }
    }

    private void loadReviews() {
        List<Review> reviews = dbHelper.getBookReviews(bookId);
        reviewAdapter.updateData(reviews);
        TextView tvNoReviews = findViewById(R.id.tvNoReviews);
        if (reviews.isEmpty()) {
            tvNoReviews.setVisibility(View.VISIBLE);
        } else {
            tvNoReviews.setVisibility(View.GONE);
        }
    }

    private void submitReview() {
        String text = editReviewText.getText().toString().trim();
        int rating = (int) ratingBar.getRating();
        if (text.isEmpty()) {
            Toast.makeText(this, "Escreva uma resenha antes de avaliar", Toast.LENGTH_SHORT).show();
            return;
        }
        if (rating == 0) {
            Toast.makeText(this, "Selecione uma avaliação (1-5 estrelas)", Toast.LENGTH_SHORT).show();
            return;
        }
        long result = dbHelper.insertReview(session.getUserId(), bookId, text, rating);
        if (result > 0) {
            Toast.makeText(this, "Resenha publicada!", Toast.LENGTH_SHORT).show();
            editReviewText.setText("");
            ratingBar.setRating(0);
            loadReviews();
            loadAverageRating();
        } else {
            Toast.makeText(this, "Erro ao publicar resenha", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPhotoOptions(Book book) {
        String[] options = { "Tirar Foto", "Escolher da Galeria", "Remover Capa" };
        new AlertDialog.Builder(this)
                .setTitle("Capa do Livro")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            openCamera();
                            break;
                        case 1:
                            openGallery();
                            break;
                        case 2:
                            updateBookCover(book, "");
                            break;
                    }
                })
                .show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            return;
        }
        try {
            Intent cameraIntent = photoHelper.createCameraIntent();
            if (cameraIntent != null) {
                startActivityForResult(cameraIntent, PhotoHelper.REQUEST_CAMERA);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir a câmera", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent = photoHelper.createGalleryIntent();
        startActivityForResult(galleryIntent, PhotoHelper.REQUEST_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else if (requestCode == PERMISSION_REQUEST_CAMERA) {
            Toast.makeText(this, "Permissão de câmera negada", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String photoPath = null;
        if (requestCode == PhotoHelper.REQUEST_CAMERA && resultCode == RESULT_OK) {
            photoPath = photoHelper.getCurrentPhotoPath();
        } else if (requestCode == PhotoHelper.REQUEST_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            photoPath = photoHelper.processGalleryResult(imageUri);
        }
        
        if (photoPath != null) {
            Book book = dbHelper.getBookById(bookId);
            if (book != null) {
                updateBookCover(book, photoPath);
            }
        }
    }

    private void updateBookCover(Book book, String newPath) {
        boolean success = dbHelper.updateBook(book.getId(), book.getTitle(), book.getAuthor(), book.getSynopsis(), newPath, book.getGenre(), book.getIsbn());
        if (success) {
            book.setCoverPath(newPath);
            FirebaseSyncHelper.getInstance(this).syncBookToCloud(book);
            loadBookDetails();
            Toast.makeText(this, "Capa atualizada com sucesso!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Erro ao atualizar a capa.", Toast.LENGTH_SHORT).show();
        }
    }
}
