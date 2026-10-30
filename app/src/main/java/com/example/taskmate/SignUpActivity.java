
package com.example.taskmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private Button btnCreateAccount;
    private TextView tvGoToLogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Connect Java variables with XML views
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Create Account button
        btnCreateAccount.setOnClickListener(v -> registerUser());

        // Navigate to Login screen
        tvGoToLogin.setOnClickListener(v -> openLoginScreen());
    }

    private void registerUser() {

        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword =
                etConfirmPassword.getText().toString();

        // Check username
        if (username.isEmpty()) {
            etUsername.setError("Enter your username");
            etUsername.requestFocus();
            return;
        }

        // Check email
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

        // Check password
        if (password.isEmpty()) {
            etPassword.setError("Enter a password");
            etPassword.requestFocus();
            return;
        }

        if (!isStrongPassword(password)) {
            etPassword.setError(
                    "Use at least 8 characters with uppercase, " +
                            "lowercase, a number and a special character"
            );
            etPassword.requestFocus();
            return;
        }

        // Check confirm password
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

        // Disable button while Firebase processes registration
        btnCreateAccount.setEnabled(false);

        // Create account in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {

                            // Save username in Firebase user profile
                            UserProfileChangeRequest profileUpdates =
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(username)
                                            .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {

                                        btnCreateAccount.setEnabled(true);

                                        if (profileTask.isSuccessful()) {

                                            Toast.makeText(
                                                    SignUpActivity.this,
                                                    "Account created successfully!",
                                                    Toast.LENGTH_LONG
                                            ).show();

                                            // Sign out before opening Login screen
                                            mAuth.signOut();
                                            openLoginScreen();

                                        } else {

                                            Toast.makeText(
                                                    SignUpActivity.this,
                                                    "Account created, but username could not be saved. Please try again.",
                                                    Toast.LENGTH_LONG
                                            ).show();

                                            mAuth.signOut();
                                        }
                                    });

                        } else {
                            btnCreateAccount.setEnabled(true);
                            Toast.makeText(
                                    SignUpActivity.this,
                                    "Account created, but user information is unavailable.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                    } else {

                        btnCreateAccount.setEnabled(true);

                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed. Please try again.";

                        Toast.makeText(
                                SignUpActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // Navigate to LoginActivity
    private void openLoginScreen() {

        Intent intent = new Intent(
                SignUpActivity.this,
                LoginActivity.class
        );

        startActivity(intent);
        finish();
    }

    // Check password strength
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
