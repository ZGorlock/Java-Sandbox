/*
 * File:    PhoneMusicProcessor.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import commons.access.Filesystem;

public class PhoneMusicProcessor {
    
    private static final File MUSIC_ROOT_DIR = new File("E:\\Music");
    
    private static final File MUSIC_DIR = new File("E:\\Music");
    
    private static final File PHONE_DIR = new File("G:\\Devices\\Phone\\Phone Music");
    
    private static final File PHONE_MUSIC_DIR = new File(PHONE_DIR, "Music");
    
    private static final File PHONE_PLAYLIST_DIR = new File(PHONE_DIR, "Playlists");
    
    private static final List<String> PLAYLISTS = List.of(
            "Playlists/Artist/The Comet Is Coming",
            
            "Playlists/Pandora/80s Pop Radio",
            "Playlists/Pandora/AC DC Radio",
            "Playlists/Pandora/Arctic Monkeys Radio",
            "Playlists/Pandora/Electric Light Orchestra Radio",
            "Playlists/Pandora/Migos Radio",
            "Playlists/Pandora/The Black Keys Radio",
            "Playlists/Pandora/Tim McGraw Radio",
            "Playlists/Pandora/Trap Remix Radio",
            
            "Youtube/Pop/Youtube Pop Favorites",
            "Youtube/Trap/Youtube Trap Favorites",
            
            "../Documents/DnD/Music/Bardify/Cities and Villages",
            "../Documents/DnD/Music/Bardify/Combat",
            "../Documents/DnD/Music/Bardify/Dungeons and Crypts",
            "../Documents/DnD/Music/Bardify/Events and Situations",
            "../Documents/DnD/Music/Bardify/Tavern",
            "../Documents/DnD/Music/Bardify/Travel"
    );
    
    public static void main(String[] args) {
        Filesystem.createDirectory(PHONE_DIR);
        Filesystem.createDirectory(PHONE_MUSIC_DIR);
        Filesystem.createDirectory(PHONE_PLAYLIST_DIR);
        
        final List<String> phoneSongs = new ArrayList<>();
        final List<String> phonePlaylists = new ArrayList<>();
        
        for (String playlist : PLAYLISTS) {
            final boolean isArtist = playlist.toUpperCase().contains("/ARTIST/");
            final boolean isPandora = playlist.toUpperCase().contains("/PANDORA/");
            final boolean isYoutube = playlist.toUpperCase().startsWith("YOUTUBE/");
            final boolean isDnD = playlist.toUpperCase().contains("/DND/");
            
            final String cleanedPlaylist = playlist
                    .replaceAll(".*/([^/]+)$", "$1")
                    .replaceAll("^(.+)$", ((isDnD ? "DnD - " : "") + "$1"))
                    .replaceAll("(?i)(\\sRadio)$", (isPandora ? "" : "$1"));
            
            final File playlistFile = new File(MUSIC_ROOT_DIR, playlist + ".m3u");
            final File phonePlaylistFile = new File(PHONE_PLAYLIST_DIR, cleanedPlaylist + ".m3u");
            
            System.out.println("Processing: " + phonePlaylistFile.getName());
            
            final List<String> playlistLines = Filesystem.readLines(playlistFile);
            final List<String> phonePlaylistLines = new ArrayList<>();
            
            playlistLines.forEach(song -> {
                final File songFile = song.contains(":") ? new File(song) : new File(playlistFile.getParentFile(), song);
                final File phoneArtistDir = new File(PHONE_MUSIC_DIR, (isDnD ? "DnD" : songFile.getParentFile().getName()));
                final File phoneSongFile = new File(phoneArtistDir, songFile.getName());
                
                if (!phoneSongFile.exists() || (Filesystem.checksum(songFile) != Filesystem.checksum(phoneSongFile))) {
                    System.out.println("          : " +
                            "Copying: '" + songFile.getAbsolutePath() + "' " +
                            "to: '" + phoneSongFile.getAbsolutePath() + "'");
                    Filesystem.createDirectory(phoneArtistDir);
                    Filesystem.copyFile(songFile, phoneSongFile, true);
                }
                
                phoneSongs.add(phoneSongFile.getAbsolutePath());
                phonePlaylistLines.add(String.join("/", "..", PHONE_MUSIC_DIR.getName(), phoneArtistDir.getName(), phoneSongFile.getName()));
            });
            System.out.println();
            
            phonePlaylists.add(phonePlaylistFile.getAbsolutePath());
            Filesystem.writeLines(phonePlaylistFile, phonePlaylistLines);
        }
        
        final List<File> existingPhonePlaylists = Filesystem.getFilesRecursively(PHONE_PLAYLIST_DIR);
        final List<File> existingPhoneSongs = Filesystem.getFilesRecursively(PHONE_MUSIC_DIR);
        final List<File> existingPhoneArtists = Filesystem.getDirsRecursively(PHONE_MUSIC_DIR);
        
        Stream.of(
                        existingPhonePlaylists.stream().filter(e -> !phonePlaylists.contains(e.getAbsolutePath())),
                        existingPhoneSongs.stream().filter(e -> !phoneSongs.contains(e.getAbsolutePath())),
                        existingPhoneArtists.stream().filter(Filesystem::directoryIsEmpty)
                ).flatMap(e -> e)
                .forEachOrdered(e -> {
                    System.out.println("Deleting: " + e.getAbsolutePath());
                    Filesystem.deleteFile(e);
                });
    }
    
}
