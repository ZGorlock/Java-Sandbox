/*
 * File:    EnglishDictionary.java
 * Package: main.dict
 * Author:  Zachary Gill
 */

package main.dict;

import java.util.List;

import main.dict.base.StandardDictionary;

public class EnglishDictionary extends StandardDictionary {
    
    //Constants
    
    public static final String DICT_NAME = "dictionary";
    
    
    //Methods
    
    @Override
    public String getDictName() {
        return DICT_NAME;
    }
    
    @Override
    public List<String> alphabet() {
        return LETTERS;
    }
    
}
