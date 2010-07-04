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
    private static final int CAPACITY = TwitterAPI.DEFAULT_REST_API_REQUESTS_PER_HOUR_LIMIT / 2;

    private final BlockingQueue<String> inQueue;

    private final Set<String> placeIdsSet;

    private boolean closed = false;

    public PlaceMappingQueue(final TwitterClient client,
                             final Handler<Place, E> resultHandler) {
        this.inQueue = new LinkedBlockingQueue<String>(CAPACITY);
        this.placeIdsSet = Collections.synchronizedSet(new HashSet<String>());

        Thread t = new Thread(new Runnable() {
            public void run() {
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
                    } catch (Exception e) {
                        LOGGER.severe("caught error. Queue will quit. Stack trace follows.");
                        e.printStackTrace(System.err);
                        closed = true;
                    }
                }
            }
        });
        t.start();
    }

    public boolean offer(String id) {
        if (closed) {
            return false;
        } else if (placeIdsSet.size() >= CAPACITY) {
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
