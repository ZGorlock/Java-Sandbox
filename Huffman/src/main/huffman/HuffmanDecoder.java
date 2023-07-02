/*
 * File:    HuffmanDecoder.java
 * Package: main.huffman
 * Author:  Zachary Gill
 */

package main.huffman;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class HuffmanDecoder {
    
    //Fields
    
    public final HuffmanTree tree;
    
    public HuffmanNode currentNode;
    
    public LinkedHashMap<String, Character> decodingKey;
    
    
    //Constructors
    
    public HuffmanDecoder(HuffmanTree tree) {
        this.tree = tree;
        this.currentNode = this.tree.getRoot();
        this.decodingKey = this.currentNode.getLeaves().stream()
                .filter(Objects::nonNull).filter(HuffmanNode::isLeaf)
                .sorted(Comparator.comparing(HuffmanNode::getCode)).sorted(Comparator.reverseOrder())
                .sorted(Comparator.comparingInt(o -> o.getCode().length()))
                .collect(Collectors.toMap(HuffmanNode::getCode, HuffmanNode::getSymbol,
                        (x, y) -> y, LinkedHashMap::new));
    }
    
    
    //Methods
    
    public Optional<Character> decode(boolean bit) {
        currentNode = currentNode.traverse(bit);
        if (currentNode.isLeaf()) {
            final Optional<Character> symbol = Optional.ofNullable(currentNode.getSymbol());
            currentNode = getTree().getRoot();
            return symbol;
        }
        return Optional.empty();
    }
    
    public String decodeData(String data) {
        return data.chars()
                .mapToObj(i -> (i != '0'))
                .map(this::decode)
                .filter(Optional::isPresent).map(Optional::get)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
    
    
    //Getters
    
    public HuffmanTree getTree() {
        return tree;
    }
    
    public HuffmanNode getCurrentNode() {
        return currentNode;
    }
    
    public LinkedHashMap<String, Character> getDecodingKey() {
        return decodingKey;
    }
    
}
