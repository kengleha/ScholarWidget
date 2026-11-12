package io.github.scholarwidget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/** Draws the citations-per-year bar chart into a bitmap for the widget's ImageView. */
public final class ChartRenderer {
    private ChartRenderer() {}

    private static final int WIDTH = 720;
    private static final int HEIGHT = 200;

    /** Returns null when there is nothing to draw. */
    public static Bitmap render(Context context, ScholarStats s) {
        int n = s.years.length;
        if (n == 0) {
            return null;
        }
        int max = 1;
        for (int c : s.counts) {
            max = Math.max(max, c);
        }

        Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint bar = new Paint(Paint.ANTI_ALIAS_FLAG);
        bar.setColor(context.getColor(R.color.chart_bar));

        Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
        label.setColor(context.getColor(R.color.chart_label));
        label.setTextSize(26f);
        label.setTextAlign(Paint.Align.CENTER);

        float labelBand = 34f;
        float chartHeight = HEIGHT - labelBand;
        float slot = (float) WIDTH / n;
        float barWidth = slot * 0.7f;
        // Label every year when there is room, otherwise every 2nd/5th,
        // always anchored so the most recent year is labelled.
        int step = n <= 12 ? 1 : (n <= 24 ? 2 : 5);

        for (int i = 0; i < n; i++) {
            float left = i * slot + (slot - barWidth) / 2f;
            float h = chartHeight * s.counts[i] / max;
            if (s.counts[i] > 0) {
                h = Math.max(h, 3f);
                canvas.drawRoundRect(
                        new RectF(left, chartHeight - h, left + barWidth, chartHeight),
                        4f, 4f, bar);
            }
            if ((n - 1 - i) % step == 0) {
                String text = step == 1 && n > 8
                        ? "'" + String.format("%02d", s.years[i] % 100)
                        : String.valueOf(s.years[i]);
                canvas.drawText(text, i * slot + slot / 2f, HEIGHT - 6f, label);
            }
        }
        return bitmap;
    }
}
