
package com.example.taskmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvGoToSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvGoToSignUp = findViewById(R.id.tvGoToSignUp);

        btnLogin.setOnClickListener(v -> loginUser());

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    ForgotPasswordActivity.class
            );
            startActivity(intent);
        });

        tvGoToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    SignUpActivity.class
            );
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty()) {
            etEmail.setError("Enter your email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Enter your password");
            etPassword.requestFocus();
            return;
        }

        Toast.makeText(
                LoginActivity.this,
                "Input validated! Authentication will be added next.",
                Toast.LENGTH_SHORT
        ).show();

        // Firebase login will be implemented later.
    }
}
