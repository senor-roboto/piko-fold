package app.morphe.extension.twitter.patches.fold;

import android.app.Activity;
import android.os.Build;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/** Runtime predicates for native inbox/conversation rules and XChat reduced motion. */
public final class FoldChat {
    private static WeakReference<Activity> foreground = new WeakReference<>(null);
    private static Object fade;
    private FoldChat() {}
    static void resumed(Activity activity) { foreground = new WeakReference<>(activity); }
    static void paused(Activity activity) {
        if (foreground.get() == activity) foreground.clear();
    }
    static String selectedFragment(Activity activity) {
        View pager = findPager(activity.findViewById(android.R.id.content));
        if (pager == null) return "";
        try {
            Method getter = pager.getClass().getDeclaredMethod("getCurrentFragment");
            getter.setAccessible(true);
            Object fragment = getter.invoke(pager);
            return fragment == null ? "" : fragment.getClass().getName();
        } catch (ReflectiveOperationException | RuntimeException ignored) { return ""; }
    }
    private static View findPager(View view) {
        if (view == null) return null;
        if (view.getClass().getName().equals("com.twitter.app.main.BottomNavViewPager")) return view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View found = findPager(group.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }
    static boolean isInbox(Activity activity) {
        return activity.getClass().getName().equals("com.twitter.app.dm.RootDMActivity")
                || isInboxFragment(selectedFragment(activity));
    }
    static boolean isInboxFragment(String name) {
        return name.equals("com.twitter.app.dm.inbox.DMInboxFragment")
                || name.equals("com.twitter.feature.xchat.XChatTabFragment");
    }
    static boolean target(Activity activity) {
        float density = activity.getResources().getDisplayMetrics().density;
        if (Build.VERSION.SDK_INT >= 30) {
            Rect bounds = activity.getWindowManager().getCurrentWindowMetrics().getBounds();
            return FoldGeometry.isTarget(bounds.width() / density, bounds.height() / density);
        }
        return FoldGeometry.isTarget(activity.getResources().getConfiguration().screenWidthDp,
                activity.getResources().getConfiguration().screenHeightDp);
    }
    public static boolean reduceMotion(boolean nativeValue) {
        Activity activity = foreground.get();
        if (activity == null || !isInbox(activity) || !target(activity)) return nativeValue;
        var saved = activity.getSharedPreferences("piko_settings", 0).getAll();
        return nativeValue || (!Boolean.FALSE.equals(saved.get("fold_enabled"))
                && !Boolean.FALSE.equals(saved.get("fold_reduce_chat_motion")));
    }
    public static Object animator(Object original) {
        if (!reduceMotion(false)) return original;
        try {
            if (fade == null) {
                // X already ships this Decompose fade animator. Keep its lifecycle and back stack.
                Class<?> factory = Class.forName("com.arkivanov.decompose.extensions.compose.experimental.stack.animation.d0");
                Class<?> spec = Class.forName("androidx.compose.animation.core.h3");
                fade = factory.getMethod("a", spec, int.class).invoke(null, null, 3);
            }
            return fade;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return original;
        }
    }
}
