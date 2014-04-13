package se.fishtank.pegless;

import java.io.PrintStream;
import java.util.*;

import se.fishtank.pegless.internal.ast.*;
import se.fishtank.pegless.internal.compiler.Emitter;
import se.fishtank.pegless.internal.compiler.Instructions;
import se.fishtank.pegless.internal.util.CharacterSet;
import se.fishtank.pegless.internal.util.Printer;

/**
 * Represents a pattern.
 * <p/>
 * Use this class and its methods to create a patterns.
 *
 * @author Christer Sandberg
 */
public class Pattern {

    /** Represents a pattern that always fail. */
    public static final Pattern FAIL = new Pattern(FalseNode.SINGLETON);

    /** Represents a pattern that always succeed. */
    public static final Pattern SUCCEED = new Pattern(TrueNode.SINGLETON);

    /** The underlying root node. */
    final Node root;

    /** Referenced rule names. */
    final ArrayList<String> refs;

    /**
     * Create a new pattern.
     *
     * @param root The root node for the pattern.
     */
    Pattern(Node root) {
        this(root, null);
    }

    /**
     * Create a new pattern.
     *
     * @param root The root node for the pattern.
     * @param refs The referenced rule names for the pattern.
     */
    Pattern(Node root, ArrayList<String> refs) {
        this.root = root;
        this.refs = refs;
    }

    /**
     * Compile this pattern into a pattern matcher.
     *
     * @return A pattern matcher.
     */
    public PatternMatcher compile() {
        Node<?> node = root.copy();
        finalFix(node, null, refs);

        Emitter emitter = new Emitter();
        Instructions instructions = emitter.emit(node);

        return new PatternMatcher(instructions);
    }

    /**
     * Print this pattern to the specified {@linkplain java.io.PrintStream print stream}.
     *
     * @param ps Print stream to print to.
     */
    public void print(PrintStream ps) {
        print(ps, false);
    }

    /**
     * Print this pattern to the specified {@linkplain java.io.PrintStream print stream}.
     *
     * @param ps Print stream to print to.
     * @param optimized Whether to print in optimized form or not.
     */
    public void print(PrintStream ps, boolean optimized) {
        if (refs != null) {
            ps.print('[');

            int i = 0;
            for (String ref : refs) {
                if (i != 0)
                    ps.print(", ");

                ps.printf("%d = %s", i, ref);
                ++i;
            }

            ps.println(']');
        }

        Node<?> node = root;
        if (optimized) {
            node = node.copy();
            finalFix(node, null, refs);
        }

        Printer.printNode(ps, node);
    }

    /**
     * Create a pattern from a string.
     * <p/>
     * Represents a pattern that matches the string literally.
     * <p/>
     * Note: A {@code null} or empty string always matches.
     *
     * @param str A string
     * @return A new pattern.
     */
    public static Pattern str(String str) {
        if (str == null || str.isEmpty())
            return new Pattern(TrueNode.SINGLETON);

        int len = str.codePointCount(0, str.length());
        if (len == 1)
            return new Pattern(new CharNode(str.codePointAt(0)));

        int i = len - 3;
        SeqNode root = new SeqNode(new CharNode(str.codePointAt(len - 2)), new CharNode(str.codePointAt(len - 1)));
        while (i >= 0)
            root = new SeqNode(new CharNode(str.codePointAt(i--)), root);

        return new Pattern(root);
    }

    /**
     * Create a pattern from a character.
     * <p/>
     * Represents a pattern that matches the character literally.
     *
     * @param c A character
     * @return A new pattern.
     */
    public static Pattern ch(char c) {
        return str(String.valueOf(c));
    }

    /**
     * Create a pattern from a character.
     * <p/>
     * Represents a pattern that matches the character literally.
     *
     * @see java.lang.Character#toChars(int)
     *
     * @param c A character
     * @return A new pattern.
     */
    public static Pattern ch(int c) {
        return str(String.valueOf(Character.toChars(c)));
    }

    /**
     * Create a pattern from a number.
     * <p/>
     * If {@code n} is {@code 0} it represents a pattern that always succeed.
     * <br/>
     * If {@code n} is a non-negative number it represents a pattern that matches
     * exactly {@code n} characters.
     * <br/>
     * If {@code n} is a negative number it represents a pattern that succeed only
     * if the input string has less than {@code n} characters left.
     *
     * @param n Number
     * @return A new pattern.
     */
    public static Pattern n(int n) {
        switch (n) {
        case 0:
            return new Pattern(TrueNode.SINGLETON);
        case 1:
            return new Pattern(AnyNode.SINGLETON);
        case -1:
            return new Pattern(new NotNode(AnyNode.SINGLETON));
        }

        SeqNode root = new SeqNode(AnyNode.SINGLETON, AnyNode.SINGLETON);

        int x = (n > 0 ? n : -n) - 2;
        for (int i = 0; i < x; ++i)
            root = new SeqNode(AnyNode.SINGLETON, root);

        if (n > 0)
            return new Pattern(root);

        return new Pattern(new NotNode(root));
    }

