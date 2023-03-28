/*
 * File:    OriginalWordleGuessDictionary.java
 * Package: main.dict
 * Author:  Zachary Gill
 */

package main.dict;

public class OriginalWordleGuessDictionary extends WordleGuessDictionary {
    
    //Constants
    
    public static final String DICT_NAME = WordleGuessDictionary.DICT_NAME + "-original";
    
    
    //Methods
    
    @Override
    public String getDictName() {
        return DICT_NAME;
    }
    
}
