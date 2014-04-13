package se.fishtank.pegless.internal.compiler;

import java.util.ArrayList;
import java.util.BitSet;

import se.fishtank.pegless.internal.ast.*;
import se.fishtank.pegless.internal.util.CharacterSet;

/**
 * Converts a {@link se.fishtank.pegless.internal.ast.Node} to
 * {@link se.fishtank.pegless.internal.compiler.Instructions}.
 *
 * @author Christer Sandberg
 */
public class Emitter {

    final ArrayList<Instruction> instructions = new ArrayList<>();

    /**
     * Emit instructions for the specified node.
     *
     * @param node The node to emit instructions for.
     * @return The instructions emitted.
     */
    public Instructions emit(Node<?> node) {
        emit(node, false, -1, CharacterSet.FULL_SET);
        addInstruction(Opcode.END);
        peephole();
        instructions.trimToSize();

        return new Instructions(instructions);
    }

    private void peephole() {
        int size = instructions.size();

        Instruction instruction;
        for (int i = 0; i < size; ++i) {
            instruction = getInstruction(i);
            switch (instruction.opcode) {
            case CHOICE: case CALL: case COMMIT: case PARTIAL_COMMIT:
            case BACK_COMMIT: case TEST_CHAR: case TEST_ANY:
                jumpToHere(i, finalLabel(i));
                break;
            case TEST_SET:
                jumpToHere(i, finalLabel(i));

                TestSetInstruction testSetInstruction = (TestSetInstruction) instruction;
                instructions.set(i, testSetInstruction.convert());
                break;
            case JMP:
                int finalTarget = finalTarget(i);
                Instruction x = getInstruction(finalTarget);
                switch (x.opcode) {
                case RET: case FAIL: case FAIL_TWICE: case END:
                    instructions.set(i, x);
                    break;
                case COMMIT: case PARTIAL_COMMIT: case BACK_COMMIT:
                    int finalLabel = finalLabel(finalTarget);
                    // Since we're updating the offset we need to make a copy.
                    Instruction copy = new Instruction(x.opcode);
                    copy.offset = x.offset;
                    instructions.set(i, copy);
                    jumpToHere(i, finalLabel);
                    i--; // TODO: Is this really needed?
                    break;
                default:
                    jumpToHere(i, finalTarget);
                    break;
                }

                break;
            default:
                break;
            }
        }
    }

    private void emit(Node<?> node, boolean opt, int tt, BitSet followSet) {
        for (;;) {
            if (node instanceof CharNode) {
                emitChar(((CharNode) node).ch, tt);
                return;
            } else if (node instanceof AnyNode) {
                addInstruction(Opcode.ANY);
                return;
            } else if (node instanceof SetNode) {
                emitCharacterSet(((SetNode) node).characterSet, tt);
                return;
            } else if (node instanceof TrueNode) {
                return;
            } else if (node instanceof FalseNode) {
                addInstruction(Opcode.FAIL);
                return;
            } else if (node instanceof ChoiceNode) {
                emitChoice((ChoiceNode) node, opt, followSet);
                return;
            } else if (node instanceof RepeatNode) {
                emitRep(((RepeatNode) node).getSibling(), opt, followSet);
                return;
            } else if (node instanceof BehindNode) {
                emitBehind((BehindNode) node);
                return;
            } else if (node instanceof NotNode) {
                emitNot(((NotNode) node).getSibling());
                return;
            } else if (node instanceof AndNode) {
                emitAnd(((AndNode) node).getSibling(), tt);
                return;
            } else if (node instanceof ActionNode) {
                emitAction((ActionNode) node, tt);
                return;
            } else if (node instanceof GrammarNode) {
                emitGrammar((GrammarNode) node);
                return;
            } else if (node instanceof CallNode) {
                emitCall((CallNode) node);
                return;
            } else if (node instanceof SeqNode) {
                SeqNode seqNode = (SeqNode) node;
                tt = emitSeq(seqNode, tt, followSet);
                node = seqNode.getSecondSibling();
                continue;
            }

            throw new IllegalStateException("Did not expect node: " + node.getClass());
        }
    }

    private void emitChar(int c, int tt) {
        if (tt >= 0) {
            Instruction instruction = getInstruction(tt);
            if (instruction.opcode == Opcode.TEST_CHAR) {
                if (instruction.aux == c) {
                    addInstruction(Opcode.ANY);
                    return;
                }
            }
        }

        Instruction instruction = new Instruction(Opcode.CHAR);
        instruction.aux = c;
        addInstruction(instruction);
    }

