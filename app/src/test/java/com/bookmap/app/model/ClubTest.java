package com.bookmap.app.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testes unitarios para o modelo Club.
 * Verifica getters/setters e campos transientes.
 */
public class ClubTest {

    @Test
    public void testDefaultConstructor() {
        Club club = new Club();
        assertEquals(0, club.getId());
        assertNull(club.getName());
        assertFalse(club.isPublic());
    }

    @Test
    public void testSettersAndGetters() {
        Club club = new Club();
        club.setId(1);
        club.setName("Clube de Leitura SP");
        club.setDescription("Encontros semanais para discutir livros");
        club.setPublic(true);
        club.setCreatorId(10);
        club.setBannerPath("/banners/club1.jpg");
        club.setCreatedAt("2025-01-01");

        assertEquals(1, club.getId());
        assertEquals("Clube de Leitura SP", club.getName());
        assertEquals("Encontros semanais para discutir livros", club.getDescription());
        assertTrue(club.isPublic());
        assertEquals(10, club.getCreatorId());
        assertEquals("/banners/club1.jpg", club.getBannerPath());
        assertEquals("2025-01-01", club.getCreatedAt());
    }

    @Test
    public void testTransientFields() {
        Club club = new Club();
        club.setMemberCount(15);
        club.setCreatorName("Maria");

        assertEquals(15, club.getMemberCount());
        assertEquals("Maria", club.getCreatorName());
    }

    @Test
    public void testPrivateClub() {
        Club club = new Club();
        club.setPublic(false);
        assertFalse(club.isPublic());
    }

    @Test
    public void testPublicClub() {
        Club club = new Club();
        club.setPublic(true);
        assertTrue(club.isPublic());
    }
}
