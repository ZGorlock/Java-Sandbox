/*
 * File:    BillboardHot100Processor.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import commons.access.Filesystem;
import commons.file.media.FFmpeg;
import commons.object.collection.MapUtility;
import commons.object.string.StringUtility;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class BillboardHot100Processor {
    
    private static final File MUSIC_DIR = new File("E:\\Music\\Music");
    
    private static final File PLAYLIST_DIR = new File("E:\\Music\\Billboard Hot 100");
    
    private static final File WORK_DIR = new File("E:\\Downloads");
    
    public static void main(String[] args) {
//        organize();
//        customEdits();
//        updateTags();
//        fixOrdering();
//        combinePlaylists();
    }
    
    private static void organize() {
        File dir = new File(WORK_DIR, "a");
        File out = new File(WORK_DIR, "b");
        Filesystem.createDirectory(out);
        
        String startAt = null;
        boolean skip = startAt != null;
        
        List<File> fd = Filesystem.getDirs(dir);
        for (File d : fd) {
            if (skip) {
                if (d.getName().equals(startAt)) {
                    skip = false;
                } else {
                    System.out.println("Skip " + d.getName());
                    continue;
                }
            }
            
            List<File> playlist = new ArrayList<>();
            
            List<File> ff = Filesystem.getFiles(d);
            for (File f : ff) {
                FFmpeg.MediaInfo.MetadataTags tags = FFmpeg.getMetadata(f);
                String artist = tags.get("artist");
                String title = tags.get("title");
                if (artist == null || title == null) {
                    int manualFixNull = 1;
                }
                
                artist = artist
                        .replaceAll("/", " & ")
                        .replace("\"", "'")
                        .replaceAll("^\\*", "")
                        .replace(" *", " ");
                title = title
                        .replaceAll("Remastered:", "Remastered - ")
                        .replaceAll("\\?$", "")
                        .replaceAll("\\? \\(", " (")
                        .replaceAll("[/:]", " - ")
                        .replace("(from ", "(From ")
                        .replace("(From \"", "(From '")
                        .replace("(From the Film \"", "(From the Film '")
                        .replace("(From the Show \"", "(From the Show '")
                        .replace("(Theme from \"", "(Theme from '")
                        .replace("(Theme from the Film \"", "(Theme from the Film '")
                        .replace("(Theme from the Show \"", "(Theme from the Show '").replace("Motion Picture \"", "Motion Picture '")
                        .replace("\")", "')")
                        .replace("\" Soundtrack", "' Soundtrack")
                        .replace("\" Original Soundtrack", "' Original Soundtrack")
                        .replace("7\" Version", "7in Version")
                        .replace("7\" Mix", "7in Mix")
                        .replace("7\" Edit", "7in Edit")
                        .replace("12\" Version", "12in Version")
                        .replace("12\" Mix", "12in Mix")
                        .replace("12\" Edit", "12in Edit");
                
                artist = StringUtility.fixSpaces(artist);
                title = StringUtility.fixSpaces(title);
                
                if (artist.matches(".*[\\\\/:*?\"<>|].*") || title.matches(".*[\\\\/:*?\"<>|].*")) {
                    int manualFix = 1;
                }
                artist = artist.replaceAll("[\\\\/:*?\"<>|]", "-");
                title = title.replaceAll("[\\\\/:*?\"<>|]", "-");
                
                File moved = new File(new File(out, artist), title + ".mp3");
                Filesystem.copyFile(f, moved);
                
                playlist.add(moved);
            }
            
            List<String> playlistText = new ArrayList<>();
            for (File px : playlist) {
                playlistText.add(px.getAbsolutePath().replace(out.getAbsolutePath(), MUSIC_DIR.getAbsolutePath()));
            }
            Filesystem.writeLines(new File(out, d.getName() + " - Billboard Hot 100.m3u"), playlistText);
            
            System.out.println("Done " + d.getName());
            int yearDone = 1;
//            break;
        }
    }
    
    private static void customEdits() {
        File dir = new File(WORK_DIR, "b");
        File out = new File(WORK_DIR, "b");
        Filesystem.createDirectory(out);
        
        File customEditsCsv = new File("resources", "manual.csv");
        List<String> customOrig = new ArrayList<>();
        List<String> customEdit = new ArrayList<>();
        Pattern csvPattern = Pattern.compile("^\"(?<from>[^\"]+)\",\"(?<to>[^\"]+)\"$");
        for (String customEditCsvLine : Filesystem.readLines(customEditsCsv)) {
            Matcher csvMatcher = csvPattern.matcher(customEditCsvLine);
            if (csvMatcher.matches()) {
                customOrig.add(csvMatcher.group("from"));
                customEdit.add(csvMatcher.group("to"));
            }
        }
        Map<String, String> customEdits = MapUtility.mapOf(customOrig, customEdit);
        
        Map<String, List<String>> playlistTexts = new LinkedHashMap<>();
        for (File m3u : Filesystem.getFiles(dir)) {
            List<String> playlistText = Filesystem.readLines(m3u);
            playlistTexts.put(m3u.getName().replaceAll("\\..+$", ""),
                    playlistText.stream().map(e -> e.replace(MUSIC_DIR.getAbsolutePath(), "")).collect(Collectors.toList()));
        }
        
        for (File artist : Filesystem.getDirs(dir)) {
            if (customEdits.containsKey(artist.getName())) {
                String original = artist.getName();
                String replaced = customEdits.get(artist.getName());
                
                File newArtist = new File(artist.getParentFile(), replaced);
                for (File song : Filesystem.getFiles(artist)) {
                    for (Map.Entry<String, List<String>> playlist : playlistTexts.entrySet()) {
                        for (int i = 0; i < playlist.getValue().size(); i++) {
                            if (playlist.getValue().get(i).contains("\\" + original + "\\") || playlist.getValue().get(i).contains("\\" + original + ".\\")) {
                                playlist.getValue().set(i, playlist.getValue().get(i)
                                        .replace("\\" + original + ".\\", "\\" + replaced + "\\")
                                        .replace("\\" + original + "\\", "\\" + replaced + "\\"));
                                System.out.println("Renaming in " + playlist.getKey());
                            }
                        }
                    }
                    File newSong = new File(newArtist, song.getName());
                    System.out.println("Moving " + song.getAbsolutePath() + " - To " + newSong);
                    Filesystem.moveFile(song, newSong);
                }
                System.out.println("Deleting " + artist.getAbsolutePath());
                Filesystem.deleteDirectory(artist);
            }
        }
        
        for (Map.Entry<String, List<String>> playlist : playlistTexts.entrySet()) {
            Filesystem.writeLines(new File(out, playlist.getKey() + ".m3u"), playlist.getValue());
        }

//        Map<String, List<String>> count = new HashMap<>();
        for (File f : Filesystem.getFiles(out)) {
            List<String> lines = Filesystem.readLines(f);
            if (lines.isEmpty()) {
                int playlistEmpty = 0;
            }
            for (String line : lines) {
//                count.putIfAbsent(line, new ArrayList<>());
//                count.get(line).add(f.getName());
//                if (count.get(line).size() > 1) {
//                    System.out.println("Multiple references: " + line);
//                    for (String pl : count.get(line)) {
//                        System.out.println("   " + pl);
//                    }
//                }
                
                for (String doubleCheck : customEdits.keySet()) {
                    if (line.contains("\\" + doubleCheck + "\\")) {
                        int playlistBad = 1;
                    }
                }
            }
        }
    }
    
    private static void updateTags() {
        File dir = new File(WORK_DIR, "b");
        File out = new File(WORK_DIR, "c");
        Filesystem.createDirectory(out);
        
        for (File artist : Filesystem.getDirs(dir)) {
            for (File song : Filesystem.getFiles(artist)) {
                File moved = new File(new File(out, artist.getName()), song.getName());
                Filesystem.createFile(moved);
                FFmpeg.addMetadata(song, new FFmpeg.MediaInfo.MetadataTags((LinkedHashMap<String, String>)
                        MapUtility.mapOf(LinkedHashMap.class,
                                new ImmutablePair<>("title", song.getName()),
                                new ImmutablePair<>("artist", artist.getName()),
                                new ImmutablePair<>("album", "Billboard Hot 100"),
                                new ImmutablePair<>("track", null),
                                new ImmutablePair<>("disc", null)
                        )), moved);
                System.out.println("Done " + moved.getAbsolutePath());
            }
        }
        
        for (File m3u : Filesystem.getFiles(dir)) {
            List<String> finalLines = new ArrayList<>();
            for (String line : Filesystem.readLines(m3u)) {
                List<String> lineParts = StringUtility.tokenize(line, Matcher.quoteReplacement("\\"));
                File song = new File(new File(out, lineParts.get(1)), lineParts.get(2));
                if (!song.exists()) {
                    System.out.println("Song missing: " + song.getAbsolutePath() + " - From " + m3u.getName());
                }
                finalLines.add(MUSIC_DIR.getAbsolutePath() + line);
            }
            Filesystem.writeLines(new File(out, m3u.getName()), finalLines);
        }
        
        for (File m3u : Filesystem.getFiles(out)) {
            for (String line : Filesystem.readLines(m3u)) {
                List<String> lineParts = StringUtility.tokenize(line, Matcher.quoteReplacement("\\"));
                File song = new File(new File(out, lineParts.get(3)), lineParts.get(4));
                if (!song.exists()) {
                    System.out.println("Song missing: " + song.getAbsolutePath() + " - From " + m3u.getName());
                }
            }
        }
    }
    
    private static void fixOrdering() {
        File dir = new File(WORK_DIR, "c");
        
        for (File m3u : Filesystem.getFiles(dir)) {
            List<String> songs = Filesystem.readLines(m3u);
            if (songs.size() < 100) {
                continue;
            }
            
            String no100 = songs.get(10);
            songs.remove(10);
            songs.add(no100);
            Filesystem.writeLines(m3u, songs);
        }
        
    }
    
    public static void combinePlaylists() {
        for (File decadeDir : Filesystem.getDirs(PLAYLIST_DIR)) {
            
            File combinedPlaylist = new File(PLAYLIST_DIR, "Best of The " + decadeDir.getName() + ".m3u");
            
            List<String> content = new ArrayList<>();
            for (File yearPlaylist : Filesystem.getFiles(decadeDir)) {
                content.addAll(Filesystem.readLines(yearPlaylist));
            }
            
            List<String> finalContent = content.stream().filter(e -> !e.isEmpty()).distinct().collect(Collectors.toList());
            
            Filesystem.writeLines(combinedPlaylist, finalContent);
        }
    }
    
}
