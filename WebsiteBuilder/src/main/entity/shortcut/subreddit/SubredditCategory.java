/*
 * File:    SubredditCategory.java
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
import java.util.stream.Stream;

import commons.object.string.StringUtility;
import main.entity.base.Tree;
import main.util.persistence.VariableUtil;

public class SubredditCategory extends Tree<Subreddit> {
    
    //Constants
    
    public static final String FAVORITE_FOLDER_NAME = VariableUtil.get(0x7478);
    
    public static final String BANNED_FOLDER_NAME = VariableUtil.get(0x22b3);
    
    public static final String PRIVATE_FOLDER_NAME = VariableUtil.get(0xae0b);
    
    
    //Static Fields
    
    public static final Map<File, SubredditCategory> loaded = new ConcurrentHashMap<>();
    
    
    //Fields
    
    protected SubredditAttributes attributes;
    
    protected SubredditMulti mutliSubreddit;
    
    
    //Constructors
    
    protected SubredditCategory(File categoryDir) {
        super(categoryDir);
        
        this.attributes = new SubredditAttributes(this);
        
        this.name = String.join(" ",
                StringUtility.fileString(categoryDir).replaceAll("^.*?/([^/]+)(?:/\\.[^/]+)?$", "$1"),
                attributes.titleTag()).trim();
        
        setEntities(Subreddit.loadAllSubreddits(categoryDir, false, true));
        setSubFolders(SubredditCategory.loadAllSubredditCategories(categoryDir));
        
        this.mutliSubreddit = new SubredditMulti(this);
    }
    
    
    //Methods
    
    @Override
    protected int count() {
        return super.count() + 1;
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    
    //Getters
    
    public SubredditMulti getMutliSubreddit() {
        return mutliSubreddit;
    }
    
    public SubredditAttributes getAttributes() {
        return attributes;
    }
    
    public List<SubredditCategory> getSubredditCategories(SubredditAttributes.Attribute filter) {
        return getSubFolders().stream()
                .map(SubredditCategory.class::cast)
                .filter(e -> Optional.ofNullable(filter).map(e2 -> e.getAttributes().getAttributeMap().get(filter)).orElse(true))
                .collect(Collectors.toList());
    }
    
    public List<SubredditCategory> getSubredditCategories() {
        return getSubredditCategories(null);
    }
    
    public List<SubredditCategory> getAllSubredditCategories(SubredditAttributes.Attribute filter) {
        return Stream.concat(
                        getSubredditCategories().stream(),
                        getSubredditCategories().stream()
                                .flatMap(e -> e.getAllSubredditCategories().stream()))
                .filter(e -> Optional.ofNullable(filter).map(e2 -> e.getAttributes().getAttributeMap().get(filter)).orElse(true))
                .collect(Collectors.toList());
    }
    
    public List<SubredditCategory> getAllSubredditCategories() {
        return getAllSubredditCategories(null);
    }
    
    public List<Subreddit> getSubreddits(SubredditAttributes.Attribute filter) {
        return getEntities().stream()
                .filter(e -> Optional.ofNullable(filter).map(e2 -> e.getAttributes().getAttributeMap().get(filter)).orElse(true))
                .collect(Collectors.toList());
    }
    
    public List<Subreddit> getSubreddits() {
        return getSubreddits(null);
    }
    
    public List<Subreddit> getAllSubreddits(SubredditAttributes.Attribute filter) {
        return Stream.concat(
                        getSubreddits().stream(),
                        getSubredditCategories().stream()
                                .flatMap(e -> e.getAllSubreddits().stream()))
                .filter(e -> Optional.ofNullable(filter).map(e2 -> e.getAttributes().getAttributeMap().get(filter)).orElse(true))
                .collect(Collectors.toList());
    }
    
    public List<Subreddit> getAllSubreddits() {
        return getAllSubreddits(null);
    }
    
    public List<SubredditMulti> getAllMultiSubreddits(SubredditAttributes.Attribute filter) {
        return Stream.concat(
                        Stream.of(getMutliSubreddit()),
                        getAllSubredditCategories().stream()
                                .map(SubredditCategory::getMutliSubreddit))
                .filter(e -> Optional.ofNullable(filter).map(e2 -> e.getAttributes().getAttributeMap().get(filter)).orElse(true))
                .collect(Collectors.toList());
    }
    
    public List<SubredditMulti> getAllMultiSubreddits() {
        return getAllMultiSubreddits(null);
    }
    
    
    //Static Methods
    
    public static SubredditCategory loadSubredditCategory(File categoryDir) {
        if (!categoryDir.isDirectory()) {
            throw new UnsupportedOperationException();
        }
        return Optional.ofNullable(loaded.get(categoryDir))
                .orElseGet(() -> {
                    final SubredditCategory subredditCategory = new SubredditCategory(categoryDir);
                    loaded.put(categoryDir, subredditCategory);
                    return subredditCategory;
                });
    }
    
    public static List<SubredditCategory> loadAllSubredditCategories(File subredditCategoryDir) {
        if (!subredditCategoryDir.isDirectory()) {
            throw new UnsupportedOperationException();
        }
        return findSubredditCategoryDirectoriesInFolder(subredditCategoryDir).stream()
                .map(SubredditCategory::loadSubredditCategory).collect(Collectors.toList());
    }
    
    @SuppressWarnings("RedundantStreamOptionalCall")
    public static List<File> findSubredditCategoryDirectoriesInFolder(File subredditCategoryDir) {
        return findEntityDirectoriesInFolder(subredditCategoryDir).stream()
                .filter(e -> true)
                .collect(Collectors.toList());
    }
    
}
