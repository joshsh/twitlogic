package net.fortytwo.twitlogic.util.intervals;

import junit.framework.TestCase;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class IntervalsTest extends TestCase {
    public void testIntervalComparison() {
        // one interval precedes the other
        assertEquals(Interval.IntervalRelation.PRECEDES,
                new Interval<Integer>(1, 2).compareTo(new Interval<Integer>(3, 4)));
        assertEquals(Interval.IntervalRelation.PRECEDES,
                new Interval<Integer>(1, 1).compareTo(new Interval<Integer>(2, 2)));

        // intervals are the same
        assertEquals(Interval.IntervalRelation.OVERLAPSWITH,
                new Interval<Integer>(1, 2).compareTo(new Interval<Integer>(1, 2)));
        // one interval contains the other
        assertEquals(Interval.IntervalRelation.OVERLAPSWITH,
                new Interval<Integer>(1, 2).compareTo(new Interval<Integer>(0, 3)));
        assertEquals(Interval.IntervalRelation.OVERLAPSWITH,
                new Interval<Integer>(0, 20).compareTo(new Interval<Integer>(4, 6)));
        // partial (but non-trivial) overlap
        assertEquals(Interval.IntervalRelation.OVERLAPSWITH,
                new Interval<Integer>(1, 3).compareTo(new Interval<Integer>(2, 4)));
        assertEquals(Interval.IntervalRelation.OVERLAPSWITH,
                new Interval<Integer>(5, 9).compareTo(new Interval<Integer>(3, 7)));
        // intervals share a point
        assertEquals(Interval.IntervalRelation.OVERLAPSWITH,
                new Interval<Integer>(5, 9).compareTo(new Interval<Integer>(9, 100)));
        assertEquals(Interval.IntervalRelation.OVERLAPSWITH,
                new Interval<Integer>(5, 9).compareTo(new Interval<Integer>(3, 5)));

        // one interval succeeds the other
        assertEquals(Interval.IntervalRelation.SUCCEEDS,
                new Interval<Integer>(10, 234).compareTo(new Interval<Integer>(3, 4)));
        assertEquals(Interval.IntervalRelation.SUCCEEDS,
                new Interval<Integer>(5, 5).compareTo(new Interval<Integer>(0, 0)));
    }

    public void testIdentity() {
        IntervalSequence<Integer> s1 = fromArray(new Integer[][]{
                {1, 2},
                {3, 4}});
        assertTrue(s1.identicalTo(s1));

        IntervalSequence<Integer> s2 = fromArray(new Integer[][]{
                {1, 2},
                {3, 5}});
        assertFalse(s1.identicalTo(s2));
        assertFalse(s2.identicalTo(s1));
    }

    public void testInsert() throws Exception {
        IntervalSequence<Integer> s1 = fromArray(new Integer[][]{
                {0, 1},
                {4, 5},
                {8, 9}});
        IntervalSequence<Integer> s2;

        s2 = new IntervalSequence<Integer>(s1);
        s2.extendTo(new Interval<Integer>(-5, -2));
        assertTrue(s2.identicalTo(fromArray(new Integer[][]{
                {-5, -2},
                {0, 1},
                {4, 5},
                {8, 9}})));

        s2 = new IntervalSequence<Integer>(s1);
        s2.extendTo(new Interval<Integer>(6, 7));
        assertTrue(s2.identicalTo(fromArray(new Integer[][]{
                {0, 1},
                {4, 5},
                {6, 7},
                {8, 9}})));

        s2 = new IntervalSequence<Integer>(s1);
        s2.extendTo(new Interval<Integer>(11, 42));
        assertTrue(s2.identicalTo(fromArray(new Integer[][]{
                {0, 1},
                {4, 5},
                {8, 9},
                {11, 42}})));
    }


    public void testMerge() throws Exception {
        IntervalSequence<Integer> s1 = fromArray(new Integer[][]{
                {0, 1},
                {3, 5},
                {8, 11}});
        IntervalSequence<Integer> s2;

        s2 = new IntervalSequence<Integer>(s1);
        s2.extendTo(new Interval<Integer>(-5, 0));
        assertTrue(s2.identicalTo(fromArray(new Integer[][]{
                {-5, 1},
                {3, 5},
                {8, 11}})));

        s2 = new IntervalSequence<Integer>(s1);
        s2.extendTo(new Interval<Integer>(-5, 4));
        assertTrue(s2.identicalTo(fromArray(new Integer[][]{
                {-5, 5},
                {8, 11}})));

        s2 = new IntervalSequence<Integer>(s1);
        s2.extendTo(new Interval<Integer>(2, 4));
        assertTrue(s2.identicalTo(fromArray(new Integer[][]{
                {0, 1},
                {2, 5},
                {8, 11}})));

        s2 = new IntervalSequence<Integer>(s1);
        s2.extendTo(new Interval<Integer>(9, 10));
        assertTrue(s2.identicalTo(fromArray(new Integer[][]{
                {0, 1},
                {3, 5},
                {8, 11}})));

        s2 = new IntervalSequence<Integer>(s1);
        s2.extendTo(new Interval<Integer>(4, 12));
        assertTrue(s2.identicalTo(fromArray(new Integer[][]{
                {0, 1},
                {3, 12}})));

        s2 = new IntervalSequence<Integer>(s1);
        s2.extendTo(new Interval<Integer>(-100, 100));
        assertTrue(s2.identicalTo(fromArray(new Integer[][]{
                {-100, 100}})));
    }

    private IntervalSequence<Integer> fromArray(Integer[][] array) {
        IntervalSequence<Integer> s = new IntervalSequence<Integer>();
        for (Integer[] pair : array) {
            Interval<Integer> interval = new Interval<Integer>(pair[0], pair[1]);
            s.extendTo(interval);
        }
        return s;
    }
}
