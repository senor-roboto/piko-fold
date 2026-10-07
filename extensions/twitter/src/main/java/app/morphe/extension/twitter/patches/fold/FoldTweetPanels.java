package app.morphe.extension.twitter.patches.fold;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import androidx.recyclerview.widget.RecyclerView;

/** Post on the left, the original reply RecyclerView on the right. No second Activity. */
final class FoldTweetPanels {
    private final Activity activity;
    private final View root;
    private RecyclerView replies;
    private RecyclerView post;
    private RecyclerView.f source;
    private RecyclerView.l animator;
    private FoldPostAdapter postAdapter, replyAdapter;
    private ViewGroup parent;
    private ViewGroup.LayoutParams originalParams;
    private int originalIndex;
    private Columns columns;
    private long focalId;
    private int focalType;
    private boolean scheduled, failed, requested;
    private View composer;
    private RelativeLayout.LayoutParams composerParams;
    private final RecyclerView.h observer = new RecyclerView.h() {
        @Override public void d() { changed(); }
        @Override public void e() { changed(); }
        @Override public void f(int start, int count, Object payload) { changed(); }
        @Override public void g(int start, int count) { changed(); }
        @Override public void h(int start, int count) { changed(); }
        @Override public void i(int from, int to) { changed(); }
    };
    FoldTweetPanels(Activity activity, View root) { this.activity = activity; this.root = root; }
    boolean isSplit() { return columns != null; }

