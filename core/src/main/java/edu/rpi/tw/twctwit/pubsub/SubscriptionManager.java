package edu.rpi.tw.twctwit.pubsub;

import net.fortytwo.twitlogic.TwitLogic;

import java.util.logging.Logger;

/**
 * User: josh
 * Date: Apr 17, 2010
 * Time: 5:18:50 PM
 */
public class SubscriptionManager<K, V> {
    private static final Logger LOGGER = TwitLogic.getLogger(SubscriptionManager.class);

    private static final long MIN_ITERATION_TIME = 5000;

    private final Object mutex = "";
    private boolean closed = false;
    private final PairSet<K, V> subscriptions;

    public SubscriptionManager() {
        subscriptions = new PairSet<K, V>();

        new Thread(new Runnable() {
            public void run() {
                try {
                    refreshUntilClosed();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void close() {
        closed = true;
    }

    private void refreshAll() {

    }

    private void refreshUntilClosed() throws InterruptedException {
        long lastTime = System.currentTimeMillis();

        while (!closed) {
            long currentTime = System.currentTimeMillis();
            long d = currentTime - lastTime;
            if (d < MIN_ITERATION_TIME) {
                wait(MIN_ITERATION_TIME - d);
                currentTime += (MIN_ITERATION_TIME - d);
                LOGGER.info("query demand is is met at "
                        + MIN_ITERATION_TIME + "ms cycle: "
                        + subscriptionInfo());
            } else {
                LOGGER.warning("query demand is over capacity at "
                        + MIN_ITERATION_TIME + "ms cycle: "
                        + subscriptionInfo()
                        + " (" + (d - MIN_ITERATION_TIME) + "ms behind schedule)");
            }

            lastTime = currentTime;
            refreshAll();
        }
    }

    private String subscriptionInfo() {
        synchronized (mutex) {
            return "" + subscriptions.keySize() + " sources with " + subscriptions.valueSize() + " subscribers";
        }
    }
}