    /**
     * Create a pattern from some ranges.
     * <p/>
     * Represents a pattern that matches any single character belonging to one of
     * the given ranges. Each range is a string {@code xy} of length 2 representing
     * all characters with a code between the codes {@code x} and {@code y} (both inclusive).
     * <p/>
     * Note: If {@code ranges} is {@code null} or empty this represents a pattern that always fail.
     *
     * @param ranges An array of ranges.
     * @return A new pattern.
     */
    public static Pattern range(String... ranges) {
        BitSet characterSet = new BitSet();
        if (ranges != null) {
            for (String range : ranges) {
                int length = range.codePointCount(0, range.length());
                if (length != 2)
                    throw new IllegalArgumentException("A range must have two characters: " + range);

                int x = range.codePointAt(0);
                int y = range.codePointAt(1);
                for (int i = x; i <= y; ++i)
                    characterSet.set(i);
            }
        }

        return new Pattern(new SetNode(characterSet));
    }

    /**
     * Create a pattern from a set of characters.
     * <p/>
     * Represents a pattern that matches any single character that appears in the given string.
     * <p/>
     * Note: If {@code str} is {@code null} or empty this represents a pattern that always fail.
     *
     * @param str A set of characters.
     * @return A new pattern.
     */
    public static Pattern set(String str) {
        BitSet characterSet = new BitSet();
        if (str != null && !str.isEmpty()) {
            int len = str.codePointCount(0, str.length());
            for (int i = 0; i < len; ++i)
                characterSet.set(str.codePointAt(i));
        }

        return new Pattern(new SetNode(characterSet));
    }

    /**
     * Returns a pattern that represents a pattern that matches only if
     * the input string is matched by {@code pattern}, but without consuming
     * any input, independently of success or failure.
     *
     * @param pattern A pre-existing pattern.
     * @return A new pattern.
     */
    public static Pattern test(Pattern pattern) {
        return new Pattern(new AndNode(pattern.root.copy()), pattern.refs);
    }

    /**
     * Returns a pattern that represents a pattern that matches only if
     * the input string is <strong>not</strong> matched by {@code pattern},
     * but without consuming any input, independently of success or failure.
     *
     * @param pattern A pre-existing pattern.
     * @return A new pattern.
     */
    public static Pattern testNot(Pattern pattern) {
        return new Pattern(new NotNode(pattern.root.copy()), pattern.refs);
    }

    /**
     * Same as {@link #behind(Pattern)} specifying this instance as argument.
     *
     * @return A new pattern.
     */
    public Pattern behind() {
        return behind(this);
    }

    /**
     * Returns a pattern that represents a pattern that matches only if
     * the input string at the current position is preceded by what is matched
     * by {@code pattern}, but without consuming any input, independently of success
     * or failure. The pattern represented by {@code pattern} must only match strings
     * with a fixed length and it cannot contain any actions.
     *
     * @param pattern A pre-existing pattern.
     * @return A new pattern.
     */
    public static Pattern behind(Pattern pattern) {
        int len = pattern.root.getFixedLength();
        if (len <= 0)
            throw new IllegalArgumentException("The pattern may not have a fixed length");

        if (Support.hasActions(pattern.root))
            throw new IllegalArgumentException("The pattern can not have actions");

        return new Pattern(new BehindNode(len, pattern.root.copy()), pattern.refs);
    }

    /**
     * Same as {@link #choice(Pattern, Pattern, Pattern...)} specifying this instance as
     * the first argument and {@code pattern} as the second.
     *
     * @return A new weaver.
     */
    public Pattern or(Pattern pattern) {
        return choice(this, pattern);
    }

