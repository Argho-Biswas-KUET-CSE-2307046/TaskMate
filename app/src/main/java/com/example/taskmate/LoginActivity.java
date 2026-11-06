package com.example.taskmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogle;
    private TextView tvForgotPassword, tvGoToSignUp;
    private ImageButton btnTogglePassword;

    private FirebaseAuth mAuth;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvGoToSignUp = findViewById(R.id.tvGoToSignUp);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);

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

        btnTogglePassword.setOnClickListener(v -> togglePassword());

        btnGoogle.setOnClickListener(v ->
                Toast.makeText(
                        LoginActivity.this,
                        "Google Sign-In will be configured next.",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    private void togglePassword() {

        int cursorPosition = etPassword.getSelectionStart();

        if (isPasswordVisible) {
            etPassword.setTransformationMethod(
                    PasswordTransformationMethod.getInstance()
            );
            btnTogglePassword.setImageResource(
                    android.R.drawable.ic_menu_view
            );
            isPasswordVisible = false;

        } else {
            etPassword.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance()
            );
            btnTogglePassword.setImageResource(
                    android.R.drawable.ic_menu_close_clear_cancel
            );
            isPasswordVisible = true;
        }

        etPassword.setSelection(
                Math.max(0, cursorPosition)
        );
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
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Enter your password");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Login successful!",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent = new Intent(
                                LoginActivity.this,
                                TaskActivity.class
                        );

                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );

                        startActivity(intent);
                        finish();

                    } else {

                        String errorMessage =
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Please check your email and password.";

                        Toast.makeText(
                                LoginActivity.this,
                                "Login failed: " + errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