    private void emitCharacterSet(BitSet characterSet, int tt) {
        int[] charSink = new int[1];
        switch (getCharacterSetType(characterSet, charSink)) {
        case CHAR:
            emitChar(charSink[0], tt);
            break;
        case SET:
            if (tt >= 0) {
                Instruction instruction = getInstruction(tt);
                if (instruction instanceof TestSetInstruction) {
                    if (characterSet.equals(((TestSetInstruction) instruction).characterSet)) {
                        addInstruction(Opcode.ANY);
                        return;
                    }
                }
            }

            Instruction instruction = new Instruction(Opcode.SET);
            instruction.matcher = CharacterMatcher.newCharacterMatcher(characterSet);
            addInstruction(instruction);
            break;
        case FAIL:
            addInstruction(Opcode.FAIL);
            break;
        default:
            throw new IllegalArgumentException("Bug alert!");
        }
    }

    private void emitChoice(ChoiceNode choiceNode, boolean opt, BitSet followSet) {
        boolean emptySecondSibling = choiceNode.getSecondSibling() instanceof TrueNode;

        BitSet characterSet = new BitSet();
        int x = getFirstSet(choiceNode.getFirstSibling(), CharacterSet.FULL_SET, characterSet);
        if (choiceNode.getFirstSibling().isHeadFail() || (x == 0 &&
                checkSecondSiblingInChoice(choiceNode.getSecondSibling(), followSet, characterSet))) {
            int test = emitTestSet(characterSet, false);
            int jmp = -1;
            emit(choiceNode.getFirstSibling(), false, test, followSet);

            if (!emptySecondSibling)
                jmp = addInstruction(Opcode.JMP);

            jumpToHere(test);
            emit(choiceNode.getSecondSibling(), opt, -1, followSet);
            jumpToHere(jmp);
        } else if (opt && emptySecondSibling) {
            jumpToHere(addInstruction(Opcode.PARTIAL_COMMIT));
            emit(choiceNode.getFirstSibling(), true, -1, CharacterSet.FULL_SET);
        } else {
            int test = emitTestSet(characterSet, x != 0);
            int choice = addInstruction(Opcode.CHOICE);
            emit(choiceNode.getFirstSibling(), emptySecondSibling, test, CharacterSet.FULL_SET);

            int commit = addInstruction(Opcode.COMMIT);
            jumpToHere(choice);
            jumpToHere(test);
            emit(choiceNode.getSecondSibling(), opt, -1, followSet);
            jumpToHere(commit);
        }
    }

    private boolean checkSecondSiblingInChoice(Node<?> secondSibling, BitSet followSet, BitSet firstSet) {
        BitSet characterSet = new BitSet();
        getFirstSet(secondSibling, followSet, characterSet);

        return !firstSet.intersects(characterSet);
    }

    private int emitTestSet(BitSet characterSet, boolean acceptEmptyString) {
        if (acceptEmptyString)
            return -1;

        int[] charSink = new int[1];
        switch (getCharacterSetType(characterSet, charSink)) {
        case FAIL:
            return addInstruction(Opcode.JMP);
        case ANY:
            return addInstruction(Opcode.TEST_ANY);
        case CHAR:
            Instruction instruction = new Instruction(Opcode.TEST_CHAR);
            instruction.aux = charSink[0];
            return addInstruction(instruction);
        case SET:
            return addInstruction(new TestSetInstruction(characterSet));
        default:
            throw new IllegalStateException("Bug alert!");
        }
    }

    private void emitRep(Node<?> node, boolean opt, BitSet followSet) {
        BitSet characterSet = new BitSet();
        if (CharacterSet.fillCharacterSet(node, characterSet)) {
            Instruction instruction = new Instruction(Opcode.SPAN);
            instruction.matcher = CharacterMatcher.newCharacterMatcher(characterSet);
            addInstruction(instruction);
            return;
        }

        int x = getFirstSet(node, CharacterSet.FULL_SET, characterSet);
        if (node.isHeadFail() || (x == 0 && !characterSet.intersects(followSet))) {
            int test = emitTestSet(characterSet, false);
            emit(node, opt, test, CharacterSet.FULL_SET);

            int jmp = addInstruction(Opcode.JMP);
            jumpToHere(test);
            jumpToHere(jmp, test);
            return;
        }

        int test = emitTestSet(characterSet, x != 0);
        int choice = -1;
        if (opt) {
            jumpToHere(addInstruction(Opcode.PARTIAL_COMMIT));
        } else {
            choice = addInstruction(Opcode.CHOICE);
        }

        int index = instructions.size();
        emit(node, false, -1, CharacterSet.FULL_SET);

        int commit = addInstruction(Opcode.PARTIAL_COMMIT);
        jumpToHere(commit, index);
        jumpToHere(choice);
        jumpToHere(test);
    }

    private void emitBehind(BehindNode behindNode) {
        if (behindNode.n > 0) {
            Instruction instruction = new Instruction(Opcode.BEHIND);
            instruction.aux = behindNode.n;
            addInstruction(instruction);
        }

        emit(behindNode.getSibling(), false, -1, CharacterSet.FULL_SET);
    }

