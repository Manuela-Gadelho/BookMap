package com.bookmap.app.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testes unitarios para o modelo User.
 * Verifica getters/setters, construtor e metodos de verificacao de papel.
 */
public class UserTest {

    @Test
    public void testDefaultConstructor() {
        User user = new User();
        assertEquals(0, user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
    }

    @Test
    public void testParameterizedConstructor() {
        User user = new User("Maria", "maria@test.com", "hash123", "Fantasia,Terror", "READER");
        assertEquals("Maria", user.getName());
        assertEquals("maria@test.com", user.getEmail());
        assertEquals("hash123", user.getPasswordHash());
        assertEquals("Fantasia,Terror", user.getFavoriteGenres());
        assertEquals("READER", user.getRole());
    }

    @Test
    public void testSettersAndGetters() {
        User user = new User();
        user.setId(1);
        user.setName("Joao");
        user.setEmail("joao@test.com");
        user.setPasswordHash("abc123");
        user.setBio("Leitor voraz");
        user.setPhotoPath("/photos/joao.jpg");
        user.setFavoriteGenres("Romance");
        user.setRole("ORGANIZER");
        user.setLatitude(-23.5505);
        user.setLongitude(-46.6333);
        user.setLanguage("Portugues");
        user.setCreatedAt("2025-01-01");

        assertEquals(1, user.getId());
        assertEquals("Joao", user.getName());
        assertEquals("joao@test.com", user.getEmail());
        assertEquals("abc123", user.getPasswordHash());
        assertEquals("Leitor voraz", user.getBio());
        assertEquals("/photos/joao.jpg", user.getPhotoPath());
        assertEquals("Romance", user.getFavoriteGenres());
        assertEquals("ORGANIZER", user.getRole());
        assertEquals(-23.5505, user.getLatitude(), 0.0001);
        assertEquals(-46.6333, user.getLongitude(), 0.0001);
        assertEquals("Portugues", user.getLanguage());
        assertEquals("2025-01-01", user.getCreatedAt());
    }

    @Test
    public void testIsGuest() {
        User user = new User();
        user.setRole("GUEST");
        assertTrue(user.isGuest());
        assertFalse(user.isReader());
        assertFalse(user.isOrganizer());
    }

    @Test
    public void testIsReader() {
        User user = new User();
        user.setRole("READER");
        assertFalse(user.isGuest());
        assertTrue(user.isReader());
        assertFalse(user.isOrganizer());
    }

    @Test
    public void testIsOrganizer() {
        User user = new User();
        user.setRole("ORGANIZER");
        assertFalse(user.isGuest());
        assertFalse(user.isReader());
        assertTrue(user.isOrganizer());
    }

    @Test
    public void testNullRole() {
        User user = new User();
        user.setRole(null);
        assertFalse(user.isGuest());
        assertFalse(user.isReader());
        assertFalse(user.isOrganizer());
    }

    @Test
    public void testLocationCoordinates() {
        User user = new User();
        user.setLatitude(0.0);
        user.setLongitude(0.0);
        assertEquals(0.0, user.getLatitude(), 0.0);
        assertEquals(0.0, user.getLongitude(), 0.0);

        user.setLatitude(-90.0);
        user.setLongitude(180.0);
        assertEquals(-90.0, user.getLatitude(), 0.0);
        assertEquals(180.0, user.getLongitude(), 0.0);
    }
}
