package se.fishtank.pegless.internal.util;

import java.io.PrintStream;
import java.util.BitSet;

import se.fishtank.pegless.internal.ast.*;
import se.fishtank.pegless.internal.compiler.Instruction;
import se.fishtank.pegless.internal.compiler.Instructions;

/**
 * Contains debug print methods.
 *
 * @author Christer Sandberg
 */
public abstract class Printer {

    /**
     * Print {@code instructions} to the specified {@linkplain java.io.PrintStream print stream}.
     *
     * @param ps Print stream for instructions output.
     * @param instructions Instructions to print.
     */
    public static void printInstructions(PrintStream ps, Instructions instructions) {
        int index = 0;
        int size = instructions.size();
        while (index < size) {
            Instruction instruction = instructions.get(index);

            ps.printf("%02d: %s ", index, instruction.opcode);
            switch (instruction.opcode) {
            case CHAR:
                if (Character.isWhitespace(instruction.aux)) {
                    ps.printf("(%x)", instruction.aux);
                } else {
                    ps.printf("'%s'", CharacterSet.toString(instruction.aux));
                }

                break;
            case TEST_CHAR:
                if (Character.isWhitespace(instruction.aux)) {
                    ps.printf("(%x)", instruction.aux);
                } else {
                    ps.printf("'%s' ", CharacterSet.toString(instruction.aux));
                }

                printJump(ps, index, instruction.offset);
                break;
            case SET: case SPAN:
                ps.printf("[%s]", instruction.matcher);
                break;
            case TEST_SET:
                ps.printf("[%s]", instruction.matcher);
                printJump(ps, index, instruction.offset);
                break;
            case OPEN_CALL:
                ps.printf("-> %d", instruction.offset);
                break;
            case BEHIND:
                ps.printf("%d", instruction.aux);
                break;
            case JMP: case CALL: case COMMIT: case CHOICE:
            case PARTIAL_COMMIT: case BACK_COMMIT:
            case TEST_ANY:
                printJump(ps, index, instruction.offset);
                break;
            }

            ps.println();
            index += 1;
        }
    }

    /**
     * Print {@code node} to the specified {@linkplain java.io.PrintStream print stream}.
     *
     * @param ps Print stream for tree output.
     * @param node Node to print.
     */
    public static void printNode(PrintStream ps, Node<?> node) {
        printNode(ps, node, 0);
    }

    private static void printJump(PrintStream ps, int instructionIndex, int offset) {
        ps.printf("-> %d", instructionIndex + offset);
    }

    private static void printNode(PrintStream ps, Node<?> node, int indent) {
        indent(ps, indent);

        if (node instanceof AnyNode) {
            ps.println("any");
        } else if (node instanceof TrueNode) {
            ps.println("true");
        } else if (node instanceof FalseNode) {
            ps.println("false");
        } else if (node instanceof CharNode) {
            ps.printf("char %s%n", CharacterSet.toString(((CharNode) node).ch));
        } else if (node instanceof SetNode) {
            ps.print("set ");
            printCharacterSet(ps, ((SetNode) node).characterSet);
            ps.println();
        } else if (node instanceof AndNode) {
            ps.println("and");
            printNode(ps, ((AndNode) node).getSibling(), indent + 2);
        } else if (node instanceof NotNode) {
            ps.println("not");
            printNode(ps, ((NotNode) node).getSibling(), indent + 2);
        } else if (node instanceof CallNode) {
            CallNode callNode = (CallNode) node;
            if (callNode.isOpen()) {
                ps.printf("opencall index: %d%n", callNode.ruleIndex);
            } else {
                ps.printf("call index: %d, name: '%s'%n", callNode.ruleIndex, callNode.getRuleNode().name);
            }
        } else if (node instanceof BehindNode) {
            BehindNode behindNode = (BehindNode) node;
            ps.printf("behind %d%n", behindNode.n);
            printNode(ps, behindNode.getSibling(), indent + 2);
        } else if (node instanceof ActionNode) {
            ActionNode actionNode = (ActionNode) node;
            ps.println("action");
            printNode(ps, actionNode.getSibling(), indent + 2);
        } else if (node instanceof RepeatNode) {
            ps.println("rep");
            printNode(ps, ((RepeatNode) node).getSibling(), indent + 2);
        } else if (node instanceof RuleNode) {
            RuleNode ruleNode = (RuleNode) node;
            ps.printf("rule index: %d, name = '%s'%n", ruleNode.index, ruleNode.name);
            printNode(ps, ruleNode.getSibling(), indent + 2);
        } else if (node instanceof GrammarNode) {
            GrammarNode grammarNode = (GrammarNode) node;
            ps.printf("grammar %d%n", grammarNode.ruleNodes.size());
            for (RuleNode ruleNode : grammarNode.ruleNodes)
                printNode(ps, ruleNode, indent + 2);
        } else if (node instanceof ChoiceNode) {
            ChoiceNode choiceNode = (ChoiceNode) node;
            ps.println("choice");
            printNode(ps, choiceNode.getFirstSibling(), indent + 2);
            printNode(ps, choiceNode.getSecondSibling(), indent + 2);
        } else if (node instanceof SeqNode) {
            SeqNode seqNode = (SeqNode) node;
            ps.println("seq");
            printNode(ps, seqNode.getFirstSibling(), indent + 2);
            printNode(ps, seqNode.getSecondSibling(), indent + 2);
        } else {
            throw new UnsupportedOperationException("Don't know how to print node: " + node.getClass());
        }
    }

    private static void printCharacterSet(PrintStream ps, BitSet characterSet) {
        ps.print('[');

        for (int i = characterSet.nextSetBit(0); i >= 0; i = characterSet.nextSetBit(i + 1)) {
            int j = characterSet.nextClearBit(i);
            if ((j - i) > 1) {
                ps.printf("(%h-%h)", i, j - 1);
                i = j;
            } else {
                ps.printf("(%h)", i);
            }
        }

        ps.print(']');
    }

    private static void indent(PrintStream ps, int indent) {
        while (indent-- >= 0)
            ps.print(' ');
    }

}
