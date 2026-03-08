package com.bookmap.app.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager handles local authentication state using SharedPreferences.
 * Manages login/logout and stores current user info.
 */
public class SessionManager {

    private static final String PREF_NAME = "BookMapSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ROLE = "userRole";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createLoginSession(long userId, String name, String email, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "GUEST");
    }

    public boolean isGuest() {
        return !isLoggedIn() || "GUEST".equals(getUserRole());
    }

    public boolean isReader() {
        return isLoggedIn() && ("READER".equals(getUserRole()) || "ORGANIZER".equals(getUserRole()));
    }

    public boolean isOrganizer() {
        return isLoggedIn() && "ORGANIZER".equals(getUserRole());
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
