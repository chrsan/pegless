package se.fishtank.pegless;

import org.junit.Test;

import static org.junit.Assert.*;
import static se.fishtank.pegless.Pattern.*;
import static se.fishtank.pegless.Rule.rule;

/**
 * Pattern matcher tests
 *
 * @author Christer Sandberg
 */
public class PatternMatcherTest {

    public static Pattern EOS = n(-1);

    public static Pattern DIGIT = set("0123456789");

    public static Pattern LETTER = choice(set(""), range("AZ"), range("az"));

    @Test
    public void basic_optimizations() {
        Pattern a = ch('a');

        assertMatch(1, FAIL.or(a), "a");
        assertMatch(0, SUCCEED.or(a), "a");
        assertFail(a.or(FAIL), "b");
        assertMatch(0, a.or(SUCCEED), "b");

        assertFail(FAIL.then(a), "a");
        assertMatch(1, SUCCEED.then(a), "a");
        assertFail(a.then(FAIL), "a");
        assertMatch(1, a.then(SUCCEED), "a");

        assertFail(test(FAIL).then(a), "a");
        assertMatch(1, test(SUCCEED).then(a), "a");
        assertFail(a.then(test(FAIL)), "a");
        assertMatch(1, a.then(test(SUCCEED)), "a");
    }

    @Test
    public void basic_tests() {
        assertSuccess(n(3), "aaaa");
        assertSuccess(n(4), "aaaa");
        assertFail(n(5), "aaaa");
        assertSuccess(n(-3), "aa");
        assertFail(n(-3), "aaa");
        assertFail(n(-3), "aaaa");
        assertFail(n(-4), "aaaa");
        assertSuccess(n(-5), "aaaa");

        assertMatch(1, str("a"), "alo");
        assertMatch(2, str("al"), "alo");
        assertFail(str("alu"), "alo");
        assertMatch(0, SUCCEED, "");

        assertSuccess(set("0123456789"), "7");
        assertFail(set("0123456789"), "x");

        assertSuccess(range("AZ"), "C");
        assertFail(range("AZ"), "c");

        assertSuccess(range("az", "AZ"), "c");
        assertSuccess(range("az", "AZ"), "C");
        assertFail(range("az", "AZ"), "0");

        assertSuccess(set("01234567").or(str("8").or(str("9"))), "8");
        assertSuccess(set("01234567").or(str("8").or(str("9"))), "5");
        assertFail(set("01234567").or(str("8").or(str("9"))), "a");

        assertSuccess(DIGIT.repeat(0).then(LETTER).then(DIGIT).then(EOS), "1298a1");
        assertFail(DIGIT.repeat(0).then(LETTER).then(EOS), "1257a1");

        Pattern alpha = choice(LETTER, DIGIT, Pattern.range());
        Pattern word = seq(alpha.repeat(1), (n(1).diff(alpha)).repeat(0));

        assertSuccess(word.repeat(0).then(n(-1)), "alo alo");
        assertSuccess(word.repeat(1).then(n(-1)), "alo alo");
        assertSuccess(word.repeat(2).then(n(-1)), "alo alo");
        assertFail(word.repeat(3).then(n(-1)), "alo alo");

        assertFail(word.repeat(-1).then(n(-1)), "alo alo");
        assertSuccess(word.repeat(-2).then(n(-1)), "alo alo");
        assertSuccess(word.repeat(-3).then(n(-1)), "alo alo");

        Pattern grammar = createParensGrammar();

        assertSuccess(grammar, "(al())()");
        assertFail(grammar.then(n(-1)), "(al())()");
        assertSuccess(grammar.then(n(-1)), "((al())()(é))");
        assertFail(grammar, "(al()()");

        assertFail(repeat(LETTER, 1).diff(str("for")), "foreach");
        assertSuccess(repeat(LETTER, 1).diff(seq(str("for"), EOS)), "foreach");
        assertFail(repeat(LETTER, 1).diff(seq(str("for"), EOS)), "for");
    }

    @Test
    public void basic_captures_tests() {
        Capturer capturer = new Capturer();

        assertSuccess(basicLookFor(capturer.capture(repeat(LETTER, 1))), "   4achou123...");
        assertEquals(1, capturer.size());
        assertEquals("achou", capturer.captures()[0].value);

        capturer.clear();

        assertSuccess(basicLookFor(capturer.capture(repeat(LETTER, 1))).repeat(0), " two words, one more  ");
        assertEquals(4, capturer.size());

        Capture[] captures = capturer.captures();
        assertEquals("two", captures[0].value);
        assertEquals("words", captures[1].value);
        assertEquals("one", captures[2].value);
        assertEquals("more", captures[3].value);

        capturer.clear();

        PositionCapturer positionCapturer = new PositionCapturer();

        Pattern grammar = createParensGrammar();
        assertSuccess(basicLookFor(seq(seq(test(grammar), n(1)), positionCapturer.capture())), "  (  (a)");
        assertEquals(1, positionCapturer.size());
        assertEquals(6, positionCapturer.positions()[0]);

        positionCapturer.clear();

        assertSuccess(seq(positionCapturer.capture(), LETTER.repeat(1), positionCapturer.capture()), "abcd");
        assertEquals(2, positionCapturer.size());

        int[] positions = positionCapturer.positions();
        assertEquals(0, positions[0]);
        assertEquals(4, positions[1]);

        assertSuccess(grammar(rule("1", capturer.capture(seq(capturer.capture(n(1)), ref("1")).or(n(-1))))), "abc");

        assertEquals(7, capturer.size());

        captures = capturer.captures();
        assertEquals("abc", captures[0].value);
        assertEquals("a", captures[1].value);
        assertEquals("bc", captures[2].value);
        assertEquals("b", captures[3].value);
        assertEquals("c", captures[4].value);
        assertEquals("c", captures[5].value);
        assertEquals("", captures[6].value);
    }

    private Pattern basicLookFor(Pattern pattern) {
        return grammar(rule("1", choice(pattern, seq(n(1), ref("1")))));
    }

    private static PatternMatchResult assertMatch(int pos, Pattern pattern, String subject) {
        PatternMatchResult matchResult = pattern.compile().match(subject);

        assertTrue(matchResult.matched());
        assertEquals(pos, matchResult.getEnd());

        return matchResult;
    }

    private static void assertFail(Pattern pattern, String subject) {
        assertFalse(pattern.compile().match(subject).matched());
    }

    private static PatternMatchResult assertSuccess(Pattern pattern, String subject) {
        PatternMatchResult matchResult = pattern.compile().match(subject);

        assertTrue(matchResult.matched());

        return matchResult;
    }

    private static Pattern createParensGrammar() {
        Pattern openParen = str("(");
        Pattern closeParen = str(")");
        Pattern notParen = n(1).diff(set("()"));
        return grammar(rule("1", seq(openParen,
                choice(notParen, seq(test(openParen), ref("1"))).repeat(0),
                closeParen)));
    }

}
