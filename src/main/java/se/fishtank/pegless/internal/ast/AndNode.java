package se.fishtank.pegless.internal.ast;

/**
 * Represents an AST <em>and</em> node.
 *
 * @author Christer Sandberg
 */
public class AndNode extends UnaryNode<AndNode> {

    /**
     * Create a new <em>and</em> node.
     *
     * @param sibling Sibling
     */
    public AndNode(Node<?> sibling) {
        this.sibling = sibling;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AndNode copy() {
        return new AndNode(sibling.copy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHeadFail() {
        return sibling.isHeadFail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNoFail() {
        return sibling.isNoFail();
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
