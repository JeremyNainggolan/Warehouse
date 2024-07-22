package com.example.warehouse.network;

import static com.example.warehouse.network.AppUrl.BASE_URL;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
public class MyHttpTask extends AsyncTask<String, Void, String> {

    public boolean result = false;

    public boolean getResult(){
        return this.result;
    }
    public void setResult(boolean result){
        this.result = result;
    }

    private HttpTaskListener listener;

    public MyHttpTask(HttpTaskListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... urls) {
        String result = null;
        try {
            result = downloadData(urls[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            // Check if the response code is 404 (Not Found)
            if (result.contains("404 Not Found")) {
                // Handle the 404 error here
                Log.e("HTTP Error", "404 Error: Resource not found");
            } else {
                setResult(true);
                // Handle the successful response here
                Log.i("HTTP Success", result);
            }
        } else {
            // Handle other errors or exceptions here
            Log.e("HTTP Error", BASE_URL +" An error occurred while making the request");
        }
        if (listener != null) {
            listener.onTaskComplete(result);
        }
    }
    private String downloadData(String urlString) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000); // 10 seconds
            conn.setConnectTimeout(15000); // 15 seconds
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                return content.toString();
            } else {
                // Handle other HTTP response codes here
                Log.e("HTTP Error", "HTTP Response Code: " + responseCode);
                return null;
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
