package com.example.warehouse.network;

public interface AppUrl {
    String BASE_URL = "http://103.75.54.70/yim/warehouse/index.php/api/";
    String LOGIN_URL = BASE_URL +  "login/proceed";
    String GET_BATCH = BASE_URL +  "main/getbatchinfo/";
    String GET_AREA = BASE_URL + "main/getarea/";
    String MOVE_AREA = BASE_URL + "main/move";
    String CHECK = BASE_URL + "main/check/";
    String STOCK_TAKE = BASE_URL + "main/stocktake/";
}
