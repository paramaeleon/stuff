/*
 * Licensed under the terms of the Apache License 2.0. For terms and conditions,
 * see http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.paramaeleon.collections;

/**
 * Something that is able to introduce itself.
 * 
 * <p>This interface is related to {@link Clan}.
 * 
 * @param <I> type of identifier
 * @author Matthias Ronge ‹matthias.ronge@freenet.de›
 */
public interface Introducer<I> {
    /**
     * Identifies with its identifier.
     * 
     * @return its identifier
     */
    I identify();
}
