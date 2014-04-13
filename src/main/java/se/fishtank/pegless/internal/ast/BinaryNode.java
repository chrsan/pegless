package se.fishtank.pegless.internal.ast;

/**
 * Base class for nodes with two siblings.
 *
 * @author Christer Sandberg
 */
public abstract class BinaryNode<T extends Node> extends Node<T> {

    protected Node<?> firstSibling;

    protected Node<?> secondSibling;

    /**
     * Returns the first sibling.
     *
     * @return The first sibling.
     */
    public Node<?> getFirstSibling() {
        return firstSibling;
    }

    /**
     * Returns the second sibling.
     *
     * @return The second sibling.
     */
    public Node<?> getSecondSibling() {
        return secondSibling;
    }

    /**
     * Transform left associative constructs into right associative ones.
     */
    public abstract void correctAssociativity();

}
