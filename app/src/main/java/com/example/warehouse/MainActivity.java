package com.example.warehouse;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.warehouse.model.SessionManager;
import com.example.warehouse.network.AppUrl;
import com.example.warehouse.network.InternetReceiver;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONObject;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The main activity of the application.
 *
 * @author JN
 * @date 19 June 2024
 */
public class MainActivity extends AppCompatActivity {

    private SessionManager session;
    private LinearLayout mainUnLoad;
    private FrameLayout mainLoad;
    private SwipeRefreshLayout refreshLayout;
    private CardView scanItem, stockTake, logOut;
    private BroadcastReceiver broadcastReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        construct();

        broadcastReceiver = new InternetReceiver();
        internetStatus();

        session = new SessionManager(getApplicationContext());
        scanItem.setOnClickListener(v -> scanProcessing());
//        scanItem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent temp = new Intent(getApplicationContext(), AreaActivity.class);
//                temp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                temp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(temp);
//            }
//        });
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


        refreshLayout.setOnRefreshListener(() -> new android.os.Handler().postDelayed(
                () -> {
                    refreshLayout.setEnabled(true);
                    refreshLayout.setRefreshing(false);
                },
                2000 // Delay in milliseconds
        ));
    }

    protected void construct() {
        logOut = findViewById(R.id.cardOut);
        scanItem = findViewById(R.id.cardQR);
        stockTake = findViewById(R.id.cardSTK);
        mainLoad = findViewById(R.id.main_load);
        mainUnLoad = findViewById(R.id.main_unload);
        refreshLayout = findViewById(R.id.mainRefresh);
    }

    private void internetStatus() {
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    /**
     * Launches the scanner layout from the zxing library.
     * The result will be passed to the `getResult` function.
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
            setVisibility(true, false);
            checkItem(result.getContents());
        }
    });

    private void checkItem(String _content) {
        String URL = AppUrl.GET_BATCH + _content;
        RequestQueue queue = Volley.newRequestQueue(this);
        @SuppressLint("SetTextI18n") StringRequest request = new StringRequest(Request.Method.GET, URL, s -> {
            try {
                JSONObject jsonObject = new JSONObject(s);
                int status = Integer.parseInt(jsonObject.getString("status"));
                switch (status) {
                    case 201:
                        setVisibility(false, true);
                        Log.d("TAG", "FALSE");
                        showAlert("MOVE_ITEM");
                        break;
                    case 210:
//                        setVisibility(false, true);
                        Log.d("TAG", "TRUE");
                        Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                        intent.putExtra("batch_number", _content);
                        startActivity(intent);
                        break;
                }
            } catch (Throwable throwable) {
                setVisibility(false, true);
                Log.d("Throwable", Objects.requireNonNull(throwable.getMessage()));
            }
        }, volleyError -> {

        });
        queue.add(request);
    }

    /**
     * Launches the stock take activity.
     * The result will be passed to the `getResult` function.
     */
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
            setVisibility(true, false);
            checkStockTake(result.getContents());
        }
    });

    protected void setVisibility(boolean _load, boolean _unload) {
        if (_load) {
            this.mainLoad.setVisibility(View.VISIBLE);
            this.mainUnLoad.setVisibility(View.GONE);
        } else if (_unload) {
            this.mainLoad.setVisibility(View.GONE);
            this.mainUnLoad.setVisibility(View.VISIBLE);
        }
    }

    protected void showAlert(String _mode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        if (_mode.equals("STOCK_TAKE")) {
            builder.setTitle("Stock Take Event")
                    .setMessage("Stock Take Event is not active");
        } else if (_mode.equals("MOVE_ITEM")) {
            builder.setTitle("Move Item")
                    .setMessage("Unknown Batch Number");
        }
        builder.setPositiveButton("Yes", (dialog, which) -> dialog.cancel()).show();
    }

    protected void checkStockTake(String _content) {
        String URL = AppUrl.CHECK + _content;
        RequestQueue queue = Volley.newRequestQueue(this);
        @SuppressLint("SetTextI18n") StringRequest request = new StringRequest(Request.Method.GET, URL, s -> {
            try {
                JSONObject jsonObject = new JSONObject(s);
                int status = Integer.parseInt(jsonObject.getString("status"));
                switch (status) {
                    case 201:
                        setVisibility(false, true);
                        Log.d("TAG", "FALSE");
                        showAlert("STOCK_TAKE");
                        break;
                    case 210:
//                        setVisibility(false, true);
                        Log.d("TAG", "TRUE");
                        Intent intent = new Intent(getApplicationContext(), StockTakeActivity.class);
                        intent.putExtra("batch_number", _content);
                        startActivity(intent);
                        break;
                }
            } catch (Throwable throwable) {
                setVisibility(false, true);
                Log.d("Throwable", Objects.requireNonNull(throwable.getMessage()));
            }
        }, volleyError -> {

        });
        queue.add(request);
    }

    /**
     * Performs the logout processing.
     */
    protected void logoutProcessing() {
        session.logout();
    }
}