
package com.example.taskmate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null) {
            return;
        }

        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {

            Log.d(TAG, "Device restarted. Boot completed.");

            // Reminder rescheduling will be implemented
            // in ReminderScheduler.java.
        }
    }
}
