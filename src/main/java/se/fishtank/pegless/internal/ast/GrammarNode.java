package se.fishtank.pegless.internal.ast;

import java.util.ArrayList;

/**
 * Represents an AST <em>grammar</em> node.
 *
 * @author Christer Sandberg
 */
public class GrammarNode extends Node<GrammarNode> {

    /** Maximum number of rules allowed. */
    public static final int MAX_RULES = 200;

    /** The rules for this grammar. */
    public final ArrayList<RuleNode> ruleNodes;

    /**
     * Create a new <em>grammar</em> node.
     *
     * @param ruleNodes The rules for the grammar.
     */
    public GrammarNode(ArrayList<RuleNode> ruleNodes) {
        if (ruleNodes.isEmpty())
            throw new IllegalArgumentException("No rules");

        this.ruleNodes = ruleNodes;
    }

    @Override
    public GrammarNode copy() {
        ArrayList<RuleNode> rules = new ArrayList<>(ruleNodes.size());
        for (RuleNode ruleNode : ruleNodes)
            rules.add(ruleNode.copy());

        return new GrammarNode(rules);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHeadFail() {
        return ruleNodes.get(0).isHeadFail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNoFail() {
        return ruleNodes.get(0).isNoFail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNullable() {
        return ruleNodes.get(0).isNullable();
    }

    /**
     * Verify this grammar.
     */
    public void verify() {
        RuleNode[] passed = new RuleNode[MAX_RULES];
        for (RuleNode ruleNode : ruleNodes)
            verifyRule(ruleNode.sibling, passed, 0, false);

        for (RuleNode ruleNode : ruleNodes) {
            if (Support.hasPotentialInfiniteLoop(ruleNode.sibling))
                throw new IllegalStateException("Empty loop in rule: " + ruleNode.name);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getFixedLength(int callCount, int length) {
        return ruleNodes.get(0).getFixedLength(callCount, length);
    }

    private boolean verifyRule(Node<?> node, RuleNode[] passed, int numberOfPassed, boolean nullable) {
        for (;;) {
            if (node instanceof CharNode || node instanceof SetNode ||
                    node instanceof AnyNode || node instanceof FalseNode)
                return nullable;

            if (node instanceof TrueNode || node instanceof BehindNode)
                return true;

            if (node instanceof GrammarNode)
                return node.isNullable();

            if (node instanceof ActionNode) {
                node = ((ActionNode) node).sibling;
                continue;
            }

            if (node instanceof CallNode) {
                CallNode callNode = (CallNode) node;
                if (callNode.isOpen())
                    throw new IllegalStateException("Unexpected open call node");

                node = callNode.sibling;
                continue;
            }

            if (node instanceof NotNode || node instanceof AndNode || node instanceof RepeatNode) {
                UnaryNode unaryNode = (UnaryNode) node;
                node = unaryNode.sibling;
                nullable = true;
                continue;
            }

            if (node instanceof SeqNode) {
                SeqNode seqNode = (SeqNode) node;
                if (!verifyRule(seqNode.firstSibling, passed, numberOfPassed, false))
                    return nullable;

                node = seqNode.secondSibling;
                continue;
            }

            if (node instanceof ChoiceNode) {
                ChoiceNode choiceNode = (ChoiceNode) node;
                nullable = verifyRule(choiceNode.firstSibling, passed, numberOfPassed, nullable);
                node = choiceNode.secondSibling;
                continue;
            }

            if (node instanceof RuleNode) {
                RuleNode ruleNode = (RuleNode) node;
                if (numberOfPassed >= MAX_RULES)
                    throw verifyError(passed, numberOfPassed);

                passed[numberOfPassed++] = ruleNode;
                node = ruleNode.sibling;
                continue;
            }

            throw new IllegalStateException("Did not expect node: " + node.getClass());
        }
    }

    private IllegalStateException verifyError(RuleNode[] passed, int numberOfPassed) {
        for (int i = numberOfPassed - 1; i >= 0; --i) {
            for (int j = i - 1; j >= 0; --j) {
                if (passed[i].index == passed[j].index)
                    return new IllegalStateException("Rule may be left recursive: " + passed[i].name);
            }
        }

        return new IllegalStateException("Too many left calls in grammar");
    }

}
