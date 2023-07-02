/*
 * File:    HuffmanTree.java
 * Package: main.huffman
 * Author:  Zachary Gill
 */

package main.huffman;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicReference;

public class HuffmanTree {
    
    //Fields
    
    public HuffmanNode root;
    
    public boolean loadedFromFile = false;
    
    
    //Constructors
    
    public HuffmanTree(Map<Character, Integer> symbols, boolean print) {
        final PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();
        symbols.entrySet().stream()
                .map(HuffmanNode::new)
                .forEach(queue::add);
        
        while (queue.size() > 1) {
            queue.add(new HuffmanNode(queue.poll(), queue.poll()));
        }
        this.root = queue.peek();
        
        calculate();
        
        if (print) {
            print();
        }
    }
    
    public HuffmanTree(Map<Character, Integer> symbols) {
        this(symbols, false);
    }
    
    public HuffmanTree(HuffmanNode root, boolean print) {
        this.root = root;
        
        calculate();
        
        if (print) {
            print();
        }
    }
    
    public HuffmanTree(HuffmanNode root) {
        this(root, false);
    }
    
    
    //Methods
    
    protected void calculate() {
        root.calculate("");
    }
    
    protected void print() {
        root.print();
    }
    
    
    //Getters
    
    public HuffmanNode getRoot() {
        return root;
    }
    
    
    //Static Methods
    
    public static String storeTree(HuffmanTree tree) {
        return HuffmanNode.storeNode(tree.getRoot());
    }
    
    public static HuffmanTree loadTree(AtomicReference<String> bitString) {
        final HuffmanTree loadedTree = new HuffmanTree(HuffmanNode.loadNode(bitString), true);
        loadedTree.loadedFromFile = true;
        return loadedTree;
    }
    
    public static HuffmanTree loadTree(String bitString) {
        return loadTree(new AtomicReference<>(bitString));
    }
    
}
