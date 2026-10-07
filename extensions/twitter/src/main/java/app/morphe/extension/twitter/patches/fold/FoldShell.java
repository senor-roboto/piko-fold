package app.morphe.extension.twitter.patches.fold;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.Map;

/** A reversible outer layout. X still owns the original screen, fragments and tab actions. */
public class FoldShell extends FrameLayout {
    private final Activity activity;
    private final FrameLayout pane;
    private final ScrollView rail;
    private final LinearLayout buttons;
    private final SharedPreferences preferences;
    private final ViewTreeObserver.OnGlobalLayoutListener layoutListener = this::syncTabs;
    private ViewGroup tabs;
    private View tabsContainer;
    private ViewGroup tabStrip;
    private int originalContainerHeight;
    private int originalTabsHeight;
    private int originalAccessibility;
    private boolean collapsed;
    private boolean enabled;
    private boolean railEnabled;
    private boolean active;
    private boolean embedded;
    private boolean railClaimed;
    private final boolean mainScreen;
    private int railTop;
    private int railBottom;
    private int readingWidth;

    public FoldShell(Activity activity) {
        super(activity);
        this.activity = activity;
        mainScreen = activity.getClass().getName().equals("com.twitter.app.main.MainActivity");
        preferences = activity.getSharedPreferences("piko_settings", Context.MODE_PRIVATE);
        pane = new FrameLayout(activity);
        addView(pane, new LayoutParams(-1, -1));
        rail = new ScrollView(activity);
        rail.setFillViewport(false);
        rail.setVerticalScrollBarEnabled(false);
        buttons = new LinearLayout(activity);
        buttons.setOrientation(LinearLayout.VERTICAL);
        buttons.setGravity(Gravity.CENTER_HORIZONTAL);
        buttons.setPadding(dp(4), 0, dp(4), dp(8));
        rail.addView(buttons, new ScrollView.LayoutParams(-1, -2));
        addView(rail, new LayoutParams(dp(64), -1));
        rail.setVisibility(GONE);
        refreshPreferences();
    }

    public void addOriginal(View view, ViewGroup.LayoutParams params) {
        pane.addView(view, params);
    }

    public void refreshPreferences() {
        Map<String, ?> saved = preferences.getAll();
        enabled = !Boolean.FALSE.equals(saved.get("fold_enabled"));
        railEnabled = !Boolean.FALSE.equals(saved.get("fold_rail"));
        readingWidth = 640;
        try {
            Object value = saved.get("fold_reading_width");
            if (value != null) readingWidth = Integer.parseInt(value.toString());
        } catch (NumberFormatException ignored) {}
        requestLayout();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
    }

