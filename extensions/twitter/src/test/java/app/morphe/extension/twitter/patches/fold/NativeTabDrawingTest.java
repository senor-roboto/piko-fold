package app.morphe.extension.twitter.patches.fold;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35, qualifiers = "mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class NativeTabDrawingTest {
    @Test public void centersOffCenterNativeIconWithoutShrinkingOrLosingBadge() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        FrameLayout nativeTab = new FrameLayout(activity);
        nativeTab.setBackgroundColor(Color.BLUE);
        ImageView icon = new ImageView(activity);
        icon.setImageDrawable(new ColorDrawable(Color.RED));
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(24, 24);
        iconParams.leftMargin = 104;
        iconParams.topMargin = 2;
        nativeTab.addView(icon, iconParams);
        View badge = new View(activity);
        badge.setBackgroundColor(Color.GREEN);
        FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(6, 6);
        badgeParams.leftMargin = 128;
        nativeTab.addView(badge, badgeParams);
        measure(nativeTab, 160, 48);
        NativeTabButton button = new NativeTabButton(activity, nativeTab, () -> {});
        measure(button, 64, 56);
        Bitmap bitmap = Bitmap.createBitmap(64, 56, Bitmap.Config.ARGB_8888);
        button.draw(new Canvas(bitmap));
        assertEquals(Color.RED, bitmap.getPixel(22, 28));
        assertEquals(Color.RED, bitmap.getPixel(42, 28));
        assertEquals(Color.GREEN, bitmap.getPixel(47, 17));
        assertEquals(Color.TRANSPARENT, bitmap.getPixel(0, 28));
    }

    private static void measure(View view, int width, int height) {
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, width, height);
    }
}
