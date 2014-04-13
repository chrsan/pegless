package se.fishtank.pegless.internal.compiler;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Character matching.
 *
 * @author Christer Sandberg
 */
public abstract class CharacterMatcher {

    /**
     * Returns whether this character matcher matches character {@code c}.
     *
     * @param c The character to match.
     * @return {@code true} or {@code false}
     */
    public abstract boolean match(int c);

    CharacterMatcher() {
    }

    /**
     * Create a new character matcher from the given character set.
     *
     * @param characterSet A character set.
     * @return A new character matcher.
     */
    public static CharacterMatcher newCharacterMatcher(BitSet characterSet) {
        if (characterSet.isEmpty())
            return new CharacterMatcher() {
                @Override
                public boolean match(int c) {
                    return false;
                }

                @Override
                public String toString() {
                    return "()";
                }
            };

        if (characterSet.nextClearBit(0) == 0x110000)
            return new CharacterMatcher() {
                @Override
                public boolean match(int c) {
                    return true;
                }

                @Override
                public String toString() {
                    return "(0-10ffff)";
                }
            };

        // TODO: Maybe one set should suffice?
        LinkedList<CharacterMatcher> result = new LinkedList<>();

        LinkedList<Integer> set = null;
        for (int i = characterSet.nextSetBit(0); i >= 0; i = characterSet.nextSetBit(i + 1)) {
            int j = characterSet.nextClearBit(i);
            if ((j - i) > 1) {
                if (set != null) {
                    result.add(toCharMatcher(set));
                    set = null;
                }

                result.add(new RangeMatcher(i, j - 1));
                i = j;
            } else {
                if (set == null)
                    set = new LinkedList<>();

                set.add(i);
            }
        }

        if (set != null)
            result.add(toCharMatcher(set));

        return compose(result);
    }

    private static CharacterMatcher compose(LinkedList<CharacterMatcher> matchers) {
        Iterator<CharacterMatcher> iterator = matchers.iterator();

        CharacterMatcher first = iterator.next();
        if (!iterator.hasNext())
            return first;

        ComposedMatcher composed = new ComposedMatcher(first, iterator.next());
        while (iterator.hasNext())
            composed = new ComposedMatcher(composed, iterator.next());

        return composed;
    }

    private static CharacterMatcher toCharMatcher(LinkedList<Integer> set) {
        if (set.size() == 1) {
            final int value = set.getFirst();
            return new CharacterMatcher() {
                @Override
                public boolean match(int c) {
                    return c == value;
                }

                @Override
                public String toString() {
                    return String.format("(%x)", value);
                }
            };
        }

        int i = 0;
        int[] array = new int[set.size()];
        for (Integer n : set)
            array[i++] = n;

        return new SetMatcher(array);
    }

    static class RangeMatcher extends CharacterMatcher {

        final int start;

        final int end;

        RangeMatcher(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean match(int c) {
            return c >= start && c <= end;
        }

        @Override
        public String toString() {
            return String.format("(%x-%x)", start, end);
        }

    }

    static class SetMatcher extends CharacterMatcher {

        final int[] characters;

        SetMatcher(int[] characters) {
            this.characters = characters;
        }

        @Override
        public boolean match(int c) {
            int low = characters[0];
            int high = characters[characters.length - 1];

            return (c == low || c == high) || (c > low && c < high && Arrays.binarySearch(characters, c) >= 0);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(characters.length * 2);
            for (int character : characters)
                sb.append("(").append(Integer.toHexString(character)).append(")");

            return sb.toString();
        }

    }

    static class ComposedMatcher extends CharacterMatcher {

        final CharacterMatcher first;

        final CharacterMatcher second;

        ComposedMatcher(CharacterMatcher first, CharacterMatcher second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean match(int c) {
            return first.match(c) || second.match(c);
        }

        @Override
        public String toString() {
            return first.toString() + second.toString();
        }

    }

}
