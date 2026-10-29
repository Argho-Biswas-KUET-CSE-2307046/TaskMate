
package com.example.taskmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btnSignUp, btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect Java variables to XML views
        tvWelcome = findViewById(R.id.tvWelcome);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnLogin = findViewById(R.id.btnLogin);

        // Welcome message
        tvWelcome.setText("Welcome to TaskMate!");

        // Open Sign Up screen
        btnSignUp.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(
                        MainActivity.this,
                        SignUpActivity.class
                );
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(
                        MainActivity.this,
                        "Unable to open Sign Up screen.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Open Login screen
        btnLogin.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(
                        MainActivity.this,
                        LoginActivity.class
                );
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(
                        MainActivity.this,
                        "Unable to open Login screen.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