    void sync(boolean enable) {
        requested = enable;
        if (!enable) { restore(); return; }
        if (columns != null) {
            // X can replace the adapter after retrying a failed load. Let it take ownership.
            if (replies.getAdapter() != replyAdapter) restore();
            else alignComposer();
            return;
        }
        if (scheduled || failed) return;
        RecyclerView found = findList(root);
        if (found == null) return;
        int focal = findFocal(found);
        if (focal < 0) return;
        scheduled = true;
        root.post(() -> {
            scheduled = false;
            if (!requested || !root.isAttachedToWindow() || !FoldChat.target(activity)) return;
            try { install(found, focal); }
            catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
                failed = true;
                restore();
                Log.w("PikoFold", "Post/replies split unavailable; keeping native list", error);
            }
        });
    }
    private RecyclerView findList(View view) {
        if (view instanceof RecyclerView) return (RecyclerView) view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                RecyclerView found = findList(group.getChildAt(i));
                if (found != null && findFocal(found) >= 0) return found;
            }
        }
        return null;
    }
    private int findFocal(RecyclerView list) {
        int tag = activity.getResources().getIdentifier("weaverComponent", "id", activity.getPackageName());
        if (tag == 0) return -1;
        for (int i = 0; i < list.getChildCount(); i++) {
            View row = list.getChildAt(i);
            if (!isFocal(row, tag)) continue;
            RecyclerView.e0 holder = list.V(row);
            if (holder != null) return holder.getBindingAdapterPosition();
        }
        return -1;
    }
    static boolean isFocal(View view, int tag) {
        // Exact merge tag: a quoted post or a reply must never become the pinned post.
        if ("FocalTweet".equals(view.getTag(tag))) return true;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++)
                if (isFocal(group.getChildAt(i), tag)) return true;
        }
        return false;
    }
    void install(RecyclerView list, int focal) throws ReflectiveOperationException {
        if (columns != null || !(list.getParent() instanceof ViewGroup)) return;
        RecyclerView.f adapter = list.getAdapter();
        if (adapter == null || adapter instanceof FoldPostAdapter || focal < 0 || focal >= adapter.getItemCount()) return;
        // Create the native manager before changing anything. The APK ABI is checked at patch time.
        Class<?> manager = Class.forName("androidx.recyclerview.widget.LinearLayoutManager");
        RecyclerView.o layout = (RecyclerView.o) manager.getConstructor(int.class).newInstance(1);
        RecyclerView primary = new RecyclerView(activity, null);
        primary.setLayoutManager(layout);
        primary.setItemAnimator(null);
        FoldPostAdapter left = new FoldPostAdapter(adapter, focal, true);
        FoldPostAdapter right = new FoldPostAdapter(adapter, focal, false);
        replies = list;
        post = primary;
        source = adapter;
        animator = list.getItemAnimator();
        focalId = adapter.getItemId(focal);
        focalType = adapter.getItemViewType(focal);
        postAdapter = left;
        replyAdapter = right;
        parent = (ViewGroup) list.getParent();
        originalIndex = parent.indexOfChild(list);
        originalParams = list.getLayoutParams();
        columns = new Columns(activity);
        parent.removeView(list);
        columns.addView(primary, new FrameLayout.LayoutParams(-1, -1));
        columns.addView(list, new FrameLayout.LayoutParams(-1, -1));
        View divider = new View(activity);
        divider.setBackgroundColor(0x44646464);
        columns.addView(divider, new FrameLayout.LayoutParams(1, -1));
        parent.addView(columns, originalIndex, originalParams);
        list.setItemAnimator(null);
        list.setAdapter(right);
        primary.setAdapter(left);
        adapter.registerAdapterDataObserver(observer);
        alignComposer();
        root.requestLayout();
    }
    private void changed() {
        if (scheduled || columns == null) return;
        scheduled = true;
        root.post(() -> {
            scheduled = false;
            if (columns == null) return;
            int focal = locateFocal(source, focalId, focalType);
            if (focal < 0 && source.getItemCount() > 0) { restore(); return; }
            // Full invalidation coalesces refreshes and avoids mutating while X is laying out.
            postAdapter.focal = replyAdapter.focal = focal;
            postAdapter.notifyDataSetChanged();
            replyAdapter.notifyDataSetChanged();
        });
    }
    static int locateFocal(RecyclerView.f adapter, long id, int type) {
        int count = adapter.getItemCount();
        for (int i = 0; i < count; i++) {
            if (adapter.hasStableIds() ? adapter.getItemId(i) == id : adapter.getItemViewType(i) == type) return i;
        }
        return -1;
    }
    private void alignComposer() {
        int id = activity.getResources().getIdentifier("persistent_reply", "id", activity.getPackageName());
        View view = id == 0 ? null : root.findViewById(id);
        if (view == null || !(view.getLayoutParams() instanceof RelativeLayout.LayoutParams)) return;
        if (composer != view) {
            restoreComposer();
            composer = view;
            composerParams = new RelativeLayout.LayoutParams((RelativeLayout.LayoutParams) view.getLayoutParams());
        }
        int width = columns.getWidth() - columns.leftWidth() - columns.dividerWidth();
        if (width <= 0 || view.getLayoutParams().width == width) return;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(composerParams);
        params.width = width;
        params.removeRule(RelativeLayout.ALIGN_PARENT_START);
        params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        view.setLayoutParams(params);
    }
    private void restoreComposer() {
        if (composer != null) composer.setLayoutParams(composerParams);
        composer = null;
        composerParams = null;
    }
    void restore() {
        restoreComposer();
        if (columns == null) return;
        source.unregisterAdapterDataObserver(observer);
        post.setAdapter(null);
        if (replies.getAdapter() == replyAdapter) replies.setAdapter(source);
        replies.setItemAnimator(animator);
        columns.removeView(replies);
        parent.removeView(columns);
        parent.addView(replies, Math.min(originalIndex, parent.getChildCount()), originalParams);
        columns = null;
        replies = post = null;
        source = null;
        root.requestLayout();
    }
    static final class Columns extends FrameLayout {
        Columns(Activity activity) { super(activity); }
        int dividerWidth() { return Math.max(1, Math.round(getResources().getDisplayMetrics().density)); }
        int leftWidth() { return Math.round(getWidth() * 0.46f); }
        @Override protected void onMeasure(int w, int h) {
            int width = MeasureSpec.getSize(w), height = MeasureSpec.getSize(h);
            int left = Math.round(width * 0.46f), divider = dividerWidth();
            for (int i = 0; i < getChildCount(); i++) {
                int size = i == 0 ? left : i == 1 ? Math.max(0, width - left - divider) : divider;
                getChildAt(i).measure(MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            }
            setMeasuredDimension(width, height);
        }
        @Override protected void onLayout(boolean c, int l, int t, int r, int b) {
            int left = leftWidth(), divider = dividerWidth();
            if (getChildCount() != 3) return;
            getChildAt(0).layout(0, 0, left, getHeight());
            getChildAt(1).layout(left + divider, 0, getWidth(), getHeight());
            getChildAt(2).layout(left, 0, left + divider, getHeight());
        }
    }
}
