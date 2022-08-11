/*
 * File:    PhoneMusicProcessor.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import commons.access.Filesystem;

public class PhoneMusicProcessor {
    
    private static final File MUSIC_ROOT_DIR = new File("E:\\Music");
    
    private static final File MUSIC_DIR = new File("E:\\Music");
    
    private static final File PHONE_DIR = new File("F:\\Devices\\Phone\\Phone Music");
    
    private static final File PHONE_MUSIC_DIR = new File(PHONE_DIR, "Music");
    
    private static final File PHONE_PLAYLIST_DIR = new File(PHONE_DIR, "Playlists");
    
    private static final List<String> PLAYLISTS = List.of(
            "Playlists/The Comet Is Coming",
            "Pandora/80s Pop Radio",
            "Pandora/AC DC Radio",
            "Pandora/Arctic Monkeys Radio",
            "Pandora/Electric Light Orchestra Radio",
            "Pandora/Migos Radio",
            "Pandora/The Black Keys Radio",
            "Pandora/Tim McGraw Radio",
            "Pandora/Trap Remix Radio",
            "Youtube/Pop/Youtube Pop Favorites",
            "Youtube/Trap/Youtube Trap Favorites"
    );
    
    public static void main(String[] args) {
        Filesystem.createDirectory(PHONE_DIR);
        Filesystem.createDirectory(PHONE_MUSIC_DIR);
        Filesystem.createDirectory(PHONE_PLAYLIST_DIR);
        
        for (String playlist : PLAYLISTS) {
            final String cleanedPlaylist = playlist
                    .replaceAll(".*/([^/]+)$", "$1")
                    .replaceAll("(?i)\\sRadio$", "");
            
            final File playlistFile = new File(MUSIC_ROOT_DIR, playlist + ".m3u");
            final File phonePlaylistFile = new File(PHONE_PLAYLIST_DIR, cleanedPlaylist + ".m3u");
    
            System.out.println("Processing: " + phonePlaylistFile.getName());
            
            final List<String> playlistLines = Filesystem.readLines(playlistFile);
            final List<String> phonePlaylistLines = new ArrayList<>();
    
            playlistLines.forEach(song -> {
                final File songFile = song.contains(":") ? new File(song) : new File(playlistFile.getParentFile(), song);
                final File phoneArtistDir = new File(PHONE_MUSIC_DIR, songFile.getParentFile().getName());
                final File phoneSongFile = new File(phoneArtistDir, songFile.getName());
                
                Filesystem.createDirectory(phoneArtistDir);
                Filesystem.copyFile(songFile, phoneSongFile, false);
                
                phonePlaylistLines.add(String.join("/", "..", PHONE_MUSIC_DIR.getName(), phoneArtistDir.getName(), phoneSongFile.getName()));
            });
            
            Filesystem.writeLines(phonePlaylistFile, phonePlaylistLines);
        }
    }
    
}
