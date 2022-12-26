/*
 * File:    BaseDictionary.java
 * Package: main.dict.base
 * Author:  Zachary Gill
 */

package main.dict.base;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;

public abstract class BaseDictionary {
    
    //Constants
    
    protected static final List<String> LETTERS = IntStream.rangeClosed('A', 'Z')
            .mapToObj(i -> String.valueOf((char) i))
            .collect(Collectors.toList());
    
    
    //Fields
    
    protected final List<String> words = new ArrayList<>();
    
    protected final List<String> alphabet = new ArrayList<>();
    
    protected final Map<Integer, List<String>> sequencesOfLength = new HashMap<>();
    
    protected final Map<Integer, List<String>> illegalSequencesOfLength = new HashMap<>();
    
    protected final Map<Integer, List<String>> illegalStartingSequencesOfLength = new HashMap<>();
    
    protected final Map<Integer, List<String>> illegalEndingSequencesOfLength = new HashMap<>();
    
    protected final AtomicBoolean loaded = new AtomicBoolean(false);
    
    
    //Methods
    
    public final void load() {
        if (loaded.compareAndSet(false, true)) {
            words.addAll(loadLexicon());
            alphabet.addAll(loadAlphabet());
        }
    }
    
    public abstract String getDictName();
    
    public File getDictFile() {
        return new File("resources", (getDictName() + ".txt"));
    }
    
