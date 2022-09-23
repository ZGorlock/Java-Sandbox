/*
 * File:    Dictionary.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;

public class Dictionary {
    
    //Constants
    
    public static final List<String> DICTIONARY = loadWords();
    
    public static final String LETTERS = IntStream.rangeClosed('A', 'Z')
            .mapToObj(i -> String.valueOf((char) i))
            .collect(Collectors.joining());
    
    
    //Static Fields
    
    private static final Map<Integer, List<String>> wordsOfLength = new HashMap<>();
    
    private static final Map<Integer, List<String>> sequencesOfLength = new HashMap<>();
    
    private static final Map<Integer, List<String>> illegalSequencesOfLength = new HashMap<>();
    
    private static final Map<String, List<String>> illegalSequencesOfLengthInWordsOfLength = new HashMap<>();
    
    private static final Map<Integer, List<String>> illegalStartingSequencesOfLength = new HashMap<>();
    
    private static final Map<String, List<String>> illegalStartingSequencesOfLengthInWordsOfLength = new HashMap<>();
    
    private static final Map<Integer, List<String>> illegalEndingSequencesOfLength = new HashMap<>();
    
    private static final Map<String, List<String>> illegalEndingSequencesOfLengthInWordsOfLength = new HashMap<>();
    
    
    //Static Methods
    
    private static List<String> loadWords() {
        try {
            return FileUtils.readLines(new File("resources", "dictionary.txt"), StandardCharsets.UTF_8).stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static List<String> wordsOfLength(int wordLength) {
        return wordsOfLength.computeIfAbsent(wordLength,
                i -> DICTIONARY.stream().filter(e -> (e.length() == i)).collect(Collectors.toList()));
    }
    
    public static List<String> sequencesOfLength(int sequenceLength) {
        return sequencesOfLength.computeIfAbsent(sequenceLength,
                i -> {
                    final List<String> sequences = new ArrayList<>();
                    calculateSequencesOfLength(sequences, "", sequenceLength);
                    return sequences;
                });
    }
    
    public static List<String> illegalSequencesOfLength(int sequenceLength) {
        final List<String> sequencesOfLength = sequencesOfLength(sequenceLength);
        return illegalSequencesOfLength.computeIfAbsent(sequenceLength,
                i -> sequencesOfLength.stream().filter(e -> DICTIONARY.stream().noneMatch(e2 -> e2.contains(e))).collect(Collectors.toList()));
    }
    
    public static List<String> illegalSequencesOfLengthInWordsOfLength(int sequenceLength, int wordLength) {
        if (sequenceLength > wordLength) {
            return null;
        }
        
        final List<String> wordsOfLength = wordsOfLength(wordLength);
        final List<String> sequencesOfLength = sequencesOfLength(sequenceLength);
        return illegalSequencesOfLengthInWordsOfLength.computeIfAbsent((wordLength + ":" + sequenceLength),
                key -> sequencesOfLength.stream().filter(e -> wordsOfLength.stream().noneMatch(e2 -> e2.contains(e))).collect(Collectors.toList()));
    }
    
    public static List<String> illegalStartingSequencesOfLength(int sequenceLength) {
        final List<String> sequencesOfLength = sequencesOfLength(sequenceLength);
        return illegalStartingSequencesOfLength.computeIfAbsent(sequenceLength,
                i -> sequencesOfLength.stream().filter(e -> DICTIONARY.stream().noneMatch(e2 -> e2.startsWith(e))).collect(Collectors.toList()));
    }
    
    public static List<String> illegalStartingSequencesOfLengthInWordsOfLength(int sequenceLength, int wordLength) {
        if (sequenceLength > wordLength) {
            return null;
        }
        
        final List<String> wordsOfLength = wordsOfLength(wordLength);
        final List<String> sequencesOfLength = sequencesOfLength(sequenceLength);
        return illegalStartingSequencesOfLengthInWordsOfLength.computeIfAbsent((wordLength + ":" + sequenceLength),
                key -> sequencesOfLength.stream().filter(e -> wordsOfLength.stream().noneMatch(e2 -> e2.startsWith(e))).collect(Collectors.toList()));
    }
    
    public static List<String> illegalEndingSequencesOfLength(int sequenceLength) {
        final List<String> sequencesOfLength = sequencesOfLength(sequenceLength);
        return illegalEndingSequencesOfLength.computeIfAbsent(sequenceLength,
                i -> sequencesOfLength.stream().filter(e -> DICTIONARY.stream().noneMatch(e2 -> e2.endsWith(e))).collect(Collectors.toList()));
    }
    
    public static List<String> illegalEndingSequencesOfLengthInWordsOfLength(int sequenceLength, int wordLength) {
        if (sequenceLength > wordLength) {
            return null;
        }
        
        final List<String> wordsOfLength = wordsOfLength(wordLength);
        final List<String> sequencesOfLength = sequencesOfLength(sequenceLength);
        return illegalEndingSequencesOfLengthInWordsOfLength.computeIfAbsent((wordLength + ":" + sequenceLength),
                key -> sequencesOfLength.stream().filter(e -> wordsOfLength.stream().noneMatch(e2 -> e2.endsWith(e))).collect(Collectors.toList()));
    }
    
    private static void calculateSequencesOfLength(List<String> sequences, String prefix, int sequenceLength) {
        if (sequenceLength == 0) {
            sequences.add(prefix);
            return;
        }
        
        final int sequenceCount = sequences.size();
        for (int i = 0; i < LETTERS.length(); i++) {
            calculateSequencesOfLength(sequences, (prefix + LETTERS.charAt(i)), (sequenceLength - 1));
        }
    }
    
}
