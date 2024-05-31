/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import mil.arl.gift.common.util.StringUtils.Stringifier;

/**
 * Helper {@link HashMap} that maintains unique values as well as keys. Not
 * thread safe.
 *
 * @author sharrison
 *
 * @param <K> the key object
 * @param <V> the value object
 */
public class BiDirectionalHashMap<K, V> {

    /** The standard Key/Value map */
    private final HashMap<K, V> forwardMap = new HashMap<>();

    /** The reverse map of {@link #forwardMap} to maintain unique values */
    private final HashMap<V, K> backwardMap = new HashMap<>();

    /**
     * Constructor
     */
    public BiDirectionalHashMap() {
    }

    /**
     * Inserts the key/value pair into the map. Both the key and the value must
     * be unique.
     *
     * @param key the key to insert. Can't be null.
     * @param value the value to insert. Can't be null.
     * @return The old value that was mapped to the specified key. Can be null
     *         if no value was previously mapped.
     * @throws IllegalArgumentException if the given value is already bound to a
     *         different key. The map will remain unmodified in this event. To
     *         avoid this exception, call {@link #forcePut(Object, Object)}
     *         instead.
     */
    public V put(K key, V value) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("The parameter 'key' cannot be null.");
        } else if (value == null) {
            throw new IllegalArgumentException("The parameter 'value' cannot be null.");
        }

        K backwardKey = backwardMap.get(value);
        if (backwardKey != null) {
            if (backwardKey.equals(key)) {
                /* This pairing already exists */
                return value;
            }
            throw new IllegalArgumentException("The parameter 'value' already belongs to another key.");
        }

        /* Now that the 'unique' value check has been performed, insert the
         * key/value pair */
        return forcePut(key, value);
    }

    /**
     * Forces the insertion of the key/value pair into the map. If either the
     * key or value existing previously, the existing mappings will be removed.
     *
     * @param key the key to forcibly insert
     * @param value the value to forcibly insert
     * @return The old value that was mapped to the specified key. Can be null
     *         if no value was previously mapped.
     * @throws IllegalArgumentException if the given value is already bound to a
     *         different key. The map will remain unmodified in this event. To
     *         avoid this exception, call forcePut(K, V) instead.
     */
    public V forcePut(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("The parameter 'key' cannot be null.");
        } else if (value == null) {
            throw new IllegalArgumentException("The parameter 'value' cannot be null.");
        }

        /* Remove the old values from the maps */
        V oldValue = removeKey(key);
        removeValue(value);

        /* Insert into the maps */
        forwardMap.put(key, value);
        backwardMap.put(value, key);

        return oldValue;
    }

    /**
     * Returns the unique value from the provided key.
     *
     * @param key the key
     * @return the value
     */
    public V getFromKey(K key) {
        return forwardMap.get(key);
    }

    /**
     * Returns the unique key from the provided value.
     *
     * @param value the value
     * @return the key
     */
    public K getFromValue(V value) {
        return backwardMap.get(value);
    }

    /**
     * Clears the map.
     */
    public void clear() {
        forwardMap.clear();
        backwardMap.clear();
    }

    /**
     * Return whether or not the map contains the key.
     *
     * @param key the key to find
     * @return true if the map contains the key; false otherwise.
     */
    public boolean containsKey(K key) {
        return forwardMap.containsKey(key);
    }

    /**
     * Return whether or not the map contains the value.
     *
     * @param value the value to find
     * @return true if the map contains the value; false otherwise.
     */
    public boolean containsValue(V value) {
        return backwardMap.containsKey(value);
    }

    /**
     * Removes the key/value pair from the map by key
     *
     * @param key the key to remove
     * @return the previous value associated with key, or null if there was no
     *         mapping for key.
     */
    public V removeKey(K key) {
        V value = forwardMap.remove(key);
        if (value != null) {
            backwardMap.remove(value);
        }
        return value;
    }

    /**
     * Removes the key/value pair from the map by value
     *
     * @param value the value to remove
     * @return the previous key associated with value, or null if there was no
     *         mapping for value.
     */
    public K removeValue(V value) {
        K key = backwardMap.remove(value);
        if (key != null) {
            forwardMap.remove(key);
        }
        return key;
    }

    /**
     * Returns the set of keys from the map
     *
     * @return the unmodifiable key set. Can't be null.
     */
    public Set<K> keySet() {
        return Collections.unmodifiableSet(forwardMap.keySet());
    }

    /**
     * Returns the set of values from the map
     *
     * @return the unmodifiable value set. Can't be null.
     */
    public Set<V> values() {
        return Collections.unmodifiableSet(backwardMap.keySet());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[BiDirectionalHashMap: ");

        sb.append(" forward map = [");
        StringUtils.join(", ", forwardMap.entrySet(), new Stringifier<Entry<K, V>>() {
            @Override
            public String stringify(Entry<K, V> obj) {
                StringBuilder sb = new StringBuilder("{");
                sb.append(obj.getKey().toString()).append(", ").append(obj.getValue().toString());
                sb.append("}");
                return sb.toString();
            }
        }, sb);
        sb.append("]");

        sb.append(", backward map = [");
        StringUtils.join(", ", backwardMap.entrySet(), new Stringifier<Entry<V, K>>() {
            @Override
            public String stringify(Entry<V, K> obj) {
                StringBuilder sb = new StringBuilder("{");
                sb.append(obj.getKey().toString()).append(", ").append(obj.getValue().toString());
                sb.append("}");
                return sb.toString();
            }
        }, sb);
        sb.append("]");

        sb.append("]");
        return sb.toString();
    }

    /**
     * Getter for the number of entries within the map.
     *
     * @return The number of elements contained within the map. Will always be
     *         greater than or equal to zero.
     */
    public int size() {
        return backwardMap.size();
    }
}