    protected List<String> loadLexicon() {
        try {
            return FileUtils.readLines(getDictFile(), StandardCharsets.UTF_8).stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    protected List<String> loadAlphabet() {
        return words().stream()
                .flatMapToInt(String::chars).distinct()
                .sorted().mapToObj(i -> String.valueOf((char) i))
                .collect(Collectors.toList());
    }
    
    public List<String> alphabet() {
        return alphabet;
    }
    
    public String charset() {
        return String.join("", alphabet());
    }
    
    public String charsetToken() {
        return charset().replaceAll("^(.).*(.)$", "[$1-$2]");
    }
    
    public List<String> words() {
        return words;
    }
    
    public List<String> wordsThatContain(String search) {
        return words().stream().filter(e -> e.contains(search)).collect(Collectors.toList());
    }
    
    public List<String> wordsThatDoNotContain(String search) {
        return words().stream().filter(e -> e.contains(search)).collect(Collectors.toList());
    }
    
    public List<String> wordsThatContainAll(String... search) {
        return words().stream().filter(e -> Arrays.stream(search).allMatch(e::contains)).collect(Collectors.toList());
    }
    
    public List<String> wordsThatContainAny(String... search) {
        return words().stream().filter(e -> Arrays.stream(search).anyMatch(e::contains)).collect(Collectors.toList());
    }
    
    public List<String> wordsThatContainNone(String... search) {
        return words().stream().filter(e -> Arrays.stream(search).noneMatch(e::contains)).collect(Collectors.toList());
    }
    
    public List<String> wordsThatStartWith(String search) {
        return words().stream().filter(e -> e.startsWith(search)).collect(Collectors.toList());
    }
    
    public List<String> wordsThatStartWithAny(String... search) {
        return words().stream().filter(e -> Arrays.stream(search).anyMatch(e::startsWith)).collect(Collectors.toList());
    }
    
    public List<String> wordsThatDoNotStartWith(String search) {
        return words().stream().filter(e -> !e.startsWith(search)).collect(Collectors.toList());
    }
    
    public List<String> wordsThatDoNotStartWithAny(String... search) {
        return words().stream().filter(e -> Arrays.stream(search).noneMatch(e::startsWith)).collect(Collectors.toList());
    }
    
    public List<String> wordsThatEndWith(String search) {
        return words().stream().filter(e -> e.endsWith(search)).collect(Collectors.toList());
    }
    
    public List<String> wordsThatEndWithAny(String... search) {
        return words().stream().filter(e -> Arrays.stream(search).anyMatch(e::endsWith)).collect(Collectors.toList());
    }
    
    public List<String> wordsThatDoNotEndWith(String search) {
        return words().stream().filter(e -> !e.endsWith(search)).collect(Collectors.toList());
    }
    
    public List<String> wordsThatDoNotEndWithAny(String... search) {
        return words().stream().filter(e -> Arrays.stream(search).noneMatch(e::endsWith)).collect(Collectors.toList());
    }
    
    public List<String> wordsThatMatch(String regexSearch) {
        final Pattern regexPattern = Pattern.compile(regexSearch);
        return words().stream().filter(e -> regexPattern.matcher(e).matches()).collect(Collectors.toList());
    }
    
    public List<String> wordsThatDoNotMatch(String regexSearch) {
        final Pattern regexPattern = Pattern.compile(regexSearch);
        return words().stream().filter(e -> !regexPattern.matcher(e).matches()).collect(Collectors.toList());
    }
    
    public List<String> sequencesOfLength(int sequenceLength) {
        return sequencesOfLength.computeIfAbsent(sequenceLength,
                i -> {
                    final List<String> sequences = new ArrayList<>();
                    calculateSequencesOfLength(sequences, "", alphabet(), sequenceLength);
                    return sequences;
                });
    }
    
    public List<String> illegalSequencesOfLength(int sequenceLength) {
        final List<String> sequencesOfLength = sequencesOfLength(sequenceLength);
        return illegalSequencesOfLength.computeIfAbsent(sequenceLength,
                i -> sequencesOfLength.stream().filter(e -> words().stream().noneMatch(e2 -> e2.contains(e))).collect(Collectors.toList()));
    }
    
    public List<String> illegalStartingSequencesOfLength(int sequenceLength) {
        final List<String> sequencesOfLength = sequencesOfLength(sequenceLength);
        return illegalStartingSequencesOfLength.computeIfAbsent(sequenceLength,
                i -> sequencesOfLength.stream().filter(e -> words().stream().noneMatch(e2 -> e2.startsWith(e))).collect(Collectors.toList()));
    }
    
    public List<String> illegalEndingSequencesOfLength(int sequenceLength) {
        final List<String> sequencesOfLength = sequencesOfLength(sequenceLength);
        return illegalEndingSequencesOfLength.computeIfAbsent(sequenceLength,
                i -> sequencesOfLength.stream().filter(e -> words().stream().noneMatch(e2 -> e2.endsWith(e))).collect(Collectors.toList()));
    }
    
    public List<String> unscrambleSequence(String scrambledSequence, boolean partial) {
        return unscrambleSequenceFromList(words(), scrambledSequence, partial);
    }
    
    public List<String> unscrambleSequence(String scrambledSequence) {
        return unscrambleSequence(scrambledSequence, false);
    }
    
    
    //Static Methods
    
    private static void calculateSequencesOfLength(List<String> sequences, String prefix, List<String> alphabet, int sequenceLength) {
        if (sequenceLength == 0) {
            sequences.add(prefix);
        } else {
            alphabet.forEach(letter ->
                    calculateSequencesOfLength(sequences, (prefix + letter), alphabet, (sequenceLength - 1)));
        }
    }
    
    protected static List<String> unscrambleSequenceFromList(List<String> options, String scrambledSequence, boolean partial) {
        return options.stream()
                .map(e -> Map.entry(e, e.length() -
                        scrambledSequence.chars().mapToObj(i -> String.valueOf((char) i))
                                .reduce(e, (s, c) -> s.replaceFirst(Pattern.quote(c), "")).length()))
                .filter(e -> (partial || (e.getValue() == 0)))
                .sorted((o1, o2) -> Integer.compare(o2.getValue(), o1.getValue()))
                .map(e -> e.getValue() + ": " + e.getKey())
                .collect(Collectors.toList());
    }
    
}
