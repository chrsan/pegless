package se.fishtank.pegless;

import java.util.LinkedList;

import org.junit.Test;
import se.fishtank.pegless.internal.ast.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Pattern tests
 *
 * @author Christer Sandberg
 */
public class PatternTest {

    @Test
    public void test_abc_followed_by_at_most_3_d() {
        Pattern pattern = Pattern.set("abc").then(Pattern.ch('d').repeat(-3));
        LinkedList<Node<?>> nodes = flatten(pattern.root);

        assertTrue(nodes.pop() instanceof SeqNode);
        assertTrue(nodes.peek() instanceof SetNode);

        SetNode setNode = (SetNode) nodes.pop();
        assertEquals(3, setNode.characterSet.cardinality());
        assertTrue(setNode.characterSet.get('a'));
        assertTrue(setNode.characterSet.get('b'));
        assertTrue(setNode.characterSet.get('c'));

        assertTrue(nodes.pop() instanceof ChoiceNode);
        assertTrue(nodes.pop() instanceof SeqNode);

        assertTrue(nodes.peek() instanceof CharNode);
        assertEquals('d', ((CharNode) nodes.pop()).ch);

        assertTrue(nodes.pop() instanceof ChoiceNode);
        assertTrue(nodes.pop() instanceof SeqNode);

        assertTrue(nodes.peek() instanceof CharNode);
        assertEquals('d', ((CharNode) nodes.pop()).ch);

        assertTrue(nodes.pop() instanceof ChoiceNode);

        assertTrue(nodes.peek() instanceof CharNode);
        assertEquals('d', ((CharNode) nodes.pop()).ch);

        assertTrue(nodes.pop() instanceof TrueNode);
        assertTrue(nodes.pop() instanceof TrueNode);
        assertTrue(nodes.pop() instanceof TrueNode);

        assertTrue(nodes.isEmpty());
    }

    public static LinkedList<Node<?>> flatten(Node<?> root) {
        LinkedList<Node<?>> nodes = new LinkedList<>();
        flatten(root, nodes);

        return nodes;
    }

    private static void flatten(Node<?> node, LinkedList<Node<?>> result) {
        if (node == null)
            return;

        result.add(node);

        if (node instanceof UnaryNode) {
            flatten(((UnaryNode) node).getSibling(), result);
        } else if (node instanceof BinaryNode) {
            BinaryNode binaryNode = (BinaryNode) node;
            flatten(binaryNode.getFirstSibling(), result);
            flatten(binaryNode.getSecondSibling(), result);
        }
    }

}
