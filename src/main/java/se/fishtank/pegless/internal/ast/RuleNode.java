package se.fishtank.pegless.internal.ast;

/**
 * Represents an AST <em>rule</em> node.
 *
 * @author Christer Sandberg
 */
public class RuleNode extends UnaryNode<RuleNode> {

    /** The index for this rule. */
    public int index;

    /** The name of this rule. */
    public final String name;

    /**
     * Create a new <em>rule</em> node.
     *
     * @param index The index for the rule.
     * @param name The name of the rule.
     * @param sibling Sibling
     */
    public RuleNode(int index, String name, Node<?> sibling) {
        this.index = index;
        this.name = name;
        this.sibling = sibling;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RuleNode copy() {
        return new RuleNode(index, name, sibling.copy());
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
        return sibling.isNullable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getFixedLength(int callCount, int length) {
        return sibling.getFixedLength(callCount, length);
    }

}
