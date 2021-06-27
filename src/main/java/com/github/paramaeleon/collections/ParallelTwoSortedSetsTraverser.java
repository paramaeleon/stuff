/*
 * Licensed under the terms of the Apache License 2.0. For terms and conditions,
 * see http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.paramaeleon.collections;

import java.util.*;

/**
 * Compares two sorted sets in a very performing manner.
 * <p>
 * Often, two collections are compared by making a copy of the first, and
 * removing all elements from the second from it.
 *
 * <pre>
 * Collection firstOnly = new ArrayList<>(first);
 * firstOnly.removeAll(second);
 * </pre>
 *
 * If both margins are needed, the same is done for the other side. This works,
 * but given your sets are sorted anyway (maybe ID lists returned from a
 * database), there is a much faster performing way to get this done.
 * <p>
 * Parallel traversal works in constant time (O(𝑛)), even for very large sets.
 * <p>
 * This class implements functionality for comparing <i>two</i> sorted sets,
 * where you typically have a conceptual “left side” and “right side”.
 */
public class ParallelTwoSortedSetsTraverser {

    /**
     * Compares two sorted sets and returns the differences. Finding the
     * differences between two sets is a task typically needed to synchronize
     * two sides. The method takes two arbitrary iterables to be generic. So you
     * can, for example, also compare two arbitrary collections of files or
     * database IDs, which are known to be sorted sets (that is, ascending order
     * as per the native {@code compareTo(…)} of the objects, and no
     * duplicates). If you know this, but the database only returns a generic
     * collection type, you can pass it anyway. The calling side is responsible
     * for that these are actually sorted. If they are not, the outcome is
     * undefined. If you’re unsure, pass in two {@link TreeSet}s.
     *
     * @param <T>   type of objects to be iterated over, must be
     *              {@code Comparable<T>}.
     *
     * @param left  the one sorted set
     * @param right the other sorted set
     * @return a map with the differences, mapped as T on {@code Boolean}, where
     *         {@code true} if it appears in the first list and not in the
     *         second, {@code false} if it appears in the second list and not in
     *         the first.
     */
    public static <T extends Comparable<T>> Map<T, Boolean> findDifferences(Iterable<T> left, Iterable<T> right) {
        final Map<T, Boolean> result = new HashMap<>();
        Iterator<T> leftIterator = left.iterator();
        Iterator<T> rightIterator = right.iterator();
        boolean nextLeft = true;
        boolean nextRight = true;
        T currentLeft = null;
        T currentRight = null;
        while (nextLeft || nextRight) {
            if (nextLeft) {
                currentLeft = leftIterator.hasNext() ? leftIterator.next() : null;
                nextLeft = false;
            }
            if (nextRight) {
                currentRight = rightIterator.hasNext() ? rightIterator.next() : null;
                nextRight = false;
            }
            boolean comparable = currentLeft != null && currentRight != null;
            if (currentLeft != null && currentRight == null || comparable && currentLeft.compareTo(currentRight) < 0) {
                result.put(currentLeft, Boolean.TRUE);
                nextLeft = true;
            } else if (currentLeft == null && currentRight != null
                    || comparable && currentLeft.compareTo(currentRight) > 0) {
                result.put(currentRight, Boolean.FALSE);
                nextRight = true;
            } else if (comparable) {
                nextLeft = true;
                nextRight = true;
            }
        }
        return result;
    }
}
