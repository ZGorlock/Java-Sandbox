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
