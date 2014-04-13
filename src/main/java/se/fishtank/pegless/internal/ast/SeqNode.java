package se.fishtank.pegless.internal.ast;

/**
 * Represents an AST <em>seq</em> node.
 *
 * @author Christer Sandberg
 */
public class SeqNode extends BinaryNode<SeqNode> {

    /**
     * Create a new <em>seq</em> node.
     *
     * @param firstSibling First sibling
     * @param secondSibling Second sibling
     */
    public SeqNode(Node<?> firstSibling, Node<?> secondSibling) {
        this.firstSibling = firstSibling;
        this.secondSibling = secondSibling;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SeqNode copy() {
        return new SeqNode(firstSibling.copy(), secondSibling.copy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHeadFail() {
        return secondSibling.isNoFail() && firstSibling.isHeadFail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNoFail() {
        return firstSibling.isNoFail() && secondSibling.isNoFail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNullable() {
        return firstSibling.isNullable() && secondSibling.isNullable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getFixedLength(int callCount, int length) {
        length = firstSibling.getFixedLength(callCount, length);
        if (length < 0)
            return -1;

        return secondSibling.getFixedLength(callCount, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void correctAssociativity() {
        Node<?> firstSibling = this.firstSibling;
        while (firstSibling instanceof SeqNode) {
            SeqNode s = (SeqNode) firstSibling;

            Node<?> ff = s.firstSibling;
            s.firstSibling = s.secondSibling;
            s.secondSibling = this.secondSibling;

            this.firstSibling = ff;
            this.secondSibling = firstSibling;

            firstSibling = ff;
        }
    }

}
