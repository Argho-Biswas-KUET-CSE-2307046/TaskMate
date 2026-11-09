package com.example.taskmate;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class LocalImageRepository {

    public interface SaveCallback {
        void onSuccess(String imagePath);

        void onFailure(String error);
    }

    public void saveImage(
            Context context,
            Uri imageUri,
            SaveCallback callback
    ) {

        if (imageUri == null) {
            callback.onFailure("No image selected.");
            return;
        }

        try {

            // Get the image's file extension.
            String mimeType = context.getContentResolver()
                    .getType(imageUri);

            String extension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(mimeType);

            if (extension == null) {
                extension = "jpg";
            }

            // Create a private folder for TaskMate images.
            File imageDirectory = new File(
                    context.getFilesDir(),
                    "TaskMateImages"
            );

            if (!imageDirectory.exists()) {
                imageDirectory.mkdirs();
            }

            // Generate a unique image filename.
            String fileName = UUID.randomUUID()
                    + "." + extension;

            File imageFile = new File(
                    imageDirectory,
                    fileName
            );

            // Copy the selected image into app storage.
            try (
                    InputStream inputStream =
                            context.getContentResolver()
                                    .openInputStream(imageUri);

                    FileOutputStream outputStream =
                            new FileOutputStream(imageFile)
            ) {

                if (inputStream == null) {
                    callback.onFailure("Unable to open selected image.");
                    return;
                }

                byte[] buffer = new byte[8192];

                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {

                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.flush();
            }

            callback.onSuccess(imageFile.getAbsolutePath());

        } catch (Exception e) {

            callback.onFailure(
                    "Image saving failed: " + e.getMessage()
            );
        }
    }
}