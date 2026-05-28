package com.bookmap.app.nonfunctional;

import com.bookmap.app.util.PasswordUtil;

import org.junit.Test;

import static org.junit.Assert.*;


public class SecurityTest {

    @Test
    public void testPasswordNotStoredInPlainText() {
        String password = "minhasenha123";
        String hash = PasswordUtil.hashPassword(password);
        assertNotEquals("Hash nao deve ser igual a senha original", password, hash);
    }

    @Test
    public void testHashIsHexadecimal() {
        String hash = PasswordUtil.hashPassword("teste");
        assertTrue("Hash deve conter apenas caracteres hexadecimais",
                hash.matches("[0-9a-f]+"));
    }

    @Test
    public void testHashLengthIs64() {
        String hash = PasswordUtil.hashPassword("qualquersenha");
        assertEquals("Hash SHA-256 deve ter 64 caracteres", 64, hash.length());
    }

    @Test
    public void testDifferentPasswordsDifferentHashes() {
        String hash1 = PasswordUtil.hashPassword("senha1");
        String hash2 = PasswordUtil.hashPassword("senha2");
        String hash3 = PasswordUtil.hashPassword("Senha1");
        assertNotEquals(hash1, hash2);
        assertNotEquals(hash1, hash3);
        assertNotEquals(hash2, hash3);
    }

    @Test
    public void testSamePasswordSameHash() {
        String hash1 = PasswordUtil.hashPassword("senhaIdentica");
        String hash2 = PasswordUtil.hashPassword("senhaIdentica");
        assertEquals("Mesma senha deve gerar mesmo hash", hash1, hash2);
    }

    @Test
    public void testEmptyPasswordHasHash() {
        String hash = PasswordUtil.hashPassword("");
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        assertEquals(64, hash.length());
    }

    @Test
    public void testLongPasswordSupported() {
        StringBuilder longPassword = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longPassword.append("a");
        }
        String hash = PasswordUtil.hashPassword(longPassword.toString());
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    public void testSpecialCharactersInPassword() {
        String hash = PasswordUtil.hashPassword("!@#$%^&*()_+-=[]{}|;':\",./<>?");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    public void testVerifyPreventsBruteForce() {
        String hash = PasswordUtil.hashPassword("senhaCorreta");
        assertFalse(PasswordUtil.verifyPassword("senha1", hash));
        assertFalse(PasswordUtil.verifyPassword("senha2", hash));
        assertFalse(PasswordUtil.verifyPassword("123456", hash));
        assertFalse(PasswordUtil.verifyPassword("password", hash));
        assertTrue(PasswordUtil.verifyPassword("senhaCorreta", hash));
    }

    @Test
    public void testHashIrreversibility() {
        String password = "senhaSecreta";
        String hash = PasswordUtil.hashPassword(password);
        assertFalse("Hash nao deve conter a senha original", hash.contains(password));
    }
}
