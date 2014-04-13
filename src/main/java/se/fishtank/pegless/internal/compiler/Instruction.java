package se.fishtank.pegless.internal.compiler;

import se.fishtank.pegless.Action;

/**
 * A union like container that represents an instruction in the <em>VM</em>.
 *
 * @author Christer Sandberg
 */
public class Instruction {

    /** Give up instruction. */
    public static final Instruction GIVE_UP = new Instruction(Opcode.GIVE_UP);

    /** Opcode for the instruction. */
    public final Opcode opcode;

    /** Auxilliary field (used for characters, look behind, rules etc). */
    public int aux = -1;

    /** The offset for the other instruction. */
    public int offset = 0;

    /** Action */
    public Action action = null;

    /** The character matcher used with set instructions. */
    public CharacterMatcher matcher = null;

    /**
     * Create a new instance.
     *
     * @param opcode The opcode for the instruction to create.
     */
    Instruction(Opcode opcode) {
        this.opcode = opcode;
    }

}