    /**
     * Returns a pattern that represents a pattern equivalent to an ordered choice
     * between what's matched by the specified patterns.
     * <p/>
     * It tries them in order with no backtracking one one of them succeeds.
     *
     * @param pattern1 A pre-existing pattern.
     * @param pattern2 A pre-existing pattern.
     * @return A new pattern.
     */
    public static Pattern choice(Pattern pattern1, Pattern pattern2) {
        BitSet characterSet1 = CharacterSet.getCharacterSet(pattern1.root);
        BitSet characterSet2 = CharacterSet.getCharacterSet(pattern2.root);
        if (characterSet1 != null && characterSet2 != null) {
            characterSet1.or(characterSet2);
            return new Pattern(new SetNode(characterSet1));
        }

        if (pattern1.root.isNoFail() || pattern2.root instanceof FalseNode)
            return new Pattern(pattern1.root.copy(), pattern1.refs);

        if (pattern1.root instanceof FalseNode)
            return new Pattern(pattern2.root.copy(), pattern2.refs);

        ChoiceNode root = new ChoiceNode(pattern1.root.copy(), pattern2.root.copy());
        if (pattern1.refs == null && pattern2.refs == null)
            return new Pattern(root);

        ArrayList<String> refs = new ArrayList<>();

        int n = copyRefs(refs, pattern1, pattern2);
        Support.correctRuleIndices(root.getSecondSibling(), n);

        return new Pattern(root, refs);
    }

    /**
     * Returns a pattern that represents a pattern equivalent to an ordered choice
     * between what's matched by the specified patterns.
     * <p/>
     * It tries them in order with no backtracking one one of them succeeds.
     *
     * @param pattern1 A pre-existing pattern.
     * @param pattern2 A pre-existing pattern.
     * @param patterns Optional more pre-existing patterns.
     * @return A new pattern.
     */
    public static Pattern choice(Pattern pattern1, Pattern pattern2, Pattern... patterns) {
        Pattern result = choice(pattern1, pattern2);
        for (Pattern pattern : patterns)
            result = choice(result, pattern);

        return result;
    }

    /**
     * Same as {@link #diff(Pattern, Pattern)} specifying this instance as
     * the first argument and {@code pattern} as the second.
     *
     * @return A new pattern.
     */
    public Pattern diff(Pattern pattern) {
        return diff(this, pattern);
    }

    /**
     * Returns a pattern that represents a pattern equivalent to {@code !x2 x1}.
     * It asserts that the input does not match what {@code pattern2} is matching
     * and then what {@code pattern1} is matching.
     *
     * @param pattern1 A pre-existing pattern.
     * @param pattern2 A pre-existing pattern.
     * @return A new pattern.
     */
    public static Pattern diff(Pattern pattern1, Pattern pattern2) {
        BitSet characterSet1 = CharacterSet.getCharacterSet(pattern1.root);
        BitSet characterSet2 = CharacterSet.getCharacterSet(pattern2.root);
        if (characterSet1 != null && characterSet2 != null) {
            characterSet1.andNot(characterSet2);
            return new Pattern(new SetNode(characterSet1));
        }

        SeqNode root = new SeqNode(new NotNode(pattern2.root.copy()), pattern1.root.copy());
        if (pattern1.refs == null && pattern2.refs == null)
            return new Pattern(root);

        ArrayList<String> refs = new ArrayList<>();

        int n = copyRefs(refs, pattern1, pattern2);
        Support.correctRuleIndices(root.getFirstSibling(), n);

        return new Pattern(root, refs);
    }

    /**
     * Same as {@link #seq(Pattern, Pattern, Pattern...)} specifying this instance as
     * the first argument and {@code pattern} as the second.
     *
     * @return A new pattern.
     */
    public Pattern then(Pattern pattern) {
        return seq(this, pattern);
    }

    /**
     * Returns a pattern that represents a pattern that matches what the
     * specified patterns matches in order ({@code pattern2} matches where
     * {@code pattern1} finished etc).
     *
     * @param pattern1 A pre-existing pattern.
     * @param pattern2 A pre-existing pattern.
     * @return A new pattern.
     */
    public static Pattern seq(Pattern pattern1, Pattern pattern2) {
        if (pattern1.root == FalseNode.SINGLETON || pattern2.root == TrueNode.SINGLETON)
            return new Pattern(pattern1.root.copy(), pattern1.refs);

        if (pattern1.root == TrueNode.SINGLETON)
            return new Pattern(pattern2.root.copy(), pattern2.refs);

        SeqNode root = new SeqNode(pattern1.root.copy(), pattern2.root.copy());
        if (pattern1.refs == null && pattern2.refs == null)
            return new Pattern(root);

        ArrayList<String> refs = new ArrayList<>();

        int n = copyRefs(refs, pattern1, pattern2);
        Support.correctRuleIndices(root.getSecondSibling(), n);

        return new Pattern(root, refs);
    }

