package net.fortytwo.twitlogic.ranking;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User: josh
 * Date: Oct 4, 2009
 * Time: 6:22:28 AM
 */
public class WeightedMatrix<A, B> {
    private final Map<A, WeightedVector<B>> map;

    public WeightedMatrix() {
        map = new HashMap<A, WeightedVector<B>>();
    }

    public Set<A> keySet() {
        return map.keySet();
    }

    public Collection<WeightedVector<B>> values() {
        return map.values();
    }

    public WeightedVector<B> getRow(final A a) {
        WeightedVector<B> result = map.get(a);
        if (null == result) {
            result = new WeightedVector<B>();
        }
        return result;
    }

    public void addWeight(final A a, final B b, final double weight) {
        WeightedVector<B> v = map.get(a);
        if (null == v) {
            v = new WeightedVector<B>();
            map.put(a, v);
        }
        v.addWeight(b, weight);
    }

    public double getMagnitude() {
        double sum = 0;
        for (WeightedVector<B> wv : map.values()) {
            sum += wv.getMagnitude();
        }
        return sum;
    }

    public double getWeight(final B b) {
        double sum = 0;
        for (WeightedVector<B> wv : map.values()) {
            sum += wv.getWeight(b);
        }
        return sum;
    }

    public WeightedVector<B> rowNorm() {
        WeightedVector<B> sum = new WeightedVector<B>();
        for (WeightedVector<B> wv : map.values()) {
            sum = sum.add(wv);
        }
        return sum;
    }

    public WeightedVector<A> columnNorm() {
        WeightedVector<A> sum = new WeightedVector<A>();
        for (A a : map.keySet()) {
            sum.setWeight(a, map.get(a).getMagnitude());
        }
        return sum;
    }
}
