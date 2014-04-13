package se.fishtank.pegless.internal.ast;

import se.fishtank.pegless.Action;

/**
 * Represents an AST <em>action</em> node.
 *
 * @author Christer Sandberg
 */
public class ActionNode extends UnaryNode<ActionNode> {

    public final Action action;

    /**
     * Create a new <em>action</em> node.
     *
     * @param action The action
     * @param sibling Sibling
     */
    public ActionNode(Action action, Node<?> sibling) {
        this.action = action;
        this.sibling = sibling;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionNode copy() {
        return new ActionNode(action, sibling.copy());
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
        return sibling.isNullable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getFixedLength(int callCount, int length) {
        return -1;
    }

}
