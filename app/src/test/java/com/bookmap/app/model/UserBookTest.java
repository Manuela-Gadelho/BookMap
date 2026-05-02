package com.bookmap.app.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testes unitarios para o modelo UserBook (Estante Virtual).
 * Verifica getters/setters, campos de juncao e valores de status.
 */
public class UserBookTest {

    @Test
    public void testDefaultConstructor() {
        UserBook ub = new UserBook();
        assertEquals(0, ub.getId());
        assertEquals(0, ub.getProgress());
        assertNull(ub.getStatus());
    }

    @Test
    public void testSettersAndGetters() {
        UserBook ub = new UserBook();
        ub.setId(1);
        ub.setUserId(10);
        ub.setBookId(20);
        ub.setStatus("LENDO");
        ub.setProgress(45);
        ub.setCreatedAt("2025-01-01");

        assertEquals(1, ub.getId());
        assertEquals(10, ub.getUserId());
        assertEquals(20, ub.getBookId());
        assertEquals("LENDO", ub.getStatus());
        assertEquals(45, ub.getProgress());
        assertEquals("2025-01-01", ub.getCreatedAt());
    }

    @Test
    public void testJoinedFieldsFromBooks() {
        UserBook ub = new UserBook();
        ub.setBookTitle("Dom Casmurro");
        ub.setBookAuthor("Machado de Assis");
        ub.setBookGenre("Literatura Brasileira");
        ub.setBookCoverPath("/covers/dom.jpg");
        ub.setBookSynopsis("Um classico");

        assertEquals("Dom Casmurro", ub.getBookTitle());
        assertEquals("Machado de Assis", ub.getBookAuthor());
        assertEquals("Literatura Brasileira", ub.getBookGenre());
        assertEquals("/covers/dom.jpg", ub.getBookCoverPath());
        assertEquals("Um classico", ub.getBookSynopsis());
    }

    @Test
    public void testStatusLendo() {
        UserBook ub = new UserBook();
        ub.setStatus("LENDO");
        assertEquals("LENDO", ub.getStatus());
    }

    @Test
    public void testStatusLido() {
        UserBook ub = new UserBook();
        ub.setStatus("LIDO");
        assertEquals("LIDO", ub.getStatus());
    }

    @Test
    public void testStatusQueroLer() {
        UserBook ub = new UserBook();
        ub.setStatus("QUERO_LER");
        assertEquals("QUERO_LER", ub.getStatus());
    }

    @Test
    public void testProgressBoundaries() {
        UserBook ub = new UserBook();

        ub.setProgress(0);
        assertEquals(0, ub.getProgress());

        ub.setProgress(100);
        assertEquals(100, ub.getProgress());

        ub.setProgress(50);
        assertEquals(50, ub.getProgress());
    }
}
