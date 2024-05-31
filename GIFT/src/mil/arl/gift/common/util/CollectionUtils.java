/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A utility class that provides useful methods for operating on collections
 * 
 * @author mhoffman
 */
public class CollectionUtils {

    /** The default {@link Equator} to use if none is provided */
    private final static Equator<Object> DEFAULT = new Equator<Object>() {
        @Override
        public boolean equals(Object o1, Object o2) {
            return Objects.deepEquals(o1, o2);
        }
    };

    /**
     * Interface that is responsible for equating two Objects.
     * 
     * @author tflowers
     * @param <T> The type of element the equator should equate.
     */
    public interface Equator<T> {
        /**
         * Checks if the two objects are equal.
         * 
         * @param o1 the first object.
         * @param o2 the second object.
         * @return true if the two objects are equal; false otherwise.
         */
        public boolean equals(T o1, T o2);
    }

    /**
     * Null safe equality check on two maps. Since map is unordered by nature,
     * order does not matter.
     * 
     * <pre>
     * [A=1; B=1; C=1] == [A=1; B=1; C=1]
     * [A=1; B=1; C=1] == [A=1; C=1; B=1]
     * [A=1; B=1; C=1] != [A=1; B=2; C=1]
     * [A=1; B=1; C=1] != [A=1; B=1]
     * </pre>
     * 
     * @param a the first map to use in the comparison.
     * @param b the second map to use in the comparison.
     * @return true if the two map contents are equal; false otherwise.
     */
    public static <T, U> boolean equals(Map<? extends T, ? extends U> a, Map<? extends T, ? extends U> b) {
        return equals(a, b, null, null);
    }

