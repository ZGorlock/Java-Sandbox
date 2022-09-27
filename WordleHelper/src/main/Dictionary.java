/*
 * File:    Dictionary.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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
    
    public static List<String> wordsThatContain(String search) {
        return DICTIONARY.stream().filter(e -> e.contains(search)).collect(Collectors.toList());
    }
    
    public static List<String> wordsThatDoNotContain(String search) {
        return DICTIONARY.stream().filter(e -> e.contains(search)).collect(Collectors.toList());
    }
    
    public static List<String> wordsThatContainAll(String... search) {
        return DICTIONARY.stream().filter(e -> Arrays.stream(search).allMatch(e::contains)).collect(Collectors.toList());
    }
    
    public static List<String> wordsThatContainAny(String... search) {
        return DICTIONARY.stream().filter(e -> Arrays.stream(search).anyMatch(e::contains)).collect(Collectors.toList());
    }
    
    public static List<String> wordsThatContainNone(String... search) {
        return DICTIONARY.stream().filter(e -> Arrays.stream(search).noneMatch(e::contains)).collect(Collectors.toList());
    }
    
    public static List<String> wordsThatStartWith(String search) {
        return DICTIONARY.stream().filter(e -> e.startsWith(search)).collect(Collectors.toList());
    }
    
    public static List<String> wordsThatStartWithAny(String... search) {
        return DICTIONARY.stream().filter(e -> Arrays.stream(search).anyMatch(e::startsWith)).collect(Collectors.toList());
    }
    
    public static List<String> wordsThatDoNotStartWith(String search) {
        return DICTIONARY.stream().filter(e -> !e.startsWith(search)).collect(Collectors.toList());
    }
    
    public static List<String> wordsThatDoNotStartWithAny(String... search) {
        return DICTIONARY.stream().filter(e -> Arrays.stream(search).noneMatch(e::startsWith)).collect(Collectors.toList());
    }
    
    public static List<String> wordsThatEndWith(String search) {
        return DICTIONARY.stream().filter(e -> e.endsWith(search)).collect(Collectors.toList());
    }
    
    public static List<String> wordsThatEndWithAny(String... search) {
        return DICTIONARY.stream().filter(e -> Arrays.stream(search).anyMatch(e::endsWith)).collect(Collectors.toList());
    }
    
    public static List<String> wordsThatDoNotEndWith(String search) {
        return DICTIONARY.stream().filter(e -> !e.endsWith(search)).collect(Collectors.toList());
    }
    
    public static List<String> wordsThatDoNotEndWithAny(String... search) {
        return DICTIONARY.stream().filter(e -> Arrays.stream(search).noneMatch(e::endsWith)).collect(Collectors.toList());
    }
    
    public static List<String> wordsThatMatch(String regexSearch) {
        final Pattern regexPattern = Pattern.compile(regexSearch);
        return DICTIONARY.stream().filter(e -> regexPattern.matcher(e).matches()).collect(Collectors.toList());
    }
    
    public static List<String> wordsThatDoNotMatch(String regexSearch) {
        final Pattern regexPattern = Pattern.compile(regexSearch);
        return DICTIONARY.stream().filter(e -> !regexPattern.matcher(e).matches()).collect(Collectors.toList());
    }
    
    public static List<String> wordsOfLengthThatContain(int wordLength, String search) {
        return (search.length() > wordLength) ? List.of() :
                wordsOfLength(wordLength).stream().filter(e -> e.contains(search)).collect(Collectors.toList());
    }
    
    public static List<String> wordsOfLengthThatDoNotContain(int wordLength, String search) {
        return wordsOfLength(wordLength).stream().filter(e -> e.contains(search)).collect(Collectors.toList());
    }
    
    public static List<String> wordsOfLengthThatContainAll(int wordLength, String... search) {
        return Arrays.stream(search).anyMatch(e -> (e.length() > wordLength)) ? List.of() :
                wordsOfLength(wordLength).stream().filter(e -> Arrays.stream(search).allMatch(e::contains)).collect(Collectors.toList());
    }
    
    public static List<String> wordsOfLengthThatContainAny(int wordLength, String... search) {
        return Arrays.stream(search).allMatch(e -> (e.length() > wordLength)) ? List.of() :
                wordsOfLength(wordLength).stream().filter(e -> Arrays.stream(search).anyMatch(e::contains)).collect(Collectors.toList());
    }
    
    public static List<String> wordsOfLengthThatContainNone(int wordLength, String... search) {
        return wordsOfLength(wordLength).stream().filter(e -> Arrays.stream(search).noneMatch(e::contains)).collect(Collectors.toList());
    }
    
    public static List<String> wordsOfLengthThatStartWith(int wordLength, String search) {
        return (search.length() > wordLength) ? List.of() :
                wordsOfLength(wordLength).stream().filter(e -> e.startsWith(search)).collect(Collectors.toList());
    }
    
    public static List<String> wordsOfLengthThatStartWithAny(int wordLength, String... search) {
        return Arrays.stream(search).allMatch(e -> (e.length() > wordLength)) ? List.of() :
                wordsOfLength(wordLength).stream().filter(e -> Arrays.stream(search).anyMatch(e::startsWith)).collect(Collectors.toList());
    }
    
    public static List<String> wordsOfLengthThatDoNotStartWith(int wordLength, String search) {
        return wordsOfLength(wordLength).stream().filter(e -> !e.startsWith(search)).collect(Collectors.toList());
    }
    
    public static List<String> wordsOfLengthThatDoNotStartWithAny(int wordLength, String... search) {
        return wordsOfLength(wordLength).stream().filter(e -> Arrays.stream(search).noneMatch(e::startsWith)).collect(Collectors.toList());
    }
    
    public static List<String> wordsOfLengthThatEndWith(int wordLength, String search) {
        return (search.length() > wordLength) ? List.of() :
                wordsOfLength(wordLength).stream().filter(e -> e.endsWith(search)).collect(Collectors.toList());
    }
    
    public static List<String> wordsOfLengthThatEndWithAny(int wordLength, String... search) {
        return Arrays.stream(search).allMatch(e -> (e.length() > wordLength)) ? List.of() :
                wordsOfLength(wordLength).stream().filter(e -> Arrays.stream(search).anyMatch(e::endsWith)).collect(Collectors.toList());
    }
    
    public static List<String> wordsOfLengthThatDoNotEndWith(int wordLength, String search) {
        return wordsOfLength(wordLength).stream().filter(e -> !e.endsWith(search)).collect(Collectors.toList());
    }
    
    public static List<String> wordsOfLengthThatDoNotEndWithAny(int wordLength, String... search) {
        return wordsOfLength(wordLength).stream().filter(e -> Arrays.stream(search).noneMatch(e::endsWith)).collect(Collectors.toList());
    }
    
    public static List<String> wordsOfLengthThatMatch(int wordLength, String regexSearch) {
        final Pattern regexPattern = Pattern.compile(regexSearch);
        return wordsOfLength(wordLength).stream().filter(e -> regexPattern.matcher(e).matches()).collect(Collectors.toList());
    }
    
    public static List<String> wordsOfLengthThatDoNotMatch(int wordLength, String regexSearch) {
        final Pattern regexPattern = Pattern.compile(regexSearch);
        return wordsOfLength(wordLength).stream().filter(e -> !regexPattern.matcher(e).matches()).collect(Collectors.toList());
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
