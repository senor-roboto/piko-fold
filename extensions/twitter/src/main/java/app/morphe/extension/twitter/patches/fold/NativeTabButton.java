package app.morphe.extension.twitter.patches.fold;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;

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
        if (!TextUtils.equals(getContentDescription(), source.getContentDescription()))
            setContentDescription(source.getContentDescription());
        if (isEnabled() != source.isEnabled()) setEnabled(source.isEnabled());
        if (isSelected() != source.isSelected()) setSelected(source.isSelected());
        invalidate();
    }

    private float dp(float value) { return value * getResources().getDisplayMetrics().density; }

    private static ImageView findIcon(View root) {
        if (root instanceof ImageView && root.getVisibility() == VISIBLE
                && ((ImageView) root).getDrawable() != null) return (ImageView) root;
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                ImageView icon = findIcon(group.getChildAt(i));
                if (icon != null) return icon;
            }
        }
        return null;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (source.isSelected()) {
            canvas.drawRoundRect(getWidth() / 2f - dp(24), getHeight() / 2f - dp(22),
                    getWidth() / 2f + dp(24), getHeight() / 2f + dp(22), dp(22), dp(22), selectionPaint);
        }
        int width = source.getWidth(), height = source.getHeight();
        if (width <= 0 || height <= 0) return;
        float centerX = width / 2f, centerY = height / 2f;
        ImageView icon = findIcon(source);
        if (icon != null && source instanceof ViewGroup) {
            Rect bounds = new Rect(0, 0, icon.getWidth(), icon.getHeight());
            ((ViewGroup) source).offsetDescendantRectToMyCoords(icon, bounds);
            centerX = bounds.exactCenterX();
            centerY = bounds.exactCenterY();
        }
        int save = canvas.save();
        Path clip = new Path();
        clip.addRoundRect(getWidth() / 2f - dp(24), getHeight() / 2f - dp(22),
                getWidth() / 2f + dp(24), getHeight() / 2f + dp(22), dp(22), dp(22), Path.Direction.CW);
        canvas.clipPath(clip);
        canvas.translate(getWidth() / 2f - centerX, getHeight() / 2f - centerY);
        // Keep native tint and badges at their original size. Clip the old horizontal-tab background.
        source.draw(canvas);
        canvas.restoreToCount(save);
    }

    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName("android.widget.Button");
        info.setSelected(source.isSelected());
    }
}
