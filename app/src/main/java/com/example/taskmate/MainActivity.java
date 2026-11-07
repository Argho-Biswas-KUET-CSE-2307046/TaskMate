
package com.example.taskmate;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Open Login screen directly.
        Intent intent = new Intent(
                MainActivity.this,
                LoginActivity.class
        );

        startActivity(intent);

        // Close splash activity.
        finish();
    }
}
