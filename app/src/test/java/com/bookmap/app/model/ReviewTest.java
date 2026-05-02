package com.bookmap.app.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testes unitarios para o modelo Review.
 * Verifica getters/setters e o metodo getStars().
 * Restricao 1: WriteReview inclui RateBook (rating obrigatorio com resenha).
 */
public class ReviewTest {

    @Test
    public void testDefaultConstructor() {
        Review review = new Review();
        assertEquals(0, review.getId());
        assertEquals(0, review.getRating());
        assertNull(review.getText());
    }

    @Test
    public void testSettersAndGetters() {
        Review review = new Review();
        review.setId(1);
        review.setUserId(10);
        review.setBookId(20);
        review.setText("Excelente livro!");
        review.setRating(5);
        review.setUserName("Maria");
        review.setCreatedAt("2025-01-15");

        assertEquals(1, review.getId());
        assertEquals(10, review.getUserId());
        assertEquals(20, review.getBookId());
        assertEquals("Excelente livro!", review.getText());
        assertEquals(5, review.getRating());
        assertEquals("Maria", review.getUserName());
        assertEquals("2025-01-15", review.getCreatedAt());
    }

    @Test
    public void testGetStars5Stars() {
        Review review = new Review();
        review.setRating(5);
        assertEquals("\u2605\u2605\u2605\u2605\u2605", review.getStars());
    }

    @Test
    public void testGetStars3Stars() {
        Review review = new Review();
        review.setRating(3);
        assertEquals("\u2605\u2605\u2605\u2606\u2606", review.getStars());
    }

    @Test
    public void testGetStars0Stars() {
        Review review = new Review();
        review.setRating(0);
        assertEquals("\u2606\u2606\u2606\u2606\u2606", review.getStars());
    }

    @Test
    public void testGetStars1Star() {
        Review review = new Review();
        review.setRating(1);
        assertEquals("\u2605\u2606\u2606\u2606\u2606", review.getStars());
    }

    @Test
    public void testRatingBoundaries() {
        Review review = new Review();

        review.setRating(1);
        assertEquals(1, review.getRating());

        review.setRating(5);
        assertEquals(5, review.getRating());
    }
}
