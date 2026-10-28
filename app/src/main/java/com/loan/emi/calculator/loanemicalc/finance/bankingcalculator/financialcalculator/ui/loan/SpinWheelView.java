package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;

public class SpinWheelView extends View {
    public static final String[] PRIZES = {
            "Upto 1 Lac",
            "Upto 5 Lac",
            "Upto 3 Lac"
    };

    private final Paint slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();
    private final int[] colors = new int[3];

    public SpinWheelView(Context context) {
        super(context);
        init();
    }

    public SpinWheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        colors[0] = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        colors[1] = ContextCompat.getColor(getContext(), R.color.colorAccent);
        colors[2] = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(6f);
        borderPaint.setColor(0xFFFFFFFF);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(getWidth(), getHeight());
        float pad = 12f;
        oval.set(pad, pad, size - pad, size - pad);
        float sweep = 360f / PRIZES.length;
        textPaint.setTextSize(size * 0.055f);

        for (int i = 0; i < PRIZES.length; i++) {
            slicePaint.setColor(colors[i % colors.length]);
            float start = -90f + i * sweep;
            canvas.drawArc(oval, start, sweep, true, slicePaint);
            canvas.drawArc(oval, start, sweep, true, borderPaint);

            double mid = Math.toRadians(start + sweep / 2f);
            float radius = size / 2f;
            float tx = (float) (radius + Math.cos(mid) * radius * 0.55f);
            float ty = (float) (radius + Math.sin(mid) * radius * 0.55f);
            canvas.save();
            canvas.rotate((float) Math.toDegrees(mid) + 90f, tx, ty);
            canvas.drawText(PRIZES[i], tx, ty + textPaint.getTextSize() / 3f, textPaint);
            canvas.restore();
        }

        // center hub
        slicePaint.setColor(0xFFFFFFFF);
        canvas.drawCircle(size / 2f, size / 2f, size * 0.08f, slicePaint);
        slicePaint.setColor(colors[0]);
        canvas.drawCircle(size / 2f, size / 2f, size * 0.045f, slicePaint);
    }

    /** Returns prize index for current rotation degrees. */
    public static int prizeIndexForRotation(float rotationDegrees) {
        float normalized = ((rotationDegrees % 360f) + 360f) % 360f;
        // pointer at top; slices start at -90 in drawing, so adjust
        float pointerAngle = (360f - normalized) % 360f;
        float sweep = 360f / PRIZES.length;
        int index = (int) (pointerAngle / sweep) % PRIZES.length;
        return Math.max(0, Math.min(PRIZES.length - 1, index));
    }
}
