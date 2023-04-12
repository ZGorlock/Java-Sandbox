/*
 * File:    WordleHelper.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.util.List;

import main.dict.CustomWordleDictionary;
import main.dict.EnglishDictionary;
import main.dict.WordleSolutionDictionary;
import main.dict.core.wordle.WordleDictionary;

public class WordleHelper {
    
    //Static Functions
    
    private static WordleDictionary DEFAULT_DICTIONARY =
            new WordleSolutionDictionary();
//            new WordleGuessDictionary();
//            new OriginalWordleSolutionDictionary();
//            new OriginalWordleGuessDictionary();
    
    
    //Static Fields
    
    private static String pattern = "S ? !A !T ? [LE]";
    
    private static WordleDictionary dictionary;
    
    
    //Main Method
    
    public static void main(String[] args) {
        init(args);
        
        final List<String> results = dictionary.findOptions(pattern);
//        final List<String> results = dictionary.wordsThatEndWith("ATE");
//        final List<String> results = dictionary.wordsThatContainAll("G", "D", "B");
//        final List<String> results = dictionary.unscrambleSequence("WYPCBM", true);
        
        results.forEach(System.out::println);
    }
    
    
    //Static Methods
    
    private static void init(String[] args) {
        Integer wordLength = null;
        switch (args.length) {
            case 2:
                wordLength = Integer.parseInt(args[1]);
            case 1:
                pattern = args[0];
        }
        
        if ((wordLength == null) || (wordLength == DEFAULT_DICTIONARY.getWordLength())) {
            dictionary = DEFAULT_DICTIONARY;
        } else {
            dictionary = new CustomWordleDictionary(wordLength, new EnglishDictionary());
        }
        dictionary.load();
    }
    
}
