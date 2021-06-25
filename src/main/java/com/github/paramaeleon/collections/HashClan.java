/*
 * Licensed under the terms of the Apache License 2.0. For terms and conditions,
 * see http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.paramaeleon.collections;

import java.util.*;
import java.util.function.*;

/**
 * A {@code Clan} implementation based on a hash map.
 * 
 * @author Matthias Ronge ‹matthias.ronge@freenet.de›
 *
 * @param <K> type of identifier
 * @param <V> type of elements in the clan
 */
public class HashClan<K, V extends Introducer<K>> implements Clan<K, V> {
    /**
     * The members of the clan.
     */
    private final Map<K, V> members = new HashMap<>();

    @Override
    public boolean add(V e) {
        return !e.equals(members.put(e.identify(), e));
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        boolean changed = false;
        for (V e : c) changed |= add(e);
        return changed;
    }

    @Override
    public void changeIdentifier(K previousKey, Consumer<V> changeAction) {
        V e = members.remove(previousKey);
        changeAction.accept(e);
        members.put(e.identify(), e);
    }

    @Override
    public void clear() {
        members.clear();
    }

    @Override
    public boolean contains(Object o) {
        return members.containsValue(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.parallelStream().allMatch(λ -> members.containsValue(λ));
    }

    @Override
    public V get(K key) {
        return members.get(key);
    }

    @Override
    public boolean isEmpty() { return members.isEmpty(); }

    @Override
    public Iterator<V> iterator() {
        return members.values().iterator();
    }

    @Override
    public boolean remove(Object o) {
        return members.values().remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return members.values().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return members.values().retainAll(c);
    }

    @Override
    public int size() {
        return members.size();
    }

    @Override
    public Object[] toArray() {
        return members.values().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return members.values().toArray(a);
    }
}
