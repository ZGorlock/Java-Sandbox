/*
 * File:    HuffmanNode.java
 * Package: main.huffman
 * Author:  Zachary Gill
 */

package main.huffman;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import main.util.BinaryUtil;

public class HuffmanNode implements Comparable<HuffmanNode> {
    
    //Fields
    
    public Character symbol = null;
    
    public Integer weight = 0;
    
    public String code = "";
    
    public HuffmanNode left = null;
    
    public HuffmanNode right = null;
    
    
    //Constructors
    
    public HuffmanNode(Character symbol, Integer weight) {
        if (symbol != null) {
            System.out.println("Creating node: " + symbol + " " + weight);
        }
        this.symbol = symbol;
        this.weight = weight;
    }
    
    public HuffmanNode(Map.Entry<Character, Integer> entry) {
        this(entry.getKey(), entry.getValue());
    }
    
    public HuffmanNode(Character symbol) {
        this(symbol, 0);
    }
    
    public HuffmanNode(HuffmanNode left, HuffmanNode right) {
        this(null, Stream.of(left, right).filter(Objects::nonNull).mapToInt(HuffmanNode::getWeight).sum());
        
        this.left = left;
        this.right = right;
    }
    
    public HuffmanNode() {
    }
    
    
    //Methods
    
    public boolean isLeaf() {
        return (getSymbol() != null) &&
                (getLeft() == null) && (getRight() == null);
    }
    
    public List<HuffmanNode> getLeaves() {
        final List<HuffmanNode> leaves = new ArrayList<>();
        if (isLeaf()) {
            leaves.add(this);
        } else {
            Optional.ofNullable(getLeft()).map(HuffmanNode::getLeaves).ifPresent(leaves::addAll);
            Optional.ofNullable(getRight()).map(HuffmanNode::getLeaves).ifPresent(leaves::addAll);
        }
        return leaves;
    }
    
    protected void calculate(String prefix) {
        if (isLeaf()) {
            code = prefix;
        } else {
            Optional.ofNullable(getLeft()).ifPresent(e -> e.calculate(prefix + '0'));
            Optional.ofNullable(getRight()).ifPresent(e -> e.calculate(prefix + '1'));
        }
    }
    
    protected void print() {
        if (isLeaf()) {
            System.out.println(this);
        } else {
            Optional.ofNullable(getLeft()).ifPresent(HuffmanNode::print);
            Optional.ofNullable(getRight()).ifPresent(HuffmanNode::print);
        }
    }
    
    public HuffmanNode traverse(boolean bit) {
        return (bit ? getRight() : getLeft());
    }
    
    @Override
    public String toString() {
        return (getSymbol() + " : [" + getCode() + "]");
    }
    
    @Override
    public int compareTo(HuffmanNode o) {
        final int weightCompare = Integer.compare(getWeight(), o.getWeight());
        return ((weightCompare != 0) || (getSymbol() == null) || (o.getSymbol() == null)) ? weightCompare :
                -getSymbol().compareTo(o.getSymbol());
    }
    
    
    //Getters
    
    public Character getSymbol() {
        return symbol;
    }
    
    public Integer getWeight() {
        return weight;
    }
    
    public String getCode() {
        return code;
    }
    
    public HuffmanNode getLeft() {
        return left;
    }
    
    public HuffmanNode getRight() {
        return right;
    }
    
    
    //Static Methods
    
    public static String storeNode(HuffmanNode node) {
        final StringBuilder nodeData = new StringBuilder();
        if (node.isLeaf()) {
            nodeData.append('1');
            nodeData.append(BinaryUtil.toBitString(node.getSymbol()));
        } else {
            nodeData.append('0');
            Optional.ofNullable(node.getLeft()).map(HuffmanNode::storeNode).ifPresent(nodeData::append);
            Optional.ofNullable(node.getRight()).map(HuffmanNode::storeNode).ifPresent(nodeData::append);
        }
        return nodeData.toString();
    }
    
    public static HuffmanNode loadNode(AtomicReference<String> bitString) {
        if (bitString.getAndUpdate(e -> e.substring(1)).charAt(0) == '1') {
            return new HuffmanNode((char) BinaryUtil.toByte(
                    bitString.getAndUpdate(e -> e.substring(8)).substring(0, 8)));
        } else {
            return new HuffmanNode(loadNode(bitString), loadNode(bitString));
        }
    }
    
    public static HuffmanNode loadNode(String bitString) {
        return loadNode(new AtomicReference<>(bitString));
    }
    
}
