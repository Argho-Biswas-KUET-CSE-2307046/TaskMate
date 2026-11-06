
package com.example.taskmate;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ToneGenerator toneGenerator;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display TaskMate splash screen
        setContentView(R.layout.activity_main);

        // Prepare startup sound
        toneGenerator = new ToneGenerator(
                AudioManager.STREAM_MUSIC, 80
        );

        // Wait for 3 seconds
        handler.postDelayed(() -> {

            // Play startup sound
            if (toneGenerator != null) {
                toneGenerator.startTone(
                        ToneGenerator.TONE_PROP_ACK,
                        400
                );
            }

            // Wait briefly for the sound
            handler.postDelayed(() -> {

                // Always open Login screen
                Intent intent = new Intent(
                        MainActivity.this,
                        LoginActivity.class
                );

                startActivity(intent);

                // Prevent returning to splash screen
                finish();

            }, 500);

        }, 3000);
    }

    @Override
    protected void onDestroy() {

        // Cancel pending tasks
        handler.removeCallbacksAndMessages(null);

        // Release sound resources
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }

        super.onDestroy();
    }
}
