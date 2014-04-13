package se.fishtank.pegless.internal.ast;

/**
 * Represents an AST <em>char</em> node.
 *
 * @author Christer Sandberg
 */
public class CharNode extends Node<CharNode> {

    /** Character value */
    public int ch;

    /**
     * Create a new <em>char</em> node.
     *
     * @param ch Character value
     */
    public CharNode(int ch) {
        this.ch = ch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CharNode copy() {
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
