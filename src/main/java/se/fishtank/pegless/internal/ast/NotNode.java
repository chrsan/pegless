package se.fishtank.pegless.internal.ast;

/**
 * Represents an AST <em>not</em> node.
 *
 * @author Christer Sandberg
 */
public class NotNode extends UnaryNode<NotNode> {

    /**
     * Create a new <em>not</em> node.
     *
     * @param sibling Sibling
     */
    public NotNode(Node<?> sibling) {
        this.sibling = sibling;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotNode copy() {
        return new NotNode(sibling.copy());
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
