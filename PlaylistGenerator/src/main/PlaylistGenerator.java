/*
 * File:    PlaylistGenerator.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import common.Filesystem;
import common.StringUtility;

public class PlaylistGenerator {
    
    public static final File videoDir = new File("E:\\Videos");
    
    public static void main(String[] args) {
        List<String> skipDirs = Arrays.asList("Youtube", "Anime", "Short Films", "To Watch");
        List<File> shows = Filesystem.listFiles(videoDir,
                e -> e.isDirectory() && !skipDirs.contains(e.getName()));
        shows.addAll(Filesystem.getDirs(new File(videoDir, "Anime")));
        
        for (File show : shows) {
            String playlistPath = show.getAbsolutePath() + '\\';
            List<String> showPlaylist = new ArrayList<>();
            List<File> seasons = Filesystem.getDirs(show, "Season.*");
            seasons.sort(Comparator.comparingInt(o -> Integer.parseInt(StringUtility.trim(StringUtility.rSnip(o.getName(), 2)))));
            for (File season : seasons) {
                List<String> seasonPlaylist = new ArrayList<>();
                List<File> episodes = Filesystem.getFiles(season);
                for (File episode : episodes) {
                    seasonPlaylist.add(episode.getAbsolutePath().replace(playlistPath, ""));
                    showPlaylist.add(episode.getAbsolutePath().replace(playlistPath, ""));
                }
                Filesystem.writeLines(new File(show, season.getName() + ".m3u"), seasonPlaylist);
            }
            Filesystem.writeLines(new File(show, show.getName() + ".m3u"), showPlaylist);
        }
    }
    
}
