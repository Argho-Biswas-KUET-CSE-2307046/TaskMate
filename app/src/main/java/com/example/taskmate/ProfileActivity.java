
package com.example.taskmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfileActivity extends AppCompatActivity {

    // Profile views
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView btnProfileBack;
    private TextView btnLogout;
    private ImageView ivProfileAvatar;

    // Profile options
    private LinearLayout optionEditProfile;
    private LinearLayout optionSyncBackup;
    private LinearLayout optionTheme;
    private LinearLayout optionAbout;

    // Notification switch
    private Switch switchNotifications;

    // Firebase
    private FirebaseAuth auth;

    // Local preferences
    private SharedPreferences preferences;

    // Image picker
    private ActivityResultLauncher<String[]> imagePickerLauncher;

    private static final String IMAGE_KEY = "profile_image_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();

        preferences = getSharedPreferences(
                "TaskMateSettings",
                MODE_PRIVATE
        );

        initializeViews();

        // Register image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        saveProfileImage(uri);
                    }
                }
        );

        loadUserInformation();
        loadSavedProfileImage();
        setupNotificationSwitch();
        setupClickListeners();
    }

    // Connect Java variables with XML views.
    private void initializeViews() {

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        btnProfileBack = findViewById(R.id.btnProfileBack);
        btnLogout = findViewById(R.id.btnLogout);

        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);

        optionEditProfile = findViewById(R.id.optionEditProfile);
        optionSyncBackup = findViewById(R.id.optionSyncBackup);
        optionTheme = findViewById(R.id.optionTheme);
        optionAbout = findViewById(R.id.optionAbout);

        switchNotifications = findViewById(R.id.switchNotifications);
    }

    // Display logged-in user's information.
    private void loadUserInformation() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            openLoginScreen();
            return;
        }

        String name = user.getDisplayName();

        if (name == null || name.trim().isEmpty()) {

            if (user.getEmail() != null) {
                name = user.getEmail().split("@")[0];
            } else {
                name = "User";
            }
        }

        tvProfileName.setText(name);

        String email = user.getEmail();

        tvProfileEmail.setText(
                email != null ? email : "No email available"
        );
    }

    // Open Android document picker for selecting an image.
    private void openImagePicker() {

        imagePickerLauncher.launch(
                new String[]{"image/*"}
        );
    }

    // Save selected image URI and display the image.
    private void saveProfileImage(Uri imageUri) {

        try {

            // Keep permission to access the selected image.
            getContentResolver().takePersistableUriPermission(
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            // Save image URI locally.
            preferences.edit()
                    .putString(IMAGE_KEY, imageUri.toString())
                    .apply();

            // Display selected image.
            ivProfileAvatar.setImageURI(imageUri);

            Toast.makeText(
                    this,
                    "Profile picture updated successfully",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to save this image. Please select another image.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // Restore previously selected image.
    private void loadSavedProfileImage() {

        String savedImage = preferences.getString(
                IMAGE_KEY,
                null
        );

        if (savedImage != null) {

            try {

                Uri imageUri = Uri.parse(savedImage);

                ivProfileAvatar.setImageURI(imageUri);

            } catch (Exception e) {

                preferences.edit()
                        .remove(IMAGE_KEY)
                        .apply();
            }
        }
    }

    // Save and restore notification preference.
    private void setupNotificationSwitch() {

        boolean enabled = preferences.getBoolean(
                "notifications_enabled",
                true
        );

        switchNotifications.setChecked(enabled);

        switchNotifications.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    preferences.edit()
                            .putBoolean(
                                    "notifications_enabled",
                                    isChecked
                            )
                            .apply();

                    String message = isChecked
                            ? "Notifications enabled"
                            : "Notifications disabled";

                    Toast.makeText(
                            ProfileActivity.this,
                            message,
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );
    }

    // Connect all click listeners.
    private void setupClickListeners() {

        btnProfileBack.setOnClickListener(v -> finish());

        // Select profile picture.
        ivProfileAvatar.setOnClickListener(
                v -> openImagePicker()
        );

        // Edit Profile.
        optionEditProfile.setOnClickListener(
                v -> showEditProfileDialog()
        );

        // Sync and Backup.
        optionSyncBackup.setOnClickListener(
                v -> showSyncBackupMessage()
        );

        // Theme.
        optionTheme.setOnClickListener(
                v -> showThemeMessage()
        );

        // About.
        optionAbout.setOnClickListener(
                v -> showAboutDialog()
        );

        // Logout.
        btnLogout.setOnClickListener(
                v -> logoutUser()
        );
    }

    // Show Edit Profile dialog.
    private void showEditProfileDialog() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            openLoginScreen();
            return;
        }

        EditText nameInput = new EditText(this);

        nameInput.setHint("Enter your name");
        nameInput.setSingleLine(true);
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        nameInput.setPadding(40, 25, 40, 25);

        if (user.getDisplayName() != null) {
            nameInput.setText(user.getDisplayName());
        }

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setMessage("Enter your new display name")
                .setView(nameInput)

                .setPositiveButton("Save", (dialog, which) -> {

                    String newName = nameInput.getText()
                            .toString()
                            .trim();

                    if (newName.isEmpty()) {

                        Toast.makeText(
                                this,
                                "Name cannot be empty",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    updateUserName(newName);
                })

                .setNegativeButton("Cancel", null)
                .show();
    }

    // Update username in Firebase Authentication.
    private void updateUserName(String newName) {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            openLoginScreen();
            return;
        }

        UserProfileChangeRequest profileUpdates =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (task.isSuccessful()) {

                        tvProfileName.setText(newName);

                        Toast.makeText(
                                this,
                                "Profile updated successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                this,
                                "Failed to update profile: " +
                                        (task.getException() != null
                                                ? task.getException().getMessage()
                                                : "Unknown error"),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // Show Sync and Backup information.
    private void showSyncBackupMessage() {

        new AlertDialog.Builder(this)
                .setTitle("Sync & Backup")
                .setMessage(
                        "Cloud synchronization and backup " +
                                "will be available after Firebase Firestore integration."
                )
                .setPositiveButton("OK", null)
                .show();
    }

    // Show current theme information.
    private void showThemeMessage() {

        new AlertDialog.Builder(this)
                .setTitle("Appearance")
                .setMessage(
                        "TaskMate is currently using Light Theme."
                )
                .setPositiveButton("OK", null)
                .show();
    }

    // Show application information.
    private void showAboutDialog() {

        new AlertDialog.Builder(this)
                .setTitle("About TaskMate")
                .setMessage(
                        "TaskMate - Smart Personal Task & Reminder Manager\n\n" +
                                "Version: 1.0\n\n" +
                                "Organize your daily tasks and improve your productivity."
                )
                .setPositiveButton("OK", null)
                .show();
    }

    // Logout confirmation.
    private void logoutUser() {

        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")

                .setPositiveButton("Logout", (dialog, which) -> {

                    auth.signOut();

                    openLoginScreen();
                })

                .setNegativeButton("Cancel", null)
                .show();
    }

    // Open Login screen and clear previous activities.
    private void openLoginScreen() {

        Intent intent = new Intent(
                ProfileActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (auth != null &&
                tvProfileName != null &&
                tvProfileEmail != null) {

            loadUserInformation();
        }
    }
}
