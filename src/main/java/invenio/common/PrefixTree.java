/*
 *  A simple structure of a prefix tree - allowing to efficiently store and check for existence
 *  Every word can have a list of objects assigned ... when checking, we return this complete list.
 *  We always add a word with an object of the type T
 */
package invenio.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Piotr Praczyk
 */
public class PrefixTree<T> { // could be done: implements Map<String, T>

    public static class PrefixTreeNode<T> {

        public HashMap<Character, PrefixTreeNode<T>> subnodes;
        public LinkedList<T> objects;

        public PrefixTreeNode() {
            this.subnodes = new HashMap<Character, PrefixTreeNode<T>>();
            this.objects = new LinkedList<T>();
        }
    }
    private PrefixTreeNode<T> root;

    public PrefixTree() {
        this.root = new PrefixTreeNode<T>();
    }

    public void addString(String text, T obj) {
        PrefixTreeNode<T> curNode = this.root;
        for (int ind=0; ind < text.length(); ++ind) {
            Character c = text.charAt(ind);
            if (!curNode.subnodes.containsKey(c)) {
                curNode.subnodes.put(c, new PrefixTreeNode<T>());
            }
            curNode = curNode.subnodes.get(c);
        }
        curNode.objects.add(obj);
    }

    public List<T> getStringObjects(String s) {
        PrefixTreeNode<T> curNode = this.root;
        for (int ind=0; ind < s.length(); ++ind){
            Character c = s.charAt(ind);
            if (!curNode.subnodes.containsKey(c)){
                return new LinkedList<T>();
            }
            curNode = curNode.subnodes.get(c);
        }
        return curNode.objects;
    }
}
