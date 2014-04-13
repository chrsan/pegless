package se.fishtank.pegless.internal.ast;

/**
 * Represents an AST <em>open call</em> or <em>call</em> node.
 * <p/>
 * A call node is <em>open</em> until it's associated with a rule.
 *
 * @author Christer Sandberg
 */
public class CallNode extends UnaryNode<CallNode> {

    /** The index for the rule that this node is associated with. */
    public int ruleIndex = 0;

    /**
     * Returns whether this node represents an <em>open call</em>.
     * <p/>
     * A call node is <em>open</em> until it's associated with a rule.
     *
     * @return {@code true} or {@code false}
     */
    public boolean isOpen() {
        return sibling == null;
    }

    /**
     * Returns the associated rule node.
     *
     * @return The rule node or {@code null}
     */
    public RuleNode getRuleNode() {
        if (isOpen())
            return null;

        return (RuleNode) sibling;
    }

    /**
     * Set the associated rule node.
     *
     * @param ruleNode The rule node to set.
     */
    public void setRuleNode(RuleNode ruleNode) {
        // TODO: Is this really needed? Are they always the same?
        ruleIndex = ruleNode.index;
        sibling = ruleNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CallNode copy() {
        CallNode node = new CallNode();
        node.ruleIndex = ruleIndex;
        node.sibling = sibling;

        return node;
    }

    @Override
    public boolean isHeadFail() {
        if (isOpen())
            throw new IllegalStateException("Can not check for an open call");

        return sibling.isHeadFail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNoFail() {
        return !isOpen() && sibling.isNoFail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNullable() {
        return !isOpen() && sibling.isNullable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getFixedLength(int callCount, int length) {
        if (isOpen())
            return -1;

        if (callCount++ >= 200)
            return -1;

        return sibling.getFixedLength(callCount, length);
    }

}
