package edu.tarleton.drduplex.index.plain;

import edu.tarleton.drduplex.clones.Pos;

/**
 * The representation of the stack node.
 *
 * @author Zdenek Tronicek
 */
public class StackNode {

    private final TrieNode node;
    private final Pos pos;

    public StackNode(TrieNode node, Pos pos) {
        this.node = node;
        this.pos = pos;
    }

    public TrieNode getNode() {
        return node;
    }

    public Pos getPos() {
        return pos;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", node, pos);
    }
}
