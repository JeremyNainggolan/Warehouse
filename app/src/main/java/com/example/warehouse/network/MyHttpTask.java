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
/**
 * Represents an asynchronous HTTP task.
 * This task is responsible for making an HTTP GET request and handling the response.
 * @author JN
 * @date 19 June 2024
 */
public class MyHttpTask extends AsyncTask<String, Void, String> {

    /**
     * Represents the result of an HTTP task.
     */
    public boolean result = false;

    /**
     * Gets the result of the HTTP task.
     *
     * @return true if the task was successful, false otherwise.
     */
    public boolean getResult(){
        return this.result;
    }

    /**
     * Sets the result of the HTTP task.
     *
     * @param result the result of the task.
     */
    public void setResult(boolean result){
        this.result = result;
    }

    /**
     * Represents a class that performs an HTTP task and notifies a listener when the task is completed.
     */
    private HttpTaskListener listener;

    /**
     * Constructs a new MyHttpTask with the specified listener.
     *
     * @param listener the listener to be notified when the task is complete.
     */
    public MyHttpTask(HttpTaskListener listener) {
        this.listener = listener;
    }

    /**
        * Executes the background task to download data from the specified URL.
        *
        * @param urls The URLs to download data from.
        * @return The downloaded data as a string.
        */
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

    /**
     * This method is called after the execution of the HTTP request is complete.
     * It handles the response received from the server.
     *
     * @param result The response received from the server as a String.
     */
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

    /**
     * Downloads data from the specified URL.
     *
     * @param urlString the URL to download data from.
     * @return the downloaded data as a string.
     * @throws IOException if an error occurs while downloading the data.
     */
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