    private void emitNot(Node<?> node) {
        BitSet characterSet = new BitSet();
        int x = getFirstSet(node, CharacterSet.FULL_SET, characterSet);
        int test = emitTestSet(characterSet, x != 0);
        if (node.isHeadFail()) {
            addInstruction(Opcode.FAIL);
        } else {
            int choice = addInstruction(Opcode.CHOICE);
            emit(node, false, -1, CharacterSet.FULL_SET);
            addInstruction(Opcode.FAIL_TWICE);
            jumpToHere(choice);
        }

        jumpToHere(test);
    }

    private void emitAnd(Node<?> node, int tt) {
        int len = node.getFixedLength();
        if (len >= 0 && !Support.hasActions(node)) {
            emit(node, false, tt, CharacterSet.FULL_SET);
            if (len > 0) {
                Instruction instruction = new Instruction(Opcode.BEHIND);
                instruction.aux = len;
                addInstruction(instruction);
            }
        } else {
            int choice = addInstruction(Opcode.CHOICE);
            emit(node, false, tt, CharacterSet.FULL_SET);

            int commit = addInstruction(Opcode.BACK_COMMIT);
            jumpToHere(choice);
            addInstruction(Opcode.FAIL);
            jumpToHere(commit);
        }
    }

    private void emitAction(ActionNode actionNode, int tt) {
        Instruction instruction1 = new Instruction(Opcode.OPEN_ACTION);
        instruction1.action = actionNode.action;
        addInstruction(instruction1);

        emit(actionNode.getSibling(), false, tt, CharacterSet.FULL_SET);

        Instruction instruction2 = new Instruction(Opcode.CLOSE_ACTION);
        addInstruction(instruction2);
    }

    private void emitGrammar(GrammarNode grammarNode) {
        int firstCall = addInstruction(Opcode.CALL);
        int jumpToEnd = addInstruction(Opcode.JMP);

        int start = instructions.size();
        jumpToHere(firstCall);

        ArrayList<Integer> positions = new ArrayList<>();
        for (RuleNode ruleNode : grammarNode.ruleNodes) {
            positions.add(instructions.size());
            emit(ruleNode.getSibling(), false, -1, CharacterSet.FULL_SET);
            addInstruction(Opcode.RET);
        }

        jumpToHere(jumpToEnd);
        correctCalls(positions, start, instructions.size());
    }

    private void correctCalls(ArrayList<Integer> positions, int from, int to) {
        for (int i = from; i < to; ++i) {
            Instruction instruction = instructions.get(i);
            if (instruction.opcode == Opcode.OPEN_CALL) {
                int rule = positions.get(instruction.aux);
                if (rule != from && getInstruction(rule - 1).opcode != Opcode.RET)
                    throw new IllegalStateException("Bug alert!");

                if (instructions.get(finalTarget(i + 1)).opcode == Opcode.RET) {
                    instructions.set(i, new Instruction(Opcode.JMP));
                } else {
                    Instruction call = new Instruction(Opcode.CALL);
                    call.aux = instruction.aux;
                    instructions.set(i, call);
                }

                jumpToHere(i, rule);
            }
        }
    }

    private void emitCall(CallNode callNode) {
        Instruction instruction = new Instruction(Opcode.OPEN_CALL);
        instruction.aux = callNode.getRuleNode().index;
        addInstruction(instruction);
    }

    private int emitSeq(SeqNode seqNode, int tt, BitSet followSet) {
        if (needFollowSet(seqNode.getFirstSibling())) {
            BitSet characterSet = new BitSet();
            getFirstSet(seqNode.getSecondSibling(), followSet, characterSet);
            emit(seqNode.getFirstSibling(), false, tt, characterSet);
        } else {
            emit(seqNode.getFirstSibling(), false, tt, CharacterSet.FULL_SET);
        }

        if (seqNode.getFirstSibling().getFixedLength() != 0)
            return -1;

        return tt;
    }

    private boolean needFollowSet(Node<?> node) {
        for (;;) {
            if (node instanceof ChoiceNode || node instanceof RepeatNode)
                return true;

            if (node instanceof SeqNode) {
                SeqNode seqNode = (SeqNode) node;
                node = seqNode.getSecondSibling();
                continue;
            }

            return false;
        }
    }

    private Opcode getCharacterSetType(BitSet characterSet, int[] charSink) {
        if (characterSet.isEmpty())
            return Opcode.FAIL;

        if (characterSet.cardinality() == 1) {
            charSink[0] = characterSet.nextSetBit(0);
            return Opcode.CHAR;
        }

        int lastChar = characterSet.length() - 1;
        if (lastChar == 0x10FFFF && characterSet.previousClearBit(lastChar) == -1)
            return Opcode.ANY;

        return Opcode.SET;
    }

