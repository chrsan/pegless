package se.fishtank.pegless.internal.ast;

/**
 * Represents an AST <em>choice</em> node.
 *
 * @author Christer Sandberg
 */
public class ChoiceNode extends BinaryNode<ChoiceNode> {

    /**
     * Create a new <em>choice</em> node.
     *
     * @param firstSibling First sibling
     * @param secondSibling Second sibling
     */
    public ChoiceNode(Node<?> firstSibling, Node<?> secondSibling) {
        this.firstSibling = firstSibling;
        this.secondSibling = secondSibling;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChoiceNode copy() {
        return new ChoiceNode(firstSibling.copy(), secondSibling.copy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHeadFail() {
        return firstSibling.isHeadFail() && secondSibling.isHeadFail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNoFail() {
        return secondSibling.isNoFail() || firstSibling.isNoFail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNullable() {
        return secondSibling.isNullable() || firstSibling.isNullable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getFixedLength(int callCount, int length) {
        int len1 = firstSibling.getFixedLength(callCount, length);
        if (len1 < 0)
            return -1;

        int len2 = secondSibling.getFixedLength(callCount, length);
        if (len1 == len2)
            return len1;

        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void correctAssociativity() {
        Node<?> firstSibling = this.firstSibling;
        while (firstSibling instanceof ChoiceNode) {
            ChoiceNode c = (ChoiceNode) firstSibling;

            Node<?> ff = c.firstSibling;
            c.firstSibling = c.secondSibling;
            c.secondSibling = this.secondSibling;

            this.firstSibling = ff;
            this.secondSibling = firstSibling;

            firstSibling = ff;
        }
    }

}
