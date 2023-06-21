/*
 * File:    Folder.java
 * Package: main.entity.base
 * Author:  Zachary Gill
 */

package main.entity.base;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class Tree<T extends Entity> extends Folder<T> {
    
    //Fields
    
    protected List<Tree<T>> subFolders;
    
    
    //Constructors
    
    protected Tree(File location) {
        super(location);
        
        this.subFolders = new ArrayList<>();
    }
    
    
    //Methods
    
    @Override
    protected int count() {
        return super.count() +
                subFolders.stream().mapToInt(Entity::count).sum();
    }
    
    
    //Getters
    
    public List<Tree<T>> getSubFolders() {
        return subFolders;
    }
    
    public int getSubFolderCount() {
        return subFolders.size();
    }
    
    
    //Setters
    
    protected void setSubFolders(List<? extends Tree<T>> subFolders) {
        this.subFolders.clear();
        this.subFolders.addAll(subFolders);
        this.count = count();
    }
    
}
