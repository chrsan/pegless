package se.fishtank.pegless.internal.ast;

/**
 * Represents an AST <em>true</em> node.
 *
 * @author Christer Sandberg
 */
public class TrueNode extends Node<TrueNode> {

    /** Singleton instance */
    public static final TrueNode SINGLETON = new TrueNode();

    /**
     * Private
     */
    private TrueNode() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrueNode copy() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHeadFail() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNoFail() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNullable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getFixedLength(int callCount, int length) {
        return length;
    }

}
