/*
 * File:    Episode.java
 * Package: main.entity.video.show
 * Author:  Zachary Gill
 */

package main.entity.video.show;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import main.entity.video.Video;

public class Episode extends Video {
    
    //Static Fields
    
    public static final Map<File, Episode> loaded = new ConcurrentHashMap<>();
    
    
    //Constructors
    
    protected Episode(File episode, boolean autoClean) {
        super(episode, false);
        
        if (autoClean) {
            autoClean();
        }
    }
    
    protected Episode(File episode) {
        this(episode, CLEAN.auto());
    }
    
    
    //Static Methods
    
    public static Episode loadEpisode(File clip) {
        if (clip.isDirectory()) {
            throw new UnsupportedOperationException();
        }
        return loaded.computeIfAbsent(clip, Episode::new);
    }
    
    public static List<Episode> loadAllEpisodes(File seriesDir) {
        if (!seriesDir.isDirectory()) {
            throw new UnsupportedOperationException();
        }
        return findEpisodesInFolder(seriesDir).stream()
                .map(Episode::loadEpisode).collect(Collectors.toList());
    }
    
    @SuppressWarnings("RedundantStreamOptionalCall")
    public static List<File> findEpisodesInFolder(File seriesDir) {
        return findVideosInFolder(new File(seriesDir, Series.EPISODE_DIR_NAME)).stream()
                .filter(e -> true)
                .collect(Collectors.toList());
    }
    
}
