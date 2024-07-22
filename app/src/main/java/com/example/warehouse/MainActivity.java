package com.example.warehouse;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.warehouse.model.SessionManager;
import com.example.warehouse.network.InternetReceiver;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;
    private BroadcastReceiver broadcastReceiver = null;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        broadcastReceiver = new InternetReceiver();
        Internetstatus();

        CardView scanItem = findViewById(R.id.cardQR);
        CardView stockTake = findViewById(R.id.cardSTK);
        CardView logOut = findViewById(R.id.cardOut);

        session = new SessionManager(getApplicationContext());

        scanItem.setOnClickListener(v -> scanProcessing());
        stockTake.setOnClickListener(v -> stockProcessing());
        logOut.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Confirm?")
                    .setMessage("Are you sure to logout from the application?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        logoutProcessing();
                        finish();

                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.cancel()).show();
        });


        refreshLayout = findViewById(R.id.mainRefresh);
        refreshLayout.setOnRefreshListener(() -> new android.os.Handler().postDelayed(
                () -> {
                    refreshLayout.setEnabled(true);
                    refreshLayout.setRefreshing(false);
                },
                2000 // Delay in milliseconds
        ));
    }

    private void Internetstatus() {
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        unregisterReceiver(broadcastReceiver);
//    }

    /**
     * function scanProcessing
     * return void
     * parameter none
     * To launch the scanner layout from the zxing library
     *  which will be described through the launcher function
     *  and will be passed in the getResult function.
     * author : JN
     * date : 13 June 2024
     */
    protected void scanProcessing() {
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scanOptions.setBeepEnabled(false);
        scanOptions.setOrientationLocked(true);
        scanOptions.setCaptureActivity(CaptureAct.class);
        scanOptions.setPrompt("Volume button to toggle Flash On/Off");
        scan.launch(scanOptions);
    }

    ActivityResultLauncher<ScanOptions> scan = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
            intent.putExtra("batch_number", result.getContents());
            startActivity(intent);
        }
    });

    protected void stockProcessing() {
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scanOptions.setBeepEnabled(false);
        scanOptions.setOrientationLocked(true);
        scanOptions.setCaptureActivity(CaptureAct.class);
        scanOptions.setPrompt("Volume button to toggle Flash On/Off");
        stock.launch(scanOptions);
    }

    ActivityResultLauncher<ScanOptions> stock = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            Intent intent = new Intent(getApplicationContext(), StockTakeActivity.class);
            intent.putExtra("batch_number", result.getContents());
            startActivity(intent);
        }
    });

    protected void logoutProcessing() {
        session.logout();
    }
}