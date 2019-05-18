/*
 * Licensed under the terms of the Apache License 2.0. For terms and conditions,
 * see http://www.apache.org/licenses/LICENSE-2.0
 */

package com.github.paramaeleon.toolbox.collections;

import java.util.*;

/**
 * Compares two strings according to their lexicographical order, specifically
 * as defined in German DIN 5007 part 2.
 * <p>
 * 
 * Rules:
 * <ul>
 * <li>Symbols go before numbers, numbers go before letters.</li>
 * <li>Numbers are sorted to their mathematical order, without requiring leading
 * zeroes. Numbers with leading zeroes go before numbers without such.</li>
 * <li>Letters are compared taking into account their comparison expansion form.
 * For German umlauts, these are AE for Ä, OE for Ö, SS for ẞ, and UE for Ü. The
 * French ligature œ is compared as OE accordingly.</li>
 * <li>Strings are compared case-insensitive, unless they are equal. Then, they
 * are compared case-sensitive, where upper case goes before lower case.</li>
 * </ul>
 * <p>
 * 
 * This class is based on works I did during my studies in computational
 * linguistics at CIS Munich, in the beginning of the 2000s and was originally
 * implemented in C. This is a port to Java, which has become my primary
 * programming language since then.
 *
 * @author Matthias Ronge ‹matthias.ronge@freenet.de›
 */
public class DinStringComparator implements Comparator<String> {
    /**
     * A tokenizer with type detection, insertions for umlauts and number group
     * detection. For example, the String "Grüße166.txt" will be tokenized to
     * ['G', 'r', 'u', 'e', 's', 's', 'e', 166, '.', 't', 'x', 't', -1, -1, …].
     *
     * @author Matthias Ronge ‹matthias.ronge@freenet.de›
     */
    private class Tokenizer {
        /**
         * Whether to compare in a case-insensitive manner.
         */
        private final boolean caseInsensitive;

        /**
         * If a letter must be inserted to resolve umlauts or sharp s, it is
         * cached here.
         */
        private short insertion;

        /**
         * The reading position within the string.
         */
        private int position;

        /**
         * String to tokenize.
         */
        private final String data;

        /**
         * The length of the string (for not calculating it over and over
         * again).
         */
        private final int dataLength;

        /**
         * Creates a new Tokenizer.
         *
         * @param toTokenize      String to tokenize
         * @param caseInsensitive
         */
        private Tokenizer(String toTokenize, boolean caseInsensitive) {
            data = toTokenize;
            position = SYMBOL;
            dataLength = toTokenize != null ? toTokenize.length() : -1;
            insertion = NONE;
            this.caseInsensitive = caseInsensitive;
        }

        /**
         * Returns the code point at the current {@code position}. If comparing
         * in a case-insensitive way, the uppercase variant of the code point is
         * returned.
         * 
         * @return the code point at the current {@code position}
         */
        private int currentCodePoint() {
            int codePoint = data.codePointAt(position);
            return caseInsensitive ? Character.toUpperCase(codePoint) : codePoint;
        }

        /**
         * Returns the next token.
         * 
         * <p>
         * Returns an array with the two entries, type and value; in case of
         * numbers three entries type, value, and number of leading zeroes.
         * After the end of data, an array with one type value of -1 is
         * returned.
         *
         * @return the tokens of the string, each at a time.
         */
        private int[] next() {
            if (data == null) return new int[] { Integer.MIN_VALUE };
            if (insertion > NONE) {
                short insertValue = insertion;
                insertion = NONE;
                return new int[] { TYPES[insertValue], insertValue };
            }
            if (position >= dataLength) {
                return new int[] { END };
            }
            int codePoint = currentCodePoint();
            position++;
            if (codePoint > LAST_MAPPED_CODE_POINT) {
                return new int[] { SYMBOL, codePoint };
            }
            short type = TYPES[codePoint];
            if (type != 1) {
                insertion = INSERTS[codePoint];
                return new int[] { type, BASES[codePoint] };
            } else {
                int value = BASES[codePoint];
                int leadingZeroes = value == 0 ? 1 : 0;
                while (position < dataLength && TYPES[codePoint = currentCodePoint()] == 1) {
                    value = 10 * value + BASES[codePoint];
                    if (value == 0) {
                        leadingZeroes++;
                    }
                    position++;
                }
                return new int[] { 1, value, leadingZeroes };
            }
        }
    }

