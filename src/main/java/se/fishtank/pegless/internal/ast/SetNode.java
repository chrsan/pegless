package se.fishtank.pegless.internal.ast;

import java.util.BitSet;

/**
 * Represents an AST <em>set</em> node.
 *
 * @author Christer Sandberg
 */
public class SetNode extends Node<SetNode> {

    /** A set of characters. */
    public final BitSet characterSet;

    /**
     * Create a new <em>set</em> node.
     *
     * @param characterSet A set of characters.
     */
    public SetNode(BitSet characterSet) {
        this.characterSet = characterSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SetNode copy() {
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
