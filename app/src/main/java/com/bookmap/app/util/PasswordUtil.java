package com.bookmap.app.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for password hashing.
 * Uses SHA-256 for simple local hashing.
 */
public class PasswordUtil {

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return password; // Fallback
        }
    }

    public static boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }
}
