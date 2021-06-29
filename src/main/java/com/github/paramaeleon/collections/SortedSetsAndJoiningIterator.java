/*
 * Licensed under the terms of the Apache License 2.0. For terms and conditions,
 * see http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.paramaeleon.collections;

import java.util.*;

/**
 * An iterator that very efficiently calculates an and-join on several sets. The
 * sets must be sorted by their native order, as defined by the
 * {@code compareTo()} of the objects they contain. The method takes a list of
 * arbitrary iterables to be generic. So you can, for example, also compare two
 * arbitrary collections of files or database IDs, which are known to be sorted
 * sets (that is, ascending order and no duplicates). If you know this, but the
 * database only returns a generic collection type, you can pass it anyway. The
 * calling side is responsible for that these are actually sorted. If they are
 * not, the outcome is undefined. If you’re unsure, pass in {@link TreeSet}s.
 *
 * @param <T> type of objects to be iterated over, must be {@code Comparable<T>}
 */
public class SortedSetsAndJoiningIterator<T extends Comparable<T>> implements Iterator<T> {

    private T comparee;
    private final List<T> currentValues = new ArrayList<>();
    private final List<Iterator<T>> iterators = new ArrayList<>();
    private T next = null;
    private boolean ongoing = true;

    public SortedSetsAndJoiningIterator(List<Iterable<T>> sortedSets) {
        assert sortedSets != null : "'sortedSets' should not be null";
        assert sortedSets.parallelStream()
                .allMatch(OrderChecker::isStrictlyMonotonouslyIncreasing) : "all 'sortedSets' should be sorted";

        if (sortedSets.isEmpty()) {
            ongoing = false;
        } else {
            for (Iterable<T> sortedSet : sortedSets) {
                Iterator<T> iterator = sortedSet.iterator();
                iterators.add(iterator);
                currentValues.add(iterator.next());
            }
            comparee = currentValues.get(0);
        }
    }

    private void findNext() {
        int comparison = 0;
        T compared = null;
        WHILE: while (true) {
            for (int i = 0; i < currentValues.size(); i++) {
                while ((comparison = (compared = currentValues.get(i)).compareTo(comparee)) < 0) {
                    Iterator<T> iterator = iterators.get(i);
                    if (!iterator.hasNext()) {
                        ongoing = false;
                        break WHILE;
                    } else {
                        currentValues.set(i, iterator.next());
                    }
                }
                if (comparison > 0) {
                    break; // for
                }
            }
            if (comparison > 0) {
                comparee = compared;
            } else {
                next = comparee;
                for (int i = 0; i < currentValues.size(); i++) {
                    Iterator<T> iterator = iterators.get(i);
                    if (iterator.hasNext()) {
                        currentValues.set(i, iterator.next());
                    } else {
                        ongoing = false;
                        break WHILE;
                    }
                }
                comparee = Collections.min(currentValues);
                break; // while
            }
        }
    }

    @Override
    public boolean hasNext() {
        if (ongoing && next == null) {
            findNext();
        }
        return next != null;
    }

    @Override
    public T next() {
        if (ongoing && next == null) {
            findNext();
        }
        if (next == null) throw new NoSuchElementException("No such element");
        T nextT = next;
        next = null;
        return nextT;
    }
}
