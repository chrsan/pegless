package se.fishtank.pegless;

import java.util.HashMap;

/**
 * An {@linkplain se.fishtank.pegless.Action action} that captures matched
 * substrings that can be retrieved by name.
 *
 * @author Christer Sandberg
 */
public class NamedCapturer {

    /** The collected matches. */
    private final HashMap<String, String> matches = new HashMap<>();

    /**
     * Returns a pattern that captures the matched substring saving it
     * for later retrieval by the specified name.
     *
     * @param name The name for the matched substring.
     * @param pattern The pattern to capture.
     * @return A new pattern.
     */
    public Pattern capture(final String name, Pattern pattern) {
        return Pattern.action(pattern, new Action() {
            @Override
            public int match(String subject, int offset, int position) {
                matches.put(name, subject.substring(offset, position));
                return position;
            }
        });
    }

    /**
     * Clear this capturer by removing all matched substrings.
     */
    public void clear() {
        matches.clear();
    }

    /**
     * Returns the captured matched substring for the specified name.
     *
     * @param name The name for the matched substring.
     * @return The captured matched substring or {@code null}
     */
    public String get(String name) {
        return matches.get(name);
    }

}
