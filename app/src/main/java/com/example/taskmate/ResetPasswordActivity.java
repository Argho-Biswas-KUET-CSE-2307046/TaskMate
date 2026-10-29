
package com.example.taskmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etEmail, etVerificationCode;
    private EditText etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etEmail = findViewById(R.id.etEmail);
        etVerificationCode = findViewById(R.id.etVerificationCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Receive the email from the previous screen, if provided.
        String email = getIntent().getStringExtra("email");
        if (email != null) {
            etEmail.setText(email);
        }

        btnResetPassword.setOnClickListener(v -> resetPassword());

        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ResetPasswordActivity.this,
                    LoginActivity.class
            );
            startActivity(intent);
            finish();
        });
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();
        String code = etVerificationCode.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (email.isEmpty()) {
            etEmail.setError("Enter your email");
            etEmail.requestFocus();
            return;
        }

        if (code.isEmpty()) {
            etVerificationCode.setError("Enter the verification code");
            etVerificationCode.requestFocus();
            return;
        }

        if (!isStrongPassword(newPassword)) {
            etNewPassword.setError(
                    "Use 8+ characters with uppercase, lowercase, " +
                            "a number and a special character"
            );
            etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        Toast.makeText(
                this,
                "Inputs validated. Server verification is still required.",
                Toast.LENGTH_LONG
        ).show();

        // Later:
        // 1. Verify the code securely on the backend.
        // 2. Confirm the code has not expired or been used.
        // 3. Update the password only after verification succeeds.
    }

    private boolean isStrongPassword(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean uppercase = false;
        boolean lowercase = false;
        boolean digit = false;
        boolean special = false;

        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                uppercase = true;
            } else if (Character.isLowerCase(ch)) {
                lowercase = true;
            } else if (Character.isDigit(ch)) {
                digit = true;
            } else if (!Character.isLetterOrDigit(ch)) {
                special = true;
            }
        }

        return uppercase && lowercase && digit && special;
    }
}
