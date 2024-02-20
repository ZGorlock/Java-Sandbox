/*
 * File:    Folder.java
 * Package: main.entity.base
 * Author:  Zachary Gill
 */

package main.entity.base;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import commons.access.Filesystem;

public abstract class Folder<T extends Entity> extends Entity {
    
    //Fields
    
    protected File location;
    
    protected String name;
    
    protected List<T> entities;
    
    protected int count;
    
    
    //Constructors
    
    protected Folder(File location) {
        super(location, false);
        
        this.location = location;
        this.name = location.getName();
        
        this.entities = new ArrayList<>();
    }
    
    
    //Methods
    
    @Override
    protected boolean needsProcessing() {
        return true;
    }
    
    @Override
    protected int count() {
        return entities.stream().mapToInt(Entity::count).sum();
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    
    //Getters
    
    public File getLocation() {
        return location;
    }
    
    public String getName() {
        return name;
    }
    
    public List<T> getEntities() {
        return entities;
    }
    
    
    //Setters
    
    protected void setEntities(List<? extends T> entities) {
        this.entities.clear();
        this.entities.addAll(entities);
        this.count = count();
    }
    
    
    //Static Methods
    
    public static List<File> findEntityFilesInFolder(File entityDir) {
        return Filesystem.getFiles(entityDir,
                f -> true);
    }
    
    public static List<File> findEntityDirectoriesInFolder(File entityDir) {
        return Filesystem.getDirs(entityDir,
                d -> true);
    }
    
    public static void groupFoldersBySize(File source) {
        for (File d : Filesystem.getDirs(source)) {
            if (d.getName().startsWith("_")) {
                continue;
            }
            for (int n : List.of(10, 20, 50, 100, 200, 300, 400, 500, 750, 1000)) {
                if (Filesystem.getFiles(d).size() <= n) {
                    Filesystem.moveDirectory(d, new File(source, "_" + n + "\\" + d.getName()));
                    break;
                }
            }
        }
    }
    
}
