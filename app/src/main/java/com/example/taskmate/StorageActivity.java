
package com.example.taskmate;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StorageActivity extends AppCompatActivity {

    private ImageView imagePreview;

    private Button btnSelectImage;
    private Button btnUploadImage;

    private ProgressBar storageProgress;

    private TextView tvStorageStatus;
    private TextView tvDownloadUrl;

    private Uri selectedImageUri;

    private LocalImageRepository imageRepository;

    private ActivityResultLauncher<String> imagePicker;

    private final ExecutorService imageExecutor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_storage);

        // Connect XML views.
        imagePreview = findViewById(R.id.imagePreview);

        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUploadImage = findViewById(R.id.btnUploadImage);

        storageProgress = findViewById(R.id.storageProgress);

        tvStorageStatus = findViewById(R.id.tvStorageStatus);
        tvDownloadUrl = findViewById(R.id.tvDownloadUrl);

        // Initialize local image repository.
        imageRepository = new LocalImageRepository();

        // Open the phone's image picker.
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {

                    if (uri != null) {

                        selectedImageUri = uri;

                        imagePreview.setImageURI(uri);

                        tvStorageStatus.setText(
                                "Image selected successfully."
                        );

                        tvDownloadUrl.setText("");
                    }
                }
        );

        // Select Image button.
        btnSelectImage.setOnClickListener(v -> {

            imagePicker.launch("image/*");
        });

        // Save Image button.
        btnUploadImage.setText("Save Image");

        btnUploadImage.setOnClickListener(v -> saveImage());
    }

    // Save the selected image locally.
    private void saveImage() {

        if (selectedImageUri == null) {

            tvStorageStatus.setText(
                    "Please select an image first."
            );

            return;
        }

        btnUploadImage.setEnabled(false);
        btnSelectImage.setEnabled(false);

        storageProgress.setVisibility(View.VISIBLE);

        tvStorageStatus.setText(
                "Saving image to TaskMate..."
        );

        tvDownloadUrl.setText("");

        imageExecutor.execute(() -> {

            imageRepository.saveImage(
                    getApplicationContext(),
                    selectedImageUri,
                    new LocalImageRepository.SaveCallback() {

                        @Override
                        public void onSuccess(String imagePath) {

                            mainHandler.post(() -> {

                                btnUploadImage.setEnabled(true);
                                btnSelectImage.setEnabled(true);

                                storageProgress.setVisibility(View.GONE);

                                tvStorageStatus.setText(
                                        "Image saved successfully!"
                                );

                                tvDownloadUrl.setText(
                                        "Saved Image Path:\n" + imagePath
                                );

                                displaySavedImage(imagePath);
                            });
                        }

                        @Override
                        public void onFailure(String error) {

                            mainHandler.post(() -> {

                                btnUploadImage.setEnabled(true);
                                btnSelectImage.setEnabled(true);

                                storageProgress.setVisibility(View.GONE);

                                tvStorageStatus.setText(error);
                            });
                        }
                    }
            );
        });
    }

    // Display the saved image.
    private void displaySavedImage(String imagePath) {

        File imageFile = new File(imagePath);

        if (!imageFile.exists()) {

            tvStorageStatus.setText(
                    "Saved image could not be found."
            );

            return;
        }

        android.graphics.Bitmap bitmap =
                android.graphics.BitmapFactory.decodeFile(imagePath);

        if (bitmap != null) {

            imagePreview.setImageBitmap(bitmap);

            tvStorageStatus.setText(
                    "Image saved and retrieved successfully!"
            );

        } else {

            tvStorageStatus.setText(
                    "Image saved, but preview could not be loaded."
            );
        }
    }

    @Override
    protected void onDestroy() {

        imageExecutor.shutdown();

        super.onDestroy();
    }
}
