package com.example.progetto.data.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class LoginUtils {

    private static final String PREFS_NAME = "login_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_IS_LOGGED_IN_WITH_GOOGLE = "is_logged_in_with_google";

    // Aggiunto il tag per il log
    private static final String TAG = "LoginUtils";

    public static void saveLoginState(Context context, boolean isLoggedIn, String displayName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putString(KEY_DISPLAY_NAME, displayName);
        editor.apply();
    }

    public static void saveGoogleLoginState(Context context, boolean isLoggedIn) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN_WITH_GOOGLE, isLoggedIn);
        editor.apply();
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        boolean isLoggedInWithGoogle = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN_WITH_GOOGLE, false);

        // Stampa nei log dove è loggato l'utente
        if (isLoggedInWithGoogle) {
            Log.d(TAG, "L'utente è loggato tramite Google.");
        } else if (isLoggedIn) {
            Log.d(TAG, "L'utente è loggato normalmente.");
        } else {
            Log.d(TAG, "L'utente non è loggato.");
        }

        return isLoggedIn || isLoggedInWithGoogle;
    }

    public static String getDisplayName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_DISPLAY_NAME, null);
    }

    public static void clearLoginState(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