    /**
     * Array of base characters for the characters for the unicode points U+0000
     * .. U+017E.
     */
    // @formatter:off
    private static final short[] BASES = new short[] {
        /* U+0000 */  0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        /* U+0010 */  16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
        /* U+0020 */  32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
        /* U+0030 */  0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 58, 59, 60, 61, 62, 63,
        /* U+0040 */  64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
        /* U+0050 */  80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95,
        /* U+0060 */  96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112,
        /* U+0070 */  113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127,

        /* U+0080 */  128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143,
        /* U+0090 */  144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159,
        /* U+00A0 */  160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175,
        /* U+00B0 */  176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191,
        /* U+00C0 */  65, 65, 65, 65, 65, 65, 65, 67, 69, 69, 69, 69, 73, 73, 73, 73,
        /* U+00D0 */  68, 78, 79, 79, 79, 79, 79, 215, 79, 85, 85, 85, 85, 89, 222, 115,
        /* U+00E0 */  97, 97, 97, 97, 97, 97, 97, 99, 101, 101, 101, 101, 105, 105, 105, 105,
        /* U+00F0 */  240, 110, 111, 111, 111, 111, 111, 247, 111, 117, 117, 117, 117, 121, 254, 121,

        /* U+0100 */  65, 97, 65, 97, 65, 97, 67, 99, 67, 99, 67, 99, 67, 99, 68, 100,
        /* U+0110 */  68, 100, 69, 101, 69, 101, 69, 101, 69, 101, 69, 101, 71, 103, 71, 103,
        /* U+0120 */  71, 103, 71, 103, 72, 104, 72, 104, 73, 105, 73, 105, 73, 105, 73, 105,
        /* U+0130 */  73, 105, 73, 105, 74, 106, 75, 107, 107, 76, 108, 76, 108, 76, 108, 76,
        /* U+0140 */  108, 108, 108, 78, 110, 78, 110, 78, 110, 110, 78, 110, 79, 111, 79, 111,
        /* U+0150 */  79, 111, 79, 111, 82, 114, 82, 114, 82, 114, 83, 115, 83, 115, 83, 115,
        /* U+0160 */  83, 115, 84, 116, 84, 116, 84, 116, 85, 117, 85, 117, 85, 117, 85, 117,
        /* U+0170 */  85, 117, 85, 117, 87, 119, 89, 121, 89, 90, 122, 90, 122, 90, 122
    };
    // @formatter:on

    /**
     * Type value indicating the end of the string was reached. Sorts before any
     * other.
     */
    private static final short END = -1;

    /**
     * Constant value indicating that this letter does not require another
     * letter to be inserted in the comparison String.
     */
    private static final short NONE = -1;

    /**
     * Array of umlaut and ligature resolving insertion (secondary) characters
     * for the characters U+0000 .. U+017E. Example: 'e' (101) for 'ä'.
     */
    // @formatter:off
    private static final short[] INSERTS = new short[] {
        /* U+0000 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+0010 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+0020 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+0030 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+0040 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+0050 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+0060 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+0070 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,

        /* U+0080 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+0090 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+00A0 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+00B0 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+00C0 */  NONE, NONE, NONE, NONE, 69, NONE, 69, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+00D0 */  NONE, NONE, NONE, NONE, NONE, NONE, 69, NONE, NONE, NONE, NONE, NONE, 69, NONE, NONE, 115,
        /* U+00E0 */  NONE, NONE, NONE, NONE, 101, NONE, 101, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+00F0 */  NONE, NONE, NONE, NONE, NONE, NONE, 101, NONE, NONE, NONE, NONE, NONE, 101, NONE, NONE, NONE,

        /* U+0100 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+0110 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+0120 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+0130 */  NONE, NONE, 74, 106, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+0140 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+0150 */  NONE, NONE, 69, 101, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+0160 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
        /* U+0170 */  NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE
    };
    // @formatter:on

    /**
     * Last code point that can be resolved using the mapping tables. Each table
     * has a length of 383 entries for the code points U+0000 (0) to U+017E
     * (382). All characters above this point are treated as symbols.
     */
    private static final int LAST_MAPPED_CODE_POINT = 0x017E;

    /**
     * Type value indicating the character was found to be a lower case letter.
     * Sorts after any other. As second array entry, the base value for the
     * letter, like 97 ('a') e.g. for both 'ä' and 'à', is returned.
     */
    private static final short LOWER = 3;

    /**
     * Type value indicating the character was found to be a number. Sorts after
     * symbols, before upper case letters. As second array entry, the full
     * number, which may be represented by multiple ciphers in the input string,
     * is returned. As third array entry, the number of leading zeroes found
     * while looking for the end of the number, is returned. This string
     * comparator does only support handling of non-negative integers, which
     * MUST NOT contain thousands’ separator characters.
     */
    private static final short NUMERAL = 1;

    /**
     * Type value indicating the character was found to be a symbol. Sorts
     * before any other type, but after a String that already has terminated. As
     * second array entry, the code point value of the symbol is returned.
     */
    private static final short SYMBOL = 0;

    /**
     * Type value indicating the character was found to be an uppercase letter.
     * Sorts after numbers, before lowercase letters. As second array entry, the
     * base value for the letter, like 65 ('A') e.g. for both 'Á' and 'À', is
     * returned.
     */
    private static final short UPPER = 2;

