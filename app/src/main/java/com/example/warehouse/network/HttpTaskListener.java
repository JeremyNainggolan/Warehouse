package com.example.warehouse.network;

/**
 * The `HttpTaskListener` interface defines a callback method that is invoked when an HTTP task is complete.
 * Implement this interface to receive the result of the HTTP task.
 */
public interface HttpTaskListener {
    void onTaskComplete(String result);
}
