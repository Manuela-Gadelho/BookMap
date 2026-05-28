package com.bookmap.app.util;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
public class SessionManagerTest {

    private SessionManager session;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        session = new SessionManager(context);
        session.logout();
    }

    @Test
    public void testInitialStateNotLoggedIn() {
        assertFalse(session.isLoggedIn());
        assertEquals(-1, session.getUserId());
        assertEquals("", session.getUserName());
        assertEquals("", session.getUserEmail());
        assertEquals("GUEST", session.getUserRole());
    }

    @Test
    public void testCreateLoginSession() {
        session.createLoginSession(1, "Maria", "maria@test.com", "READER");
        assertTrue(session.isLoggedIn());
        assertEquals(1, session.getUserId());
        assertEquals("Maria", session.getUserName());
        assertEquals("maria@test.com", session.getUserEmail());
        assertEquals("READER", session.getUserRole());
    }

    @Test
    public void testLogout() {
        session.createLoginSession(1, "Maria", "maria@test.com", "READER");
        assertTrue(session.isLoggedIn());
        session.logout();
        assertFalse(session.isLoggedIn());
    }

    @Test
    public void testIsGuest() {
        assertTrue(session.isGuest());
        session.createLoginSession(1, "Guest", "guest@test.com", "GUEST");
        assertTrue(session.isGuest());
    }

    @Test
    public void testIsReader() {
        assertFalse(session.isReader());
        session.createLoginSession(1, "Reader", "reader@test.com", "READER");
        assertTrue(session.isReader());
    }

    @Test
    public void testIsOrganizer() {
        assertFalse(session.isOrganizer());
        session.createLoginSession(1, "Org", "org@test.com", "ORGANIZER");
        assertTrue(session.isOrganizer());
    }

    @Test
    public void testOrganizerIsAlsoReader() {
        session.createLoginSession(1, "Org", "org@test.com", "ORGANIZER");
        assertTrue(session.isReader());
        assertTrue(session.isOrganizer());
    }

    @Test
    public void testSessionPersistence() {
        session.createLoginSession(5, "Joao", "joao@test.com", "READER");
        Context context = ApplicationProvider.getApplicationContext();
        SessionManager newSession = new SessionManager(context);
        assertTrue(newSession.isLoggedIn());
        assertEquals(5, newSession.getUserId());
        assertEquals("Joao", newSession.getUserName());
    }

    @Test
    public void testLogoutClearsAllData() {
        session.createLoginSession(1, "Maria", "maria@test.com", "ORGANIZER");
        session.logout();
        assertEquals(-1, session.getUserId());
        assertEquals("", session.getUserName());
        assertEquals("", session.getUserEmail());
        assertEquals("GUEST", session.getUserRole());
    }
}
