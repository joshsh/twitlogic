package net.fortytwo.twitlogic.services.twitter;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Place;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Jul 1, 2010
 * Time: 3:21:08 PM
 */
public class PlaceMappingQueue<E extends Exception> {
    protected static final Logger LOGGER = TwitLogic.getLogger(PlaceMappingQueue.class);

    // Don't monopolize the Twitter API request quota.
    private final int capacity;

    private final BlockingQueue<String> inQueue;

    private final Set<String> placeIdsSet;

    private boolean closed = false;

    public PlaceMappingQueue(final TwitterClient client,
                             final Handler<Place, E> resultHandler) {
        capacity = client.getLimits().getRestApiRequestsPerHourLimit() / 2;
        this.inQueue = new LinkedBlockingQueue<String>(capacity);
        this.placeIdsSet = Collections.synchronizedSet(new HashSet<String>());

        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    while (!closed) {
                        String id;

                        try {
                            id = dequeue();
                        } catch (InterruptedException e) {
                            LOGGER.severe("runnable was interrupted. Queue will quit. ");
                            closed = true;
                            continue;
                        }

                        try {
                            if (!resultHandler.handle(client.fetchPlace(id))) {
                                closed = true;
                            }
                        } catch (TwitterClientException e) {
                            LOGGER.warning("caught Twitter client error, will attempt to recover. Stack trace follows.");
                            e.printStackTrace(System.err);
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.severe("caught error. Queue will quit. Stack trace follows.");
                    e.printStackTrace(System.err);
                }
            }
        }

                , "place mapping queue");
        t.start();
    }

    public boolean offer(final String id) {
        if (closed) {
            return false;
        } else if (placeIdsSet.size() >= capacity) {
            LOGGER.fine("place mapping queue is full. Discarding current item.");
            return false;
        } else return placeIdsSet.add(id) && inQueue.add(id);
    }

    private String dequeue() throws InterruptedException {
        String id = inQueue.take();
        placeIdsSet.remove(id);
        return id;
    }
}
