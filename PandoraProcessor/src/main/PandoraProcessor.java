/*
 * File:    PandoraProcessor.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.List;

import commons.access.Filesystem;
import commons.access.FilesystemMacro;
import commons.list.ListUtility;

public class PandoraProcessor {
    
    private static final File PANDORA = new File("E:/Music/Pandora");
    
    private static final File MUSIC = new File("E:/Music/Music");
    
    public static void main(String[] args) {
        fixPlaylists();
        moveNewSongs();
    }
    
    private static void fixPlaylists() {
        Filesystem.deleteFile(new File(PANDORA, "newMusic.m3u"));
        
        List<File> playlists = Filesystem.listFiles(PANDORA, e -> e.getName().endsWith(".m3u"));
        for (File playlist : playlists) {
            FilesystemMacro.unprependLinesInFile(playlist, "E:\\Music\\Pandora\\New Music\\");
            FilesystemMacro.unprependLinesInFile(playlist, "E:\\Music\\Music\\");
            FilesystemMacro.prependLinesInFile(playlist, "E:\\Music\\Music\\");
            
            List<String> lines = Filesystem.readLines(playlist);
            ListUtility.removeDuplicates(lines);
            Filesystem.writeLines(playlist, lines);
        }
    }
    
    private static void moveNewSongs() {
        final File newMusic = new File(PANDORA, "New Music");
        
        List<File> artists = Filesystem.getDirs(newMusic);
        for (File artist : artists) {
            final File artistDest = new File(MUSIC, artist.getName());
            if (!artistDest.exists()) {
                Filesystem.createDirectory(artistDest);
            }
            
            List<File> songs = Filesystem.getFiles(artist);
            for (File song : songs) {
                final File songDest = new File(artistDest, song.getName());
                if (!songDest.exists()) {
                    Filesystem.moveFile(song, songDest);
                } else {
                    if (song.length() > songDest.length()) {
                        Filesystem.moveFile(song, songDest, true);
                    } else {
                        Filesystem.deleteFile(song);
                    }
                }
            }
        }
        
        Filesystem.clearDirectory(newMusic);
    }
    
}
