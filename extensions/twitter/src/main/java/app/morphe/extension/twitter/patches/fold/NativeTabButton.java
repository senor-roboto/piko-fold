package app.morphe.extension.twitter.patches.fold;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

/** Renders and invokes the original TabView, including its selected icon and badges. */
final class NativeTabButton extends View {
    final View source;
    private final Paint selectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    NativeTabButton(Context context, View source, Runnable onNavigate) {
        super(context);
        this.source = source;
        setClickable(true);
        setFocusable(true);
        setMinimumHeight((int) (56 * getResources().getDisplayMetrics().density));
        TypedValue background = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, background, true)
                && background.resourceId != 0) setBackgroundResource(background.resourceId);
        selectionPaint.setColor(Color.argb(36, 29, 155, 240));
        setOnClickListener(view -> {
            if (source.isEnabled()) {
                source.performClick();
                onNavigate.run();
            }
        });
        setOnLongClickListener(view -> source.performLongClick());
        sync();
    }

    void sync() {
        setContentDescription(source.getContentDescription());
        setEnabled(source.isEnabled());
        setSelected(source.isSelected());
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (source.isSelected()) {
            canvas.drawRoundRect(4, 4, getWidth() - 4, getHeight() - 4, 16, 16, selectionPaint);
        }
        int width = source.getWidth(), height = source.getHeight();
        if (width <= 0 || height <= 0) return;
        float scale = Math.min((getWidth() - 8f) / width, (getHeight() - 8f) / height);
        int save = canvas.save();
        canvas.translate((getWidth() - width * scale) / 2f, (getHeight() - height * scale) / 2f);
        canvas.scale(scale, scale);
        // Live draw, not a screenshot: the native icon tint, badges and tab customisations survive.
        source.draw(canvas);
        canvas.restoreToCount(save);
    }

    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName("android.widget.Button");
        info.setSelected(source.isSelected());
    }
}
