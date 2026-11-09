
package com.example.taskmate;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class FirebaseStorageRepository {

    private final FirebaseStorage storage;

    public FirebaseStorageRepository() {
        storage = FirebaseStorage.getInstance();
    }

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(String error);
    }

    public void uploadImage(Uri imageUri, UploadCallback callback) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onFailure("Please log in first.");
            return;
        }

        if (imageUri == null) {
            callback.onFailure("No image selected.");
            return;
        }

        String fileName = UUID.randomUUID().toString() + ".jpg";

        StorageReference imageRef = storage.getReference()
                .child("users")
                .child(user.getUid())
                .child("images")
                .child(fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        callback.onSuccess(uri.toString()))
                                .addOnFailureListener(e ->
                                        callback.onFailure(e.getMessage()))
                )
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }
}
