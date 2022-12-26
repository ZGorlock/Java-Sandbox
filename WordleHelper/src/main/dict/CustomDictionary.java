/*
 * File:    CustomDictionary.java
 * Package: main.dict
 * Author:  Zachary Gill
 */

package main.dict;

import java.io.File;
import java.util.List;
import java.util.UUID;

import main.dict.base.StandardDictionary;

public class CustomDictionary extends StandardDictionary {
    
    //Fields
    
    protected File customDictFile;
    
    protected List<String> customLexicon;
    
    public String customDictName;
    
    
    //Constructors
    
    public CustomDictionary(String name, List<String> lexicon) {
        this.customDictName = name;
        this.customLexicon = lexicon;
    }
    
    public CustomDictionary(List<String> lexicon) {
        this(UUID.randomUUID().toString(), lexicon);
    }
    
    public CustomDictionary(String name, File dictFile) {
        this.customDictName = name;
        this.customDictFile = dictFile;
    }
    
    public CustomDictionary(File dictFile) {
        this(UUID.randomUUID().toString(), dictFile);
    }
    
    
    //Methods
    
    @Override
    public String getDictName() {
        return customDictName;
    }
    
    @Override
    public File getDictFile() {
        return customDictFile;
    }
    
    @Override
    protected List<String> loadLexicon() {
        return customLexicon;
    }
    
}
