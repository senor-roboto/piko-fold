package app.morphe.extension.twitter.patches.fold;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.util.Pair;
import android.view.WindowMetrics;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/** Uses the public OEM Window Extensions API without replacing X's AndroidX libraries. */
public final class FoldEmbedding {
    private static Application application;
    private static Object component;
    private static Method setRules;
    private static Method isEmbedded;
    private static Set<Object> rules;
    private static Boolean configured;

    private FoldEmbedding() {}

    public static void initialize(Application app) {
        application = app;
        if (Build.VERSION.SDK_INT < 31) return;
        try {
            Class<?> provider = Class.forName("androidx.window.extensions.WindowExtensionsProvider");
            Object extensions = provider.getMethod("getWindowExtensions").invoke(null);
            Class<?> extensionApi = Class.forName("androidx.window.extensions.WindowExtensions");
            component = extensionApi.getMethod("getActivityEmbeddingComponent").invoke(extensions);
            if (component == null) return;
            Class<?> api = Class.forName("androidx.window.extensions.embedding.ActivityEmbeddingComponent");
            setRules = api.getMethod("setEmbeddingRules", Set.class);
            isEmbedded = api.getMethod("isActivityEmbedded", Activity.class);
            rules = buildRules();
            refresh();
        } catch (ReflectiveOperationException | LinkageError | RuntimeException error) {
            component = null;
            Log.i("PikoFold", "Native activity embedding unavailable; using one column", error);
        }
    }

    private static Set<Object> buildRules() throws ReflectiveOperationException {
        Predicate<Pair<Activity, Activity>> pair = value -> matchesPair(
                value.first.getClass().getName(), value.second.getClass().getName());
        Predicate<Pair<Activity, Intent>> intentPair = value -> matchesPair(
                value.first.getClass().getName(), componentName(value.second));
        Predicate<WindowMetrics> metrics = value -> {
            Rect bounds = value.getBounds();
            float density = application.getResources().getDisplayMetrics().density;
            return supportsTwoPanes(bounds.width() / density, bounds.height() / density);
        };
        Class<?> pairBuilder = Class.forName("androidx.window.extensions.embedding.SplitPairRule$Builder");
        Object builder = pairBuilder.getConstructor(Predicate.class, Predicate.class, Predicate.class)
                .newInstance(pair, intentPair, metrics);
        pairBuilder.getMethod("setSplitRatio", float.class).invoke(builder, 0.5f);
        pairBuilder.getMethod("setFinishPrimaryWithSecondary", int.class).invoke(builder, 0); // NEVER
        pairBuilder.getMethod("setFinishSecondaryWithPrimary", int.class).invoke(builder, 1); // ALWAYS
        pairBuilder.getMethod("setShouldClearTop", boolean.class).invoke(builder, true);
        addNativeDivider(pairBuilder, builder);
        Set<Object> result = new HashSet<>();
        result.add(pairBuilder.getMethod("build").invoke(builder));

        // Camera, media, composer and settings must fill the whole window.
        Class<?> expandBuilder = Class.forName("androidx.window.extensions.embedding.ActivityRule$Builder");
        Predicate<Activity> expandActivity = value -> shouldExpand(value.getClass().getName());
        Predicate<Intent> expandIntent = value -> shouldExpand(componentName(value));
        Object expand = expandBuilder.getConstructor(Predicate.class, Predicate.class)
                .newInstance(expandActivity, expandIntent);
        expandBuilder.getMethod("setShouldAlwaysExpand", boolean.class).invoke(expand, true);
        result.add(expandBuilder.getMethod("build").invoke(expand));
        return result;
    }

