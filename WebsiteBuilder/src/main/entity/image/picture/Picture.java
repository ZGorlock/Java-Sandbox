/*
 * File:    Picture.java
 * Package: main.entity.image.picture
 * Author:  Zachary Gill
 */

package main.entity.image.picture;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import main.entity.image.Image;

public class Picture extends Image {
    
    //Static Fields
    
    public static final Map<File, Picture> loaded = new ConcurrentHashMap<>();
    
    
    //Constructors
    
    protected Picture(File image, boolean autoClean) {
        super(image, false);
        
        if (autoClean) {
            autoClean();
        }
    }
    
    protected Picture(File image) {
        this(image, CLEAN.auto());
    }
    
    
    //Methods
    
    @Override
    protected boolean needsProcessing() {
        return needsProcessing(Picture.class);
    }
    
    
    //Static Methods
    
    public static Picture loadPicture(File image) {
        if (image.isDirectory()) {
            throw new UnsupportedOperationException();
        }
        return loaded.computeIfAbsent(image, Picture::new);
    }
    
    public static List<Picture> loadAllPictures(File albumDir) {
        if (!albumDir.isDirectory()) {
            throw new UnsupportedOperationException();
        }
        return findPicturesInFolder(albumDir).stream()
                .map(Picture::loadPicture).collect(Collectors.toList());
    }
    
    @SuppressWarnings("RedundantStreamOptionalCall")
    public static List<File> findPicturesInFolder(File albumDir) {
        return findImagesInFolder(albumDir).stream()
                .filter(e -> true)
                .collect(Collectors.toList());
    }
    
}
