package net.fortytwo.twitlogic.flow;

import net.fortytwo.twitlogic.services.twitter.HandlerException;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A buffered Handler which expects exactly one thread to write to it (via
 * handle) and one thread to read from it (via flush).
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class ConcurrentBuffer<T> implements Handler<T> {
    private final Queue<T> outQueue;
    private final Handler<T> handler;

    public ConcurrentBuffer(final Handler<T> handler) {
        this.handler = handler;
        outQueue = new ConcurrentLinkedQueue<T>();
    }

    public boolean isOpen() {
        return true;
    }

    public void handle(final T t) throws HandlerException {
        outQueue.add(t);
    }

    public boolean flush() throws HandlerException {
        int size = outQueue.size();

        for (int i = 0; i < size; i++) {
            if (!handler.isOpen()) {
                break;
            }
            handler.handle(outQueue.remove());
        }

        return true;
    }
}
