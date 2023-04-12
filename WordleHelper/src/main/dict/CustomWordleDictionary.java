/*
 * File:    CustomWordleDictionary.java
 * Package: main.dict
 * Author:  Zachary Gill
 */

package main.dict;

import java.util.List;

import main.dict.core.standard.StandardDictionary;
import main.dict.core.wordle.WordleDictionary;

public class CustomWordleDictionary extends WordleDictionary {
    
    //Fields
    
    protected final StandardDictionary rootDictionary;
    
    protected final int wordLength;
    
    
    //Constructors
    
    public CustomWordleDictionary(int wordLength, StandardDictionary dictionary) {
        this.wordLength = wordLength;
        this.rootDictionary = dictionary;
    }
    
    
    //Methods
    
    @Override
    public String getDictName() {
        return rootDictionary.getDictName() + "_" + wordLength;
    }
    
    @Override
    protected List<String> loadLexicon() {
        rootDictionary.load();
        return rootDictionary.wordsOfLength(wordLength);
    }
    
    @Override
    public int getWordLength() {
        return wordLength;
    }
    
}
