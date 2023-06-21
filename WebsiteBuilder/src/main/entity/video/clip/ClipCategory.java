/*
 * File:    ClipCategory.java
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

import main.entity.base.Entity;
import main.entity.base.Tree;

public class ClipCategory extends Tree<Clip> {
    
    //Static Fields
    
    public static final Map<File, ClipCategory> loaded = new ConcurrentHashMap<>();
    
    
    //Fields
    
    protected final List<List<String>> lexicon = IntStream.range(0, ClipLibrary.TITLE_SEGMENTS)
            .mapToObj(ArrayList<String>::new)
            .collect(Collectors.toList());
    
    
    //Constructors
    
    protected ClipCategory(File categoryDir) {
        super(categoryDir);
        
        setEntities(Clip.loadAllClips(categoryDir));
    }
    
    
    //Getters
    
    protected List<List<String>> getLexicon() {
        return lexicon;
    }
    
    
    //Setters
    
    @Override
    protected void setEntities(List<? extends Clip> entities) {
        super.setEntities(entities);
        
        getEntities().stream()
                .map(Entity::getFileTitle)
                .map(title -> title.split("(?=[A-Z])", -1))
                .filter(titleSegments -> (titleSegments.length == ClipLibrary.TITLE_SEGMENTS))
                .forEach(titleSegments -> IntStream.range(0, ClipLibrary.TITLE_SEGMENTS)
                        .forEach(i -> lexicon.get(i).add(titleSegments[i])));
    }
    
    
    //Static Methods
    
    public static ClipCategory loadClipCategory(File categoryDir) {
        if (!categoryDir.isDirectory()) {
            throw new UnsupportedOperationException();
        }
        return loaded.computeIfAbsent(categoryDir, ClipCategory::new);
    }
    
    public static List<ClipCategory> loadAllClipCategories(File clipLibraryDir) {
        if (!clipLibraryDir.isDirectory()) {
            throw new UnsupportedOperationException();
        }
        return findClipCategoryDirectoriesInFolder(clipLibraryDir).stream()
                .map(ClipCategory::loadClipCategory).collect(Collectors.toList());
    }
    
    public static List<File> findClipCategoryDirectoriesInFolder(File clipLibraryDir) {
        return findEntityDirectoriesInFolder(clipLibraryDir).stream()
                .filter(e -> !e.getName().startsWith(META_PREFIX))
                .collect(Collectors.toList());
    }
    
}
