/*
 * File:    WordleDictionaryInterface.java
 * Package: main.dict.core.wordle
 * Author:  Zachary Gill
 */

package main.dict.core.wordle;

import java.util.List;

import main.dict.core.base.BaseDictionaryInterface;

public interface WordleDictionaryInterface extends BaseDictionaryInterface {
    
    //Methods
    
    int getWordLength();
    
    List<String> findOptions(String pattern);
    
}
