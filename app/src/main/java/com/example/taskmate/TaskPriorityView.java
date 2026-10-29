
package com.example.taskmate;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import androidx.appcompat.widget.AppCompatTextView;

public class TaskPriorityView extends AppCompatTextView {

    public TaskPriorityView(Context context) {
        super(context);
        initialize();
    }

    public TaskPriorityView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public TaskPriorityView(
            Context context,
            AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        setTextSize(14);
        setGravity(Gravity.CENTER);
        setPadding(12, 6, 12, 6);
        setTextColor(Color.BLACK);
        setBackgroundColor(Color.LTGRAY);
    }

    public void setPriority(String priority) {
        if (priority == null) {
            priority = "Low";
        }

        String value = priority.trim();

        switch (value.toLowerCase()) {
            case "high":
                setText("High Priority");
                setTextColor(Color.rgb(180, 30, 30));
                setBackgroundColor(Color.rgb(255, 230, 230));
                break;

            case "medium":
                setText("Medium Priority");
                setTextColor(Color.rgb(140, 90, 0));
                setBackgroundColor(Color.rgb(255, 245, 210));
                break;

            case "low":
                setText("Low Priority");
                setTextColor(Color.rgb(25, 110, 55));
                setBackgroundColor(Color.rgb(225, 245, 230));
                break;

            default:
                setText(value);
                setTextColor(Color.BLACK);
                setBackgroundColor(Color.LTGRAY);
                break;
        }
    }
}
