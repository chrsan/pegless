package se.fishtank.pegless.internal.util;

import java.util.BitSet;

import se.fishtank.pegless.internal.ast.AnyNode;
import se.fishtank.pegless.internal.ast.CharNode;
import se.fishtank.pegless.internal.ast.Node;
import se.fishtank.pegless.internal.ast.SetNode;

/**
 * Static utility methods for character sets.
 *
 * @author Christer Sandberg
 */
public abstract class CharacterSet {

    /** All unicode characters (must not be modified). */
    public static final BitSet FULL_SET = newFullSet();

    /**
     * Get a character set for the specified node.
     *
     * @see #fillCharacterSet(se.fishtank.pegless.internal.ast.Node, java.util.BitSet)
     *
     * @param node A node
     * @return A character set or {@code null}.
     */
    public static BitSet getCharacterSet(Node<?> node) {
        BitSet target = new BitSet();
        if (fillCharacterSet(node, target))
            return target;

        return null;
    }

    /**
     * Fill the specified set with characters from the given node.
     * <p/>
     * It will only be filled if the {@code node} is of type set, char
     * or any. Otherwise the set will be left untouched and {@code false}
     * will be returned.
     *
     * @param node A node
     * @param target The set to fill.
     * @return {@code true} or {@code false}
     */
    public static boolean fillCharacterSet(Node<?> node, BitSet target) {
        if (node instanceof SetNode) {
            target.clear();
            target.or(((SetNode) node).characterSet);
            return true;
        }

        if (node instanceof CharNode) {
            target.clear();
            target.set(((CharNode) node).ch);
            return true;
        }

        if (node instanceof AnyNode) {
            target.clear();
            target.or(newFullSet());
            return true;
        }

        return false;
    }

    /**
     * Creates a new set of all Unicode characters (U+0000 - U+10FFFF) with all bits set.
     *
     * @return A new set with all bits set.
     */
    public static BitSet newFullSet() {
        BitSet bitSet = new BitSet(0x10FFFF + 1);
        bitSet.set(0, 0x10FFFF + 1);

        return bitSet;
    }

    /**
     * Convert the specified code point to a string.
     *
     * @param codePoint Code point
     * @return A new string.
     */
    public static String toString(int codePoint) {
        return new String(new int[] { codePoint }, 0, 1);
    }

    /**
     * Returns a substring of the specified string.
     *
     * @param str A string
     * @param offset The offset in the string to start at.
     * @param numberOfCodePoints The number of code points to grab.
     * @return A substring of length {@code numberOfCodePoints}.
     */
    public static String substring(String str, int offset, int numberOfCodePoints) {
        int[] codePoints = new int[numberOfCodePoints];
        for (int i = 0; i < numberOfCodePoints; ++i)
            codePoints[i] = str.codePointAt(offset + i);

        return new String(codePoints, 0, numberOfCodePoints);
    }

}
