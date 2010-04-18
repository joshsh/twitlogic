package edu.rpi.tw.twctwit.pubsub;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * User: josh
 * Date: Apr 17, 2010
 * Time: 6:33:37 PM
 */
public class PairSet<K, V> {
    private final Map<K, Set<V>> keyToValues;
    private final Map<V, Set<K>> valueToKeys;
    private final Object mutex = "";

    public PairSet() {
        keyToValues = new HashMap<K, Set<V>>();
        valueToKeys = new HashMap<V, Set<K>>();
    }

    public void addPair(final K k,
                        final V v) {
        synchronized (mutex) {
            Set<V> values = keyToValues.get(k);
            if (null == values) {
                values = new HashSet<V>();
                keyToValues.put(k, values);
            }

            values.add(v);

            Set<K> keys = valueToKeys.get(v);
            if (null == keys) {
                keys = new HashSet<K>();
                valueToKeys.put(v, keys);
            }

            keys.add(k);
        }
    }

    public void removePair(final K k,
                           final V v) {
        synchronized (mutex) {
            Set<V> values = keyToValues.get(k);
            if (null != values) {
                values.remove(v);

                if (0 == values.size()) {
                    keyToValues.remove(k);
                }

                Set<K> keys = valueToKeys.get(v);
                keys.remove(k);

                if (0 == keys.size()) {
                    valueToKeys.remove(v);
                }
            }
        }
    }

    public void removeKey(final K k) {
        synchronized (mutex) {
            Set<V> values = keyToValues.get(k);

            if (null != values) {
                for (V v : values) {
                    Set<K> keys = valueToKeys.get(v);
                    keys.remove(k);
                    if (0 == keys.size()) {
                        valueToKeys.remove(v);
                    }
                }

                keyToValues.remove(k);
            }
        }
    }

    public void removeValue(final V v) {
        synchronized (mutex) {
            Set<K> keys = valueToKeys.get(v);

            if (null != keys) {
                for (K k : keys) {
                    Set<V> values = keyToValues.get(k);
                    values.remove(v);
                    if (0 == values.size()) {
                        keyToValues.remove(k);
                    }
                }

                valueToKeys.remove(v);
            }
        }
    }

    public <E extends Exception> boolean handlePairs(final PairHandler<K, V, E> handler) throws E {
        Collection<KeyWithValues> buffer;

        synchronized (mutex) {
            buffer = new LinkedList<KeyWithValues>();
            for (K key : keyToValues.keySet()) {
                KeyWithValues kv = new KeyWithValues();
                kv.key = key;
                kv.values = (V[]) keyToValues.get(key).toArray();
                buffer.add(kv);
            }
        }

        for (KeyWithValues kv : buffer) {
            for (V value : kv.values) {
                if (!handler.handle(kv.key, value)) {
                    return false;
                }
            }
        }

        return true;
    }

    public interface PairHandler<K, V, E extends Exception> {
        boolean handle(K key, V value) throws E;
    }

    public int keySize() {
        // TODO: synchronization may not be necessary
        synchronized (mutex) {
            return keyToValues.size();
        }
    }

    public int valueSize() {
        // TODO: synchronization may not be necessary
        synchronized (mutex) {
            return valueToKeys.size();
        }
    }

    private class KeyWithValues {
        public K key;
        public V[] values;
    }
}
