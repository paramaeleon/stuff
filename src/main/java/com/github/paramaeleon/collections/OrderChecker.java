package com.github.paramaeleon.collections;

import java.util.*;

/**
 * Provides functions to check {@code Iterable}s for being sorted.
 */
public class OrderChecker {

    /**
     * Checks whether the iterated objects are sorted strictly monotonously
     * increasing. The check is based on the native {@code compareTo(…)}
     * function of the {@code Comparable} objects iterated over.
     *
     * @param <T>      type of objects to be iterated over, must be
     *                 {@code Comparable<T>}
     * @param iterable iterable to check
     * @return whether the iterated objects are sorted strictly monotonously
     *         increasing as per {@code compareTo(…)}
     * @throws NullPointerException if the {@code Iterable} contains
     *                              {@code null} objects
     */
    public static <T extends Comparable<T>> boolean isStrictlyMonotonouslyIncreasing(Iterable<T> iterable) {
        assert iterable != null : "'iterable' should not be null";

        Iterator<T> iterator = iterable.iterator();
        if (!iterator.hasNext()) return true;
        T previous = iterator.next();
        while (iterator.hasNext()) {
            T current = iterator.next();
            if (previous.compareTo(current) >= 0) return false;
            previous = current;
        }
        return true;
    }

    // Utility class. Constructor disabled.
    private OrderChecker() { }
}
