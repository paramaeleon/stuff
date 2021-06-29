/*
 * Licensed under the terms of the Apache License 2.0. For terms and conditions,
 * see http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.paramaeleon.collections;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

class SortedSetsAndJoiningIteratorTest {

    @Test
    void shouldAndJoin1() {
        TreeSet<Integer> first = new TreeSet<>();
        first.add(13);
        first.add(21);
        first.add(30);
        first.add(29);
        first.add(12);
        first.add(6);
        first.add(28);
        first.add(27);
        first.add(5);
        first.add(1);
        first.add(16);
        first.add(2);

        TreeSet<Integer> second = new TreeSet<>();
        second.add(25);
        second.add(24);
        second.add(5);
        second.add(1);
        second.add(8);
        second.add(26);
        second.add(9);
        second.add(29);
        second.add(27);
        second.add(16);
        second.add(22);
        second.add(6);
        second.add(32);

        SortedSetsAndJoiningIterator<Integer> underTest = new SortedSetsAndJoiningIterator<>(
                Arrays.asList(first, second));

        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.next(), is(equalTo(1)));
        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.next(), is(equalTo(5)));
        assertThat(underTest.next(), is(equalTo(6)));
        assertThat(underTest.next(), is(equalTo(16)));
        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.next(), is(equalTo(27)));
        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.next(), is(equalTo(29)));
        assertThat(underTest.hasNext(), is(false)); // 1st exit
        assertThrows(NoSuchElementException.class, () -> underTest.next());
    }

    @Test
    void shouldAndJoin2() {
        TreeSet<Integer> first = new TreeSet<>();
        first.add(13);
        first.add(21);
        first.add(30);
        first.add(29);
        first.add(12);
        first.add(6);
        first.add(28);
        first.add(27);
        first.add(5);
        first.add(1);
        first.add(16);
        first.add(2);

        TreeSet<Integer> second = new TreeSet<>();
        second.add(25);
        second.add(24);
        second.add(5);
        second.add(1);
        second.add(8);
        second.add(26);
        second.add(9);
        second.add(29);
        second.add(27);
        second.add(16);
        second.add(22);
        second.add(6);

        SortedSetsAndJoiningIterator<Integer> underTest
                = new SortedSetsAndJoiningIterator<>(Arrays.asList(first, second));

        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.next(), is(equalTo(1)));
        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.next(), is(equalTo(5)));
        assertThat(underTest.next(), is(equalTo(6)));
        assertThat(underTest.next(), is(equalTo(16)));
        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.next(), is(equalTo(27)));
        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.next(), is(equalTo(29)));
        assertThat(underTest.hasNext(), is(false)); // 2nd exit
        assertThrows(NoSuchElementException.class, () -> underTest.next());
    }

    @Test
    void shouldAndJoinMany() {
        TreeSet<Integer> one = new TreeSet<>();
        one.add(5);
        one.add(8);
        one.add(13);
        one.add(15);
        one.add(17);
        one.add(21);
        one.add(22);

        TreeSet<Integer> two = new TreeSet<>();
        two.add(5);
        two.add(8);
        two.add(10);
        two.add(11);
        two.add(13);
        two.add(15);
        two.add(19);
        two.add(21);
        two.add(22);
        two.add(25);

        TreeSet<Integer> three = new TreeSet<>();
        three.add(5);
        three.add(8);
        three.add(10);
        three.add(11);
        three.add(13);
        three.add(15);
        three.add(19);
        three.add(21);
        three.add(22);

        TreeSet<Integer> four = new TreeSet<>();
        four.add(5);
        four.add(8);
        four.add(10);
        four.add(13);
        four.add(15);
        four.add(19);
        four.add(21);
        four.add(22);
        four.add(28);
        four.add(29);

        TreeSet<Integer> five = new TreeSet<>();
        five.add(5);
        five.add(8);
        five.add(10);
        five.add(13);
        five.add(17);
        five.add(21);
        five.add(22);

        SortedSetsAndJoiningIterator<Integer> underTest = new SortedSetsAndJoiningIterator<>(
                Arrays.asList(one, two, three, four, five));

        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.next(), is(equalTo(5)));
        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.next(), is(equalTo(8)));
        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.next(), is(equalTo(13)));
        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.next(), is(equalTo(21)));
        assertThat(underTest.hasNext(), is(true));
        assertThat(underTest.next(), is(equalTo(22)));
        assertThat(underTest.hasNext(), is(false));
        assertThrows(NoSuchElementException.class, () -> underTest.next());
    }
}
