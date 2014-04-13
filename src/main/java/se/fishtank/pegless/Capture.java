package se.fishtank.pegless;

/**
 * A captured match.
 *
 * @author Christer Sandberg
 */
public class Capture implements Comparable<Capture> {

    /** The offset in the subject string where the match occurred. */
    public final int offset;

    /** The captured match value. */
    public final String value;

    /**
     * Create a new capture.
     *
     * @param offset The offset in the subject string where the match occurred.
     * @param value The captured match value.
     */
    public Capture(int offset, String value) {
        this.offset = offset;
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Capture other) {
        return offset - other.offset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Capture{" +
                "offset=" + offset +
                ", value='" + value + '\'' +
                '}';
    }

}
