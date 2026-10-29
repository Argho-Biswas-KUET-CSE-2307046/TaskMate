
package com.example.taskmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Your actual XML file is activity_sign_up.xml
        setContentView(R.layout.activity_sign_up);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnCreateAccount.setOnClickListener(v -> registerUser());

        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    SignUpActivity.this,
                    LoginActivity.class
            );
            startActivity(intent);
        });
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (username.isEmpty()) {
            etUsername.setError("Enter your username");
            etUsername.requestFocus();
            return;
        }

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
            etPassword.setError("Enter a password");
            etPassword.requestFocus();
            return;
        }

        if (!isStrongPassword(password)) {
            etPassword.setError(
                    "Use at least 8 characters with uppercase, "
                            + "lowercase, a number and a special character"
            );
            etPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        Toast.makeText(
                SignUpActivity.this,
                "Input validated successfully!",
                Toast.LENGTH_SHORT
        ).show();

        // Firebase registration can be added here later.
    }

    private boolean isStrongPassword(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecialCharacter = false;

        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(ch)) {
                hasLowercase = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(ch)) {
                hasSpecialCharacter = true;
            }
        }

        return hasUppercase
                && hasLowercase
                && hasDigit
                && hasSpecialCharacter;
    }
}
