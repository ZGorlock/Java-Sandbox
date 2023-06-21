/*
 * File:    SubredditRegistry.java
 * Package: main.entity.shortcut.subreddit
 * Author:  Zachary Gill
 */

package main.entity.shortcut.subreddit;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import main.entity.base.Folder;

public class SubredditRegistry extends Folder<SubredditCategory> {
    
    //Static Fields
    
    public static final Map<File, SubredditRegistry> loaded = new ConcurrentHashMap<>();
    
    
    //Constructors
    
    protected SubredditRegistry(File registryDir) {
        super(registryDir);
        
        setEntities(SubredditCategory.loadAllSubredditCategories(registryDir));
    }
    
    
    //Getters
    
    public List<SubredditCategory> getSubredditCategories(SubredditAttributes.Attribute filter) {
        return getEntities().stream()
                .filter(e -> Optional.ofNullable(filter).map(e2 -> e.getAttributes().getAttributeMap().get(filter)).orElse(true))
                .collect(Collectors.toList());
    }
    
    public List<SubredditCategory> getSubredditCategories() {
        return getSubredditCategories(null);
    }
    
    public List<SubredditCategory> getAllSubredditCategories(SubredditAttributes.Attribute filter) {
        return getSubredditCategories().stream()
                .flatMap(e -> e.getAllSubredditCategories().stream())
                .filter(e -> Optional.ofNullable(filter).map(e2 -> e.getAttributes().getAttributeMap().get(filter)).orElse(true))
                .collect(Collectors.toList());
    }
    
    public List<SubredditCategory> getAllSubredditCategories() {
        return getAllSubredditCategories(null);
    }
    
    public List<Subreddit> getAllSubreddits(SubredditAttributes.Attribute filter) {
        return getSubredditCategories().stream()
                .flatMap(e -> e.getAllSubreddits().stream())
                .filter(e -> Optional.ofNullable(filter).map(e2 -> e.getAttributes().getAttributeMap().get(filter)).orElse(true))
                .collect(Collectors.toList());
    }
    
    public List<Subreddit> getAllSubreddits() {
        return getAllSubreddits(null);
    }
    
    public List<SubredditMulti> getAllMultiSubreddits(SubredditAttributes.Attribute filter) {
        return getSubredditCategories().stream()
                .flatMap(e -> e.getAllMultiSubreddits().stream())
                .filter(e -> Optional.ofNullable(filter).map(e2 -> e.getAttributes().getAttributeMap().get(filter)).orElse(true))
                .collect(Collectors.toList());
    }
    
    public List<SubredditMulti> getAllMultiSubreddits() {
        return getAllMultiSubreddits(null);
    }
    
    
    //Static Methods
    
    public static SubredditRegistry loadRegistry(File registryDir) {
        if (!registryDir.isDirectory()) {
            throw new UnsupportedOperationException();
        }
        return loaded.computeIfAbsent(registryDir, SubredditRegistry::new);
    }
    
}
