package com.example.warehouse;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.legacy.widget.Space;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.warehouse.model.SessionManager;
import com.example.warehouse.network.AppUrl;
import com.example.warehouse.network.InternetReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * The StockTakeActivity class is responsible for managing the stock take process in the warehouse.
 * It allows the user to view and edit stock take information, confirm stock take, and navigate to other activities.
 *
 * @author JN
 * @date 19 June 2024
 */
public class StockTakeActivity extends AppCompatActivity {

    private String stockTakeId, areaId, batchId;
    private ImageView homeButton;
    private TextView batchNumber, areaName, materialName, buildingName, itemType;
    private CardView stockTakeConfirm, moveStockTake, isChecked, backHome, backStockTake;
    private BroadcastReceiver broadcastReceiver = null;
    private SwipeRefreshLayout refreshLayout;
    private RelativeLayout stockTakeUnLoad;
    private FrameLayout stockTakeLoad;
    private Space spaceNotActive, spaceNotActive_2, spaceIsActive;

    /**
     * Called when the activity is starting or being recreated.
     * Initializes the activity, sets the layout, and handles various click events.
     * Retrieves data from the intent and makes a network request to fetch additional information.
     *
     * @param savedInstanceState The saved instance state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_take);

        construct();
        broadcastReceiver = new InternetReceiver();
        internetStatus();

        Intent intent = getIntent();
        this.batchId = intent.getStringExtra("batch_number");
        String url = AppUrl.CHECK + batchId;
        setup(url);

        homeButton.setOnClickListener(v -> {
            Intent home = new Intent(getApplicationContext(), MainActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(home);
        });

        refreshLayout.setOnRefreshListener(() -> new android.os.Handler().postDelayed(
                () -> {
                    refreshLayout.setEnabled(true);
                    refreshLayout.setRefreshing(false);
                },
                2000
        ));


        stockTakeConfirm.setOnClickListener(v -> {
            showAlert();
        });

        backHome.setOnClickListener(v -> {
            goHome();
        });

        backStockTake.setOnClickListener(v -> {
            goHome();
        });

        moveStockTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent move = new Intent(getApplicationContext(), AreaActivity.class);
                move.putExtra("batch_number", batchId);
                move.putExtra("MODE", "STOCK_MODE");
                startActivity(move);
            }
        });

    }

    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StockTakeActivity.this);
        builder.setTitle("Confirm?")
                .setMessage("Are you sure to confirm stock take for " + materialName.getText().toString() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    confirm(batchId, areaId, stockTakeId);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.cancel()).show();
    }

    private void goHome() {
        Intent home = new Intent(getApplicationContext(), MainActivity.class);
        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(home);
    }

    private void internetStatus() {
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    protected void isLoad (boolean _load, boolean _unload) {
        if (_load) {
            this.stockTakeLoad.setVisibility(View.VISIBLE);
            this.stockTakeUnLoad.setVisibility(View.GONE);
        } else if (_unload) {
            this.stockTakeLoad.setVisibility(View.GONE);
            this.stockTakeUnLoad.setVisibility(View.VISIBLE);
        }
    }

    protected void setup(String _url) {
        RequestQueue queue = Volley.newRequestQueue(this);
        @SuppressLint("SetTextI18n") StringRequest request = new StringRequest(Request.Method.GET, _url, s -> {
            try {
                isLoad(true, false);
                JSONObject jsonObject = new JSONObject(s);
                int status = Integer.parseInt(jsonObject.getString("status"));
                switch (status) {
                    case 201:
                        isLoad(false, true);
                        internetStatus();
                        Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                        break;
                    case 210:
                        isLoad(false, true);
                        internetStatus();
                        try {
                            JSONObject result = new JSONObject(jsonObject.getString("body"));
                            int stockTakeStatus = Integer.parseInt(result.getString("status"));
                            setVisibility(stockTakeStatus != 0);
                            itemType.setAllCaps(true);
                            batchNumber.setText(result.getString("batch_number"));
                            areaName.setText(result.getString("area_name"));
                            materialName.setText(result.getString("item_name"));
                            itemType.setText(result.getString("item_type"));
                            buildingName.setText(result.getString("building_name"));

                            this.stockTakeId = result.getString("stock_take_id");
                            this.areaId = result.getString("area_id");
                        } catch (Exception e) {
                            Toast.makeText(StockTakeActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            } catch (Throwable throwable) {
                isLoad(false, true);
                Toast.makeText(StockTakeActivity.this, throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, volleyError -> {
        });
        queue.add(request);
    }

    private void confirm(String _batchNumber, String _idArea, String _idStockTake) {
        HashMap<String, String> userDetails = SessionManager.getUserDetails();
        String idUser = userDetails.get("idKey");

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, AppUrl.STOCK_TAKE, s -> {
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
                params.put("batch_number", _batchNumber);
                params.put("area_id", _idArea);
                params.put("userid", idUser);
                params.put("stock_take_id", _idStockTake);

                return params;
            }
        };
        queue.add(request);
    }

    protected void construct() {
        this.homeButton = findViewById(R.id.dashboardstcktake);
        this.refreshLayout = findViewById(R.id.stockRefresh);

        this.batchNumber = findViewById(R.id.idDetail);
        this.materialName = findViewById(R.id.materialName);
        this.buildingName = findViewById(R.id.buildingDetail);
        this.areaName = findViewById(R.id.areaDetail);
        this.itemType = findViewById(R.id.itemType);

        this.isChecked = findViewById(R.id.isChecked);
        this.stockTakeConfirm = findViewById(R.id.stockTakeConfirm);
        this.moveStockTake = findViewById(R.id.moveStockTake);
        this.backHome = findViewById(R.id.backHome);
        this.backStockTake = findViewById(R.id.backStockTake);

        this.spaceIsActive = findViewById(R.id.spaceIsActive);
        this.spaceNotActive = findViewById(R.id.spaceNotActive);
        this.spaceNotActive_2 = findViewById(R.id.spaceNotActive_2);

        this.stockTakeLoad = findViewById(R.id.stocktake_load);
        this.stockTakeUnLoad = findViewById(R.id.stocktake_unload);

    }

    protected void setVisibility(Boolean _isChecked) {
        if (_isChecked) {
            this.spaceIsActive.setVisibility(View.VISIBLE);
            this.isChecked.setVisibility(View.VISIBLE);
            this.backHome.setVisibility(View.VISIBLE);
            this.spaceNotActive.setVisibility(View.GONE);
            this.spaceNotActive_2.setVisibility(View.GONE);
            this.stockTakeConfirm.setVisibility(View.GONE);
            this.moveStockTake.setVisibility(View.GONE);
            this.backStockTake.setVisibility(View.GONE);
        } else {
            this.isChecked.setVisibility(View.GONE);
            this.spaceIsActive.setVisibility(View.GONE);
            this.backHome.setVisibility(View.GONE);
            this.spaceNotActive.setVisibility(View.VISIBLE);
            this.spaceNotActive_2.setVisibility(View.VISIBLE);
            this.moveStockTake.setVisibility(View.VISIBLE);
            this.backStockTake.setVisibility(View.VISIBLE);
            this.stockTakeConfirm.setVisibility(View.VISIBLE);
        }
    }
}