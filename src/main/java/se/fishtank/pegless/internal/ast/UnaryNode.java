package se.fishtank.pegless.internal.ast;

/**
 * Base class for nodes with one sibling.
 *
 * @author Christer Sandberg
 */
public abstract class UnaryNode<T extends Node> extends Node<T> {

    protected Node<?> sibling;

    /**
     * Returns the sibling node.
     *
     * @return The sibling node.
     */
    public Node<?> getSibling() {
        return sibling;
    }

}
