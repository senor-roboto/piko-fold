package app.morphe.extension.twitter.patches.fold;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, qualifiers = "w960dp-h720dp-land-mdpi")
public class FoldLayoutTest {
    private Activity activity() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        activity.getSharedPreferences("piko_settings", Context.MODE_PRIVATE).edit().clear().commit();
        return activity;
    }

    private static void layout(View view, int width, int height) {
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, width, height);
    }

    @Test public void widthLimitAndPortraitRestorationKeepOriginalViews() {
        Activity activity = activity();
        FoldShell shell = new FoldShell(activity);
        View original = new View(activity);
        original.setId(1234);
        shell.addOriginal(original, new FrameLayout.LayoutParams(-1, -1));
        activity.setContentView(shell);
        layout(shell, 960, 720);
        assertEquals(640, original.getWidth());
        assertSame(original, activity.findViewById(1234));
        RuntimeEnvironment.setQualifiers("w720dp-h960dp-port-mdpi");
        layout(shell, 720, 960);
        assertEquals(720, original.getWidth());
        assertSame(original, activity.findViewById(1234));
    }

    @Test public void preferenceDisablesLayoutAndBoundsMalformedWidth() {
        Activity activity = activity();
        FoldShell shell = new FoldShell(activity);
        View original = new View(activity);
        shell.addOriginal(original, new FrameLayout.LayoutParams(-1, -1));
        activity.setContentView(shell);
        activity.getSharedPreferences("piko_settings", 0).edit()
                .putString("fold_reading_width", "invalid").commit();
        shell.refreshPreferences();
        layout(shell, 960, 720);
        assertEquals(640, original.getWidth());
        activity.getSharedPreferences("piko_settings", 0).edit()
                .putBoolean("fold_enabled", false).commit();
        shell.refreshPreferences();
        layout(shell, 960, 720);
        assertEquals(960, original.getWidth());
    }

    @Test public void railUsesNativeClicksAndRestoresBottomBar() {
        Activity activity = activity();
        LinearLayout nativeStrip = new LinearLayout(activity);
        FrameLayout nativeTabs = new FrameLayout(activity);
        FrameLayout container = new FrameLayout(activity);
        nativeTabs.addView(nativeStrip, new FrameLayout.LayoutParams(-1, -1));
        container.addView(nativeTabs, new FrameLayout.LayoutParams(-1, 48));
        AtomicInteger clicked = new AtomicInteger();
        for (int i = 0; i < 3; i++) {
            View tab = new View(activity);
            tab.setContentDescription("Native tab " + i);
            tab.setOnClickListener(view -> clicked.incrementAndGet());
            nativeStrip.addView(tab, new LinearLayout.LayoutParams(100, 48));
        }
        FoldShell shell = new FoldShell(activity) {
            @Override protected View namedView(String name) {
                return "tabs".equals(name) ? nativeTabs : "tabsContainer".equals(name) ? container : null;
            }
        };
        shell.addOriginal(container, new FrameLayout.LayoutParams(-1, -2));
        activity.setContentView(shell);
        layout(shell, 960, 720);
        shell.getViewTreeObserver().dispatchOnGlobalLayout();
        layout(shell, 960, 720);
        assertEquals(0, container.getLayoutParams().height);
        assertEquals(View.VISIBLE, shell.getChildAt(1).getVisibility());
        FrameLayout rail = (FrameLayout) shell.getChildAt(1); // ScrollView extends FrameLayout.
        LinearLayout buttons = (LinearLayout) rail.getChildAt(0);
        assertEquals(3, buttons.getChildCount());
        buttons.getChildAt(1).performClick();
        assertEquals(1, clicked.get());
        assertEquals("Native tab 1", buttons.getChildAt(1).getContentDescription());
        int stableLeft = container.getLeft() + ((View) container.getParent()).getLeft();
        int stableWidth = container.getWidth();
        container.setVisibility(View.GONE); // Native notification -> DM transition hides this briefly.
        shell.getViewTreeObserver().dispatchOnGlobalLayout();
        layout(shell, 960, 720);
        assertEquals(View.VISIBLE, shell.getChildAt(1).getVisibility());
        assertEquals(stableLeft, container.getLeft() + ((View) container.getParent()).getLeft());
        container.setVisibility(View.VISIBLE);
        container.getLayoutParams().height = 48; // Native animator writes its old height back.
        shell.getViewTreeObserver().dispatchOnGlobalLayout();
        layout(shell, 960, 720);
        assertEquals(0, container.getLayoutParams().height);
        assertEquals(stableWidth, container.getWidth());
        activity.getSharedPreferences("piko_settings", 0).edit().putBoolean("fold_rail", false).commit();
        shell.refreshPreferences();
        layout(shell, 960, 720);
        assertEquals(-2, container.getLayoutParams().height);
        assertEquals(48, nativeTabs.getLayoutParams().height);
        assertEquals(View.GONE, shell.getChildAt(1).getVisibility());
    }

    @Test public void mediaLoginAndComposerAreExcluded() {
        assertTrue(FoldLayout.isBrowsingActivity("com.twitter.app.main.MainActivity"));
        assertTrue(FoldLayout.isBrowsingActivity("com.twitter.tweetdetail.TweetDetailActivity"));
        assertFalse(FoldLayout.isBrowsingActivity("com.twitter.composer.ComposerActivity"));
        assertFalse(FoldLayout.isBrowsingActivity("com.twitter.camera.controller.root.CameraActivity"));
        assertFalse(FoldLayout.isBrowsingActivity("com.twitter.android.login.LoginActivity"));
        assertFalse(FoldLayout.isBrowsingActivity("com.x.android.main.MainActivity"));
    }

    @Test @Config(sdk = 35) public void currentWindowMetricsOnModernAndroid() {
        Activity activity = activity();
        FoldShell shell = new FoldShell(activity);
        View original = new View(activity);
        shell.addOriginal(original, new FrameLayout.LayoutParams(-1, -1));
        activity.setContentView(shell);
        layout(shell, 960, 720);
        assertEquals(640, original.getWidth());
        // Keyboard resize must not turn a 4:3 window into a non-target wide viewport.
        layout(shell, 960, 400);
        assertEquals(640, original.getWidth());
    }

    @Test public void splitRulesKeepBothPanesUsableAndRejectOtherFormats() {
        assertTrue(FoldEmbedding.supportsTwoPanes(768, 576));
        assertTrue(FoldEmbedding.supportsTwoPanes(767.99f, 576));
        assertTrue(FoldEmbedding.supportsTwoPanes(960, 720));
        assertFalse(FoldEmbedding.supportsTwoPanes(720, 540));
        assertFalse(FoldEmbedding.supportsTwoPanes(576, 768));
        assertFalse(FoldEmbedding.supportsTwoPanes(1280, 720));
        assertTrue(FoldEmbedding.matchesPair("com.twitter.app.main.MainActivity",
                "com.twitter.tweetdetail.TweetDetailActivity"));
        assertTrue(FoldEmbedding.matchesPair("com.twitter.app.dm.RootDMActivity",
                "com.twitter.app.dm.DMActivity"));
        assertFalse(FoldEmbedding.matchesPair("com.twitter.tweetdetail.TweetDetailActivity",
                "com.twitter.app.main.MainActivity"));
        assertFalse(FoldEmbedding.matchesPair("com.twitter.app.main.MainActivity",
                "com.twitter.composer.ComposerActivity"));
        assertTrue(FoldEmbedding.shouldExpand("com.twitter.composer.ComposerActivity"));
        assertFalse(FoldEmbedding.shouldExpand("com.twitter.app.main.MainActivity"));
        assertFalse(FoldEmbedding.shouldExpand("com.twitter.tweetdetail.TweetDetailActivity"));
        assertFalse(FoldEmbedding.shouldExpand(""));
        assertFalse(FoldEmbedding.isEmbedded(activity())); // No vendor component on this runtime.
    }
}
