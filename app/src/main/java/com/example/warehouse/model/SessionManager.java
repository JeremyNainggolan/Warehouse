package com.example.warehouse.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.warehouse.LoginActivity;
import com.example.warehouse.MainActivity;

import java.util.HashMap;
public class SessionManager {
    static SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;
    public static final String myPreferences = "Warehouse";
    public static final String USERID = "idKey";
    public static final String NAME = "nameKey";
    public static final String EMAIL = "emailKey";
    public static final String LOGGED = "statusKey";

    /**
     * Manages the session for the application.
     */
    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(myPreferences, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Creates a session for the user with the specified ID, email, and name.
     *
     * @param id    The ID of the user.
     * @param email The email of the user.
     * @param name  The name of the user.
     */
    public void createSession(String id, String email, String name) {
        editor.putBoolean(LOGGED, true);
        editor.putString(USERID, id);
        editor.putString(NAME, name);
        editor.putString(EMAIL, email);
        editor.commit();
    }

    /**
     * Checks if the user is logged in. If not, it starts the LoginActivity.
     * If the user is already logged in, it starts the MainActivity.
     */
    public void checkLogin() {
        if (!this.is_login()) {
            Intent i = new Intent(context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } else {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    /**
     * Checks if the user is logged in.
     *
     * @return true if the user is logged in, false otherwise.
     */
    private boolean is_login() {
        return pref.getBoolean(LOGGED, false);
    }

    /**
     * Clears the session data and logs out the user.
     * This method clears the session data, starts the LoginActivity,
     * and clears all previous activities from the task stack.
     */
    public void logout() {
        editor.clear();
        editor.commit();
        Intent i = new Intent(context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    /**
     * Retrieves the user details from the shared preferences.
     *
     * @return A HashMap containing the user details, where the keys are the field names and the values are the corresponding values.
     */
    public static HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(USERID, pref.getString(USERID, null));
        user.put(NAME, pref.getString(NAME, null));
        user.put(EMAIL, pref.getString(EMAIL, null));
        return user;
    }
}
