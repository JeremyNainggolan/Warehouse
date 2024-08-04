package com.example.warehouse;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.warehouse.model.SessionManager;
import com.example.warehouse.network.AppUrl;
import com.example.warehouse.network.InternetReceiver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.legacy.widget.Space;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AreaActivity extends AppCompatActivity {

    protected String id;
    protected String MODE;
    private Space space;
    private ListView listArea;
    private ImageView areaToHome;
    private LinearLayout afterChoose;
    private CardView cardArea, cardSubArea, cardAreaConfirm, areaUnLoad;
    private FrameLayout areaLoad;
    private TextView cardAreaText, cardSubAreaText, batchNumber, materialNumber, materialName, itemType, quantity;

    private BroadcastReceiver broadcastReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area);

        broadcastReceiver = new InternetReceiver();
        internetStatus();

        Intent getId = getIntent();
        this.id = getId.getStringExtra("batch_number");
        this.MODE = getId.getStringExtra("MODE");
        Log.d("RESULT", getId.getStringExtra("MODE"));

        construct();
        addArea();

        cardArea.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                cardAreaText.setText("Building");
                cardSubAreaText.setText("Sub Building");
                setArea(true, false);
                setVisibility(true);
                setLoad(true, false);
                addArea();
            }
        });

        areaToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeLinked = new Intent(getApplicationContext(), MainActivity.class);
                homeLinked.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                homeLinked.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeLinked);
            }
        });
    }

    protected void construct() {
        this.space = findViewById(R.id.space);
        this.areaToHome = findViewById(R.id.areaToHome);

        this.cardArea = findViewById(R.id.building);
        this.cardSubArea = findViewById(R.id.sub_building);
        this.cardAreaText = findViewById(R.id.cardAreaText);
        this.cardSubAreaText = findViewById(R.id.cardSubAreaText);
        this.cardAreaConfirm = findViewById(R.id.areaConfirm);

        this.listArea = findViewById(R.id.listArea);

        this.afterChoose = findViewById(R.id.afterChoose);
        this.batchNumber = findViewById(R.id.area_batchNumber);
        this.materialNumber = findViewById(R.id.area_materialNumber);
        this.materialName = findViewById(R.id.area_materialName);
        this.itemType = findViewById(R.id.area_itemType);
        this.quantity = findViewById(R.id.area_quantity);

        this.areaLoad = findViewById(R.id.area_load);
        this.areaUnLoad = findViewById(R.id.area_unload);
    }

    protected void addArea() {
        setLoad(true, false);
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, AppUrl.GET_AREA, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    setLoad(true, false);
                    JSONObject object = new JSONObject(s);
                    int status = Integer.parseInt(object.getString("status"));
                    switch (status) {
                        case 201:
                            internetStatus();
                            break;
                        case 210:
                            setLoad(false, true);
                            JSONObject body = object.getJSONObject("body");
                            Iterator<String> keys = body.keys();
                            ArrayList<String> data = new ArrayList<>();
                            HashMap<String, String> idMap = new HashMap<>();

                            while (keys.hasNext()) {
                                String key = keys.next();
                                JSONObject item = body.getJSONObject(key);

                                String itemName = item.getString("name");
                                String itemId = String.valueOf(item.getInt("id"));

                                data.add(itemName);
                                idMap.put(itemName, itemId);
                            }

                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_view, data);
                            listArea.setAdapter(arrayAdapter);

                            listArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String itemName = data.get(position);
                                    String itemId = idMap.get(itemName);

                                    setCardAreaText(itemName, Integer.parseInt(itemId));

                                    if (itemName.equals("Delivered")) {
                                        setVisibility(false);
                                        setConfirm(itemId, itemName);
                                    } else {
                                        setArea(false, true);
                                        setLoad(true, false);
                                        addSubArea(itemId);
                                    }

                                }
                            });
                            break;
                    }
                } catch (Exception e) {
                    setLoad(false, true);
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, volleyError -> {

        });
        queue.add(request);
    }

    protected void addSubArea(String _areaID) {
        final String URL = AppUrl.GET_AREA + _areaID;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    setLoad(true, false);
                    JSONObject object = new JSONObject(s);
                    int status = Integer.parseInt(object.getString("status"));
                    switch (status) {
                        case 201:
                            internetStatus();
                            break;
                        case 210:
                            setLoad(false, true);
                            JSONObject body = object.getJSONObject("body");
                            Iterator<String> keys = body.keys();
                            ArrayList<String> data = new ArrayList<>();
                            HashMap<String, String> idMap = new HashMap<>();

                            while (keys.hasNext()) {
                                String key = keys.next();
                                JSONObject item = body.getJSONObject(key);

                                String itemName = item.getString("name");
                                String itemId = String.valueOf(item.getInt("id"));

                                data.add(itemName);
                                idMap.put(itemName, itemId);
                            }

                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_view, data);
                            listArea.setAdapter(arrayAdapter);

                            listArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String itemName = data.get(position);
                                    String itemId = idMap.get(itemName);
                                    setCardSubAreaText(itemName, Integer.parseInt(itemId));
                                    setLoad(true, false);
                                    setVisibility(true, true);
                                    setArea(true, true);
                                    setConfirm(itemId, itemName);
                                }
                            });
                            break;
                    }
                } catch (Exception e) {
                    setLoad(false, true);
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, volleyError -> {

        });
        queue.add(request);
    }

    protected void setArea(Boolean _cardArea, Boolean _cardSubArea) {
        if (_cardArea && _cardSubArea) {
            this.cardArea.setCardBackgroundColor(Color.parseColor("#FFeb4a4a"));
            this.cardSubArea.setCardBackgroundColor(Color.parseColor("#FFeb4a4a"));
        } else if (_cardArea) {
            this.cardArea.setCardBackgroundColor(Color.parseColor("#FFeb4a4a"));
            this.cardSubArea.setCardBackgroundColor(Color.parseColor("#FF565656"));
        } else if (_cardSubArea) {
            this.cardArea.setCardBackgroundColor(Color.parseColor("#FF565656"));
            this.cardSubArea.setCardBackgroundColor(Color.parseColor("#FFeb4a4a"));
        }
    }

    protected void setCardAreaText(String _text, int _id) {
        this.cardAreaText.setText(_text);
        this.cardAreaText.setId(_id);
    }

    protected void setCardSubAreaText(String _text, int _id) {
        this.cardSubAreaText.setText(_text);
        this.cardSubAreaText.setId(_id);
    }

    protected void setLoad(boolean _isLoad, boolean _isUnload) {
        if (_isLoad) {
            this.areaLoad.setVisibility(View.VISIBLE);
            this.areaUnLoad.setVisibility(View.GONE);
            this.cardAreaConfirm.setVisibility(View.GONE);
        } else if (_isUnload) {
            this.areaLoad.setVisibility(View.GONE);
            this.areaUnLoad.setVisibility(View.VISIBLE);
            this.cardAreaConfirm.setVisibility(View.VISIBLE);
        }
    }

    protected void setVisibility(Boolean _isActive) {
        if (_isActive) {
            this.space.setVisibility(View.VISIBLE);
            this.cardSubArea.setVisibility(View.VISIBLE);
            this.listArea.setVisibility(View.VISIBLE);
            this.afterChoose.setVisibility(View.GONE);
        } else {
            addAfterChoose();
            this.space.setVisibility(View.GONE);
            this.cardSubArea.setVisibility(View.GONE);
            this.listArea.setVisibility(View.GONE);
            this.afterChoose.setVisibility(View.VISIBLE);
        }
    }

    protected void setVisibility(Boolean _isActive, Boolean _isConfirm) {
        if (_isActive && _isConfirm) {
            addAfterChoose();
            this.listArea.setVisibility(View.GONE);
            this.afterChoose.setVisibility(View.VISIBLE);
        }
    }

    protected void addAfterChoose() {
        String url = AppUrl.GET_BATCH + id;
        RequestQueue queue = Volley.newRequestQueue(this);
        @SuppressLint("SetTextI18n") StringRequest request = new StringRequest(Request.Method.GET, url, s -> {
            try {
                setLoad(true, false);
                JSONObject jsonObject = new JSONObject(s);
                int status = Integer.parseInt(jsonObject.getString("status"));
                switch (status) {
                    case 201:
                        break;
                    case 210:
                        setLoad(false, true);
                        try {
                            JSONObject result = new JSONObject(jsonObject.getString("body"));

                            if (result.getString("item_type").equals("material")) {
                                itemType.setText("Raw Material");
                            } else if (result.getString("item_type").equals("goods")) {
                                itemType.setText("Finish Goods");
                            } else {
                                itemType.setText("Unknown");
                            }

                            batchNumber.setText(result.getString("batch_number"));
                            materialNumber.setText(result.getString("item_id"));
                            materialName.setText(result.getString("item_name"));
                            quantity.setText(result.getString("quantity"));

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            } catch (Throwable throwable) {
                setLoad(false, true);
                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, volleyError -> {

        });
        queue.add(request);
    }

    protected void setConfirm(String _id, String _name) {
        HashMap<String, String> userDetails = SessionManager.getUserDetails();
        String idUser = userDetails.get("idKey");
        Log.d("IDUSER", idUser);
        RequestQueue queue = Volley.newRequestQueue(this);

        cardAreaConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AreaActivity.this);
                builder.setTitle("Confirm?")
                        .setMessage("Are you sure to move this item to " + _name + "?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            StringRequest request = new StringRequest(Request.Method.POST, AppUrl.MOVE_AREA, s -> {
                                try {
                                    JSONObject resultJSON = new JSONObject(s);
                                    int status = Integer.parseInt(resultJSON.getString("status"));
                                    switch (status) {
                                        case 201:

                                            break;
                                        case 210:
                                            try {
                                                Toast.makeText(getApplicationContext(), resultJSON.getString("message"), Toast.LENGTH_LONG).show();
                                                Intent intent = null;
                                                if (MODE.equals("RESULT_MODE")) {
                                                    intent = new Intent(getApplicationContext(), ResultActivity.class);
                                                } else if (MODE.equals("STOCK_MODE")) {
                                                    intent = new Intent(getApplicationContext(), StockTakeActivity.class);
                                                }
                                                intent.putExtra("batch_number", id);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
                                    params.put("batch_number", id);
                                    params.put("area_id", _id);
                                    params.put("userid", idUser);

                                    return params;
                                }
                            };
                            queue.add(request);
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.cancel()).show();
            }
        });
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

}