package se.fishtank.pegless.internal.ast;

/**
 * Represents an AST <em>any</em> node.
 *
 * @author Christer Sandberg
 */
public class AnyNode extends Node<AnyNode> {

    /** Singleton instance */
    public static final AnyNode SINGLETON = new AnyNode();

    /**
     * Private
     */
    private AnyNode() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnyNode copy() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHeadFail() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNoFail() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNullable() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getFixedLength(int callCount, int length) {
        return length + 1;
    }

}
