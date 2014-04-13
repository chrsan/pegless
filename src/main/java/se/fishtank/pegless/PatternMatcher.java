package se.fishtank.pegless;

import java.io.PrintStream;
import java.util.LinkedList;

import se.fishtank.pegless.internal.compiler.Instruction;
import se.fishtank.pegless.internal.compiler.Instructions;
import se.fishtank.pegless.internal.util.Printer;

/**
 * A compiled version of a {@link Pattern}.
 * <p/>
 * Used to match a given subject.
 *
 * @author Christer Sandberg
 */
public class PatternMatcher {

    /** The compiled instructions. */
    final Instructions instructions;

    /** The number of instructions. */
    final int numberOfInstructions;

    /**
     * Create a new instance.
     *
     * @param instructions The instructions for this pattern.
     */
    PatternMatcher(Instructions instructions) {
        this.instructions = instructions;
        this.numberOfInstructions = instructions.size();
    }

    /**
     * Attempt to match this pattern against the given subject string.
     *
     * @param subject The subject to match against this pattern.
     * @return A match result.
     */
    public PatternMatchResult match(String subject) {
        return match(subject, 0);
    }

    /**
     * Attempt to match this pattern against the given subject string
     * starting at the specified offset.
     *
     * @param subject The subject to match against this pattern.
     * @return A match result.
     */
    public PatternMatchResult match(String subject, int offset) {
        if (subject == null || offset < 0 || offset > subject.codePointCount(0, subject.length()))
            throw new IllegalArgumentException("Invalid subject or offset");

        int[] end = new int[1];
        boolean matched = match(subject, offset, end);
        return new PatternMatchResult(subject, matched, offset, end[0]);
    }

    /**
     * Print the instructions for this pattern matcher to
     * the specified {@linkplain java.io.PrintStream print stream}.
     *
     * @param ps Print stream to print to.
     */
    public void print(PrintStream ps) {
        Printer.printInstructions(ps, instructions);
    }

    private boolean match(String subject, int offset, int[] end) {
        int len = subject.codePointCount(offset, subject.length());
        int pos = offset;

        int instructionIndex = 0;

        boolean fail = false;

        LinkedList<ActionEntry> actions = new LinkedList<>();

        LinkedList<StackEntry> stack = new LinkedList<>();
        stack.push(new StackEntry(-1, 0, 0));

        StackEntry entry;

        for (;;) {
            if (fail) {
                fail = false;
                do {
                    entry = stack.pop();
                    pos = entry.pos;
                } while (pos == -1);

                instructionIndex = entry.instructionIndex;
                correctActionEntries(actions, entry.numberOfActions);
            }

            Instruction instruction = instructionIndex == -1 ?
                    Instruction.GIVE_UP : instructions.get(instructionIndex);

            switch (instruction.opcode) {
            case END:
                end[0] = pos;
                return true;
            case GIVE_UP:
                end[0] = pos;
                return false;
            case RET:
                entry = stack.pop();
                instructionIndex = entry.instructionIndex;
                continue;
            case ANY:
                if (pos < len) {
                    ++pos;
                    ++instructionIndex;
                } else {
                    fail = true;
                }

                continue;
            case TEST_ANY:
                if (pos < len) {
                    ++instructionIndex;
                } else {
                    instructionIndex += instruction.offset;
                }

                continue;
            case CHAR: case SET:
                if (pos < len && match(subject.codePointAt(pos), instruction)) {
                    ++pos;
                    ++instructionIndex;
                } else {
                    fail = true;
                }

                continue;
            case TEST_CHAR: case TEST_SET:
                if (pos < len && match(subject.codePointAt(pos), instruction)) {
                    ++instructionIndex;
                } else {
                    instructionIndex += instruction.offset;
                }

                continue;
            case BEHIND:
                int n = instruction.aux;
                if (n > pos - offset) {
                    fail = true;
                } else {
                    pos -= n;
                    ++instructionIndex;
                }

                continue;
            case SPAN:
                for (; pos < len; ++pos) {
                    int c = subject.codePointAt(pos);
                    if (!match(c, instruction))
                        break;
                }

                ++instructionIndex;
                continue;
            case JMP:
                instructionIndex += instruction.offset;
                continue;
            case CHOICE:
                entry = new StackEntry(instructionIndex + instruction.offset, pos, actions.size());
                stack.push(entry);
                ++instructionIndex;
                continue;
            case CALL:
                entry = new StackEntry(instructionIndex + 1, -1, actions.size());
                stack.push(entry);
                instructionIndex += instruction.offset;
                continue;
            case COMMIT:
                stack.pop();
                instructionIndex += instruction.offset;
                continue;
            case PARTIAL_COMMIT:
                entry = stack.peek();
                entry.pos = pos;
                entry.numberOfActions = actions.size();
                instructionIndex += instruction.offset;
                continue;
            case BACK_COMMIT:
                entry = stack.pop();
                pos = entry.pos;
                correctActionEntries(actions, entry.numberOfActions);
                instructionIndex += instruction.offset;
                continue;
            case FAIL_TWICE:
                stack.pop();

                // Fall through
            case FAIL:
                fail = true;
                continue;
            case CLOSE_ACTION:
                ActionEntry actionEntry = actions.pollLast();
                int p = actionEntry.action.match(subject, actionEntry.offset, pos);
                if (p < 0) {
                    fail = true;
                    continue;
                }

                if (p < pos || p > len)
                    throw new IllegalStateException("Invalid position returned from action");

                ++instructionIndex;
                continue;
            case OPEN_ACTION:
                actions.add(new ActionEntry(pos, instruction.action));
                ++instructionIndex;
                continue;
            default:
                throw new IllegalStateException("Bug alert!");
            }
        }
    }

    private void correctActionEntries(LinkedList<ActionEntry> actions, int n) {
        int size = actions.size() - n;
        while (size-- > 0)
            actions.pollLast();
    }

    private boolean match(int c, Instruction instruction) {
        if (instruction.matcher != null)
            return instruction.matcher.match(c);

        return instruction.aux == c;
    }

    static class ActionEntry {

        int offset;

        Action action;

        ActionEntry(int offset, Action action) {
            this.offset = offset;
            this.action = action;
        }

    }

    static class StackEntry {

        int instructionIndex;

        int pos;

        int numberOfActions;

        StackEntry(int instructionIndex, int pos, int numberOfActions) {
            this.instructionIndex = instructionIndex;
            this.pos = pos;
            this.numberOfActions = numberOfActions;
        }

    }

}
