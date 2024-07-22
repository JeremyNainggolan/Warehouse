package com.example.warehouse;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.warehouse.model.SessionManager;
import com.example.warehouse.network.AppUrl;
import com.example.warehouse.network.InternetReceiver;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The LoginActivity class is responsible for handling user login functionality.
 * It sends a POST request with the user's email and password to the server and processes the server response.
 * On successful login, it creates a user session and redirects to the MainActivity.
 * This class extends the AppCompatActivity class.
 * 
 * @author JN
 * @date 19 June 2024
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText email, password;

    private SessionManager session;
    BroadcastReceiver broadcastReceiver = null;
    SwipeRefreshLayout refreshLayout;

    /**
     * Called when the activity is starting or being recreated.
     * Initializes the activity and sets up the user interface.
     *
     * @param savedInstanceState The saved instance state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        broadcastReceiver = new InternetReceiver();
        Internetstatus();

        email = findViewById(R.id.username);
        password = findViewById(R.id.password);
        Button login = findViewById(R.id.login);

        session = new SessionManager(getApplicationContext());

        login.setOnClickListener(v -> {
            if (Objects.requireNonNull(email.getText()).toString().isEmpty() || Objects.requireNonNull(password.getText()).toString().isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please input a valid username or password", Toast.LENGTH_LONG).show();
            } else {
                processLogin(email.getText().toString(), password.getText().toString());
            }
        });

        refreshLayout = findViewById(R.id.loginRefresh);

        refreshLayout.setOnRefreshListener(() -> new android.os.Handler().postDelayed(
                () -> {
                    refreshLayout.setEnabled(true);
                    refreshLayout.setRefreshing(false);
                },
                2000 // Delay in milliseconds
        ));
    }

    /**
     * Registers a broadcast receiver to monitor the internet connectivity status.
     */
    private void Internetstatus() {
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * Processes the login with the given email and password.
     *
     * @param email    The email of the user.
     * @param password The password of the user.
     */
    private void processLogin(String email, String password) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, AppUrl.LOGIN_URL, s -> {
            try {
                JSONObject resultJSON = new JSONObject(s);
                int status = Integer.parseInt(resultJSON.getString("status"));
                switch (status) {
                    case 201:
                        Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_LONG).show();
                        break;
                    case 210:
                        try {
                            JSONObject result = new JSONObject(resultJSON.getString("body"));
                            session.createSession(
                                    result.getString("id"),
                                    result.getString("name"),
                                    password
                            );
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (Throwable throwable) {
                            Toast.makeText(LoginActivity.this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        break;
                    default:
                }
            } catch (JSONException jsonException) {
                Toast.makeText(LoginActivity.this, jsonException.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("JSON", jsonException.getMessage());
            }
        }, volleyError -> Toast.makeText(LoginActivity.this, volleyError.getMessage(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };
        queue.add(request);
    }
}