    private int addInstruction(Opcode opcode) {
        return addInstruction(new Instruction(opcode));
    }

    private int addInstruction(Instruction instruction) {
        int index = instructions.size();
        instructions.add(instruction);

        return index;
    }

    private Instruction getInstruction(int instructionIndex) {
        return instructions.get(instructionIndex);
    }

    private void setOffset(int instructionIndex, int offset) {
        getInstruction(instructionIndex).offset = offset;
    }

    private void jumpToHere(int instructionIndex) {
        jumpToHere(instructionIndex, instructions.size());
    }

    private void jumpToHere(int instructionIndex, int targetIndex) {
        if (instructionIndex >= 0)
            setOffset(instructionIndex, targetIndex - instructionIndex);
    }

    private int target(int instructionIndex) {
        return instructionIndex + getInstruction(instructionIndex).offset;
    }

    private int finalTarget(int instructionIndex) {
        while (getInstruction(instructionIndex).opcode == Opcode.JMP)
            instructionIndex = target(instructionIndex);

        return instructionIndex;
    }

    private int finalLabel(int instructionIndex) {
        return finalTarget(target(instructionIndex));
    }

    private int getFirstSet(Node<?> node, BitSet followSet, BitSet firstSet) {
        for (;;) {
            if (node instanceof CharNode || node instanceof SetNode || node instanceof AnyNode) {
                CharacterSet.fillCharacterSet(node, firstSet);
                return 0;
            }

            if (node instanceof TrueNode) {
                firstSet.clear();
                firstSet.or(followSet);
                return 1;
            }

            if (node instanceof FalseNode) {
                firstSet.clear();
                return 0;
            }

            if (node instanceof ActionNode) {
                int a = getFirstSet(((ActionNode) node).getSibling(), CharacterSet.FULL_SET, firstSet);
                return a == 0 ? 0 : 2;
            }

            if (node instanceof RuleNode || node instanceof CallNode) {
                node = ((UnaryNode) node).getSibling();
                continue;
            }

            if (node instanceof GrammarNode) {
                node = ((GrammarNode) node).ruleNodes.get(0);
                continue;
            }

            if (node instanceof ChoiceNode) {
                ChoiceNode choiceNode = (ChoiceNode) node;
                BitSet choiceSet = new BitSet();
                int c1 = getFirstSet(choiceNode.getFirstSibling(), followSet, firstSet);
                int c2 = getFirstSet(choiceNode.getSecondSibling(), followSet, choiceSet);
                firstSet.or(choiceSet);
                return c1 | c2;
            }

            if (node instanceof SeqNode) {
                SeqNode seqNode = (SeqNode) node;
                if (!seqNode.getFirstSibling().isNullable()) {
                    node = seqNode.getFirstSibling();
                    followSet = CharacterSet.FULL_SET;
                    continue;
                } else {
                    BitSet seqSet = new BitSet();
                    int s2 = getFirstSet(seqNode.getSecondSibling(), followSet, seqSet);
                    int s1 = getFirstSet(seqNode.getFirstSibling(), seqSet, firstSet);
                    if (s1 == 0) {
                        return 0;
                    } else if (((s1 | s2) & 2) != 0) {
                        return 2;
                    } else {
                        return s2;
                    }
                }
            }

            if (node instanceof RepeatNode) {
                getFirstSet(((RepeatNode) node).getSibling(), followSet, firstSet);
                firstSet.or(followSet);
                return 1;
            }

            if (node instanceof AndNode) {
                int a = getFirstSet(((AndNode) node).getSibling(), followSet, firstSet);
                firstSet.and(followSet);
                return a;
            }

            if (node instanceof NotNode) {
                if (CharacterSet.fillCharacterSet(((NotNode) node).getSibling(), firstSet)) {
                    firstSet.flip(0, CharacterSet.FULL_SET.length());
                    return 1;
                }
            }

            if (node instanceof NotNode || node instanceof BehindNode) {
                int x = getFirstSet(((UnaryNode) node).getSibling(), followSet, firstSet);
                firstSet.clear();
                firstSet.or(followSet);
                return x | 1;
            }

            throw new IllegalStateException("Did not expect node: " + node.getClass());
        }
    }

    static class TestSetInstruction extends Instruction {

        final BitSet characterSet;

        TestSetInstruction(BitSet characterSet) {
            super(Opcode.TEST_SET);
            this.characterSet = characterSet;
        }

        Instruction convert() {
            Instruction instruction = new Instruction(opcode);
            instruction.offset = this.offset;
            instruction.matcher = CharacterMatcher.newCharacterMatcher(characterSet);

            return instruction;
        }

    }

}
