package se.fishtank.pegless.internal.compiler;

import java.util.ArrayList;

/**
 * Holds instructions used when matching a pattern against a given subject.
 *
 * @author Christer Sandberg
 */
public class Instructions {

    private final ArrayList<Instruction> instructions;

    Instructions(ArrayList<Instruction> instructions) {
        this.instructions = instructions;
    }

    /**
     * Get the instruction at the specified index.
     *
     * @param index Index for the instruction to get.
     * @return The instruction found at {@code index}.
     */
    public Instruction get(int index) {
        return instructions.get(index);
    }

    /**
     * Returns the total number of instructions available.
     *
     * @return The number of instructions available.
     */
    public int size() {
        return instructions.size();
    }

}
