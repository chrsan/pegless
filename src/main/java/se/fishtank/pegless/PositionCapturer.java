package se.fishtank.pegless;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * An {@linkplain se.fishtank.pegless.Action action} that captures match positions.
 *
 * @author Christer Sandberg
 */
public class PositionCapturer implements Action {

    /** The collected positions. */
    private final LinkedList<Integer> positions = new LinkedList<>();

    /**
     * Returns a pattern that captures the current position
     * with this capturer.
     *
     * @return A new pattern.
     */
    public Pattern capture() {
        return Pattern.action(Pattern.SUCCEED, this);
    }

    /**
     * Returns the collected match positions.
     *
     * @return The collected match positions.
     */
    public int[] positions() {
        int[] array = new int[positions.size()];

        int index = 0;
        for (int pos : positions)
            array[index++] = pos;

        Arrays.sort(array);
        return array;
    }

    /**
     * Clear this capturer by removing all collected positions.
     */
    public void clear() {
        positions.clear();
    }

    /**
     * Returns the number of collected positions.
     *
     * @return The number of positions collected so far.
     */
    public int size() {
        return positions.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int match(String subject, int offset, int position) {
        positions.addFirst(position);
        return position;
    }

}