    /**
     * Array of character types for the characters U+0000 .. U+017E.
     */
    // @formatter:off
    private static final short[] TYPES = new short[] {
        /* U+0000 */  SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
        /* U+0010 */  SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
        /* U+0020 */  SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
        /* U+0030 */  NUMERAL, NUMERAL, NUMERAL, NUMERAL, NUMERAL, NUMERAL, NUMERAL, NUMERAL,
                      NUMERAL, NUMERAL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
        /* U+0040 */  SYMBOL, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER,
                      UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER,
        /* U+0050 */  UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER,
                      UPPER, UPPER, UPPER, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
        /* U+0060 */  SYMBOL, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER,
                      LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER,
        /* U+0070 */  LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER,
                      LOWER, LOWER, LOWER, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,

        /* U+0080 */  SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
        /* U+0090 */  SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
        /* U+00A0 */  SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
        /* U+00B0 */  SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
        /* U+00C0 */  UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER,
                      UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER,
        /* U+00D0 */  UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, SYMBOL,
                      UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, SYMBOL, LOWER,
        /* U+00E0 */  LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER,
                      LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER,
        /* U+00F0 */  SYMBOL, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, SYMBOL,
                      LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, SYMBOL, LOWER,

        /* U+0100 */  UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
                      UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
        /* U+0110 */  UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
                      UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
        /* U+0120 */  UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
                      UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
        /* U+0130 */  UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
                      UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
        /* U+0140 */  UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
                      UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
        /* U+0150 */  UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
                      UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
        /* U+0160 */  UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
                      UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
        /* U+0170 */  UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
                      UPPER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER
    };
    // @formatter:on

    /**
     * Convenience method to get map sorted by this comparator.
     *
     * @param <V>      value type of the map
     * @param elements map to sort
     * @return a TreeSet sorted by a StringComparator instance
     */
    public static <V> TreeMap<String, V> asSortedMap(Map<String, V> elements) {
        TreeMap<String, V> result = new TreeMap<String, V>(new DinStringComparator());
        result.putAll(elements);
        return result;
    }

    /**
     * Convenience method to get a sorted collection of strings sorted by this
     * comparator.
     *
     * @param elements collection to sort
     * @return a TreeSet sorted by a StringComparator instance
     */
    public static TreeSet<String> asSortedSet(Collection<String> elements) {
        TreeSet<String> result = new TreeSet<String>(new DinStringComparator());
        result.addAll(elements);
        return result;
    }

    /**
     * Returns the canonical version of a String, that is exactly the internal
     * form used to compare the two strings.
     *
     * @param s String to convert
     * @return the canonical version of the String.
     */
    public static String getCanonical(String s) {
        StringBuilder result = new StringBuilder((int) (1.15 * s.length()));
        Tokenizer tokenizer = new DinStringComparator().new Tokenizer(s, false);
        int[] token;
        while ((token = tokenizer.next())[0] != END) {
            if (token[0] == NUMERAL) {
                for (int i = 1; i < token[2]; i++) {
                    result.append(0);
                }
                result.append(token[1]);
            } else {
                result.appendCodePoint(token[1]);
            }
        }
        return result.toString();
    }

    /**
     * Compares two strings as defined by this comparator. A value &lt;0 means
     * that the first String goes before the second one, a value of &gt;0 means
     * that the second String has to go before the first one. A value of 0 means
     * that both strings are equal. Supports {@code null} Strings, which are
     * sorted before any other.
     */
    @Override
    public int compare(String o1, String o2) {

        // first, compare case insensitive

        int result1 = compare(o1, o2, true);
        if (result1 != 0) {
            return result1;
        }

        // if equal, compare case sensitive, where uppercase < lowercase

        int result2 = compare(o1, o2, false);
        if (result2 != 0) {
            return result1;
        }

        // if equal, check for binary equality
        /* This one is important for not loosing entries in sets that do not
         * define a sort order by the rules implemented in this class. */
        return o1.compareTo(o2);

    }

    public int compare(String o1, String o2, boolean caseInsensitive) {
        Tokenizer tokenizer1 = new Tokenizer(o1, caseInsensitive);
        Tokenizer tokenizer2 = new Tokenizer(o2, caseInsensitive);
        while (true) {
            int[] token1 = tokenizer1.next();
            int[] token2 = tokenizer2.next();
            int typeComparison = token1[0] - token2[0];
            if (typeComparison != 0) {
                return typeComparison;
            }
            if (token1[0] == END) {
                return 0;
            }
            int valueComparison = token1[1] - token2[1];
            if (valueComparison != 0) {
                return valueComparison;
            }
            if (token1[0] == NUMERAL) {
                int lengthComparison = token2[2] - token1[2];
                if (lengthComparison != 0) {
                    return lengthComparison;
                }
            }
        }
    }
}
