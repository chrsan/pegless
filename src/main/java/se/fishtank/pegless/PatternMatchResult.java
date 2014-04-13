package se.fishtank.pegless;

/**
 * Contains the result of a pattern match operation.
 *
 * @author Christer Sandberg
 */
public class PatternMatchResult {

    /** The subject matched against. */
    private final String subject;

    /** If the pattern was matched successfully or not. */
    private final boolean matched;

    /** Start index of the match (i.e. subject offset). */
    private final int start;

    /** The offset after the last character matched. */
    private final int end;

    /**
     * Create a new match result.
     *
     * @param subject The subject matched against.
     * @param matched If the pattern was matched successfully or not.
     * @param start The start index of the match (i.e. subject offset).
     * @param end The offset after the last character matched.
     */
    PatternMatchResult(String subject, boolean matched, int start, int end) {
        this.subject = subject;
        this.matched = matched;
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the start index of the match (i.e. subject offset).
     *
     * @return Start index
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the offset after the last character matched or {@code -1}
     * if the match failed.
     *
     * @return The offset after the last character matched or {@code -1}.
     */
    public int getEnd() {
        return end;
    }

    /**
     * Returns whether the match was successful or not.
     *
     * @return {@code true} or {@code false}
     */
    public boolean matched() {
        return matched;
    }

    /**
     * Returns the subject matched against.
     *
     * @return The subject matched against.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "PatternMatchResult{" +
                "matched=" + matched +
                ", start=" + start +
                ", end=" + end +
                '}';
    }

}
