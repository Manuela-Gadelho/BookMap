package com.bookmap.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bookmap.app.adapter.ReviewAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.Book;
import com.bookmap.app.model.Review;
import com.bookmap.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * BookDetailsActivity - shows book details and reviews.
 * Constraint 1: WriteReview <<include>> RateBook - cannot rate without writing review.
 */
public class BookDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "book_id";

    private DatabaseHelper dbHelper;
    private SessionManager session;
    private long bookId;
    private RecyclerView recyclerReviews;
    private ReviewAdapter reviewAdapter;
    private EditText editReviewText;
    private RatingBar ratingBar;

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

        Book book = dbHelper.getBookById(bookId);
        if (book == null) {
            finish();
            return;
        }

        // Header
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Book info
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvAuthor = findViewById(R.id.tvAuthor);
        TextView tvGenre = findViewById(R.id.tvGenre);
        TextView tvSynopsis = findViewById(R.id.tvSynopsis);

        tvTitle.setText(book.getTitle());
        tvAuthor.setText(book.getAuthor());
        tvGenre.setText(book.getGenre());
        tvSynopsis.setText(book.getSynopsis() != null && !book.getSynopsis().isEmpty()
                ? book.getSynopsis() : "Sem sinopse disponivel");

        // Reviews section
        recyclerReviews = findViewById(R.id.recyclerReviews);
        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(new ArrayList<>());
        recyclerReviews.setAdapter(reviewAdapter);

        // Write review section
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

        // Add to shelf button
        Button btnAddToShelf = findViewById(R.id.btnAddToShelf);
        if (session.isLoggedIn()) {
            btnAddToShelf.setOnClickListener(v -> {
                long result = dbHelper.insertUserBook(session.getUserId(), bookId, "QUERO_LER", 0);
                if (result > 0) {
                    Toast.makeText(this, "Livro adicionado a sua estante!", Toast.LENGTH_SHORT).show();
                    btnAddToShelf.setText("Na sua estante");
                    btnAddToShelf.setEnabled(false);
                } else {
                    Toast.makeText(this, "Livro ja esta na sua estante", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            btnAddToShelf.setOnClickListener(v ->
                    Toast.makeText(this, "Faca login para adicionar livros", Toast.LENGTH_SHORT).show());
        }

        loadReviews();
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

    /**
     * Constraint 1: Cannot rate without writing a review.
     * Both text and rating are required.
     */
    private void submitReview() {
        String text = editReviewText.getText().toString().trim();
        int rating = (int) ratingBar.getRating();

        if (text.isEmpty()) {
            Toast.makeText(this, "Escreva uma resenha antes de avaliar", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rating == 0) {
            Toast.makeText(this, "Selecione uma avaliacao (1-5 estrelas)", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = dbHelper.insertReview(session.getUserId(), bookId, text, rating);
        if (result > 0) {
            Toast.makeText(this, "Resenha publicada!", Toast.LENGTH_SHORT).show();
            editReviewText.setText("");
            ratingBar.setRating(0);
            loadReviews();
        } else {
            Toast.makeText(this, "Erro ao publicar resenha", Toast.LENGTH_SHORT).show();
        }
    }
}
