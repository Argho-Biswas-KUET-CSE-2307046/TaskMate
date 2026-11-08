
package com.example.taskmate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Verify that this is our timer alarm.
        if (intent != null &&
                AlarmScheduler.ACTION_TIMER_ALARM.equals(intent.getAction())) {

            // Tell FocusTimerService to start the alarm.
            Intent serviceIntent = new Intent(
                    context,
                    FocusTimerService.class
            );

            serviceIntent.setAction(
                    FocusTimerService.ACTION_ALARM_TRIGGER
            );

            ContextCompat.startForegroundService(
                    context,
                    serviceIntent
            );
        }
    }
}
