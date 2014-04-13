package se.fishtank.pegless.internal.ast;

/**
 * Support methods for AST nodes.
 *
 * @author Christer Sandberg
 */
public abstract class Support {

    /**
     * Correct rule indices for the specified node.
     *
     * @param node The node to correct rule indices for.
     * @param n The number to increment the rule index by.
     */
    public static void correctRuleIndices(Node<?> node, int n) {
        if (n == 0)
            return;

        for (;;) {
            if (node instanceof CallNode) {
                ((CallNode) node).ruleIndex += n;
            } else if (node instanceof RuleNode) {
                ((RuleNode) node).index += n;
            }

            if (node instanceof UnaryNode) {
                node = ((UnaryNode) node).sibling;
            } else if (node instanceof BinaryNode) {
                BinaryNode x = (BinaryNode) node;
                correctRuleIndices(x.firstSibling, n);

                node = x.secondSibling;
            } else {
                return;
            }
        }
    }

    /**
     * Returns whether the specified node has actions.
     *
     * @param root The node to check.
     * @return {@code true} or {@code false}
     */
    public static boolean hasActions(Node<?> root) {
        Node<?> node = root;
        for (;;) {
            if (node instanceof ActionNode)
                return true;

            if (node instanceof UnaryNode) {
                node = ((UnaryNode) node).sibling;
                continue;
            }

            if (node instanceof BinaryNode) {
                BinaryNode n = (BinaryNode) node;
                if (hasActions(n))
                    return true;

                node = n.secondSibling;
                continue;
            }

            return false;
        }
    }

    /**
     * Returns whether the specified node has a potential infinite loop.
     *
     * @return {@code true} or {@code false}
     */
    public static boolean hasPotentialInfiniteLoop(Node<?> node) {
        for (;;) {
            if (node instanceof RepeatNode && ((RepeatNode) node).sibling.isNullable())
                return true;

            if (node instanceof GrammarNode)
                return false; // Grammars have already been checked

            if (node instanceof UnaryNode) {
                node = ((UnaryNode) node).sibling;
            } else if (node instanceof BinaryNode) {
                BinaryNode n = (BinaryNode) node;
                if (hasPotentialInfiniteLoop(n.firstSibling))
                    return true;

                node = n.secondSibling;
            } else {
                return false;
            }
        }
    }


}
