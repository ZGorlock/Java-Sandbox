/*
 * File:    SubredditMulti.java
 * Package: main.entity.shortcut.subreddit
 * Author:  Zachary Gill
 */

package main.entity.shortcut.subreddit;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.object.string.StringUtility;
import main.util.persistence.VariableUtil;

public class SubredditMulti extends Subreddit {
    
    //Constants
    
    public static final String MULTI_NAME = VariableUtil.get(0xe637);
    
    public static final Permission AUTO_BUILD = Permission.AUTO;
    
    public static final Permission DELETE_EMPTY = Permission.AUTO;
    
    
    //Fields
    
    private SubredditCategory category;
    
    private List<Subreddit> subreddits;
    
    
    //Constructors
    
    protected SubredditMulti(SubredditCategory category, boolean autoClean) {
        super(new File(category.getLocation(), (MULTI_NAME + DEFAULT_SHORTCUT_EXTENSION)), false);
        
        this.category = category;
        this.subreddits = Stream.concat(
                        category.getSubreddits().stream(),
                        category.getAllSubreddits(SubredditAttributes.Attribute.ALIVE).stream())
                .distinct().collect(Collectors.toList());
        
        if (autoClean) {
            autoClean();
        }
    }
    
    protected SubredditMulti(SubredditCategory category) {
        this(category, CLEAN.auto());
    }
    
    
    //Methods
    
    @Override
    protected boolean doCleanFile() {
        return Stream.of(
                super.doCleanFile(),
                DELETE_EMPTY.auto() && deleteEmpty(),
                AUTO_BUILD.auto() && autoBuild()
        ).anyMatch(e -> e);
    }
    
    public boolean deleteEmpty() {
        if (DELETE_EMPTY.denied() || !permitDeleteEmpty() || !permitProcessing()) {
            return false;
        }
        return doDeleteEmpty();
    }
    
    protected boolean permitDeleteEmpty() {
        return DELETE_EMPTY.allowed();
    }
    
    protected boolean doDeleteEmpty() {
        if (source.exists() && subreddits.isEmpty()) {
            delete();
        } else {
            return false;
        }
        return true;
    }
    
    public boolean autoBuild() {
        if (AUTO_BUILD.denied() || !permitAutoBuild() || !permitProcessing()) {
            return false;
        }
        return doAutoBuild();
    }
    
    protected boolean permitAutoBuild() {
        return AUTO_BUILD.allowed() &&
                !subreddits.isEmpty();
    }
    
    protected boolean doAutoBuild() {
        build(getSort());
        return true;
    }
    
    @Override
    protected boolean permitCheckHealth() {
        return false;
    }
    
    public void build(SortMethod sortMethod) {
        write();
    }
    
    public void build() {
        build(getSort());
    }
    
    @Override
    public String toString() {
        return StringUtility.format("{} ({})", category.toString(), getSubreddits().size());
    }
    
    
    //Getters
    
    public SubredditCategory getSubredditCategory() {
        return category;
    }
    
    public List<Subreddit> getSubreddits() {
        return subreddits;
    }
    
    @Override
    public String getSubName() {
        return getSubreddits().stream().map(Subreddit::getSubName).collect(Collectors.joining("+"));
    }
    
    @Override
    public String getSubDisplayName() {
        return getParentFilePath().replaceAll("^.*?/([^/]+)(?:/\\.[^/]+)?$", "$1");
    }
    
}