    @Override protected void onDetachedFromWindow() {
        getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
        restoreTabs();
        super.onDetachedFromWindow();
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @SuppressLint("NewApi")
    private boolean targetWindow() {
        float density = getResources().getDisplayMetrics().density;
        if (Build.VERSION.SDK_INT >= 30) {
            // Window bounds survive adjustResize when the keyboard opens, and reflect multi-window.
            Rect bounds = activity.getWindowManager().getCurrentWindowMetrics().getBounds();
            return FoldGeometry.isTarget(bounds.width() / density, bounds.height() / density);
        }
        return FoldGeometry.isTarget(getResources().getConfiguration().screenWidthDp,
                getResources().getConfiguration().screenHeightDp);
    }

    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        embedded = FoldEmbedding.isEmbedded(activity);
        active = enabled && (targetWindow() || embedded);
        if (!active || !railEnabled) restoreTabs();
        boolean showRail = active && railEnabled && collapsed;
        int width = MeasureSpec.getSize(widthSpec), height = MeasureSpec.getSize(heightSpec);
        // The slot remains constant while native bottom bars animate or switch visibility.
        int railWidth = active && railEnabled && (mainScreen || railClaimed) ? dp(64) : 0;
        int available = Math.max(0, width - getPaddingLeft() - getPaddingRight() - railWidth);
        int paneWidth = active
                ? Math.min(available, dp(FoldGeometry.readingWidth(
                        Math.round(available / getResources().getDisplayMetrics().density), readingWidth)))
                : available;
        pane.measure(MeasureSpec.makeMeasureSpec(paneWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(Math.max(0, height - getPaddingTop() - getPaddingBottom()),
                        MeasureSpec.EXACTLY));
        rail.setVisibility(showRail ? VISIBLE : GONE);
        updateRailInsets(height);
        if (showRail) rail.measure(MeasureSpec.makeMeasureSpec(railWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(Math.max(0, height - railTop - railBottom),
                        MeasureSpec.EXACTLY));
        setMeasuredDimension(resolveSize(width, widthSpec), resolveSize(height, heightSpec));
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        boolean rtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        int railWidth = active && railEnabled && (mainScreen || railClaimed) ? dp(64) : 0;
        int start = getPaddingLeft() + (rtl ? 0 : railWidth);
        int available = getWidth() - getPaddingLeft() - getPaddingRight() - railWidth;
        int paneLeft = start + Math.max(0, (available - pane.getMeasuredWidth()) / 2);
        pane.layout(paneLeft, getPaddingTop(), paneLeft + pane.getMeasuredWidth(),
                getPaddingTop() + pane.getMeasuredHeight());
        if (railWidth > 0) {
            int railLeft = rtl ? getWidth() - getPaddingRight() - railWidth : getPaddingLeft();
            rail.layout(railLeft, railTop, railLeft + railWidth,
                    railTop + rail.getMeasuredHeight());
        }
    }

    @SuppressLint("NewApi")
    private void updateRailInsets(int height) {
        int top = 0, bottom = 0;
        WindowInsets insets = getRootWindowInsets();
        if (insets != null) {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsetsIgnoringVisibility(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                top = bars.top;
                bottom = bars.bottom;
            } else {
                top = insets.getSystemWindowInsetTop();
                bottom = insets.getStableInsetBottom();
            }
        }
        // Subtract space already excluded by the Activity decor; keep native pane insets intact.
        int[] position = new int[2];
        getLocationOnScreen(position);
        int windowBottom = getResources().getDisplayMetrics().heightPixels;
        if (Build.VERSION.SDK_INT >= 30) {
            Rect bounds = activity.getWindowManager().getCurrentWindowMetrics().getBounds();
            top += bounds.top;
            windowBottom = bounds.bottom;
        }
        railTop = getPaddingTop() + Math.max(0, top - position[1]) + dp(8);
        railBottom = getPaddingBottom() + Math.max(0, position[1] + height - (windowBottom - bottom));
    }

    protected View namedView(String name) {
        int id = getResources().getIdentifier(name, "id", activity.getPackageName());
        return id == 0 ? null : pane.findViewById(id);
    }

    private void syncTabs() {
        if (!active || !railEnabled) return;
        View candidate = namedView("tabs");
        View container = namedView("tabsContainer");
        if (!(candidate instanceof ViewGroup) || container == null) {
            // Keep the previous rail during a transient native view replacement.
            return;
        }
        if (!collapsed && (candidate.getVisibility() != VISIBLE || container.getVisibility() != VISIBLE)) return;
        ViewGroup group = (ViewGroup) candidate;
        if (group.getChildCount() != 1 || !(group.getChildAt(0) instanceof ViewGroup)) {
            restoreTabs();
            return;
        }
        ViewGroup strip = (ViewGroup) group.getChildAt(0);
        // Only recognise the native TabLayout structure; login bars and audio docks stay untouched.
        if (strip.getChildCount() < 2 || strip.getChildCount() > 10) {
            restoreTabs();
            return;
        }
        boolean rebuild = tabStrip != strip || buttons.getChildCount() != strip.getChildCount();
        if (!rebuild) {
            for (int i = 0; i < buttons.getChildCount(); i++) {
                if (((NativeTabButton) buttons.getChildAt(i)).source != strip.getChildAt(i)) {
                    rebuild = true;
                    break;
                }
            }
        }
        if (tabs != group || tabsContainer != container) {
            restoreTabs();
            tabs = group;
            tabsContainer = container;
        }
        tabStrip = strip;
        if (rebuild) {
            buttons.removeAllViews();
            for (int i = 0; i < strip.getChildCount(); i++) {
                View source = strip.getChildAt(i);
                buttons.addView(new NativeTabButton(activity, source, () -> {
                    post(this::syncTabs);
                }), new LinearLayout.LayoutParams(-1, dp(56)));
            }
        }
        if (!collapsed) {
            originalContainerHeight = container.getLayoutParams().height;
            originalTabsHeight = group.getLayoutParams().height;
            originalAccessibility = container.getImportantForAccessibility();
            // Fixed child height keeps native tabs measured and their icons/badges alive.
            ViewGroup.LayoutParams tabParams = group.getLayoutParams();
            tabParams.height = Math.max(dp(48), group.getHeight());
            group.setLayoutParams(tabParams);
            ViewGroup.LayoutParams containerParams = container.getLayoutParams();
            containerParams.height = 0;
            container.setLayoutParams(containerParams);
            container.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            collapsed = true;
            railClaimed = true;
            requestLayout();
        }
        // Some native transitions write the bottom container height back; never animate the slot.
        if (container.getLayoutParams().height != 0) {
            ViewGroup.LayoutParams params = container.getLayoutParams();
            params.height = 0;
            container.setLayoutParams(params);
        }
        for (int i = 0; i < buttons.getChildCount(); i++) {
            ((NativeTabButton) buttons.getChildAt(i)).sync();
        }
    }

    private void restoreTabs() {
        if (!collapsed) return;
        ViewGroup.LayoutParams containerParams = tabsContainer.getLayoutParams();
        containerParams.height = originalContainerHeight;
        tabsContainer.setLayoutParams(containerParams);
        tabsContainer.setImportantForAccessibility(originalAccessibility);
        ViewGroup.LayoutParams tabParams = tabs.getLayoutParams();
        tabParams.height = originalTabsHeight;
        tabs.setLayoutParams(tabParams);
        collapsed = false;
        rail.setVisibility(GONE);
        requestLayout();
    }
}
