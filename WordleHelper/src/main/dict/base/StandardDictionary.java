/*
 * File:    StandardDictionary.java
 * Package: main.dict.base
 * Author:  Zachary Gill
 */

package main.dict.base;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class StandardDictionary extends BaseDictionary {
    
    //Fields
    
    protected final Map<Integer, List<String>> wordsOfLength = new HashMap<>();
    
    protected final Map<String, List<String>> illegalSequencesOfLengthInWordsOfLength = new HashMap<>();
    
    protected final Map<String, List<String>> illegalStartingSequencesOfLengthInWordsOfLength = new HashMap<>();
    
    protected final Map<String, List<String>> illegalEndingSequencesOfLengthInWordsOfLength = new HashMap<>();
    
    
    //Methods
    
    public List<String> wordsOfLength(int wordLength) {
        return wordsOfLength.computeIfAbsent(wordLength,
                i -> words().stream().filter(e -> (e.length() == i)).collect(Collectors.toList()));
    }
    
    public List<String> wordsOfLengthThatContain(int wordLength, String search) {
        return (search.length() > wordLength) ? List.of() :
                wordsOfLength(wordLength).stream().filter(e -> e.contains(search)).collect(Collectors.toList());
    }
    
    public List<String> wordsOfLengthThatDoNotContain(int wordLength, String search) {
        return wordsOfLength(wordLength).stream().filter(e -> e.contains(search)).collect(Collectors.toList());
    }
    
    public List<String> wordsOfLengthThatContainAll(int wordLength, String... search) {
        return Arrays.stream(search).anyMatch(e -> (e.length() > wordLength)) ? List.of() :
                wordsOfLength(wordLength).stream().filter(e -> Arrays.stream(search).allMatch(e::contains)).collect(Collectors.toList());
    }
    
    public List<String> wordsOfLengthThatContainAny(int wordLength, String... search) {
        return Arrays.stream(search).allMatch(e -> (e.length() > wordLength)) ? List.of() :
                wordsOfLength(wordLength).stream().filter(e -> Arrays.stream(search).anyMatch(e::contains)).collect(Collectors.toList());
    }
    
    public List<String> wordsOfLengthThatContainNone(int wordLength, String... search) {
        return wordsOfLength(wordLength).stream().filter(e -> Arrays.stream(search).noneMatch(e::contains)).collect(Collectors.toList());
    }
    
    public List<String> wordsOfLengthThatStartWith(int wordLength, String search) {
        return (search.length() > wordLength) ? List.of() :
                wordsOfLength(wordLength).stream().filter(e -> e.startsWith(search)).collect(Collectors.toList());
    }
    
    public List<String> wordsOfLengthThatStartWithAny(int wordLength, String... search) {
        return Arrays.stream(search).allMatch(e -> (e.length() > wordLength)) ? List.of() :
                wordsOfLength(wordLength).stream().filter(e -> Arrays.stream(search).anyMatch(e::startsWith)).collect(Collectors.toList());
    }
    
    public List<String> wordsOfLengthThatDoNotStartWith(int wordLength, String search) {
        return wordsOfLength(wordLength).stream().filter(e -> !e.startsWith(search)).collect(Collectors.toList());
    }
    
    public List<String> wordsOfLengthThatDoNotStartWithAny(int wordLength, String... search) {
        return wordsOfLength(wordLength).stream().filter(e -> Arrays.stream(search).noneMatch(e::startsWith)).collect(Collectors.toList());
    }
    
    public List<String> wordsOfLengthThatEndWith(int wordLength, String search) {
        return (search.length() > wordLength) ? List.of() :
                wordsOfLength(wordLength).stream().filter(e -> e.endsWith(search)).collect(Collectors.toList());
    }
    
    public List<String> wordsOfLengthThatEndWithAny(int wordLength, String... search) {
        return Arrays.stream(search).allMatch(e -> (e.length() > wordLength)) ? List.of() :
                wordsOfLength(wordLength).stream().filter(e -> Arrays.stream(search).anyMatch(e::endsWith)).collect(Collectors.toList());
    }
    
    public List<String> wordsOfLengthThatDoNotEndWith(int wordLength, String search) {
        return wordsOfLength(wordLength).stream().filter(e -> !e.endsWith(search)).collect(Collectors.toList());
    }
    
    public List<String> wordsOfLengthThatDoNotEndWithAny(int wordLength, String... search) {
        return wordsOfLength(wordLength).stream().filter(e -> Arrays.stream(search).noneMatch(e::endsWith)).collect(Collectors.toList());
    }
    
    public List<String> wordsOfLengthThatMatch(int wordLength, String regexSearch) {
        final Pattern regexPattern = Pattern.compile(regexSearch);
        return wordsOfLength(wordLength).stream().filter(e -> regexPattern.matcher(e).matches()).collect(Collectors.toList());
    }
    
    public List<String> wordsOfLengthThatDoNotMatch(int wordLength, String regexSearch) {
        final Pattern regexPattern = Pattern.compile(regexSearch);
        return wordsOfLength(wordLength).stream().filter(e -> !regexPattern.matcher(e).matches()).collect(Collectors.toList());
    }
    
    public List<String> illegalSequencesOfLengthInWordsOfLength(int sequenceLength, int wordLength) {
        if (sequenceLength > wordLength) {
            return null;
        }
        
        final List<String> wordsOfLength = wordsOfLength(wordLength);
        final List<String> sequencesOfLength = sequencesOfLength(sequenceLength);
        return illegalSequencesOfLengthInWordsOfLength.computeIfAbsent((wordLength + ":" + sequenceLength),
                key -> sequencesOfLength.stream().filter(e -> wordsOfLength.stream().noneMatch(e2 -> e2.contains(e))).collect(Collectors.toList()));
    }
    
    public List<String> illegalStartingSequencesOfLengthInWordsOfLength(int sequenceLength, int wordLength) {
        if (sequenceLength > wordLength) {
            return null;
        }
        
        final List<String> wordsOfLength = wordsOfLength(wordLength);
        final List<String> sequencesOfLength = sequencesOfLength(sequenceLength);
        return illegalStartingSequencesOfLengthInWordsOfLength.computeIfAbsent((wordLength + ":" + sequenceLength),
                key -> sequencesOfLength.stream().filter(e -> wordsOfLength.stream().noneMatch(e2 -> e2.startsWith(e))).collect(Collectors.toList()));
    }
    
    public List<String> illegalEndingSequencesOfLengthInWordsOfLength(int sequenceLength, int wordLength) {
        if (sequenceLength > wordLength) {
            return null;
        }
        
        final List<String> wordsOfLength = wordsOfLength(wordLength);
        final List<String> sequencesOfLength = sequencesOfLength(sequenceLength);
        return illegalEndingSequencesOfLengthInWordsOfLength.computeIfAbsent((wordLength + ":" + sequenceLength),
                key -> sequencesOfLength.stream().filter(e -> wordsOfLength.stream().noneMatch(e2 -> e2.endsWith(e))).collect(Collectors.toList()));
    }
    
    public List<String> unscrambleSequenceOfLength(int wordLength, String scrambledSequence, boolean partial) {
        return unscrambleSequenceFromList(wordsOfLength(wordLength), scrambledSequence, partial);
    }
    
    public List<String> unscrambleSequenceOfLength(int wordLength, String scrambledSequence) {
        return unscrambleSequenceOfLength(wordLength, scrambledSequence, false);
    }
    
}
