package se.fishtank.pegless;

/**
 * Rule definition for building grammars.
 *
 * @author Christer Sandberg
 */
public class Rule {

    /** Rule name. */
    public final String name;

    /** Rule pattern. */
    public final Pattern pattern;

    /**
     * Create a new instance.
     *
     * @param name Rule name.
     * @param pattern Rule pattern.
     */
    public Rule(String name, Pattern pattern) {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("name is null or empty");

        if (pattern == null)
            throw new NullPointerException("pattern must not be null");

        this.name = name;
        this.pattern = pattern;
    }

    /**
     * Create a new rule.
     *
     * @param name Rule name.
     * @param pattern Rule pattern.
     * @return A new rule.
     */
    public static Rule rule(String name, Pattern pattern) {
        return new Rule(name, pattern);
    }

}
