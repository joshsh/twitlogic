package edu.rpi.tw.twctwit.query;

import net.fortytwo.flow.rdf.ranking.Handler;
import net.fortytwo.flow.rdf.ranking.KeepResourcesFilter;
import net.fortytwo.flow.rdf.ranking.HandlerException;
import net.fortytwo.flow.rdf.ranking.Ranking;
import net.fortytwo.flow.rdf.ranking.Approximation;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.sail.SailConnection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class SimpleInferencer implements Approximation<Set<Resource>, HandlerException> {

    private Queue<Resource> curGen;
    private Queue<Resource> nextGen;
    private final SailConnection sailConnection;
    private final URI[] inPredicates, outPredicates;
    private final Set<Resource> results;

    public SimpleInferencer(final URI[] inPredicates,
                            final URI[] outPredicates,
                            final SailConnection sailConnection,
                            final Resource... seeds) {
        this.inPredicates = inPredicates;
        this.outPredicates = outPredicates;
        this.sailConnection = sailConnection;

        curGen = new LinkedList<Resource>();
        curGen.addAll(Arrays.asList(seeds));

        nextGen = new LinkedList<Resource>();

        results = new HashSet<Resource>();
        results.addAll(Arrays.asList(seeds));
    }

    public Set<Resource> currentResult() {
        return results;
    }

    public int compute(int cycles) throws HandlerException {
        //System.out.println("simple inferencer: " + cycles + " cycles over " + seeds.length + " seeds: " + seeds[0] + "...");
        for (int i = 0; i < cycles; i++) {
            if (0 == curGen.size()) {
                if (0 == nextGen.size()) {
                    return i;
                } else {
                    Queue<Resource> tmp = curGen;
                    curGen = nextGen;
                    nextGen = tmp;
                }
            } else {
                Resource r = curGen.remove();

                stepRelated(sailConnection, r, nextGen);
            }
        }

        return cycles;
    }

    private void addResult(final Resource r,
                           final Queue<Resource> queue) {
        if (!results.contains(r)) {
            results.add(r);
            //System.out.println("enqueueing: " + r);
            queue.offer(r);
        }
    }

    public void stepRelated(final SailConnection sc,
                            final Resource resource,
                            final Queue<Resource> resources) throws HandlerException {
        Handler<Resource, HandlerException> h = new Handler<Resource, HandlerException>() {
            public boolean handle(final Resource r) throws HandlerException {
                addResult(r, resources);
                return true;
            }
        };

        if (0 < inPredicates.length) {
            Ranking.traverseBackward(sc,
                    new KeepResourcesFilter(h),
                    resource,
                    inPredicates);
        }

        if (0 < outPredicates.length) {
            Ranking.traverseForward(sc,
                    new KeepResourcesFilter(h),
                    resource,
                    outPredicates);
        }
    }
}