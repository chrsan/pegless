package se.fishtank.pegless.internal.ast;

/**
 * Represents an AST <em>false</em> node.
 *
 * @author Christer Sandberg
 */
public class FalseNode extends Node<FalseNode> {

    /** Singleton instance */
    public static final FalseNode SINGLETON = new FalseNode();

    /**
     * Private
     */
    private FalseNode() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FalseNode copy() {
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
        return length;
    }

}
