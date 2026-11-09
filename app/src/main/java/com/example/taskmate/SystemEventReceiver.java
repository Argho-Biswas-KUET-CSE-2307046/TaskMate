
package com.example.taskmate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class SystemEventReceiver extends BroadcastReceiver {

    private static final String TAG = "SystemEventReceiver";

    public static final String ACTION_BATTERY_PERCENTAGE =
            "com.example.taskmate.BATTERY_PERCENTAGE";

    public static final String EXTRA_BATTERY_LEVEL = "battery_level";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();

        // Battery has entered Android's low-battery state
        if (Intent.ACTION_BATTERY_LOW.equals(action)) {

            Log.d(TAG, "System reports low battery");

            Toast.makeText(
                    context,
                    "Warning: Battery is low!",
                    Toast.LENGTH_LONG
            ).show();
        }

        // Battery has returned to a normal state
        else if (Intent.ACTION_BATTERY_OKAY.equals(action)) {

            Log.d(TAG, "Battery returned to normal");

            Toast.makeText(
                    context,
                    "Battery is back to normal",
                    Toast.LENGTH_SHORT
            ).show();
        }

        // Battery information changed
        else if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {

            int level = intent.getIntExtra(
                    BatteryManager.EXTRA_LEVEL, -1
            );

            int scale = intent.getIntExtra(
                    BatteryManager.EXTRA_SCALE, -1
            );

            if (level >= 0 && scale > 0) {

                int percentage = (level * 100) / scale;

                Log.d(TAG, "Battery: " + percentage + "%");

                Intent batteryIntent = new Intent(
                        ACTION_BATTERY_PERCENTAGE
                );

                batteryIntent.setPackage(context.getPackageName());

                batteryIntent.putExtra(
                        EXTRA_BATTERY_LEVEL,
                        percentage
                );

                context.sendBroadcast(batteryIntent);
            }
        }

        // Airplane Mode changed
        else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {

            boolean airplaneModeEnabled =
                    Settings.Global.getInt(
                            context.getContentResolver(),
                            Settings.Global.AIRPLANE_MODE_ON,
                            0
                    ) != 0;

            if (airplaneModeEnabled) {

                Log.d(TAG, "Airplane Mode ON");

                Toast.makeText(
                        context,
                        "Airplane Mode Enabled",
                        Toast.LENGTH_LONG
                ).show();

            } else {

                Log.d(TAG, "Airplane Mode OFF");

                Toast.makeText(
                        context,
                        "Airplane Mode Disabled",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}
