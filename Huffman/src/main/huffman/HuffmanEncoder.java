/*
 * File:    HuffmanEncoder.java
 * Package: main.huffman
 * Author:  Zachary Gill
 */

package main.huffman;

import java.util.BitSet;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import main.util.BinaryUtil;

public class HuffmanEncoder {
    
    //Fields
    
    public final HuffmanTree tree;
    
    public HuffmanNode currentNode;
    
    public LinkedHashMap<Character, String> encodingKey;
    
    
    //Constructors
    
    public HuffmanEncoder(HuffmanTree tree) {
        this.tree = tree;
        this.currentNode = this.tree.getRoot();
        this.encodingKey = this.currentNode.getLeaves().stream()
                .filter(Objects::nonNull).filter(HuffmanNode::isLeaf)
                .sorted(Comparator.comparing(HuffmanNode::getCode)).sorted(Comparator.reverseOrder())
                .sorted(Comparator.comparingInt(o -> o.getCode().length()))
                .collect(Collectors.toMap(HuffmanNode::getSymbol, HuffmanNode::getCode,
                        (x, y) -> y, LinkedHashMap::new));
    }
    
    
    //Methods
    
    public Optional<String> encode(Character symbol) {
        return Optional.ofNullable(symbol)
                .map(getEncodingKey()::get);
    }
    
    public String encodeData(String data) {
        return data.chars().mapToObj(e -> (char) e)
                .map(this::encode)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.joining());
    }
    
    public BitSet compileData(String encodedData, String encodedTree) {
        return BinaryUtil.toBits(encodedTree + encodedData);
    }
    
    public BitSet compileData(String encodedData) {
        return compileData(encodedData, HuffmanTree.storeTree(getTree()));
    }
    
    
    //Getters
    
    public HuffmanTree getTree() {
        return tree;
    }
    
    public HuffmanNode getCurrentNode() {
        return currentNode;
    }
    
    public LinkedHashMap<Character, String> getEncodingKey() {
        return encodingKey;
    }
    
}
