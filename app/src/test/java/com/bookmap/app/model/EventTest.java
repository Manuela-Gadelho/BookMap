package com.bookmap.app.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testes unitarios para o modelo Event.
 * Verifica getters/setters para eventos de clube.
 */
public class EventTest {

    @Test
    public void testDefaultConstructor() {
        Event event = new Event();
        assertEquals(0, event.getId());
        assertNull(event.getTitle());
    }

    @Test
    public void testSettersAndGetters() {
        Event event = new Event();
        event.setId(1);
        event.setClubId(10);
        event.setTitle("Encontro Mensal");
        event.setDescription("Discussao sobre 1984");
        event.setDateTime("2025-02-15 19:00");
        event.setLocation("Biblioteca Central");
        event.setBookId(5);
        event.setCreatedBy(3);
        event.setCreatedAt("2025-01-01");

        assertEquals(1, event.getId());
        assertEquals(10, event.getClubId());
        assertEquals("Encontro Mensal", event.getTitle());
        assertEquals("Discussao sobre 1984", event.getDescription());
        assertEquals("2025-02-15 19:00", event.getDateTime());
        assertEquals("Biblioteca Central", event.getLocation());
        assertEquals(5, event.getBookId());
        assertEquals(3, event.getCreatedBy());
        assertEquals("2025-01-01", event.getCreatedAt());
    }
}
