
package com.example.taskmate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null) {
            return;
        }

        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {

            // Retrieve the previously saved deadline.
            long savedDeadline =
                    AlarmScheduler.getSavedDeadline(context);

            // No timer was saved.
            if (savedDeadline <= 0) {
                return;
            }

            // Restore the alarm using the saved deadline.
            // If the deadline has passed, AlarmScheduler
            // will schedule it to trigger immediately.
            AlarmScheduler.scheduleDeadline(
                    context,
                    savedDeadline
            );
        }
    }
}
