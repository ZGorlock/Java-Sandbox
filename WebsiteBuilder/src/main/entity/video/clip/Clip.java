/*
 * File:    Clip.java
 * Package: main.entity.video.clip
 * Author:  Zachary Gill
 */

package main.entity.video.clip;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import main.entity.video.Video;

public class Clip extends Video {
    
    //Static Fields
    
    public static final Map<File, Clip> loaded = new ConcurrentHashMap<>();
    
    
    //Constructors
    
    protected Clip(File clip, boolean autoClean) {
        super(clip, false);
        
        if (autoClean) {
            autoClean();
        }
    }
    
    protected Clip(File clip) {
        this(clip, CLEAN.auto());
    }
    
    
    //Methods
    
    @Override
    protected boolean needsProcessing() {
        return needsProcessing(Clip.class);
    }
    
    
    //Static Methods
    
    public static Clip loadClip(File clip) {
        if (clip.isDirectory()) {
            throw new UnsupportedOperationException();
        }
        return loaded.computeIfAbsent(clip, Clip::new);
    }
    
    public static List<Clip> loadAllClips(File clipCategoryDir) {
        if (!clipCategoryDir.isDirectory()) {
            throw new UnsupportedOperationException();
        }
        return findClipsInFolder(clipCategoryDir).stream()
                .map(Clip::loadClip).collect(Collectors.toList());
    }
    
    @SuppressWarnings("RedundantStreamOptionalCall")
    public static List<File> findClipsInFolder(File clipCategoryDir) {
        return findVideosInFolder(clipCategoryDir).stream()
                .filter(e -> true)
                .collect(Collectors.toList());
    }
    
}
