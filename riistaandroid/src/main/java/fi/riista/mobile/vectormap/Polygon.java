package fi.riista.mobile.vectormap;

import android.graphics.Path;

import java.util.LinkedList;
import java.util.List;

public final class Polygon {
    private final LinearRing outerRing;
    private List<LinearRing> innerRings;

    public Polygon(final LinearRing outerRing) {
        this.outerRing = outerRing;
        this.innerRings = null;
    }

    public void addInnerRing(final LinearRing innerRing) {
        if (this.innerRings == null) {
            this.innerRings = new LinkedList<>();
        }
        this.innerRings.add(innerRing);
    }

    public Path toPath() {
        final Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        path.moveTo(outerRing.getX(0), outerRing.getY(0));

        for (int i = 1; i < outerRing.size(); i++) {
            path.lineTo(outerRing.getX(i), outerRing.getY(i));
        }

        if (innerRings != null) {
            for (LinearRing innerRing : innerRings) {
                path.moveTo(innerRing.getX(0), innerRing.getY(0));

                for (int i = 1; i < innerRing.size(); i++) {
                    path.lineTo(innerRing.getX(i), innerRing.getY(i));
                }
            }
        }

        return path;
    }
}
