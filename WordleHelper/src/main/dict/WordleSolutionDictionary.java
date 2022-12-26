/*
 * File:    WordleSolutionDictionary.java
 * Package: main.dict
 * Author:  Zachary Gill
 */

package main.dict;

public class WordleSolutionDictionary extends WordleGuessDictionary {
    
    //Constants
    
    public static final String DICT_NAME = "wordle-solutions";
    
    
    //Methods
    
    @Override
    public String getDictName() {
        return DICT_NAME;
    }
    
}
