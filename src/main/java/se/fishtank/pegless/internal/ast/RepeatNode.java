package se.fishtank.pegless.internal.ast;

/**
 * Represents an AST <em>repeat</em> node.
 *
 * @author Christer Sandberg
 */
public class RepeatNode extends UnaryNode<RepeatNode> {

    /**
     * Create a new <em>repeat</em> node.
     *
     * @param sibling Sibling
     */
    public RepeatNode(Node<?> sibling) {
        this.sibling = sibling;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RepeatNode copy() {
        return new RepeatNode(sibling.copy());
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
        return -1;
    }

}
