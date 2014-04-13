package se.fishtank.pegless.internal.ast;

/**
 * Represents an AST <em>behind</em> node.
 *
 * @author Christer Sandberg
 */
public class BehindNode extends UnaryNode<BehindNode> {

    /** How far to look behind. */
    public final int n;

    /**
     * Create a new <em>behind</em> node.
     *
     * @param n How far to look behind.
     * @param sibling Sibling
     */
    public BehindNode(int n, Node<?> sibling) {
        this.n = n;
        this.sibling = sibling;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BehindNode copy() {
        return new BehindNode(n, sibling.copy());
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
        return false;
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
