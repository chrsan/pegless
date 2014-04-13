package se.fishtank.pegless;

/**
 * Represents a match time action.
 * <p/>
 * A match time action is called immediately when a match occurs.
 *
 * @author Christer Sandberg
 */
public interface Action {

    /**
     * Called when a match occurs.
     * <p/>
     * The new position must be {@code >= position} and {@code <= subject.length()}
     * unless a negative position is returned which means that the match should fail.
     *
     * @param subject The subject being matched against.
     * @param offset The position before the match.
     * @param position The current position after the match.
     * @return The new position to match from or a negative position to fail.
     */
    public int match(String subject, int offset, int position);

}
