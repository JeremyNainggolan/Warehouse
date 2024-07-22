package com.example.warehouse;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StockTakeActivity extends AppCompatActivity {

    private ImageView home;
    private TextView batchNumber, areaID, materialName, buildingName, buildShow, areaName, changeArea, itemType;
    private CardView confirm, edit, cancel, editArea, stockTakeInfo;
    private String stockTakeId;
    private LinearLayout areaLayout;
    private BroadcastReceiver broadcastReceiver = null;
    private SwipeRefreshLayout refreshLayout;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_take);

        sessionManager = new SessionManager(getApplicationContext());

        home = findViewById(R.id.dashboardstcktake);
        home.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        broadcastReceiver = new InternetReceiver();
        internetStatus();

        refreshLayout = findViewById(R.id.stockRefresh);
        refreshLayout.setOnRefreshListener(() -> new android.os.Handler().postDelayed(
                () -> {
                    refreshLayout.setEnabled(true);
                    refreshLayout.setRefreshing(false);
                },
                2000 // Delay in milliseconds
        ));

        confirm = findViewById(R.id.cardConfirm);
        confirm.setOnClickListener(v -> confirm());

        cancel = findViewById(R.id.cardCancel);
        editArea = findViewById(R.id.editArea);
        areaLayout = findViewById(R.id.editLayArea);

        cancel.setOnClickListener(v -> {
            areaLayout.removeAllViews();
            editArea.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
            edit.setVisibility(View.VISIBLE);
        });

        edit = findViewById(R.id.cardEdit);

        edit.setOnClickListener(v -> {
            edit.setVisibility(View.GONE);
            cancel.setVisibility(View.VISIBLE);
            editAreaSetup();
        });

        Intent intent = getIntent();
        String batch = intent.getStringExtra("batch_number");

        batchNumber = findViewById(R.id.idDetail);
        areaID = findViewById(R.id.idArea);
        materialName = findViewById(R.id.materialName);
        buildingName = findViewById(R.id.buildingDetail);
        areaName = findViewById(R.id.areaDetail);
        buildShow = findViewById(R.id.buildShow);
        itemType = findViewById(R.id.itemType);

        String url = AppUrl.CHECK + batch;

        RequestQueue queue = Volley.newRequestQueue(this);
        @SuppressLint("SetTextI18n") StringRequest request = new StringRequest(Request.Method.GET, url, s -> {
            try {
                JSONObject jsonObject = new JSONObject(s);
                int status = Integer.parseInt(jsonObject.getString("status"));
                switch (status) {
                    case 201:
                        internetStatus();
                        Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                        break;
                    case 210:
                        internetStatus();
                        try {
                            JSONObject result = new JSONObject(jsonObject.getString("body"));
                            int getStatus = Integer.parseInt(result.getString("status"));
                            areaName.setText(result.getString("area_name"));
                            if (getStatus == 0) {
                                if (areaName.getText().toString().equals("Production") || areaName.getText().toString().equals("Delivered")) {
                                    if (result.getString("item_type").equals("material")) {
                                        itemType.setText("Raw Material");
                                    } else if (result.getString("item_type").equals("goods")) {
                                        itemType.setText("Finish Good");
                                    }
                                    batchNumber.setText(result.getString("batch_number"));
                                    materialName.setText(result.getString("item_name"));
                                    buildingName.setVisibility(View.GONE);
                                    buildShow.setVisibility(View.GONE);
                                    areaID.setText(result.getString("area_id"));
                                    stockTakeId = result.getString("stock_take_id");
                                    edit.setVisibility(View.GONE);
                                    confirm.setVisibility(View.GONE);
                                } else {
                                    if (result.getString("item_type").equals("material")) {
                                        itemType.setText("Raw Material");
                                    } else if (result.getString("item_type").equals("goods")) {
                                        itemType.setText("Finish Good");
                                    }
                                    batchNumber.setText(result.getString("batch_number"));
                                    materialName.setText(result.getString("item_name"));
                                    buildingName.setText(result.getString("building_name"));
                                    areaID.setText(result.getString("area_id"));
                                    stockTakeId = result.getString("stock_take_id");
                                }
                            } else {
                                stockTakeInfo = findViewById(R.id.stock_take_info);
                                stockTakeInfo.setVisibility(View.VISIBLE);
                                edit.setVisibility(View.GONE);
                                confirm.setVisibility(View.GONE);

                                if (areaName.getText().toString().equals("Production") || areaName.getText().toString().equals("Delivered")) {
                                    if (result.getString("item_type").equals("material")) {
                                        itemType.setText("Raw Material");
                                    } else if (result.getString("item_type").equals("goods")) {
                                        itemType.setText("Finish Good");
                                    }
                                    batchNumber.setText(result.getString("batch_number"));
                                    materialName.setText(result.getString("item_name"));
                                    buildingName.setVisibility(View.GONE);
                                    buildShow.setVisibility(View.GONE);
                                    areaID.setText(result.getString("area_id"));
                                    stockTakeId = result.getString("stock_take_id");
                                } else {
                                    if (result.getString("item_type").equals("material")) {
                                        itemType.setText("Raw Material");
                                    } else if (result.getString("item_type").equals("goods")) {
                                        itemType.setText("Finish Good");
                                    }
                                    batchNumber.setText(result.getString("batch_number"));
                                    materialName.setText(result.getString("item_name"));
                                    buildingName.setText(result.getString("building_name"));
                                    areaID.setText(result.getString("area_id"));
                                    stockTakeId = result.getString("stock_take_id");
                                }

                                stockTakeInfo.setOnClickListener(v -> {
                                    Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                                    intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent1);
                                });
                            }
                        } catch (Exception e) {
                            Toast.makeText(StockTakeActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            } catch (Throwable throwable) {
                Toast.makeText(StockTakeActivity.this, throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, volleyError -> {
        });
        queue.add(request);
    }

    private void internetStatus() {
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private void editAreaSetup() {
        editArea = findViewById(R.id.editArea);
        areaLayout = findViewById(R.id.editLayArea);

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

                                areaLayout.addView(tv);
                                editArea.setVisibility(View.VISIBLE);

                                tv.setOnClickListener(v -> {
                                    switch (name) {
                                        case "Production":
                                        case "Delivered":
                                            areaLayout.removeAllViews();
                                            editArea.setVisibility(View.GONE);
                                            break;
                                        default:
                                            areaLayout.removeAllViews();
                                            editArea.setVisibility(View.GONE);
                                            setupArea(id, name);
                                            break;
                                    }
                                });
                            }
                            break;
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, volleyError -> {
        });
        queue.add(request);
    }

    private void setupArea(String id, String buildName) {
        areaID = findViewById(R.id.idArea);
        areaName = findViewById(R.id.areaDetail);
        buildingName = findViewById(R.id.buildingDetail);

        editArea = findViewById(R.id.editArea);
        areaLayout = findViewById(R.id.editLayArea);

        changeArea = findViewById(R.id.changeArea);
        changeArea.setOnClickListener(v -> {
            areaLayout.removeAllViews();
            editAreaSetup();
        });

        cancel = findViewById(R.id.cardCancel);
        confirm = findViewById(R.id.cardConfirm);

        confirm.setVisibility(View.VISIBLE);
        confirm.setOnClickListener(v -> confirm());

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, AppUrl.GET_AREA + id, new Response.Listener<String>() {
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

                                areaLayout.addView(tv);
                                editArea.setVisibility(View.VISIBLE);

                                tv.setOnClickListener(v -> {
                                    areaID.setText(id);
                                    buildingName.setText(buildName);
                                    areaName.setText(name);
                                    areaLayout.removeAllViews();
                                    editArea.setVisibility(View.GONE);
                                });
                            }
                            break;
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, volleyError -> {
        });
        queue.add(request);
    }
    private void confirm() {
        batchNumber = findViewById(R.id.idDetail);
        areaID = findViewById(R.id.idArea);
        materialName = findViewById(R.id.materialName);
        buildingName = findViewById(R.id.buildingDetail);
        areaName = findViewById(R.id.areaDetail);

        HashMap<String, String> userDetails = SessionManager.getUserDetails();

        String idBatch = batchNumber.getText().toString();
        String idArea = areaID.getText().toString();
        String idStockTake = stockTakeId;
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
                params.put("batch_number", idBatch);
                params.put("area_id", idArea);
                params.put("userid", idUser);
                params.put("stock_take_id", idStockTake);

                return params;
            }
        };
        queue.add(request);
    }
}