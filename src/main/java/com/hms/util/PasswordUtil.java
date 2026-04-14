package com.hms.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Call this when creating or updating a user's password
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // Call this during login verification
    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}