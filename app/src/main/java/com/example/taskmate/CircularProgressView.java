
package com.example.taskmate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CircularProgressView extends View {

    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float progress = 1.0f;

    private final RectF circleBounds = new RectF();

    public CircularProgressView(Context context) {
        super(context);
        initialize();
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CircularProgressView(
            Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr
    ) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {

        // Background ring.
        backgroundPaint.setColor(0xFFECE5FC);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(dpToPx(7));
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        // Purple progress ring.
        progressPaint.setColor(0xFF8957E5);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(dpToPx(7));
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setProgress(float progress) {

        this.progress = Math.max(
                0f,
                Math.min(1f, progress)
        );

        invalidate();
    }

    public float getProgress() {
        return progress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float stroke = dpToPx(7);

        float radius = Math.min(
                getWidth(),
                getHeight()
        ) / 2f - stroke;

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        circleBounds.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        // Draw complete background circle.
        canvas.drawArc(
                circleBounds,
                0,
                360,
                false,
                backgroundPaint
        );

        // Draw remaining-time progress.
        canvas.drawArc(
                circleBounds,
                -90,
                360 * progress,
                false,
                progressPaint
        );
    }

    private float dpToPx(float dp) {

        return dp * getResources()
                .getDisplayMetrics()
                .density;
    }
}
