/*
 * Licensed under the terms of the Apache License 2.0. For terms and conditions,
 * see http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.paramaeleon.collections;

import java.util.*;
import java.util.function.*;

import javax.annotation.*;

/**
 * 
 * I have seen the following pattern way too often:
 *
 * <pre>
 * Thing theThing = null;
 * for(Thing thing : things) {
 *     if(thing.getId().equals(idIAmLookingFor){
 *         theThing = thing;
 *         break;
 *     }
 * }
 * </pre>
 * 
 * It is highly inefficient, especially for large collections, where hash maps
 * are available in standard Java for decades. That’s why I invented the clan as
 * a collection in which each member can be efficiently fetched through based on
 * its identification. This collection implementation is meant to be a simple
 * replacement.
 *
 * @param <K> type of identifier
 * @param <V> type of elements in the clan
 */
public interface Clan<K, V extends Introducer<K>> extends Collection<V> {
    /**
     * Procedure to change the name of a member of the clan. Name changes by
     * clan members should be avoided, but if it does not work out, it must be
     * done officially so that the clan does not get confused.
     * 
     * @param previousId   previous identifier of the clan member whose
     *                     identifier is to be changed.
     * @param changeAction what to do to change the name
     */
    public void changeIdentifier(K previousId, Consumer<V> changeAction);

    /**
     * Returns a member of the clan based on his identification.
     * 
     * @param id identification of the requested member
     * @return the wanted clan member, or {@code null} if there is no member
     *         with that identifier
     */
    @CheckForNull
    public V get(K id);
}
