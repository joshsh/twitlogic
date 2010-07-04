package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.flow.ConcurrentBuffer;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Place;
import net.fortytwo.twitlogic.persistence.beans.Feature;
import net.fortytwo.twitlogic.services.twitter.PlaceMappingQueue;
import net.fortytwo.twitlogic.services.twitter.TwitterClient;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Jul 1, 2010
 * Time: 6:48:30 PM
 */
public class PlacePersistenceHelper {
    private static final Logger LOGGER = TwitLogic.getLogger(PlacePersistenceHelper.class);

    private final PlaceMappingQueue<TweetStoreException> placeMappingQueue;
    private final ConcurrentBuffer<Place, TweetStoreException> buffer;

    public PlacePersistenceHelper(final PersistenceContext pContext,
                                  final TwitterClient client) throws TweetStoreException {
        Handler<Place, TweetStoreException> placeMappingHandler = new Handler<Place, TweetStoreException>() {
            public boolean handle(final Place p) throws TweetStoreException {
                //System.out.println("received this place: " + p.getJson());

                if (0 < p.getContainedWithin().size()) {
                    Feature f = pContext.persist(p);
                    // Refreshing the parent features is all-or-nothing.
                    Set<Feature> parents = new HashSet<Feature>();

                    for (Place par : p.getContainedWithin()) {
                        //System.out.println("and this is a parent: " + par.getJson());

                        Feature parf = pContext.persist(par);
                        parents.add(parf);
                        checkHierarchy(par, parf);
                    }

                    f.setParentFeature(parents);
                }

                return true;
            }
        };

        buffer = new ConcurrentBuffer<Place, TweetStoreException>(placeMappingHandler);
        placeMappingQueue = new PlaceMappingQueue<TweetStoreException>(client, buffer);
    }

    public boolean checkHierarchy(final Place p,
                                  final Feature f) {
        // Note: for now, links in the hierarchy are established once, and never updated.
        if (0 == f.getOwlSameAs().size()
                && 0 == f.getParentFeature().size()) {
            LOGGER.info("queueing unknown " + p.getPlaceType() + ": " + p.getJson());
            return placeMappingQueue.offer(p.getId());
        } else {
            LOGGER.fine("familiar " + p.getPlaceType() + ": " + p.getJson());
            return true;
        }
    }

    public boolean flush() throws TweetStoreException {
        return buffer.flush();
    }
}
