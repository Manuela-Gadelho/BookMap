package com.bookmap.app.functional;

import android.content.Intent;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bookmap.app.BookDetailsActivity;
import com.bookmap.app.R;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.Book;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Testes funcionais para BookDetailsActivity.
 * Verifica exibicao de detalhes, media de avaliacao e botoes de acao.
 * Monografia secao 6.5: avaliacao media quantitativa.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class BookDetailsActivityTest {

    private long validBookId;

    @Before
    public void setUp() {
        com.bookmap.app.database.DatabaseHelper.resetInstance();
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(
                androidx.test.core.app.ApplicationProvider.getApplicationContext());
        List<Book> books = dbHelper.getAllBooks();
        if (!books.isEmpty()) {
            validBookId = books.get(0).getId();
        }
    }

    private BookDetailsActivity createActivity(long bookId) {
        Intent intent = new Intent();
        intent.putExtra(BookDetailsActivity.EXTRA_BOOK_ID, bookId);
        return Robolectric.buildActivity(BookDetailsActivity.class, intent)
                .create().resume().get();
    }

    @Test
    public void testActivityCreationWithValidBook() {
        BookDetailsActivity activity = createActivity(validBookId);
        assertNotNull(activity);
        assertFalse(activity.isFinishing());
    }

    @Test
    public void testTitleDisplayed() {
        BookDetailsActivity activity = createActivity(validBookId);
        TextView tvTitle = activity.findViewById(R.id.tvTitle);
        assertNotNull(tvTitle);
        assertFalse("Titulo nao deve estar vazio", tvTitle.getText().toString().isEmpty());
    }

    @Test
    public void testAuthorDisplayed() {
        BookDetailsActivity activity = createActivity(validBookId);
        TextView tvAuthor = activity.findViewById(R.id.tvAuthor);
        assertNotNull(tvAuthor);
        assertFalse("Autor nao deve estar vazio", tvAuthor.getText().toString().isEmpty());
    }

    @Test
    public void testAverageRatingBarExists() {
        BookDetailsActivity activity = createActivity(validBookId);
        RatingBar ratingBarAvg = activity.findViewById(R.id.ratingBarAverage);
        assertNotNull("RatingBar de media deve existir", ratingBarAvg);
        assertTrue("RatingBar deve ser indicador", ratingBarAvg.isIndicator());
    }

    @Test
    public void testReviewCountDisplayed() {
        BookDetailsActivity activity = createActivity(validBookId);
        TextView tvReviewCount = activity.findViewById(R.id.tvReviewCount);
        assertNotNull("Contagem de avaliacoes deve existir", tvReviewCount);
        assertTrue(tvReviewCount.getText().toString().contains("avaliacao"));
    }

    @Test
    public void testAddToShelfButtonExists() {
        BookDetailsActivity activity = createActivity(validBookId);
        Button btnAdd = activity.findViewById(R.id.btnAddToShelf);
        assertNotNull("Botao adicionar a estante deve existir", btnAdd);
    }

    @Test
    public void testSynopsisDisplayed() {
        BookDetailsActivity activity = createActivity(validBookId);
        TextView tvSynopsis = activity.findViewById(R.id.tvSynopsis);
        assertNotNull(tvSynopsis);
        assertFalse("Sinopse nao deve estar vazia", tvSynopsis.getText().toString().isEmpty());
    }

    @Test
    public void testBackButtonExists() {
        BookDetailsActivity activity = createActivity(validBookId);
        TextView btnBack = activity.findViewById(R.id.btnBack);
        assertNotNull(btnBack);
        assertEquals("VOLTAR", btnBack.getText().toString());
    }

    @Test
    public void testInvalidBookIdHandled() {
        // Activity should either finish or handle gracefully
        try {
            BookDetailsActivity activity = createActivity(-1);
            if (activity != null) {
                assertTrue("Activity deve finalizar com bookId invalido", activity.isFinishing());
            }
        } catch (Exception e) {
            // Expected - activity finishes before completing
        }
    }
}
