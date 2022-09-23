/*
 * File:    WordleHelper.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WordleHelper {
    
    //Static Fields
    
    private static String pattern = "S ? !A !T ? [LE]";
    
    private static int wordLength = 5;
    
    
    //Main Method
    
    public static void main(String[] args) {
        parseArguments(args);
        
        final List<String> options = findOptions(pattern);
        for (String option : options) {
            System.out.println(option);
        }
    }
    
    
    //Static Methods
    
    private static void parseArguments(String[] args) {
        switch (args.length) {
            case 2:
                wordLength = Integer.parseInt(args[1]);
            case 1:
                pattern = args[0];
        }
    }
    
    private static List<String> findOptions(String pattern) {
        final Pattern wordPattern = getRegexPattern(pattern);
        final List<Character> unplacedLetters = getUnplacedLetters(pattern);
        
        return Dictionary.wordsOfLength(wordLength).stream()
                .filter(e -> wordPattern.matcher(e).matches())
                .filter(e -> unplacedLetters.stream().allMatch(l -> e.contains(String.valueOf(l))))
                .collect(Collectors.toList());
    }
    
    private static Pattern getRegexPattern(String pattern) {
        final String[] patternParts = pattern.split("\\s+");
        final String[] patternLetters = pattern.replaceAll("\\s*\\[[A-Z]*]\\s*$", "").split("\\s+");
        
        if (patternLetters.length != wordLength) {
            throw new RuntimeException("Pattern: '" + pattern + "' does not indicate a word of length " + wordLength);
        }
        
        final String letterOptions = getAbsentLetters(pattern).stream().map(String::valueOf)
                .reduce(Dictionary.LETTERS, (r, e) -> r.replace(e, ""));
        
        final StringBuilder wordPattern = new StringBuilder();
        for (String patternLetter : patternLetters) {
            if (patternLetter.equals("?")) {
                wordPattern.append('[').append(letterOptions).append(']');
                
            } else if (patternLetter.matches("[A-Z]")) {
                wordPattern.append(patternLetter);
                
            } else if (patternLetter.matches("![A-Z]+")) {
                final String thisLetterOptions = patternLetter.substring(1).chars().mapToObj(i -> String.valueOf((char) i))
                        .reduce(letterOptions, (r, e) -> r.replace(e, ""));
                wordPattern.append("[").append(thisLetterOptions).append("]");
                
            } else {
                throw new RuntimeException("Invalid pattern: '" + pattern + "'");
            }
        }
        return Pattern.compile(wordPattern.toString());
    }
    
    private static List<Character> getPlacedLetters(String pattern) {
        return Arrays.stream(pattern.split("\\s+"))
                .filter(e -> e.matches("[A-Z]"))
                .flatMap(e -> e.chars().mapToObj(i -> (char) i))
                .distinct().collect(Collectors.toList());
    }
    
    private static List<Character> getUnplacedLetters(String pattern) {
        return Arrays.stream(pattern.split("\\s+"))
                .filter(e -> e.matches("![A-Z]+"))
                .flatMap(e -> e.substring(1).chars().mapToObj(i -> (char) i))
                .distinct().collect(Collectors.toList());
    }
    
    private static List<Character> getAbsentLetters(String pattern) {
        return Arrays.stream(pattern.split("\\s+"))
                .filter(e -> e.matches("\\[[A-Z]+]"))
                .flatMap(e -> e.substring(1, (e.length() - 1)).chars().mapToObj(i -> (char) i))
                .distinct().collect(Collectors.toList());
    }
    
}
