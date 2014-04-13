package se.fishtank.pegless.internal.ast;

/**
 * Represents an AST node.
 *
 * @author Christer Sandberg
 */
public abstract class Node<T extends Node> {

    /**
     * Create a copy of this node.
     *
     * @return A copy of this node.
     */
    public abstract T copy();

    /**
     * Returns {@code true} if this node can only fail depending on the next
     * character of the subject.
     *
     * @return {@code true} or {@code false}
     */
    public abstract boolean isHeadFail();

    /**
     * Check if this node never fails for any string.
     *
     * @return {@code true} or {@code false}
     */
    public abstract boolean isNoFail();

    /**
     * Check if this node is <em>nullable</em>.
     * <p/>
     * A node is <em>nullable</em> if it can match without consuming any characters.
     *
     * @return {@code true} or {@code false}
     */
    public abstract boolean isNullable();

    /**
     * Get the number of characters this node matches.
     *
     * @return The number of characters ({@code -1} if variable).
     */
    public int getFixedLength() {
        return getFixedLength(0, 0);
    }

    /**
     * Get the number of characters this node matches.
     *
     * @param callCount Number of calls so far (used to avoid infinite loops for grammars).
     * @param length Accumulated length.
     * @return The number of characters ({@code -1} if variable).
     */
    protected abstract int getFixedLength(int callCount, int length);

}
