import app.morphe.extension.twitter.patches.fold.FoldGeometry;

public final class FoldGeometryTest {
    public static void main(String[] args) {
        check(FoldGeometry.isTarget(960, 720), "landscape 4:3");
        check(FoldGeometry.isTarget(800, 600), "Fold landscape");
        check(FoldGeometry.isTarget(720, 540), "compact 4:3");
        check(!FoldGeometry.isTarget(720, 960), "portrait 3:4 stays stock");
        check(!FoldGeometry.isTarget(960, 540), "16:9 stays stock");
        check(!FoldGeometry.isTarget(600, 600), "square stays stock");
        check(!FoldGeometry.isTarget(540, 405), "small split window stays stock");
        check(!FoldGeometry.isTarget(Float.NaN, 600), "invalid geometry");
        check(FoldGeometry.readingWidth(760, 640) == 640, "reading limit");
        check(FoldGeometry.readingWidth(400, 640) == 400, "never exceed window");
        check(FoldGeometry.readingWidth(1000, 2000) == 840, "bound excessive settings");
        check(FoldGeometry.readingWidth(700, -1) == 480, "bound small settings");
        System.out.println("12 Fold geometry checks passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
