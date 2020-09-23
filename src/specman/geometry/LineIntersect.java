package specman.geometry;

import java.awt.geom.Point2D;

/**
 * Hilfklasse zur Berechnung von Schnittpunkten zwischen zwei Linien usw.
 * Die zentrale Berechnungsfunktion ist aus dem Internet geklaut von
 * http://stackoverflow.com/questions/16314069/calculation-of-intersections-between-line-segments
 * @author less02
 */
public class LineIntersect {

    public static Point2D.Double lineLineIntersect(double x1, double y1, double x2, double y2, double x3, double y3,
            double x4, double y4) {
        double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0.0) { // Lines are parallel.
            return null;
        }
        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
        double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
        if (ua >= 0.0d && ua <= 1.0d && ub >= 0.0d && ub <= 1.0d) {
            // Get the intersection point.
            return new Point2D.Double((x1 + ua * (x2 - x1)), (y1 + ua * (y2 - y1)));
        }

        return null;
    }

}
