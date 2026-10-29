
package com.example.taskmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendCode;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnSendCode = findViewById(R.id.btnSendCode);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnSendCode.setOnClickListener(v -> sendVerificationCode());

        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ForgotPasswordActivity.this,
                    LoginActivity.class
            );
            startActivity(intent);
            finish();
        });
    }

    private void sendVerificationCode() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Enter your email address");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        Toast.makeText(
                ForgotPasswordActivity.this,
                "Email validated. Password recovery will be connected next.",
                Toast.LENGTH_LONG
        ).show();

        // Email verification will be implemented using Firebase.
    }
}
