/*
 * File:    StandardDictionaryInterface.java
 * Package: main.dict.core.standard
 * Author:  Zachary Gill
 */

package main.dict.core.standard;

import java.util.List;

import main.dict.core.base.BaseDictionaryInterface;

public interface StandardDictionaryInterface extends BaseDictionaryInterface {
    
    //Methods
    
    List<String> wordsOfLength(int wordLength);
    
    List<String> wordsOfLengthThatContain(int wordLength, String search);
    
    List<String> wordsOfLengthThatDoNotContain(int wordLength, String search);
    
    List<String> wordsOfLengthThatContainAll(int wordLength, String... search);
    
    List<String> wordsOfLengthThatContainAny(int wordLength, String... search);
    
    List<String> wordsOfLengthThatContainNone(int wordLength, String... search);
    
    List<String> wordsOfLengthThatStartWith(int wordLength, String search);
    
    List<String> wordsOfLengthThatStartWithAny(int wordLength, String... search);
    
    List<String> wordsOfLengthThatDoNotStartWith(int wordLength, String search);
    
    List<String> wordsOfLengthThatDoNotStartWithAny(int wordLength, String... search);
    
    List<String> wordsOfLengthThatEndWith(int wordLength, String search);
    
    List<String> wordsOfLengthThatEndWithAny(int wordLength, String... search);
    
    List<String> wordsOfLengthThatDoNotEndWith(int wordLength, String search);
    
    List<String> wordsOfLengthThatDoNotEndWithAny(int wordLength, String... search);
    
    List<String> wordsOfLengthThatMatch(int wordLength, String regexSearch);
    
    List<String> wordsOfLengthThatDoNotMatch(int wordLength, String regexSearch);
    
    List<String> illegalSequencesOfLengthInWordsOfLength(int sequenceLength, int wordLength);
    
    List<String> illegalStartingSequencesOfLengthInWordsOfLength(int sequenceLength, int wordLength);
    
    List<String> illegalEndingSequencesOfLengthInWordsOfLength(int sequenceLength, int wordLength);
    
    List<String> unscrambleSequenceOfLength(int wordLength, String scrambledSequence, boolean partial);
    
    List<String> unscrambleSequenceOfLength(int wordLength, String scrambledSequence);
    
}
