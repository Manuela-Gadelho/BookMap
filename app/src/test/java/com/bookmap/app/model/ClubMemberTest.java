package com.bookmap.app.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testes unitarios para o modelo ClubMember.
 * Verifica getters/setters e campos de juncao.
 */
public class ClubMemberTest {

    @Test
    public void testDefaultConstructor() {
        ClubMember member = new ClubMember();
        assertEquals(0, member.getId());
        assertNull(member.getRole());
        assertNull(member.getStatus());
    }

    @Test
    public void testSettersAndGetters() {
        ClubMember member = new ClubMember();
        member.setId(1);
        member.setClubId(10);
        member.setUserId(20);
        member.setRole("MEMBER");
        member.setStatus("PENDING");
        member.setJoinedAt("2025-01-01");

        assertEquals(1, member.getId());
        assertEquals(10, member.getClubId());
        assertEquals(20, member.getUserId());
        assertEquals("MEMBER", member.getRole());
        assertEquals("PENDING", member.getStatus());
        assertEquals("2025-01-01", member.getJoinedAt());
    }

    @Test
    public void testJoinedFields() {
        ClubMember member = new ClubMember();
        member.setUserName("Carlos");
        member.setUserEmail("carlos@test.com");
        member.setClubName("Clube de Fantasia");

        assertEquals("Carlos", member.getUserName());
        assertEquals("carlos@test.com", member.getUserEmail());
        assertEquals("Clube de Fantasia", member.getClubName());
    }

    @Test
    public void testStatusValues() {
        ClubMember member = new ClubMember();

        member.setStatus("PENDING");
        assertEquals("PENDING", member.getStatus());

        member.setStatus("APPROVED");
        assertEquals("APPROVED", member.getStatus());

        member.setStatus("REJECTED");
        assertEquals("REJECTED", member.getStatus());
    }

    @Test
    public void testRoleValues() {
        ClubMember member = new ClubMember();

        member.setRole("MEMBER");
        assertEquals("MEMBER", member.getRole());

        member.setRole("MODERATOR");
        assertEquals("MODERATOR", member.getRole());

        member.setRole("ORGANIZER");
        assertEquals("ORGANIZER", member.getRole());
    }
}
