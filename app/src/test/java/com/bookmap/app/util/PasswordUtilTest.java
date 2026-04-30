package com.bookmap.app.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testes unitarios para PasswordUtil.
 * Verifica hash SHA-256 e verificacao de senha.
 */
public class PasswordUtilTest {

    @Test
    public void testHashPasswordNotNull() {
        String hash = PasswordUtil.hashPassword("senha123");
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    public void testHashPasswordLength() {
        String hash = PasswordUtil.hashPassword("senha123");
        assertEquals(64, hash.length());
    }

    @Test
    public void testHashPasswordDeterministic() {
        String hash1 = PasswordUtil.hashPassword("minhasenha");
        String hash2 = PasswordUtil.hashPassword("minhasenha");
        assertEquals(hash1, hash2);
    }

    @Test
    public void testHashPasswordDifferentPasswords() {
        String hash1 = PasswordUtil.hashPassword("senha1");
        String hash2 = PasswordUtil.hashPassword("senha2");
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void testVerifyPasswordCorrect() {
        String password = "minhaSenhaForte";
        String hash = PasswordUtil.hashPassword(password);
        assertTrue(PasswordUtil.verifyPassword(password, hash));
    }

    @Test
    public void testVerifyPasswordIncorrect() {
        String hash = PasswordUtil.hashPassword("senhaCorreta");
        assertFalse(PasswordUtil.verifyPassword("senhaErrada", hash));
    }

    @Test
    public void testHashPasswordEmptyString() {
        String hash = PasswordUtil.hashPassword("");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    public void testHashPasswordSpecialCharacters() {
        String hash = PasswordUtil.hashPassword("s3nh@!#$%&*()");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    public void testHashPasswordUnicode() {
        String hash = PasswordUtil.hashPassword("senha\u00e7\u00e3o");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    public void testVerifyPasswordCaseSensitive() {
        String hash = PasswordUtil.hashPassword("Senha");
        assertFalse(PasswordUtil.verifyPassword("senha", hash));
    }
}
