package net.fortytwo.twitlogic.query;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class MemoizedRandomWalk<T, M> {
    private final Map<T, Alternatives> alternativesByValue;
    private final Extender<T, M> extender;
    private final Random random = new Random();

    public MemoizedRandomWalk(final Map<T, Alternatives> alternativesByValue,
                              final Extender<T, M> extender) {
        this.alternativesByValue = alternativesByValue;
        this.extender = extender;
    }

    public Memo chooseRandom(final T t) {
        Alternatives alts = alternativesByValue.get(t);
        if (null == alts) {
            alts = new Alternatives();
            alts.alternatives = extender.findAlternatives(t);
            double sum = 0;
            for (Memo m : alts.alternatives) {
                sum += m.weight;
            }
            alts.totalWeight = sum;

            alternativesByValue.put(t, alts);
        }

        double r = random.nextDouble() * alts.totalWeight;
        double sum = 0;
        for (Memo m : alts.alternatives) {
            sum += m.weight;
            if (sum >= r) {
                return m;
            }
        }

        throw new IllegalStateException("failed to choose a random value");
    }

    private class Alternatives {
        private double totalWeight;
        private Collection<Memo<T, M>> alternatives;
    }
}
