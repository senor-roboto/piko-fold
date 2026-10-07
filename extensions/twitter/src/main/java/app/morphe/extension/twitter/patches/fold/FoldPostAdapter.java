package app.morphe.extension.twitter.patches.fold;

import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

/** Two projections of one native data source. Reply positions and IDs never change. */
final class FoldPostAdapter extends RecyclerView.f {
    static final int HIDDEN = Integer.MIN_VALUE;
    final RecyclerView.f source;
    final boolean post;
    int focal;
    FoldPostAdapter(RecyclerView.f source, int focal, boolean post) {
        this.source = source;
        this.focal = focal;
        this.post = post;
        setHasStableIds(source.hasStableIds());
    }
    private int sourcePosition(int position) { return post ? focal : position; }
    private boolean hidden(int position) { return !post && position <= focal; }
    @Override public int getItemCount() {
        return post ? (focal >= 0 && focal < source.getItemCount() ? 1 : 0) : source.getItemCount();
    }
    @Override public long getItemId(int position) { return source.getItemId(sourcePosition(position)); }
    @Override public int getItemViewType(int position) {
        return hidden(position) ? HIDDEN : source.getItemViewType(sourcePosition(position));
    }
    @Override public RecyclerView.e0 onCreateViewHolder(ViewGroup parent, int type) {
        if (type != HIDDEN) return source.createViewHolder(parent, type);
        View placeholder = new View(parent.getContext());
        placeholder.setLayoutParams(new ViewGroup.LayoutParams(-1, 0));
        placeholder.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return new EmptyHolder(placeholder);
    }
    @Override public void onBindViewHolder(RecyclerView.e0 holder, int position) {
        if (!(holder instanceof EmptyHolder)) source.bindViewHolder(holder, sourcePosition(position));
    }
    @Override public int findRelativeAdapterPositionIn(RecyclerView.f adapter,
            RecyclerView.e0 holder, int position) {
        if (adapter == this) return position;
        if (position < 0 || position >= getItemCount()) return -1;
        return source.findRelativeAdapterPositionIn(adapter, holder, sourcePosition(position));
    }
    @Override public void onViewAttachedToWindow(RecyclerView.e0 holder) {
        if (!(holder instanceof EmptyHolder)) source.onViewAttachedToWindow(holder);
    }
    @Override public void onViewDetachedFromWindow(RecyclerView.e0 holder) {
        if (!(holder instanceof EmptyHolder)) source.onViewDetachedFromWindow(holder);
    }
    @Override public void onViewRecycled(RecyclerView.e0 holder) {
        if (!(holder instanceof EmptyHolder)) source.onViewRecycled(holder);
    }
    @Override public boolean onFailedToRecycleView(RecyclerView.e0 holder) {
        return holder instanceof EmptyHolder || source.onFailedToRecycleView(holder);
    }
    static final class EmptyHolder extends RecyclerView.e0 { EmptyHolder(View view) { super(view); } }
}
