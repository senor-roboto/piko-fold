package androidx.recyclerview.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/** Compile-only ABI of RecyclerView in the supported X 12.19.1 APK. Never bundled. */
public class RecyclerView extends ViewGroup {
    private f adapter;
    public RecyclerView(Context context, AttributeSet attrs) { super(context, attrs); }
    public f getAdapter() { return adapter; }
    public void setAdapter(f adapter) { this.adapter = adapter; }
    public void setLayoutManager(o manager) {}
    public l getItemAnimator() { return null; }
    public void setItemAnimator(l animator) {}
    public e0 V(View child) { return null; }
    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {}
    public static abstract class o {}
    public static abstract class l {}
    public static abstract class e0 {
        public final View itemView;
        public e0(View view) { itemView = view; }
        public final int getBindingAdapterPosition() { return -1; }
    }
    public static abstract class h {
        public void d() {}
        public void e() {}
        public void f(int start, int count, Object payload) {}
        public void g(int start, int count) {}
        public void h(int start, int count) {}
        public void i(int from, int to) {}
        public void j() {}
    }
    public static abstract class f {
        private boolean stableIds;
        public abstract int getItemCount();
        public long getItemId(int position) { return -1; }
        public int getItemViewType(int position) { return 0; }
        public final boolean hasStableIds() { return stableIds; }
        public void setHasStableIds(boolean value) { stableIds = value; }
        public abstract e0 onCreateViewHolder(ViewGroup parent, int type);
        public abstract void onBindViewHolder(e0 holder, int position);
        public final e0 createViewHolder(ViewGroup parent, int type) { return onCreateViewHolder(parent, type); }
        public final void bindViewHolder(e0 holder, int position) { onBindViewHolder(holder, position); }
        public int findRelativeAdapterPositionIn(f adapter, e0 holder, int position) { return position; }
        public void onViewAttachedToWindow(e0 holder) {}
        public void onViewDetachedFromWindow(e0 holder) {}
        public void onViewRecycled(e0 holder) {}
        public boolean onFailedToRecycleView(e0 holder) { return false; }
        public void registerAdapterDataObserver(h observer) {}
        public void unregisterAdapterDataObserver(h observer) {}
        public final void notifyDataSetChanged() {}
    }
}
