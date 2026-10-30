
package com.example.taskmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btnSignUp;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvWelcome = findViewById(R.id.tvWelcome);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnLogin = findViewById(R.id.btnLogin);

        // Check that the required XML views exist.
        if (tvWelcome == null || btnSignUp == null || btnLogin == null) {
            Toast.makeText(
                    this,
                    "Error: Check activity_main.xml view IDs.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        tvWelcome.setText("Welcome to TaskMate!");

        // Open the Sign Up screen.
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    SignUpActivity.class
            );
            startActivity(intent);
        });

        // Open the Login screen.
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    LoginActivity.class
            );
            startActivity(intent);
        });
    }
}
