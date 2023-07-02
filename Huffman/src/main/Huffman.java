/*
 * File:    Huffman.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import main.huffman.HuffmanDecoder;
import main.huffman.HuffmanEncoder;
import main.huffman.HuffmanTree;
import main.util.BinaryUtil;
import org.apache.commons.io.FileUtils;

public class Huffman {
    
    //Main Methods
    
    public static void main(String[] args) throws Exception {
//        testBinary();
        testHuffman();
    }
    
    @SuppressWarnings("UnnecessaryLocalVariable")
    private static void testHuffman() throws Exception {
//        final File testFile = new File("data/test.txt");
//        final File testFile = new File("data/test2.txt");
//        final File testFile = new File("data/pi-100.txt");
//        final File testFile = new File("data/pi-1000.txt");
        final File testFile = new File("data/pi-100000.txt");
        
        final String data = FileUtils.readFileToString(testFile, StandardCharsets.UTF_8);
//        final String data = "FFAACAFAAABBCCDEBE";
//        System.out.println("Data: \"" + data + "\"");
        
        final Map<Character, Integer> symbols = countSymbols(data);
        
        final HuffmanTree huffmanTree = new HuffmanTree(symbols, true);

//        final HuffmanNode serialNode = huffmanTree.getRoot().getRight().getLeft().getLeft();
//        final String encodedNode = HuffmanNode.storeNode(serialNode);
//        final HuffmanNode decodedNode = HuffmanNode.loadNode(encodedNode);
        
        final String encodedTree = HuffmanTree.storeTree(huffmanTree);
//        final HuffmanTree decodedTree = HuffmanTree.loadTree(encodedTree);

//        final HuffmanTree tree = decodedTree;
        final HuffmanTree tree = huffmanTree;
        
        final HuffmanEncoder encoder = new HuffmanEncoder(tree);
        final Map<Character, String> encodingKey = encoder.getEncodingKey();
        final String encodedData = encoder.encodeData(data);
        final BitSet compiledData = encoder.compileData(encodedData, encodedTree);
        
        final File outputFile = new File("data/" + testFile.getName() + ".hz");
        BinaryUtil.writeFile(outputFile, compiledData);
        
        compareEncodedData(data, encodedData, encodedTree);
        
        final BitSet loadedBinaryData = BinaryUtil.readFile(outputFile);
        final AtomicReference<String> loadedData = new AtomicReference<>(BinaryUtil.toBitString(loadedBinaryData));
        
        final HuffmanTree loadedTree = HuffmanTree.loadTree(loadedData);
        
        final HuffmanDecoder decoder = new HuffmanDecoder(loadedTree);
        final Map<String, Character> decodingKey = decoder.getDecodingKey();
        final String decodedData = decoder.decodeData(loadedData.get());
        
        assert (decodedData.equals(data));
        
        int x = 5;
    }
    
    private static void testBinary() throws Exception {
        String s1 = "10110111";
        BitSet b1 = BinaryUtil.toBits(s1);
        
        String s2 = BinaryUtil.toBitString(b1);
        BitSet b2 = BinaryUtil.toBits(s2);
        
        byte c3 = 'A';
        String s3 = BinaryUtil.toBitString(c3);
        BitSet b3 = BinaryUtil.toBits(s3);
        byte c4 = BinaryUtil.toByte(s3);
        
        int i5 = 49165;
        String s5 = BinaryUtil.toBitString(i5);
        BitSet b5 = BinaryUtil.toBits(s5);
        int i6 = BinaryUtil.toInt(s5);
        
        long l7 = 198411643514614L;
        String s7 = BinaryUtil.toBitString(l7);
        BitSet b7 = BinaryUtil.toBits(s7);
        long l8 = BinaryUtil.toLong(s7);
        
        assert (s1.equals(s2));
        assert (c3 == c4);
        assert (i5 == i6);
        assert (l7 == l8);
        
        int x = 5;
    }
    
    
    //Static Methods
    
    public static Map<Character, Integer> countSymbols(String data) {
        return data.chars().mapToObj(i -> (char) i)
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(Function.identity(), Collectors.counting()),
                        counts -> counts.entrySet().stream()
                                .sorted((o1, o2) -> -Long.compare(o1.getValue(), o2.getValue()))
                                .collect(Collectors.toMap(Map.Entry::getKey, (e -> e.getValue().intValue()),
                                        (x, y) -> y, LinkedHashMap::new))));
    }
    
    private static void compareEncodedData(String data, String encodedData, String tableData) {
        final long originalLength = (data.length() * 8L);
        final long encodedLength = encodedData.length();
        final long tableLength = tableData.length();
        
        System.out.println("Original Data: " + originalLength + " bytes");
        System.out.println("Encoded Data:  " + encodedLength + " bytes");
        System.out.println("Table Size:    " + tableLength + " bytes");
        
        System.out.println("Compression:   " + String.format("%.04f",
                ((encodedLength + tableLength) / (double) originalLength)));
    }
    
}
