package app.morphe.extension.twitter.patches.fold;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.recyclerview.widget.RecyclerView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, qualifiers = "w960dp-h720dp-land-mdpi")
public class FoldPostAdapterTest {
    private static class NativeSource extends RecyclerView.f {
        final ArrayList<Long> ids = new ArrayList<>(Arrays.asList(10L, 20L, 30L, 40L));
        final AtomicLong clicked = new AtomicLong();
        int attached, detached, recycled;
        NativeSource() { setHasStableIds(true); }
        @Override public int getItemCount() { return ids.size(); }
        @Override public long getItemId(int position) { return ids.get(position); }
        @Override public int getItemViewType(int position) { return ids.get(position) == 20 ? 5 : 6; }
        @Override public RecyclerView.e0 onCreateViewHolder(ViewGroup parent, int type) {
            return new RecyclerView.e0(new View(parent.getContext())) {};
        }
        @Override public void onBindViewHolder(RecyclerView.e0 holder, int position) {
            long id = ids.get(position);
            holder.itemView.setOnClickListener(v -> clicked.set(id));
        }
        @Override public void onViewAttachedToWindow(RecyclerView.e0 holder) { attached++; }
        @Override public void onViewDetachedFromWindow(RecyclerView.e0 holder) { detached++; }
        @Override public void onViewRecycled(RecyclerView.e0 holder) { recycled++; }
    }
    @Test public void postIsNotDuplicatedAndNativeReplyActionsKeepTheirData() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        FrameLayout parent = new FrameLayout(activity);
        NativeSource source = new NativeSource();
        FoldPostAdapter left = new FoldPostAdapter(source, 1, true);
        FoldPostAdapter right = new FoldPostAdapter(source, 1, false);
        assertEquals(1, left.getItemCount());
        assertEquals(4, right.getItemCount()); // Original positions preserved for X's list controller.
        assertEquals(FoldPostAdapter.HIDDEN, right.getItemViewType(0));
        assertEquals(FoldPostAdapter.HIDDEN, right.getItemViewType(1));
        RecyclerView.e0 placeholder = right.onCreateViewHolder(parent, FoldPostAdapter.HIDDEN);
        right.onBindViewHolder(placeholder, 1);
        assertEquals(0, placeholder.itemView.getLayoutParams().height);
        assertFalse(placeholder.itemView.hasOnClickListeners());
        RecyclerView.e0 post = left.onCreateViewHolder(parent, left.getItemViewType(0));
        left.onBindViewHolder(post, 0);
        post.itemView.performClick();
        assertEquals(20, source.clicked.get());
        RecyclerView.e0 reply = right.onCreateViewHolder(parent, right.getItemViewType(2));
        right.onBindViewHolder(reply, 2);
        reply.itemView.performClick();
        assertEquals(30, source.clicked.get());
        assertEquals(1, left.findRelativeAdapterPositionIn(source, post, 0));
        assertEquals(2, right.findRelativeAdapterPositionIn(source, reply, 2));
        assertEquals(-1, left.findRelativeAdapterPositionIn(source, post, -1));
        left.onViewAttachedToWindow(post);
        left.onViewDetachedFromWindow(post);
        left.onViewRecycled(post);
        right.onViewRecycled(placeholder);
        assertEquals(1, source.attached);
        assertEquals(1, source.detached);
        assertEquals(1, source.recycled);
    }
    @Test public void refreshAndInsertedAncestorsKeepTheOriginalPostIdentity() {
        NativeSource source = new NativeSource();
        source.ids.add(0, 99L);
        assertEquals(2, FoldTweetPanels.locateFocal(source, 20, 5));
        source.ids.remove(2);
        assertEquals(-1, FoldTweetPanels.locateFocal(source, 20, 5));
    }
    @Test public void quotedPostCannotBeMistakenForTheFocalPost() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        FrameLayout row = new FrameLayout(activity);
        View quote = new View(activity);
        quote.setTag(0x7f010001, "QuotedTweet");
        row.addView(quote);
        assertFalse(FoldTweetPanels.isFocal(row, 0x7f010001));
        row.setTag(0x7f010001, "FocalTweet");
        assertTrue(FoldTweetPanels.isFocal(row, 0x7f010001));
    }
    @Test public void columnsLeaveMoreRoomForRepliesAndKeepScrollViewsSeparate() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        FoldTweetPanels.Columns columns = new FoldTweetPanels.Columns(activity);
        columns.addView(new View(activity));
        columns.addView(new View(activity));
        columns.addView(new View(activity));
        columns.measure(View.MeasureSpec.makeMeasureSpec(960, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY));
        columns.layout(0, 0, 960, 600);
        assertEquals(442, columns.getChildAt(0).getWidth());
        assertEquals(517, columns.getChildAt(1).getWidth());
        assertEquals(443, columns.getChildAt(1).getLeft());
        assertEquals(600, columns.getChildAt(0).getHeight());
    }
    @Test public void reducedChatMotionRespectsNativeAccessibilityAndInactiveWindows() {
        assertTrue(FoldChat.reduceMotion(true));
        assertFalse(FoldChat.reduceMotion(false));
        assertTrue(FoldChat.isInboxFragment("com.twitter.feature.xchat.XChatTabFragment"));
        assertTrue(FoldChat.isInboxFragment("com.twitter.app.dm.inbox.DMInboxFragment"));
        assertFalse(FoldChat.isInboxFragment("com.twitter.app.profiles.ProfileFragment"));
    }
    @Test public void leavingTheModeRestoresTheSameListAdapterAndLayoutParams() throws Exception {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        FrameLayout root = new FrameLayout(activity);
        View before = new View(activity), after = new View(activity);
        RecyclerView list = new RecyclerView(activity, null);
        list.setId(12345);
        NativeSource source = new NativeSource();
        list.setAdapter(source);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -1);
        root.addView(before);
        root.addView(list, params);
        root.addView(after);
        activity.setContentView(root);
        FoldTweetPanels panels = new FoldTweetPanels(activity, root);
        panels.install(list, 1);
        assertTrue(panels.isSplit());
        assertSame(list, activity.findViewById(12345));
        assertEquals(3, root.getChildCount());
        assertTrue(list.getAdapter() instanceof FoldPostAdapter);
        panels.sync(false);
        assertFalse(panels.isSplit());
        assertSame(list, root.getChildAt(1));
        assertSame(params, list.getLayoutParams());
        assertSame(source, list.getAdapter());
        assertSame(before, root.getChildAt(0));
        assertSame(after, root.getChildAt(2));
    }
}
