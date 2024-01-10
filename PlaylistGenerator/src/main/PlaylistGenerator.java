/*
 * File:    PlaylistGenerator.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import common.Filesystem;
import common.StringUtility;

public class PlaylistGenerator {
    
    public static final File showsDir = new File("E:\\Videos\\Shows");
    
    public static final File animeDir = new File("E:\\Videos\\Anime");
    
    public static void main(String[] args) {
        final List<File> shows = new ArrayList<>();
        shows.addAll(Filesystem.getDirs(showsDir));
        shows.addAll(Filesystem.getDirs(animeDir));
        
        for (File show : shows) {
            final List<String> showPlaylist = new ArrayList<>();
            final File showPlaylistFile = new File(show, show.getName() + ".m3u");
            final String playlistPath = show.getAbsolutePath() + '\\';
            
            final List<File> seasons = Filesystem.getDirs(show, "Season.*").stream()
                    .sorted(Comparator.comparingInt(o -> Integer.parseInt(StringUtility.trim(StringUtility.rSnip(o.getName(), 2)))))
                    .collect(Collectors.toList());
            
            for (File season : seasons) {
                final List<String> seasonPlaylist = new ArrayList<>();
                final File seasonPlaylistFile = new File(show, season.getName() + ".m3u");
                
                final List<File> episodes = Filesystem.getFiles(season).stream()
                        .sorted(Comparator.comparing(o -> o.getName().replaceAll(".*(S\\d+\\w\\d+).*", "$1")))
                        .collect(Collectors.toList());
                
                for (File episode : episodes) {
                    seasonPlaylist.add(episode.getAbsolutePath().replace(playlistPath, ""));
                    showPlaylist.add(episode.getAbsolutePath().replace(playlistPath, ""));
                }
                
                System.out.println(seasonPlaylistFile.getAbsolutePath());
                Filesystem.writeLines(seasonPlaylistFile, seasonPlaylist);
            }
            
            System.out.println(showPlaylistFile.getAbsolutePath());
            Filesystem.writeLines(showPlaylistFile, showPlaylist);
            
            System.out.println();
        }
    }
    
}
