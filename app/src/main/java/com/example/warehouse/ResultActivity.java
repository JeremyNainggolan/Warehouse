package com.example.warehouse;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.warehouse.model.SessionManager;
import com.example.warehouse.network.AppUrl;
import com.example.warehouse.network.InternetReceiver;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The ResultActivity class is responsible for displaying the result of a scanned item.
 * It retrieves the item details from the server and displays them on the screen.
 * It also provides functionality for confirming the changes made to the item and launching the scanner.
 *
 * @author JN
 * @date 13 June 2024
 */
public class ResultActivity extends AppCompatActivity {

    private TextView batchNumber;
    private TextView materialNumber;
    private TextView materialName;
    private TextView materialType;
    private TextView materialQuantity;
    private TextView materialLocation;
    private TextView textBuilding;
    private TextView textArea;
    private LinearLayout buildingLayout, areaLayout;
    private CardView cardBuilding, cardArea, cardSave,frmInfo;

    private SwipeRefreshLayout refreshLayout;

    private BroadcastReceiver broadcastReceiver = null;

    private SessionManager sessionManager;
    private FrameLayout pbLoading;

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

        sessionManager = new SessionManager(getApplicationContext());

        ImageView home = findViewById(R.id.dashboard);
        ImageView qrScan = findViewById(R.id.resultScanner);

        broadcastReceiver = new InternetReceiver();
        Internetstatus();

        batchNumber = findViewById(R.id.batchDetail);
        materialNumber = findViewById(R.id.idDetail);
        materialName = findViewById(R.id.nameDetail);
        materialType = findViewById(R.id.materialType);
        materialQuantity = findViewById(R.id.qtyDetail);
        materialLocation = findViewById(R.id.locDetail);
        pbLoading = findViewById(R.id.pbLoading);
        frmInfo = findViewById(R.id.frmInfo);
        refreshLayout = findViewById(R.id.resultRefresh);
        refreshLayout.setOnRefreshListener(() -> new android.os.Handler().postDelayed(
                () -> {
                    refreshLayout.setEnabled(true);
                    refreshLayout.setRefreshing(false);
                },
                2000 // Delay in milliseconds
        ));

        Intent intent = getIntent();
        String id = intent.getStringExtra("batch_number");
        String url = AppUrl.GET_BATCH + id;

