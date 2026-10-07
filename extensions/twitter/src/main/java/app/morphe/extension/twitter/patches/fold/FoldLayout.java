package app.morphe.extension.twitter.patches.fold;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/** Installs once per browsing Activity; media, camera, login and composer are excluded. */
public final class FoldLayout implements Application.ActivityLifecycleCallbacks {
    private static boolean registered;
    private static Application application;

    public static synchronized void initialize(Application application) {
        if (registered) return;
        FoldLayout.application = application;
        registered = true;
        application.registerActivityLifecycleCallbacks(new FoldLayout());
        FoldEmbedding.initialize(application);
    }

    public static float tabletInset(Resources resources, int dimension) {
        if (application != null && FoldGeometry.isTarget(resources.getConfiguration().screenWidthDp,
                resources.getConfiguration().screenHeightDp)
                && !Boolean.FALSE.equals(application.getSharedPreferences("piko_settings", Context.MODE_PRIVATE)
                        .getAll().get("fold_enabled"))) return 0f;
        return resources.getDimension(dimension);
    }

    static boolean isBrowsingActivity(String name) {
        switch (name) {
            case "com.twitter.app.main.MainActivity":
            case "com.twitter.app.profiles.ProfileActivity":
            case "com.twitter.tweetdetail.TweetDetailActivity":
            case "com.twitter.android.search.implementation.results.SearchActivity":
            case "com.twitter.app.bookmarks.legacy.BookmarkActivity":
            case "com.twitter.app.bookmarks.folders.BookmarkFolderActivity":
            case "com.twitter.app.dm.RootDMActivity":
            case "com.twitter.app.dm.DMActivity":
            case "com.twitter.app.settings.SettingsRootCompatActivity":
                return true;
            default:
                return false;
        }
    }

    private void install(Activity activity) {
        if (!isBrowsingActivity(activity.getClass().getName())) return;
        View content = activity.findViewById(android.R.id.content);
        if (!(content instanceof FrameLayout)) return;
        FrameLayout parent = (FrameLayout) content;
        parent.post(() -> {
            if (activity.isDestroyed() || activity.isFinishing() || parent.getChildCount() == 0) return;
            try {
                if (parent.getChildAt(0) instanceof FoldShell) {
                    ((FoldShell) parent.getChildAt(0)).refreshPreferences();
                    return;
                }
                FoldShell shell = new FoldShell(activity);
                // Keep every original view and its ID, listeners and layout parameters intact.
                while (parent.getChildCount() > 0) {
                    View child = parent.getChildAt(0);
                    ViewGroup.LayoutParams params = child.getLayoutParams();
                    parent.removeView(child);
                    shell.addOriginal(child, params);
                }
                parent.addView(shell, new FrameLayout.LayoutParams(-1, -1));
            } catch (RuntimeException error) {
                Log.e("PikoFold", "Unable to install Fold layout", error);
            }
        });
    }

    @Override public void onActivityCreated(Activity activity, Bundle state) { install(activity); }
    @Override public void onActivityResumed(Activity activity) {
        FoldEmbedding.refresh();
        install(activity);
    }
    @Override public void onActivityStarted(Activity activity) {}
    @Override public void onActivityPaused(Activity activity) {}
    @Override public void onActivityStopped(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) {}
    @Override public void onActivityDestroyed(Activity activity) {}
}
