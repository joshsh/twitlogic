package net.fortytwo.twitlogic.model.geo;

import net.fortytwo.twitlogic.model.TweetParseException;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * A planar polygon (which approximates a small spherical polygon reasonably well)
 * <p/>
 * Source: http://www.cs.princeton.edu/introcs/35purple/Polygon.java.html
 * <p/>
 * User: josh
 * Date: Aug 20, 2010
 * Time: 7:57:58 PM
 */
public class Polygon {
    private final int N;
    private final Point[] points;

    /**
     * @param json an array of numeric pairs
     */
    public Polygon(final JSONArray json) throws TweetParseException {
        N = json.length();
        this.points = new Point[N + 1];
        for (int i = 0; i < N; i++) {
            JSONArray pair;
            try {
                pair = json.getJSONArray(i);
                points[i] = new Point(pair.getDouble(0), pair.getDouble(1));
            } catch (JSONException e) {
                throw new TweetParseException(e);
            }
        }
        this.points[N] = points[0];
    }

    /*
    public Polygon(Point[] points) {
        N = points.length;
        this.points = new Point[N + 1];
        System.arraycopy(points, 0, this.points, 0, N);
        this.points[N] = points[0];
    }*/

    private double signedArea() {
        double sum = 0.0;
        for (int i = 0; i < N; i++) {
            sum = sum + (points[i].getLongitude() * points[i + 1].getLatitude()) - (points[i].getLatitude() * points[i + 1].getLongitude());
        }
        return 0.5 * sum;
    }

    public Point findCentroid() {
        double a = signedArea();

        // Account for trivial cases, which do occur in Twitter.
        if (0 == a) {
            return points[0];
        } else {
            double cx = 0.0, cy = 0.0;
            for (int i = 0; i < N; i++) {
                cx = cx + (points[i].getLongitude() + points[i + 1].getLongitude()) * (points[i].getLatitude() * points[i + 1].getLongitude() - points[i].getLongitude() * points[i + 1].getLatitude());
                cy = cy + (points[i].getLatitude() + points[i + 1].getLatitude()) * (points[i].getLatitude() * points[i + 1].getLongitude() - points[i].getLongitude() * points[i + 1].getLatitude());
            }
            cx /= (6 * -a);
            cy /= (6 * -a);
            return new Point(cx, cy);
        }
    }

    public static void main(final String[] args) {
        /*
        double[][][] d = new double[][][] {
                {{0, 0}, {0, 1}, {1, 1}, {1, 0}},
                {{0, 0}, {1, 0}, {1, 1}, {0, 1}},
                {{1, 1}, {1, 0}, {0, 0}, {0, 1}},
                {{0, 1}, {0, 1}, {1, 0}, {0, 0}}
        };

        for (double[][] ds : d) {

        }

        Point[][] polys = new Point[][]{
                {new Point(0,0), new Point(0, 1), new Point(1, 1), new Point(1, 0)},
                {new Point(0,0), new Point(1, 0), new Point(1, 1), new Point(0, 1)},
                {new Point(1,1), new Point(1, 0), new Point(0, 0), new Point(0, 1)},
                {new Point(0,1), new Point(1, 1), new Point(1, 0), new Point(0, 0)},
                {new Point(0,0), new Point(0, 0), new Point(0, 0), new Point(0, 0)}
        };

        for (Point[] ps : polys) {
            System.out.println(new Polygon(ps).findCentroid());
        }  */
    }
}
