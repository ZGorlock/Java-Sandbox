/*
 * File:    ClipLibrary.java
 * Package: main.entity.video.clip
 * Author:  Zachary Gill
 */

package main.entity.video.clip;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import commons.object.collection.ListUtility;
import main.entity.base.Folder;

public class ClipLibrary extends Folder<ClipCategory> {
    
    //Constants
    
    public static final int TITLE_SEGMENTS = 3;
    
    
    //Static Fields
    
    public static final Map<File, ClipLibrary> loaded = new ConcurrentHashMap<>();
    
    
    //Fields
    
    protected final List<List<String>> lexicon = IntStream.range(0, TITLE_SEGMENTS)
            .mapToObj(ArrayList<String>::new)
            .collect(Collectors.toList());
    
    
    //Constructors
    
    protected ClipLibrary(File libraryDir) {
        super(libraryDir);
        
        setEntities(ClipCategory.loadAllClipCategories(libraryDir));
    }
    
    
    //Methods
    
    public String generateTitleInLexicon() {
        return getLexicon().stream()
                .map(ListUtility::selectRandom)
                .collect(Collectors.joining());
    }
    
    
    //Getters
    
    protected List<List<String>> getLexicon() {
        return lexicon;
    }
    
    
    //Setters
    
    @Override
    protected void setEntities(List<? extends ClipCategory> entities) {
        super.setEntities(entities);
        
        getEntities().stream()
                .map(ClipCategory::getLexicon)
                .forEach(categoryLexicon -> IntStream.range(0, TITLE_SEGMENTS)
                        .forEach(i -> lexicon.get(i).addAll(categoryLexicon.get(i))));
    }
    
    
    //Static Methods
    
    public static ClipLibrary loadLibrary(File libraryDir) {
        if (!libraryDir.isDirectory()) {
            throw new UnsupportedOperationException();
        }
        return loaded.computeIfAbsent(libraryDir, ClipLibrary::new);
    }
    
}
