/*
 * File:    PictureAlbum.java
 * Package: main.entity.image.picture
 * Author:  Zachary Gill
 */

package main.entity.image.picture;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import main.entity.base.Folder;

public class PictureAlbum extends Folder<Picture> {
    
    //Static Fields
    
    public static final Map<File, PictureAlbum> loaded = new ConcurrentHashMap<>();
    
    
    //Constructors
    
    protected PictureAlbum(File galleryDir) {
        super(galleryDir);
        
        setEntities(Picture.loadAllPictures(galleryDir));
    }
    
    
    //Methods
    
    public List<Picture> getImagesOfType(String format) {
        return getEntities().stream()
                .filter(e -> e.getFileType().replace(".", "")
                        .equalsIgnoreCase(format.replace(".", "")))
                .collect(Collectors.toList());
    }
    
    
    //Static Methods
    
    public static PictureAlbum loadAlbum(File galleryDir) {
        if (!galleryDir.isDirectory()) {
            throw new UnsupportedOperationException();
        }
        return loaded.computeIfAbsent(galleryDir, PictureAlbum::new);
    }
    
}
