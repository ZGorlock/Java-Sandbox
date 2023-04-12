/*
 * File:    BaseDictionaryInterface.java
 * Package: main.dict.core.base
 * Author:  Zachary Gill
 */

package main.dict.core.base;

import java.util.List;

public interface BaseDictionaryInterface {
    
    //Methods
    
    void load();
    
    String getDictName();
    
    List<String> alphabet();
    
    String charset();
    
    String charsetToken();
    
    List<String> words();
    
    List<String> wordsThatContain(String search);
    
    List<String> wordsThatDoNotContain(String search);
    
    List<String> wordsThatContainAll(String... search);
    
    List<String> wordsThatContainAny(String... search);
    
    List<String> wordsThatContainNone(String... search);
    
    List<String> wordsThatStartWith(String search);
    
    List<String> wordsThatStartWithAny(String... search);
    
    List<String> wordsThatDoNotStartWith(String search);
    
    List<String> wordsThatDoNotStartWithAny(String... search);
    
    List<String> wordsThatEndWith(String search);
    
    List<String> wordsThatEndWithAny(String... search);
    
    List<String> wordsThatDoNotEndWith(String search);
    
    List<String> wordsThatDoNotEndWithAny(String... search);
    
    List<String> wordsThatMatch(String regexSearch);
    
    List<String> wordsThatDoNotMatch(String regexSearch);
    
    List<String> sequencesOfLength(int sequenceLength);
    
    List<String> illegalSequencesOfLength(int sequenceLength);
    
    List<String> illegalStartingSequencesOfLength(int sequenceLength);
    
    List<String> illegalEndingSequencesOfLength(int sequenceLength);
    
    List<String> unscrambleSequence(String scrambledSequence, boolean partial);
    
    List<String> unscrambleSequence(String scrambledSequence);
    
}
