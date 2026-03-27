package com.hms.util;

import com.hms.model.User;

public class SessionManager {

    private static User currentUser;

    private SessionManager() {} // prevent instantiation

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentRole() {
        return currentUser != null ? currentUser.getRoleName() : null;
    }

    public static void logout() {
        currentUser = null;
    }
}