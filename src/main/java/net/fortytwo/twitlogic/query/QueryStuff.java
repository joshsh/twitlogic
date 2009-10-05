package net.fortytwo.twitlogic.query;

import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.proof.AssumptionStep;
import net.fortytwo.twitlogic.proof.InferenceStep;
import net.fortytwo.twitlogic.proof.Information;
import net.fortytwo.twitlogic.proof.NodeSet;
import net.fortytwo.twitlogic.proof.NodeSetList;
import net.fortytwo.twitlogic.proof.PMLConstruct;
import net.fortytwo.twitlogic.proof.Query;
import net.fortytwo.twitlogic.ranking.WeightedMatrix;
import net.fortytwo.twitlogic.ranking.WeightedValue;
import net.fortytwo.twitlogic.ranking.WeightedVector;
import org.openrdf.model.Resource;
import org.openrdf.sail.SailConnection;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

/**
 * User: josh
 * Date: Oct 4, 2009
 * Time: 4:48:24 AM
 */
public class QueryStuff {
    private final TweetStore store;

    public QueryStuff(final TweetStore store) {
        this.store = store;
    }

    public QueryResult freetextSearch(final String text,
                                      final int maxAnswers) {
        return null;
    }

    public QueryResult relatednessSearch(final WeightedVector<Resource> sources,
                                         final int maxAnswers) {
        return null;

    }

    public QueryResult mixedSearch(final String text,
                                   final WeightedVector<Resource> sources,
                                   final int maxAnswers) {
        return null;

    }

    private QueryResult toQueryResult(final WeightedMatrix<Resource, Resource> resultMatrix,
                                      final String queryRawString,
                                      final Resource rule,
                                      final int maxAnswers,
                                      final int maxJustifications,
                                      final PMLConstruct.RDFizerContext context) {
        Collection<NodeSet> answers = new LinkedList<NodeSet>();

        int i = 0;
        for (WeightedValue<Resource> v : resultMatrix.columnNorm().normalize().toSortedArray()) {
            if (++i > maxAnswers) {
                break;
            }

            NodeSetList nsList = null;
            int j = 0;
            for (WeightedValue<Resource> v2 : resultMatrix.getRow(v.value).normalize().toSortedArray()) {
                if (++j > maxJustifications) {
                    break;
                }

                NodeSet ns = new NodeSet(v2.value, (float) v2.weight, new AssumptionStep(context), context);
                // FIXME: these will be in reverse order of weight, which is counterintuitive
                nsList = new NodeSetList(ns, nsList, context);
            }

            InferenceStep infStep = new InferenceStep(rule, nsList, context);
            NodeSet ns = new NodeSet(v.value, (float) v.weight, infStep, context);
            answers.add(ns);
        }

        Information info = new Information(queryRawString, context);
        Query query = new Query(info, answers, context);
        return new QueryResult(query);
    }


    private WeightedMatrix<Resource, Resource> spreadFrom(final WeightedVector<Resource> source,
                                                          final int maxSteps,
                                                          final int maxDepth,
                                                          final SailConnection sc) {
        Resource cur = null;
        for (int i = 0; i < maxSteps; i++) {
            if (0 == i % maxDepth) {
                // FIXME: wasty to compute the magnitude of this vector on each call
                cur = randomValue(source);
            }


        }

        return null;
    }


    private final Random random = new Random();

    private Resource randomValue(final WeightedVector<Resource> source) {
        double mag = source.getSimpleMagnitude();

        if (0 == mag) {
            return null;
        } else {
            double r = random.nextDouble() * mag;
            double sum = 0;
            for (WeightedValue<Resource> v : source.values()) {
                sum += v.weight;
                if (sum >= r) {
                    return v.value;
                }
            }
        }

        throw new IllegalStateException("failed to choose a random value");
    }
}
