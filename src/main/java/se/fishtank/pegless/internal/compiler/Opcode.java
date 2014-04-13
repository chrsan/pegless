package se.fishtank.pegless.internal.compiler;

/**
 * Opcode for an {@link se.fishtank.pegless.internal.compiler.Instruction}.
 *
 * @author Christer Sandberg
 */
public enum Opcode {

    ANY, CHAR, SET, TEST_ANY, TEST_CHAR, TEST_SET,

    SPAN, BEHIND, RET, END, CHOICE, JMP, CALL,

    OPEN_CALL, COMMIT, PARTIAL_COMMIT, BACK_COMMIT,

    FAIL_TWICE, FAIL, GIVE_UP,

    OPEN_ACTION, CLOSE_ACTION

}