    /**
     * Returns a pattern that represents a pattern that matches what the
     * specified patterns matches in order ({@code pattern2} matches where
     * {@code pattern1} finished etc).
     *
     * @param pattern1 A pre-existing pattern.
     * @param pattern2 A pre-existing pattern.
     * @param patterns Optional more pre-existing patterns.
     * @return A new pattern.
     */
    public static Pattern seq(Pattern pattern1, Pattern pattern2, Pattern... patterns) {
        Pattern result = seq(pattern1, pattern2);
        for (Pattern pattern : patterns)
            result = seq(result, pattern);

        return result;
    }

    /**
     * Same as {@link #repeat(Pattern, int)} specifying this instance as
     * the first argument.
     *
     * @return A new pattern.
     */
    public Pattern repeat(int n) {
        return repeat(this, n);
    }

    /**
     * Returns a pattern that represents a pattern repetition of what's
     * matched by {@code pattern}.
     * <p/>
     * If {@code n} is non-negative it matches {@code n} or more occurrences
     * of whatever {@code pattern} matches.
     * <br/>
     * If {@code n} is negative it matches at most {@code n} occurrences of
     * whatever {@code pattern} matches.
     * <p/>
     * A value for {@code n} of {@code 0} can be thought of as {@code p*},
     * {@code 1} as {@code p+} and {@code -1} as {@code p?}.
     * <p/>
     * In all cases the resulting pattern (or pattern) is greedy with no
     * backtracking. That is, it matches only the longest possible sequence
     * of matches for {@code pattern}.
     *
     * @param pattern A pre-existing pattern.
     * @param n A number
     * @return A new pattern.
     */
    public static Pattern repeat(Pattern pattern, int n) {
        if (n >= 0) {
            if (pattern.root.isNullable())
                throw new IllegalArgumentException("Loop body may accept the empty string");

            RepeatNode repeatNode = new RepeatNode(pattern.root.copy());
            if (n == 0)
                return new Pattern(repeatNode, pattern.refs);

            SeqNode seqNode = new SeqNode(pattern.root.copy(), repeatNode);
            while (n-- > 1)
                seqNode = new SeqNode(pattern.root.copy(), seqNode);

            return new Pattern(seqNode, pattern.refs);
        }

        Node<?> root = null;
        for (int i = -n; i > 1; --i) {
            if (root == null) {
                root = new SeqNode(pattern.root.copy(), new ChoiceNode(pattern.root.copy(), TrueNode.SINGLETON));
            } else {
                root = new SeqNode(pattern.root.copy(), new ChoiceNode(root, TrueNode.SINGLETON));
            }
        }

        if (root == null)
            root = pattern.root.copy();

        return new Pattern(new ChoiceNode(root, TrueNode.SINGLETON), pattern.refs);
    }

    /**
     * Same as {@link #times(Pattern, int, boolean)} specifying this instance as
     * the first argument and {@code false} for the {@code grammar} argument.
     *
     * @return A new pattern.
     */
    public Pattern times(int n) {
        return times(this, n, false);
    }

    /**
     * Returns a pattern that represents a pattern repetition of what's
     * matched by {@code pattern} <strong>exactly</strong> {@code n} times.
     * <p/>
     * The {@code grammar} argument specifies if the {@code pattern} should
     * be repeated using a grammar. This could be a good choice for a complex
     * pattern. For simple patterns it might be better to not use a grammar.
     *
     * @param pattern A pre-existing pattern.
     * @param n A positive number {@code > 0}.
     * @param grammar If the pattern should be repeated using a grammar.
     * @return A new pattern.
     */
    public static Pattern times(Pattern pattern, int n, boolean grammar) {
        if (n <= 0)
            throw new IllegalArgumentException("n <= 0");

        if (n == 1)
            return pattern;

        if (!grammar) {
            if (pattern.root.isNullable())
                throw new IllegalArgumentException("Loop body may accept the empty string");

            SeqNode seqNode = new SeqNode(pattern.root.copy(), pattern.root.copy());
            while (n-- > 2)
                seqNode = new SeqNode(pattern.root.copy(), seqNode);

            return new Pattern(seqNode, pattern.refs);
        }

        Pattern ref = ref("p");
        Pattern refs = seq(ref, ref);
        for (int i = 2; i < n; ++i)
            refs = seq(refs, ref);

        return grammar(new Rule("c", refs), new Rule("p", pattern));
    }

    /**
     * Returns a pattern that calls the specified action immediately when a match occurs.
     *
     * @param pattern A pre-existing pattern.
     * @param action The action to call when a match occurs.
     * @return A new pattern.
     */
    public static Pattern action(Pattern pattern, Action action) {
        return new Pattern(new ActionNode(action, pattern.root.copy()), pattern.refs);
    }

