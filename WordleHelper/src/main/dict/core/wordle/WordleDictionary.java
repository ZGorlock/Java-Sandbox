/*
 * File:    WordleDictionary.java
 * Package: main.dict.core.wordle
 * Author:  Zachary Gill
 */

package main.dict.core.wordle;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import main.dict.core.base.BaseDictionary;

public abstract class WordleDictionary extends BaseDictionary implements WordleDictionaryInterface {
    
    //Methods
    
    @Override
    public abstract int getWordLength();
    
    @Override
    public List<String> findOptions(String pattern) {
        final Pattern wordPattern = getRegexPattern(pattern);
        final List<Character> unplacedLetters = getUnplacedLetters(pattern);
        
        return wordsThatMatch(wordPattern.pattern()).stream()
                .filter(e -> unplacedLetters.stream().allMatch(l -> e.contains(String.valueOf(l))))
                .collect(Collectors.toList());
    }
    
    protected Pattern getRegexPattern(String pattern) {
        final String[] patternParts = pattern.split("\\s+");
        final String[] patternLetters = pattern.replaceAll("\\s*\\[" + charsetToken() + "*]\\s*$", "").split("\\s+");
        
        if (patternLetters.length != getWordLength()) {
            throw new RuntimeException("Pattern: '" + pattern + "' does not represent a word of length " + getWordLength());
        }
        
        final String letterOptions = getAbsentLetters(pattern).stream().map(String::valueOf)
                .reduce(charset(), (r, e) -> r.replace(e, ""));
        
        final StringBuilder wordPattern = new StringBuilder();
        for (String patternLetter : patternLetters) {
            if (patternLetter.equals("?")) {
                wordPattern.append('[').append(letterOptions).append(']');
                
            } else if (patternLetter.matches(charsetToken())) {
                wordPattern.append(patternLetter);
                
            } else if (patternLetter.matches("!" + charsetToken() + "+")) {
                final String thisLetterOptions = patternLetter.substring(1).chars().mapToObj(i -> String.valueOf((char) i))
                        .reduce(letterOptions, (r, e) -> r.replace(e, ""));
                wordPattern.append("[").append(thisLetterOptions).append("]");
                
            } else {
                throw new RuntimeException("Invalid pattern: '" + pattern + "'");
            }
        }
        return Pattern.compile(wordPattern.toString());
    }
    
    protected List<Character> getPlacedLetters(String pattern) {
        return Arrays.stream(pattern.split("\\s+"))
                .filter(e -> e.matches(charsetToken()))
                .flatMap(e -> e.chars().mapToObj(i -> (char) i))
                .distinct().collect(Collectors.toList());
    }
    
    protected List<Character> getUnplacedLetters(String pattern) {
        return Arrays.stream(pattern.split("\\s+"))
                .filter(e -> e.matches("!" + charsetToken() + "+"))
                .flatMap(e -> e.substring(1).chars().mapToObj(i -> (char) i))
                .distinct().collect(Collectors.toList());
    }
    
    protected List<Character> getAbsentLetters(String pattern) {
        return Arrays.stream(pattern.split("\\s+"))
                .filter(e -> e.matches("\\[" + charsetToken() + "+]"))
                .flatMap(e -> e.substring(1, (e.length() - 1)).chars().mapToObj(i -> (char) i))
                .distinct().collect(Collectors.toList());
    }
    
}
