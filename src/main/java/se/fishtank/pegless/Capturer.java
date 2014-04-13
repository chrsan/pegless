package se.fishtank.pegless;

import java.util.*;

/**
 * An {@linkplain se.fishtank.pegless.Action action}
 * that collects {@linkplain se.fishtank.pegless.Capture captures}.
 *
 * @author Christer Sandberg
 */
public class Capturer implements Action {

    /** The collected captures. */
    private final LinkedList<Capture> captures = new LinkedList<>();

    /**
     * Returns a pattern that is captured by this capturer.
     *
     * @param pattern The pattern to capture.
     * @return A new pattern.
     */
    public Pattern capture(Pattern pattern) {
        return Pattern.action(pattern, this);
    }

    /**
     * Returns the collected captures.
     *
     * @return The collected captures.
     */
    public Capture[] captures() {
        Capture[] array = new Capture[captures.size()];

        int index = 0;
        for (Capture capture : captures)
            array[index++] = capture;

        Arrays.sort(array, new Comparator<Capture>() {
            @Override
            public int compare(Capture a, Capture b) {
                return a.compareTo(b);
            }
        });

        return array;
    }

    /**
     * Clear this capturer by removing all collected captures.
     */
    public void clear() {
        captures.clear();
    }

    /**
     * Returns the last capture.
     *
     * @return The last capture.
     */
    public Capture peek() {
        return captures.peek();
    }

    /**
     * Removes and returns the last capture.
     *
     * @return The last capture.
     */
    public Capture pop() {
        return captures.pop();
    }

    /**
     * Returns the number of collected captures.
     *
     * @return The number of captures collected so far.
     */
    public int size() {
        return captures.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int match(String subject, int offset, int position) {
        captures.addFirst(new Capture(offset, subject.substring(offset, position)));
        return position;
    }

}