    /**
     * Null safe equality check on two maps. Since map is unordered by nature,
     * order does not matter.
     * 
     * <pre>
     * [A=1; B=1; C=1] == [A=1; B=1; C=1]
     * [A=1; B=1; C=1] == [A=1; C=1; B=1]
     * [A=1; B=1; C=1] != [A=1; B=2; C=1]
     * [A=1; B=1; C=1] != [A=1; B=1]
     * </pre>
     * 
     * @param a the first map to use in the comparison.
     * @param b the second map to use in the comparison.
     * @param keyEquator (optional) if provided this will be used over calling
     *        the equals method on each key object in the map.
     * @param valueEquator (optional) if provided this will be used over calling
     *        the equals method on each value object in the map.
     * @return true if the two map contents are equal; false otherwise.
     */
    public static <T, U> boolean equals(Map<? extends T, ? extends U> a, Map<? extends T, ? extends U> b,
            Equator<? super T> keyEquator, Equator<? super U> valueEquator) {
        if (a == null && b == null) {
            return true;
        } else if (a == null || b == null) {
            return false;
        } else if (a.size() != b.size()) {
            return false;
        }

        if (keyEquator == null) {
            keyEquator = DEFAULT;
        }

        if (valueEquator == null) {
            valueEquator = DEFAULT;
        }

        if (!equalsIgnoreOrder(a.keySet(), b.keySet(), keyEquator)) {
            return false;
        }

        for (T key : a.keySet()) {
            if (!valueEquator.equals(a.get(key), b.get(key))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Null-safe check if the specified map is empty.
     * 
     * @param map the map to check.
     * @return true if the map is null or empty.
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Null-safe check if the specified map is not empty.
     * 
     * @param map the map to check.
     * @return true if the map is populated; false if null or empty.
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * Null safe equality check on two collections. Order matters.
     * 
     * <pre>
     * [A, A, B] == [A, A, B]
     * [A, A, B] != [A, B, A]
     * [A, A, B] != [A, B]
     * </pre>
     * 
     * @param a the first collection to use in the comparison.
     * @param b the second collection to use in the comparison.
     * @return true if the elements are equal and in the same order; false
     *         otherwise.
     */
    public static <T> boolean equals(Collection<? extends T> a, Collection<? extends T> b) {
        return equals(a, b, null);
    }

    /**
     * Null safe equality check on two collections. Order matters.
     * 
     * <pre>
     * [A, A, B] == [A, A, B]
     * [A, A, B] != [A, B, A]
     * [A, A, B] != [A, B]
     * </pre>
     * 
     * @param a the first collection to use in the comparison.
     * @param b the second collection to use in the comparison.
     * @param equator (optional) if provided this will be used over calling the
     *        equals method on each object in the collection.
     * @return true if the elements are equal and in the same order; false
     *         otherwise.
     */
    public static <T> boolean equals(Collection<? extends T> a, Collection<? extends T> b, Equator<? super T> equator) {
        if (a == null && b == null) {
            return true;
        } else if (a == null || b == null) {
            return false;
        } else if (a.size() != b.size()) {
            return false;
        }

        if (equator == null) {
            equator = DEFAULT;
        }

        boolean equal = true;
        Iterator<? extends T> aItr = a.iterator();
        Iterator<? extends T> bItr = b.iterator();

        while (aItr.hasNext() && equal) {
            equal = equator.equals(aItr.next(), bItr.next());
        }

        return equal;
    }

    /**
     * Null safe equality check on two collections. Order does not matter.
     * 
     * <pre>
     * [A, A, B] == [A, A, B]
     * [A, A, B] == [A, B, A]
     * [A, A, B] != [A, B]
     * </pre>
     * 
     * @param a the first collection to use in the comparison.
     * @param b the second collection to use in the comparison.
     * @return true if the elements in the first collection are also in the
     *         second collection and occur the same number of times; false
     *         otherwise.
     */
    public static <T> boolean equalsIgnoreOrder(Collection<? extends T> a, Collection<? extends T> b) {
        return equalsIgnoreOrder(a, b, null);
    }

    /**
     * Null safe equality check on two collections. Order does not matter.
     * 
     * <pre>
     * [A, A, B] == [A, A, B]
     * [A, A, B] == [A, B, A]
     * [A, A, B] != [A, B]
     * </pre>
     * 
     * @param a the first collection to use in the comparison.
     * @param b the second collection to use in the comparison.
     * @param equator (optional) if provided this will be used over calling the
     *        equals method on each object in the collection.
     * @return true if the elements in the first collection are also in the
     *         second collection and occur the same number of times; false
     *         otherwise.
     */
    public static <T> boolean equalsIgnoreOrder(Collection<? extends T> a, Collection<? extends T> b,
            Equator<? super T> equator) {
        if (a == null && b == null) {
            return true;
        } else if (a == null || b == null) {
            return false;
        } else if (a.size() != b.size()) {
            return false;
        }

        if (equator == null) {
            equator = DEFAULT;
        }

        /* Maintains the collection of indices from 'b' that have been matched
         * and equated to true from 'a'. */
        Set<Integer> usedIndices = new HashSet<>();

        boolean collectionEqual = true;
        Iterator<? extends T> aItr = a.iterator();

        while (aItr.hasNext() && collectionEqual) {
            final T aNext = aItr.next();
            Iterator<? extends T> bItr = b.iterator();
            boolean itemEqual = false;
            for (int i = 0; i < b.size(); i++) {
                final T bNext = bItr.next();

                /* If this item has already been used, skip it */
                if (usedIndices.contains(i)) {
                    continue;
                }

                itemEqual = equator.equals(aNext, bNext);
                if (itemEqual) {
                    usedIndices.add(i);
                    break;
                }
            }

            collectionEqual = itemEqual;
        }

        return collectionEqual;
    }

    /**
     * Null-safe check if the specified collection is empty.
     * 
     * @param collection the collection to check.
     * @return true if the collection is null or empty.
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Null-safe check if the specified collection is not empty.
     * 
     * @param collection the collection to check.
     * @return true if the collection is populated; false if null or empty.
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }
    
    /**
     * Null-safe check if a list of strings contains an element, ignoring case
     * 
     * @param collection the collection of strings to check contains the element
     * @param element the element to check for
     * @return true if the collection contains the element; false if not
     */
    public static boolean containsIgnoreCase(Collection<String> collection, String element) {
        
        if(collection == null) {
            return false;
        }

        for (String value : collection) {
            if (StringUtils.equalsIgnoreCase(value, element)) {
                return true;
            }
        }
        
        return false;
    }
}
