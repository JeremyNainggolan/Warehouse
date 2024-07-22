package com.example.warehouse;

import static com.example.warehouse.network.AppUrl.BASE_URL;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.warehouse.model.SessionManager;
import com.example.warehouse.network.HttpTaskListener;
import com.example.warehouse.network.InternetReceiver;
import com.example.warehouse.network.MyHttpTask;

/**
 * The SplashActivity class represents the splash screen of the application.
 * It is responsible for checking internet connectivity, making HTTP requests,
 * and handling the completion of the HTTP task.
 * @author JN
 * @date 19 June 2024
 */
public class SplashActivity extends AppCompatActivity implements HttpTaskListener {

    SessionManager sessionManager;
    MyHttpTask httpTask;
    BroadcastReceiver broadcastReceiver = null;
    SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        broadcastReceiver = new InternetReceiver();
        Internetstatus();

        httpTask = new MyHttpTask(this);
        httpTask.execute(BASE_URL);

        refreshLayout = findViewById(R.id.splashRefresh);

        refreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(
                () -> {
                    refreshLayout.setEnabled(true);
                    refreshLayout.setRefreshing(false);
                },
                2000 // Delay in milliseconds
        ));
    }

    /**
     * Registers the broadcast receiver to listen for changes in internet connectivity.
     */
    private void Internetstatus() {
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onTaskComplete(String result) {
        boolean Result = httpTask.getResult();
        if (Result) {
            sessionManager = new SessionManager(getApplicationContext());
            Handler handler = new Handler();
            int SPLASH_SCREEN = 3000;
            handler.postDelayed(() -> {
                sessionManager.checkLogin();
                finish();
            }, SPLASH_SCREEN);
        } else {
            Toast.makeText(getApplicationContext(), "Contact ur IT Support", Toast.LENGTH_LONG).show();
        }
    }
}