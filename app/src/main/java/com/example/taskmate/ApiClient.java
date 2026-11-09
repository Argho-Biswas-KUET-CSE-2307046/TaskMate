
package com.example.taskmate;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {

    public interface ApiCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private static final String BASE_URL =
            "https://jsonplaceholder.typicode.com/posts";

    public void getPost(ApiCallback callback) {

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {
                URL url = new URL(BASE_URL + "/1");

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                int responseCode = connection.getResponseCode();

                InputStream stream = responseCode >= 200 &&
                        responseCode < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                String response = readResponse(stream);

                if (responseCode >= 200 && responseCode < 300) {
                    mainHandler.post(() -> callback.onSuccess(
                            "HTTP Status: " + responseCode + "\n\n" + response
                    ));
                } else {
                    mainHandler.post(() -> callback.onFailure(
                            "HTTP Status: " + responseCode + "\n" + response
                    ));
                }

            } catch (Exception e) {

                mainHandler.post(() ->
                        callback.onFailure("GET Error: " + e.getMessage())
                );

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public void createPost(ApiCallback callback) {

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {
                URL url = new URL(BASE_URL);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                String jsonData =
                        "{\"title\":\"TaskMate HTTP Test\"," +
                                "\"body\":\"This is a test POST request.\"," +
                                "\"userId\":1}";

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(jsonData.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = connection.getResponseCode();

                InputStream stream = responseCode >= 200 &&
                        responseCode < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                String response = readResponse(stream);

                if (responseCode >= 200 && responseCode < 300) {
                    mainHandler.post(() -> callback.onSuccess(
                            "HTTP Status: " + responseCode + "\n\n" + response
                    ));
                } else {
                    mainHandler.post(() -> callback.onFailure(
                            "HTTP Status: " + responseCode + "\n" + response
                    ));
                }

            } catch (Exception e) {

                mainHandler.post(() ->
                        callback.onFailure("POST Error: " + e.getMessage())
                );

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private String readResponse(InputStream stream) throws Exception {

        if (stream == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }

    public void shutdown() {
        executor.shutdown();
    }
}
