/*
 * File:    WordleGuessDictionary.java
 * Package: main.dict
 * Author:  Zachary Gill
 */

package main.dict;

import main.dict.base.WordleDictionary;

public class WordleGuessDictionary extends WordleDictionary {
    
    //Constants
    
    public static final String DICT_NAME = "wordle-dictionary";
    
    public static final int WORD_LENGTH = 5;
    
    
    //Methods
    
    @Override
    public String getDictName() {
        return DICT_NAME;
    }
    
    @Override
    public int getWordLength() {
        return WORD_LENGTH;
    }
    
}
