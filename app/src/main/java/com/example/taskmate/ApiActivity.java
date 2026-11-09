
package com.example.taskmate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ApiActivity extends AppCompatActivity {

    private Button btnGet;
    private Button btnPost;
    private ProgressBar progressBar;
    private TextView tvApiResponse;

    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api);

        btnGet = findViewById(R.id.btnGet);
        btnPost = findViewById(R.id.btnPost);
        progressBar = findViewById(R.id.progressBar);
        tvApiResponse = findViewById(R.id.tvApiResponse);

        apiClient = new ApiClient();

        btnGet.setOnClickListener(v -> sendGetRequest());

        btnPost.setOnClickListener(v -> sendPostRequest());
    }

    private void sendGetRequest() {

        setLoading(true);
        tvApiResponse.setText("Sending GET request...");

        apiClient.getPost(new ApiClient.ApiCallback() {

            @Override
            public void onSuccess(String response) {
                setLoading(false);
                tvApiResponse.setText(response);
            }

            @Override
            public void onFailure(String error) {
                setLoading(false);
                tvApiResponse.setText(error);
            }
        });
    }

    private void sendPostRequest() {

        setLoading(true);
        tvApiResponse.setText("Sending POST request...");

        apiClient.createPost(new ApiClient.ApiCallback() {

            @Override
            public void onSuccess(String response) {
                setLoading(false);
                tvApiResponse.setText(response);
            }

            @Override
            public void onFailure(String error) {
                setLoading(false);
                tvApiResponse.setText(error);
            }
        });
    }

    private void setLoading(boolean loading) {

        progressBar.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        btnGet.setEnabled(!loading);
        btnPost.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {

        if (apiClient != null) {
            apiClient.shutdown();
        }

        super.onDestroy();
    }
}
