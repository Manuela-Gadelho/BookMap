package com.bookmap.app.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testes unitarios para o modelo Book.
 * Verifica getters/setters e construtores.
 */
public class BookTest {

    @Test
    public void testDefaultConstructor() {
        Book book = new Book();
        assertEquals(0, book.getId());
        assertNull(book.getTitle());
        assertNull(book.getAuthor());
    }

    @Test
    public void testParameterizedConstructor() {
        Book book = new Book("Dom Casmurro", "Machado de Assis", "Um classico", "Literatura Brasileira");
        assertEquals("Dom Casmurro", book.getTitle());
        assertEquals("Machado de Assis", book.getAuthor());
        assertEquals("Um classico", book.getSynopsis());
        assertEquals("Literatura Brasileira", book.getGenre());
    }

    @Test
    public void testSettersAndGetters() {
        Book book = new Book();
        book.setId(1);
        book.setTitle("1984");
        book.setAuthor("George Orwell");
        book.setSynopsis("Distopia totalitaria");
        book.setCoverPath("/covers/1984.jpg");
        book.setGenre("Ficcao Cientifica");
        book.setIsbn("978-8535914849");
        book.setCreatedAt("2025-01-01");

        assertEquals(1, book.getId());
        assertEquals("1984", book.getTitle());
        assertEquals("George Orwell", book.getAuthor());
        assertEquals("Distopia totalitaria", book.getSynopsis());
        assertEquals("/covers/1984.jpg", book.getCoverPath());
        assertEquals("Ficcao Cientifica", book.getGenre());
        assertEquals("978-8535914849", book.getIsbn());
        assertEquals("2025-01-01", book.getCreatedAt());
    }

    @Test
    public void testNullSynopsis() {
        Book book = new Book();
        book.setSynopsis(null);
        assertNull(book.getSynopsis());
    }

    @Test
    public void testEmptyFields() {
        Book book = new Book("", "", "", "");
        assertEquals("", book.getTitle());
        assertEquals("", book.getAuthor());
        assertEquals("", book.getSynopsis());
        assertEquals("", book.getGenre());
    }
}
