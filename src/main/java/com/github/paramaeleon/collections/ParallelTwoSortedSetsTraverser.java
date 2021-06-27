/*
 * Licensed under the terms of the Apache License 2.0. For terms and conditions,
 * see http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.paramaeleon.collections;

import java.util.*;
import java.util.function.*;

import javax.annotation.*;

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
 *
 * @param <T>
 */
public class ParallelTwoSortedSetsTraverser<T extends Comparable<T>> {

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
     *              {@code Comparable<T>}
     *
     * @param left  the one sorted set
     * @param right the other sorted set
     * @return a map with the differences, mapped as T on {@code Boolean}, where
     *         {@code true} if it appears in the first list and not in the
     *         second, {@code false} if it appears in the second list and not in
     *         the first.
     */
    public static <T extends Comparable<T>> Map<T, Boolean> findDifferences(Iterable<T> left, Iterable<T> right) {
        // assertions are checked in process()

        final Map<T, Boolean> result = new HashMap<>();
        process(left, right, λ -> result.put(λ, Boolean.TRUE), null, λ -> result.put(λ, Boolean.FALSE));
        return result;
    }

    /**
     * Compares two sorted sets and invokes functions on the objects, depending
     * on whether they are only in the left, in both, or only in the right set.
     * The method takes two arbitrary iterables to be generic. So you can, for
     * example, also compare two arbitrary collections of files or database IDs,
     * which are known to be sorted sets (that is, ascending order as per the
     * native {@code compareTo(…)} of the objects, and no duplicates). If you
     * know this, but the database only returns a generic collection type, you
     * can pass it anyway. The calling side is responsible for that these are
     * actually sorted. If they are not, the outcome is undefined. If you’re
     * unsure, pass in two {@link TreeSet}s.
     *
     * @param <T>             type of objects to be iterated over, must be
     *                        {@code Comparable<T>}
     * @param left            the one sorted set
     * @param right           the other sorted set
     * @param withLeftOnlyDo  function to invoke on objects only found in the
     *                        left set
     * @param withInBothDo    function to invoke on objects found in both sets
     * @param withRightOnlyDo function to invoke on objects only found in the
     *                        right set
     */
    public static <T extends Comparable<T>> void process(Iterable<T> left, Iterable<T> right,
            @Nullable Consumer<T> withLeftOnlyDo, @Nullable Consumer<T> withInBothDo,
            @Nullable Consumer<T> withRightOnlyDo) {
        assert left != null : "'left' should not be null";
        assert OrderChecker.isStrictlyMonotonouslyIncreasing(left) : "'left' should be sorted";
        assert right != null : "'right' should not be null";
        assert OrderChecker.isStrictlyMonotonouslyIncreasing(right) : "'right' should be sorted";

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
                if (withLeftOnlyDo != null) {
                    withLeftOnlyDo.accept(currentLeft);
                }
                nextLeft = true;
            } else if (currentLeft == null && currentRight != null
                    || comparable && currentLeft.compareTo(currentRight) > 0) {
                if (withRightOnlyDo != null) {
                    withRightOnlyDo.accept(currentRight);
                }
                nextRight = true;
            } else if (comparable) {
                if (withInBothDo != null) {
                    withInBothDo.accept(currentLeft);
                }
                nextLeft = true;
                nextRight = true;
            }
        }
    }

    /**
     * Action to perform on objects in both sets.
     */
    protected Consumer<T> withInBothDo = null;

    /**
     * Action to perform on objects in left-side set only.
     */
    protected Consumer<T> withLeftOnlyDo = null;

    /**
     * Action to perform on objects in right-side set only.
     */
    protected Consumer<T> withRightOnlyDo = null;

    /**
     * <b>Constructor.</b><!-- --> Creates a new ParallelTwoSortedSetsTraverser.
     */
    public ParallelTwoSortedSetsTraverser() { }

    /**
     * <b>Constructor.</b><!-- --> Creates a new ParallelTwoSortedSetsTraverser
     * with an action to invoke on objects found in both lists.
     *
     * @param withInBothDo action to perform on objects in both sets
     */
    public ParallelTwoSortedSetsTraverser(Consumer<T> withInBothDo) {
        this.withInBothDo = withInBothDo;
    }

    /**
     * <b>Constructor.</b><!-- --> Creates a new ParallelTwoSortedSetsTraverser
     * with actions to invoke on objects found on in one of the two sets only.
     *
     * @param withLeftOnlyDo  action to perform on objects in left-side set only
     * @param withRightOnlyDo action to perform on objects in right-side set
     *                        only
     */
    public ParallelTwoSortedSetsTraverser(Consumer<T> withLeftOnlyDo, Consumer<T> withRightOnlyDo) {
        this.withLeftOnlyDo = withLeftOnlyDo;
        this.withRightOnlyDo = withRightOnlyDo;
    }

    /**
     * <b>Constructor.</b><!-- --> Creates a new ParallelTwoSortedSetsTraverser
     * with actions to invoke on objects.
     *
     * @param withLeftOnlyDo  action to perform on objects in left-side set only
     * @param withInBothDo    action to perform on objects in both sets
     * @param withRightOnlyDo action to perform on objects in right-side set
     *                        only
     */
    public ParallelTwoSortedSetsTraverser(Consumer<T> withLeftOnlyDo, Consumer<T> withInBothDo,
            Consumer<T> withRightOnlyDo) {

        this.withLeftOnlyDo = withLeftOnlyDo;
        this.withRightOnlyDo = withRightOnlyDo;
    }

    /**
     * Processes two sets and invokes the previously defined actions on them.
     *
     * @param left  the one sorted set
     * @param right the other sorted set
     */
    public void process(Iterable<T> left, Iterable<T> right) {
        // assertions are checked in process()

        process(left, right, withLeftOnlyDo, withInBothDo, withRightOnlyDo);
    }

    /**
     * Sets an action to perform on objects that exist in both sets.
     *
     * @param withRightOnlyDo action to perform
     * @return itself, for method chaining
     */
    public ParallelTwoSortedSetsTraverser<T> withInBothDo(@Nullable Consumer<T> withInBothDo) {
        this.withInBothDo = withInBothDo;
        return this;
    }

    /**
     * Sets an action to perform on objects that only exist in the left-side
     * set.
     *
     * @param withRightOnlyDo action to perform
     * @return itself, for method chaining
     */
    public ParallelTwoSortedSetsTraverser<T> withLeftOnlyDo(@Nullable Consumer<T> withLeftOnlyDo) {
        this.withLeftOnlyDo = withLeftOnlyDo;
        return this;
    }

    /**
     * Sets an action to perform on objects that only exist in the right-side
     * set.
     *
     * @param withRightOnlyDo action to perform
     * @return itself, for method chaining
     */
    public ParallelTwoSortedSetsTraverser<T> withRightOnlyDo(@Nullable Consumer<T> withRightOnlyDo) {
        this.withRightOnlyDo = withRightOnlyDo;
        return this;
    }
}
