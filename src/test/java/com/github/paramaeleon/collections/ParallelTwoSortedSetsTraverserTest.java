/*
 * Licensed under the terms of the Apache License 2.0. For terms and conditions,
 * see http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.paramaeleon.collections;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.*;

import org.junit.jupiter.api.*;

class ParallelTwoSortedSetsTraverserTest {

    @Test
    void shouldFindDifferences() {
        TreeSet<Integer> left = new TreeSet<>();
        left.add(13);
        left.add(21);
        left.add(30);
        left.add(29);
        left.add(12);
        left.add(6);
        left.add(28);
        left.add(27);
        left.add(5);
        left.add(1);
        left.add(16);
        left.add(2);

        TreeSet<Integer> right = new TreeSet<>();
        right.add(25);
        right.add(24);
        right.add(5);
        right.add(1);
        right.add(8);
        right.add(26);
        right.add(9);
        right.add(29);
        right.add(27);
        right.add(16);
        right.add(22);
        right.add(6);

        Map<Integer, Boolean> result = ParallelTwoSortedSetsTraverser.findDifferences(left, right);

        assertThat(result.size(), is(equalTo(12)));

        assertThat(result.get(30), is(true));
        assertThat(result.get(21), is(true));
        assertThat(result.get(2), is(true));
        assertThat(result.get(13), is(true));
        assertThat(result.get(12), is(true));
        assertThat(result.get(28), is(true));

        assertThat(result.get(26), is(false));
        assertThat(result.get(25), is(false));
        assertThat(result.get(24), is(false));
        assertThat(result.get(9), is(false));
        assertThat(result.get(8), is(false));
        assertThat(result.get(22), is(false));
    }

}
