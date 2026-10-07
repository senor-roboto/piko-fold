package app.morphe.extension.twitter.patches.fold;

/** Window geometry in dp. The target is explicitly landscape 4:3, not all tablets. */
public final class FoldGeometry {
    private FoldGeometry() {}

    public static boolean isTarget(float width, float height) {
        if (!Float.isFinite(width) || !Float.isFinite(height)
                || width < 600 || height < 480 || width <= height) return false;
        float ratio = width / height;
        // Allow system bars and small Fold display variations around 4:3.
        return ratio >= 1.25f && ratio <= 1.45f;
    }

    public static int readingWidth(int available, int preferred) {
        return Math.max(0, Math.min(available, Math.max(480, Math.min(840, preferred))));
    }
}