    private static void addNativeDivider(Class<?> pairBuilder, Object builder) {
        try {
            // Vendor API 6+: the system owns drag gestures, focus, resizing and back navigation.
            Class<?> dividerBuilder = Class.forName("androidx.window.extensions.embedding.DividerAttributes$Builder");
            Class<?> dividerApi = Class.forName("androidx.window.extensions.embedding.DividerAttributes");
            Object divider = dividerBuilder.getConstructor(int.class).newInstance(2); // DRAGGABLE
            dividerBuilder.getMethod("setWidthDp", int.class).invoke(divider, 4);
            dividerBuilder.getMethod("setPrimaryMinRatio", float.class).invoke(divider, 0.5f);
            dividerBuilder.getMethod("setPrimaryMaxRatio", float.class).invoke(divider, 0.58f);
            dividerBuilder.getMethod("setDividerColor", int.class).invoke(divider, 0xff646464);
            try {
                dividerBuilder.getMethod("setDraggingToFullscreenAllowed", boolean.class).invoke(divider, true);
            } catch (NoSuchMethodException ignored) {} // Vendor API 7+ only.
            Object dividerAttributes = dividerBuilder.getMethod("build").invoke(divider);
            Object rule = pairBuilder.getMethod("build").invoke(builder);
            Class<?> splitRule = Class.forName("androidx.window.extensions.embedding.SplitRule");
            Object original = splitRule.getMethod("getDefaultSplitAttributes").invoke(rule);
            Class<?> attributes = Class.forName("androidx.window.extensions.embedding.SplitAttributes");
            Class<?> attributeBuilder = Class.forName("androidx.window.extensions.embedding.SplitAttributes$Builder");
            Object adjusted = attributeBuilder.getConstructor(attributes).newInstance(original);
            attributeBuilder.getMethod("setDividerAttributes", dividerApi).invoke(adjusted, dividerAttributes);
            pairBuilder.getMethod("setDefaultSplitAttributes", attributes)
                    .invoke(builder, attributeBuilder.getMethod("build").invoke(adjusted));
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            // Older OEM implementations keep the working fixed 50:50 split.
        }
    }

    private static String componentName(Intent intent) {
        ComponentName name = intent.getComponent();
        return name == null ? "" : name.getClassName();
    }

    static boolean supportsTwoPanes(float width, float height) {
        // 384 dp for each Activity: a 64 dp master rail still leaves 320 dp for its feed.
        return Math.round(width) >= 768 && FoldGeometry.isTarget(width, height);
    }

    static boolean matchesPair(String primary, String secondary) {
        if (primary.equals(secondary)) return false;
        boolean master = primary.equals("com.twitter.app.main.MainActivity")
                || primary.equals("com.twitter.android.search.implementation.results.SearchActivity")
                || primary.equals("com.twitter.app.profiles.ProfileActivity")
                || primary.equals("com.twitter.app.bookmarks.legacy.BookmarkActivity")
                || primary.equals("com.twitter.app.bookmarks.folders.BookmarkFolderActivity")
                || primary.equals("com.twitter.app.dm.RootDMActivity");
        boolean detail = secondary.equals("com.twitter.tweetdetail.TweetDetailActivity")
                || secondary.equals("com.twitter.app.profiles.ProfileActivity")
                || secondary.equals("com.twitter.app.dm.DMActivity");
        return master && detail;
    }

    static boolean shouldExpand(String name) {
        // Ignore implicit external intents until their actual target Activity is known.
        return !name.isEmpty() && (name.equals("com.twitter.app.settings.SettingsRootCompatActivity")
                || !FoldLayout.isBrowsingActivity(name));
    }

    public static void refresh() {
        if (component == null || application == null) return;
        var saved = application.getSharedPreferences("piko_settings", 0).getAll();
        boolean enable = !Boolean.FALSE.equals(saved.get("fold_enabled"))
                && !Boolean.FALSE.equals(saved.get("fold_two_panes"));
        if (Boolean.valueOf(enable).equals(configured)) return;
        try {
            setRules.invoke(component, enable ? rules : new HashSet<>());
            configured = enable;
        } catch (ReflectiveOperationException | RuntimeException error) {
            component = null;
            Log.w("PikoFold", "Unable to set native split rules", error);
        }
    }

    public static boolean isEmbedded(Activity activity) {
        if (component == null || !Boolean.TRUE.equals(configured)) return false;
        try {
            return Boolean.TRUE.equals(isEmbedded.invoke(component, activity));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    public static boolean isAvailable() { return component != null; }
}
