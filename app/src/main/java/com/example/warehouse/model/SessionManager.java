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

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(myPreferences, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * function createSession
     * return void
     * parameter String id - the ID of the logged-in user
     *           String email - the email of the logged-in user
     *           String name - the name of the logged-in user
     * This function creates a user session by saving the user's login status, ID,
     * email, and name in shared preferences. The session data is committed to persist
     * the changes.
     * author : JN
     * date : 19 June 2024
     */
    public void createSession(String id, String email, String name) {
        editor.putBoolean(LOGGED, true);
        editor.putString(USERID, id);
        editor.putString(NAME, name);
        editor.putString(EMAIL, email);
        editor.commit();
    }

    /**
     * function checkLogin
     * return void
     * parameter none
     * This function checks the login status of the user. If the user is not logged in,
     * it redirects them to the LoginActivity. If the user is logged in, it redirects
     * them to the MainActivity. The redirection clears the activity stack to ensure
     * a fresh start.
     * author : JN
     * date : 19 June 2024
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
     * function is_login
     * return boolean
     * parameter none
     * This private method checks whether the user is logged in by retrieving the
     * boolean value stored in shared preferences.
     * author : JN
     * date : 19 June 2024
     */
    private boolean is_login() {
        return pref.getBoolean(LOGGED, false);
    }

    /**
     * function logout
     * return void
     * parameter none
     * This function logs out the user by clearing the session data stored in shared preferences
     * and redirecting them to the LoginActivity. The redirection clears the activity stack to ensure
     * a fresh start after logout.
     * author : JN
     * date : 19 June 2024
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
     * function getUserDetails
     * return HashMap<String, String>
     * parameter none
     * This static method retrieves and returns user details stored in shared preferences,
     * including user ID, name, and email.
     * author : JN
     * date : 19 June 2024
     */
    public static HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(USERID, pref.getString(USERID, null));
        user.put(NAME, pref.getString(NAME, null));
        user.put(EMAIL, pref.getString(EMAIL, null));
        return user;
    }
}