        RequestQueue queue = Volley.newRequestQueue(this);
        @SuppressLint("SetTextI18n") StringRequest request = new StringRequest(Request.Method.GET, url, s -> {
            try {
                pbLoading.setVisibility(View.VISIBLE);
                frmInfo.setVisibility(View.GONE);
                JSONObject jsonObject = new JSONObject(s);
                int status = Integer.parseInt(jsonObject.getString("status"));
                switch (status) {
                    case 201:
                        Internetstatus();
                        Toast.makeText(ResultActivity.this, "item is not found", Toast.LENGTH_LONG).show();
                        break;
                    case 210:
                        try {
                            JSONObject result = new JSONObject(jsonObject.getString("body"));

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

                            batchNumber.setText(result.getString("batch_number"));
                            materialNumber.setText(result.getString("item_id"));
                            materialName.setText(result.getString("item_name"));
                            materialQuantity.setText(result.getString("quantity"));

                            switch (materialLocation.getText().toString()) {
                                case "Production":
                                case "Delivered":
                                    confirmation();
                                    pbLoading.setVisibility(View.GONE);
                                    frmInfo.setVisibility(View.VISIBLE);
                                    break;
                                default:
                                    setupDetail();
                                    break;
                            }

                        } catch (Exception e) {
                            Log.d("TAG-1", e.getMessage());
                            Toast.makeText(ResultActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            } catch (Throwable throwable) {
                pbLoading.setVisibility(View.GONE);
                frmInfo.setVisibility(View.VISIBLE);
                Log.d("TAG", throwable.getMessage().toString());
                Toast.makeText(ResultActivity.this, throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, volleyError -> {

        });
        queue.add(request);


        home.setOnClickListener(v -> {
            Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);
        });


        qrScan.setOnClickListener(v -> scanProcessing());
    }

    /**
     * Checks the internet status and registers a broadcast receiver to listen for connectivity changes.
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
     * Sets up the confirmation functionality.
     * This method sets the text of a button to a confirmation string,
     * makes a card view visible, and sets up a click listener for the card view.
     * When the card view is clicked, it starts the MainActivity.
     */
    private void confirmation() {
        TextView textButton = findViewById(R.id.textSave);
        cardSave = findViewById(R.id.cardSave);

        textButton.setText(R.string.confirmation);
        cardSave.setVisibility(View.VISIBLE);
        cardSave.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });
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

    /**
     * Sets up the detail view for the ResultActivity.
     * This method initializes the necessary views and makes a network request to retrieve area data.
     * It dynamically creates TextViews for each area and adds them to the buildingLayout.
     * It also handles the click events on the TextViews to perform different actions based on the selected area.
     */
    private void setupDetail() {
        cardBuilding = findViewById(R.id.cardBuilding);
        buildingLayout = findViewById(R.id.buildingLayout);

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, AppUrl.GET_AREA, new Response.Listener<String>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject object = new JSONObject(s);
                    int status = Integer.parseInt(object.getString("status"));
                    switch (status) {
                        case 201:
                            break;
                        case 210:
                            JSONObject body = object.getJSONObject("body");

                            Iterator<String> keys = body.keys();

                            while (keys.hasNext()) {
                                JSONObject item = body.getJSONObject(String.valueOf(keys.next()));
                                String id = item.getString("id");
                                String name = item.getString("name");

                                TextView tv = new TextView(getApplicationContext());
                                tv.setText(name);
                                tv.setId(Integer.parseInt(id));
                                tv.setTextColor(Color.parseColor("#FFeb4a4a"));
                                tv.setTextSize(28);
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                lp.setMargins(10, 4, 10, 4);
                                tv.setPadding(0, 50, 0, 50);
                                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                tv.setBackgroundResource(R.drawable.lbl_area);
                                tv.setLayoutParams(lp);

                                buildingLayout.addView(tv);
                                cardBuilding.setVisibility(View.VISIBLE);
                                pbLoading.setVisibility(View.GONE);
                                frmInfo.setVisibility(View.VISIBLE);

                                tv.setOnClickListener(v -> {
                                    switch (name) {
                                        case "Production":
                                            buildingLayout.removeAllViews();
                                            cardBuilding.setVisibility(View.GONE);
                                            setupProduction(id, name);
                                            break;
                                        case "Delivered":
                                            buildingLayout.removeAllViews();
                                            cardBuilding.setVisibility(View.GONE);
                                            setupDelivered(id, name);
                                            break;
                                        default:
                                            buildingLayout.removeAllViews();
                                            cardBuilding.setVisibility(View.GONE);
                                            areaDetail(id, name);
                                            break;
                                    }

                                });

                            }
                            break;
                    }
                } catch (Exception e) {
                    frmInfo.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, volleyError -> {

        });
        queue.add(request);
    }

    /**
     * Displays the area details based on the given building ID and name.
     *
     * @param idBuilding The ID of the building.
     * @param nameBuilding The name of the building.
     */
    private void areaDetail(String idBuilding, String nameBuilding) {
        cardArea = findViewById(R.id.cardArea);
        areaLayout = findViewById(R.id.areaLayout);

        textArea = findViewById(R.id.txtArea);
        textArea.setVisibility(View.GONE);

        textBuilding = findViewById(R.id.txtBuilding);
        textBuilding.setVisibility(View.VISIBLE);
        textBuilding.setText(nameBuilding);

        cardSave = findViewById(R.id.cardSave);
        cardSave.setVisibility(View.GONE);

        String url = AppUrl.GET_AREA + idBuilding;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url, s -> {
            try {
                JSONObject object = new JSONObject(s);
                int status = Integer.parseInt(object.getString("status"));
                switch (status) {
                    case 201:
                        break;
                    case 210:
                        JSONObject body = object.getJSONObject("body");
                        Iterator<String> keys = body.keys();

                        while (keys.hasNext()) {
                            JSONObject item = body.getJSONObject(String.valueOf(keys.next()));
                            String idArea = item.getString("id");
                            String nameArea = item.getString("name");

                            TextView tv = new TextView(getApplicationContext());
                            tv.setText(nameArea);
                            tv.setId(Integer.parseInt(idArea));
                            tv.setTextColor(Color.parseColor("#FFeb4a4a"));
                            tv.setTextSize(28);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(10, 4, 10, 4);
                            tv.setPadding(0, 50, 0, 50);
                            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            tv.setBackgroundResource(R.drawable.lbl_area);
                            tv.setLayoutParams(lp);

                            areaLayout.addView(tv);
                            cardArea.setVisibility(View.VISIBLE);

                            tv.setOnClickListener(v -> {
                                areaLayout.removeAllViews();
                                cardArea.setVisibility(View.GONE);
                                setupArea(idBuilding, idArea, nameBuilding, nameArea);
                            });
                        }
                        break;
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, volleyError -> {

        });
        queue.add(request);

        textBuilding.setOnClickListener(v -> {
            areaLayout.removeAllViews();
            cardArea.setVisibility(View.GONE);
            textBuilding.setVisibility(View.GONE);
            setupDetail();
        });
    }

    /**
     * Sets up the area in the ResultActivity.
     *
     * @param idBuilding   The ID of the building.
     * @param idArea       The ID of the area.
     * @param nameBuilding The name of the building.
     * @param nameArea     The name of the area.
     */
    private void setupArea(String idBuilding, String idArea, String nameBuilding, String nameArea) {
        textArea = findViewById(R.id.txtArea);

        cardSave = findViewById(R.id.cardSave);

        textBuilding = findViewById(R.id.txtBuilding);
        textBuilding.setVisibility(View.VISIBLE);

        textArea.setVisibility(View.VISIBLE);
        textArea.setText(nameArea);
        cardSave.setVisibility(View.VISIBLE);

        textArea.setOnClickListener(v -> {
            textArea.setVisibility(View.GONE);
            cardSave.setVisibility(View.GONE);
            areaDetail(idBuilding, nameBuilding);
        });

        cardSave.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ResultActivity.this);
            builder.setTitle("Confirm?")
                    .setMessage("Are you sure to move this item to " + nameArea + "?")
                    .setPositiveButton("Yes", (dialog, which) -> moveItem(idArea))
                    .setNegativeButton("No", (dialog, which) -> dialog.cancel()).show();
        });
    }

    /**
     * Sets up the delivered state for a location.
     *
     * @param idLocation The ID of the location.
     * @param name The name of the location.
     */
    private void setupDelivered(String idLocation, String name) {
        textBuilding = findViewById(R.id.txtBuilding);

        cardSave = findViewById(R.id.cardSave);

        textArea = findViewById(R.id.txtArea);
        textArea.setVisibility(View.GONE);

        textBuilding.setVisibility(View.VISIBLE);
        textBuilding.setText(name);
        cardSave.setVisibility(View.VISIBLE);

        textBuilding.setOnClickListener(v -> {
            textBuilding.setVisibility(View.GONE);
            setupDetail();
        });

        cardSave.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ResultActivity.this);
            builder.setTitle("Confirm?")
                    .setMessage("Are you sure to move this item to " + name + "?")
                    .setPositiveButton("Yes", (dialog, which) -> moveItem(idLocation))
                    .setNegativeButton("No", (dialog, which) -> dialog.cancel()).show();
        });
    }

    /**
     * Sets up the production based on the given location ID and name.
     *
     * @param idLocation The ID of the location.
     * @param name The name of the location.
     */
    private void setupProduction(String idLocation, String name) {
        textBuilding = findViewById(R.id.txtBuilding);

        cardSave = findViewById(R.id.cardSave);

        textArea = findViewById(R.id.txtArea);
        textArea.setVisibility(View.GONE);

        textBuilding.setVisibility(View.VISIBLE);
        textBuilding.setText(name);
        cardSave.setVisibility(View.VISIBLE);

        textBuilding.setOnClickListener(v -> {
            textBuilding.setVisibility(View.GONE);
            setupDetail();
        });

        cardSave.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ResultActivity.this);
            builder.setTitle("Confirm?")
                    .setMessage("Are you sure to move this item to " + name + "?")
                    .setPositiveButton("Yes", (dialog, which) -> moveItem(idLocation))
                    .setNegativeButton("No", (dialog, which) -> dialog.cancel()).show();
        });
    }

    /**
     * Moves an item to a specified area.
     *
     * @param idArea The ID of the target area.
     */
    private void moveItem(String idArea) {
        HashMap<String, String> userDetails = SessionManager.getUserDetails();

        batchNumber = findViewById(R.id.batchDetail);

        String idBatch = batchNumber.getText().toString();
        String idUser = userDetails.get("idKey");

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, AppUrl.MOVE_AREA, s -> {
            try {
                JSONObject resultJSON = new JSONObject(s);
                int status = Integer.parseInt(resultJSON.getString("status"));
                switch (status) {
                    case 201:

                        break;
                    case 210:
                        try {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            Toast.makeText(getApplicationContext(), resultJSON.getString("message"), Toast.LENGTH_LONG).show();
                            startActivity(intent);
                        } catch (Throwable throwable) {
                            Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        break;
                    default:
                }
            } catch (JSONException jsonException) {
                Toast.makeText(getApplicationContext(), jsonException.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, volleyError -> Toast.makeText(getApplicationContext(), volleyError.getMessage(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("batch_number", idBatch);
                params.put("area_id", idArea);
                params.put("userid", idUser);

                return params;
            }
        };
        queue.add(request);
    }
}