    /**
     * Returns a pattern that represents a nonterminal for a grammar.
     * Refers to the rule with the specified rule name.
     *
     * @param ruleName Rule name
     * @return A new pattern.
     */
    public static Pattern ref(String ruleName) {
        if (ruleName == null || ruleName.isEmpty())
            throw new IllegalArgumentException("ruleName must not be null or empty");

        ArrayList<String> refs = new ArrayList<>(1);
        refs.add(ruleName);

        return new Pattern(new CallNode(), refs);
    }

    /**
     * Returns a pattern that represents a grammar.
     *
     * @param rules The rules for the grammar.
     * @return A new pattern.
     */
    public static Pattern grammar(Rule... rules) {
        if (rules == null || rules.length == 0)
            throw new IllegalArgumentException("rules must not be null or empty");

        return grammar(Arrays.asList(rules));
    }

    /**
     * Returns a pattern that represents a grammar.
     *
     * @param rules The rules for the grammar.
     * @return A new pattern.
     */
    public static Pattern grammar(Collection<Rule> rules) {
        if (rules == null || rules.size() == 0)
            throw new IllegalArgumentException("rules must not be null or empty");

        LinkedHashMap<String, Pattern> map = new LinkedHashMap<>(rules.size());
        for (Rule rule : rules) {
            if (map.put(rule.name, rule.pattern) != null)
                throw new IllegalArgumentException(rule.name + " not unique");
        }

        return grammar(map);
    }

    /**
     * Returns a pattern that represents a grammar.
     *
     * @param rules The rules for the grammar.
     * @return A new pattern.
     */
    public static Pattern grammar(LinkedHashMap<String, Pattern> rules) {
        if (rules == null || rules.size() == 0)
            throw new IllegalArgumentException("rules must not be null or empty");

        ArrayList<String> refs = new ArrayList<>();
        ArrayList<RuleNode> ruleNodes = new ArrayList<>(rules.size());

        int index = 0;
        for (Map.Entry<String, Pattern> entry : rules.entrySet()) {
            Pattern pattern = entry.getValue();
            RuleNode ruleNode = new RuleNode(index++, entry.getKey(), pattern.root.copy());
            if (pattern.refs != null) {
                int n = refs.size();
                refs.addAll(0, pattern.refs);
                Support.correctRuleIndices(ruleNode.getSibling(), n);
            }

            ruleNodes.add(ruleNode);
        }

        GrammarNode grammarNode = new GrammarNode(ruleNodes);
        for (RuleNode ruleNode : ruleNodes)
            finalFix(ruleNode, grammarNode, refs);

        grammarNode.verify();
        return new Pattern(grammarNode, refs);
    }

    private static int copyRefs(ArrayList<String> target, Pattern pattern1, Pattern pattern2) {
        if (pattern1.refs == null && pattern2.refs == null)
            return 0;

        int offset = 0;
        if (pattern1.refs != null) {
            offset = pattern1.refs.size();
            target.addAll(pattern1.refs);

            if (pattern2.refs == null || pattern2.refs.equals(pattern1.refs))
                return 0;
        }

        target.addAll(pattern2.refs);
        return offset;
    }

    private static void finalFix(Node<?> node, GrammarNode grammarNode, ArrayList<String> refs) {
        for (;;) {
            if (node instanceof GrammarNode)
                return; // Already fixed

            if (node instanceof CallNode) {
                CallNode callNode = (CallNode) node;
                if (callNode.isOpen()) {
                    String ruleName = refs.get(callNode.ruleIndex);
                    if (grammarNode != null) {
                        // TODO: Could this be optimized? Settle on index?
                        RuleNode ruleNode = null;
                        for (RuleNode n : grammarNode.ruleNodes) {
                            if (n.name.equals(ruleName)) {
                                ruleNode = n;
                                break;
                            }
                        }

                        if (ruleNode == null)
                            throw new IllegalStateException(ruleName + " is undefined in the given grammar");

                        callNode.setRuleNode(ruleNode.copy());
                        return;
                    } else {
                        throw new IllegalStateException("Rule used outside of grammar: " + ruleName);
                    }
                }
            } else if (node instanceof SeqNode) {
                ((SeqNode) node).correctAssociativity();
            } else if (node instanceof ChoiceNode) {
                ((ChoiceNode) node).correctAssociativity();
            }

            if (node instanceof UnaryNode) {
                node = ((UnaryNode) node).getSibling();
            } else if (node instanceof BinaryNode) {
                BinaryNode n = (BinaryNode) node;
                finalFix(n.getFirstSibling(), grammarNode, refs);
                node = n.getSecondSibling();
            } else {
                return;
            }
        }
    }

}
