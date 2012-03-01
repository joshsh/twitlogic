package net.fortytwo.twitlogic.util.intervals;

import java.util.List;
import java.util.LinkedList;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class IntervalSequence<C extends Comparable<C>> {
    private final List<Interval<C>> consecutiveIntervals;

    public IntervalSequence() {
        consecutiveIntervals = new LinkedList<Interval<C>>();
    }

    public IntervalSequence(final IntervalSequence<C> other) {
        consecutiveIntervals = new LinkedList<Interval<C>>();
        consecutiveIntervals.addAll(other.consecutiveIntervals);            
    }

    public void extendTo(final Interval<C> interval) {
        for (int i = 0; i < consecutiveIntervals.size(); i++) {
            Interval<C> cur = consecutiveIntervals.get(i);
            switch (interval.compareTo(cur)) {
                case OVERLAPSWITH:
                    cur = Interval.merge(interval, cur);
                    while (consecutiveIntervals.size() > i + 1
                            && cur.compareTo(consecutiveIntervals.get(i + 1)) == Interval.IntervalRelation.OVERLAPSWITH) {
                        cur = Interval.merge(cur, consecutiveIntervals.get(i + 1));
                        consecutiveIntervals.remove(i + 1);
                    }
                    consecutiveIntervals.set(i, cur);
                    return;
                case PRECEDES:
                    consecutiveIntervals.add(i, interval);
                    return;
            }
        }

        consecutiveIntervals.add(interval);
    }

    public boolean identicalTo(final IntervalSequence<C> other) {
        if (this.consecutiveIntervals.size() != other.consecutiveIntervals.size()) {
            return false;
        } else {
            for (int i = 0; i < this.consecutiveIntervals.size(); i++) {
                if (!this.consecutiveIntervals.get(i).identicalTo(other.consecutiveIntervals.get(i))) {
                    return false;
                }
            }
        }

        return true;
    }

    public List<Interval<C>> getIntervals() {
        return consecutiveIntervals;    
    }
}
