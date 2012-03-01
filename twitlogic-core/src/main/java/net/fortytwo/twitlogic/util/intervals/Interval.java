package net.fortytwo.twitlogic.util.intervals;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class Interval<C extends Comparable<C>> {
    public enum IntervalRelation {
        PRECEDES, SUCCEEDS, OVERLAPSWITH
    }

    private final C start;
    private final C end;

    public Interval(final C start,
                    final C end) {
        this.start = start;
        this.end = end;
    }

    public C getStart() {
        return start;
    }

    public C getEnd() {
        return end;
    }

    public IntervalRelation compareTo(final Interval<C> other) {
        int startVsStart = start.compareTo(other.start);

        if (startVsStart < 0) {
            int endVsStart = end.compareTo(other.start);

            if (endVsStart < 0) {
                return IntervalRelation.PRECEDES;
            } else {
                return IntervalRelation.OVERLAPSWITH;
            }
        } else {
            int startVsEnd = start.compareTo(other.end);

            if (startVsEnd > 0) {
                return IntervalRelation.SUCCEEDS;
            } else {
                return IntervalRelation.OVERLAPSWITH;
            }
        }
    }

    public boolean identicalTo(final Interval<C> other) {
        return 0 == this.start.compareTo(other.start)
                && 0 == this.end.compareTo(other.end);
    }

    public static <C extends Comparable<C>> Interval merge(final Interval<C> i1,
                                                           final Interval<C> i2) {
        return new Interval<C>(min(i1.start, i2.start), max(i1.end, i2.end));
    }

    private static <C extends Comparable<C>> C min(final C d1, final C d2) {
        return d1.compareTo(d2) < 0
                ? d1
                : d2;
    }

    private static <C extends Comparable<C>> C max(final C d1, final C d2) {
        return d1.compareTo(d2) > 0
                ? d1
                : d2;
    }
}
