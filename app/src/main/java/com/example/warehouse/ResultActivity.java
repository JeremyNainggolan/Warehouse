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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.warehouse.network.AppUrl;
import com.example.warehouse.network.InternetReceiver;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONObject;

/**
 * The ResultActivity class is responsible for displaying the result of a scanned item.
 * It retrieves the item details from the server and displays them on the screen.
 * It also provides functionality for confirming the changes made to the item and launching the scanner.
 *
 * @author JN
 * @date 13 June 2024
 */
public class ResultActivity extends AppCompatActivity {
    protected String id;

    private TextView batchNumber, materialNumber, materialName, materialType, materialQuantity, materialLocation;
    private ImageView home, qrScan;
    private CardView cardMove, cardBack;
    private FrameLayout resultLoad;
    private RelativeLayout resultUnLoad;
    private SwipeRefreshLayout refreshLayout;
    private BroadcastReceiver broadcastReceiver = null;

    /**
     * Called when the activity is starting or being recreated.
     * Initializes the activity, sets the layout, and handles the logic for refreshing the layout.
     *
     * @param savedInstanceState The saved instance state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        construct();
        broadcastReceiver = new InternetReceiver();
        internetStatus();

        Intent getId = getIntent();
        this.id = getId.getStringExtra("batch_number");
        String url = AppUrl.GET_BATCH + id;

        RequestQueue queue = Volley.newRequestQueue(this);
        @SuppressLint("SetTextI18n") StringRequest request = new StringRequest(Request.Method.GET, url, s -> {
            try {
                setVisibility(true, false);
                JSONObject jsonObject = new JSONObject(s);
                int status = Integer.parseInt(jsonObject.getString("status"));
                switch (status) {
                    case 201:
                        internetStatus();
                        Toast.makeText(ResultActivity.this, "item is not found", Toast.LENGTH_LONG).show();
                        break;
                    case 210:
                        try {
                            setVisibility(false, true);
                            JSONObject result = new JSONObject(jsonObject.getString("body"));
                            String itemStatus = result.getString("status");

                            if (result.getString("item_type").equals("material")) {
                                materialType.setText("Raw Material");
                            } else if (result.getString("item_type").equals("goods")) {
                                materialType.setText("Finish Goods");
                            } else {
                                materialType.setText("Unknown");
                            }

                            if (result.getString("area_name").equals("null")) {
                                materialLocation.setText("None");
                            } else {
                                materialLocation.setText(result.getString("area_name"));
                            }

                            switch (materialLocation.getText().toString()) {
                                case "Production":
                                case "Delivered":
                                    cardMove.setVisibility(View.GONE);
                                    break;
                                default:
                                    cardMove.setVisibility(View.VISIBLE);
                                    break;
                            }

                            if (itemStatus.equals("delivered")) {
                                Log.d("STATUS", itemStatus);
                                cardMove.setVisibility(View.GONE);
                            }

                            batchNumber.setText(result.getString("batch_number"));
                            materialNumber.setText(result.getString("item_id"));
                            materialName.setText(result.getString("item_name"));
                            materialQuantity.setText(result.getString("quantity"));


                        } catch (Exception e) {
                            Toast.makeText(ResultActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            } catch (Throwable throwable) {
                setVisibility(false, true);
                Toast.makeText(ResultActivity.this, throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, volleyError -> {

        });
        queue.add(request);

        qrScan.setOnClickListener(v ->
                scanProcessing()
        );

        refreshLayout.setOnRefreshListener(() -> new android.os.Handler().postDelayed(
                () -> {
                    refreshLayout.setEnabled(true);
                    refreshLayout.setRefreshing(false);
                },
                2000
        ));

        home.setOnClickListener(v -> {
            Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);
        });


        cardMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent area = new Intent(getApplicationContext(), AreaActivity.class);
                area.putExtra("batch_number", batchNumber.getText());
                area.putExtra("MODE", "RESULT_MODE");
                startActivity(area);
            }
        });

        cardBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(getApplicationContext(), MainActivity.class);
                back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(back);
            }
        });


    }

    protected void construct() {
        cardMove = findViewById(R.id.cardMove);
        cardBack = findViewById(R.id.cardBack);
        home = findViewById(R.id.dashboard);
        qrScan = findViewById(R.id.resultScanner);
        batchNumber = findViewById(R.id.batchDetail);
        materialNumber = findViewById(R.id.idDetail);
        materialName = findViewById(R.id.nameDetail);
        materialType = findViewById(R.id.materialType);
        materialQuantity = findViewById(R.id.qtyDetail);
        materialLocation = findViewById(R.id.locDetail);
        refreshLayout = findViewById(R.id.resultRefresh);

        resultLoad = findViewById(R.id.result_load);
        resultUnLoad = findViewById(R.id.result_unload);
    }

    protected void setVisibility(boolean _load, boolean _unload) {
        if (_load) {
            this.resultLoad.setVisibility(View.VISIBLE);
            this.resultUnLoad.setVisibility(View.GONE);
        } else if (_unload) {
            this.resultLoad.setVisibility(View.GONE);
            this.resultUnLoad.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Checks the internet status and registers a broadcast receiver to listen for connectivity changes.
     */
    private void internetStatus() {
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * Performs the processing for scanning a barcode.
     * This method sets up the scan options, including the desired barcode formats,
     * beep sound, orientation lock, capture activity, and prompt message.
     * It then launches the scanning process.
     */
    protected void scanProcessing() {
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scanOptions.setBeepEnabled(false);
        scanOptions.setOrientationLocked(true);
        scanOptions.setCaptureActivity(CaptureAct.class);
        scanOptions.setPrompt("Volume button to toggle Flash On/Off");
        launcher.launch(scanOptions);
    }

    /**
     * This method registers an activity result launcher for scanning options and handles the result.
     * If the result contains valid contents, it calls the getResult() method.
     *
     * @param launcher The activity result launcher for scanning options.
     */
    ActivityResultLauncher<ScanOptions> launcher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            getResult(result.getContents());
        }
    });


    /**
     * Retrieves the result based on the given contents and starts the ResultActivity.
     *
     * @param contents the batch number used to retrieve the result
     */
    protected void getResult(String contents) {
        Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("batch_number", contents);
        startActivity(intent);
    }
}