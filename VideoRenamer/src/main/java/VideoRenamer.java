/*
 * File:    VideoRenamer.java
 * Package:
 * Author:  Zachary Gill
 */

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.Filesystem;
import common.StringUtility;

public class VideoRenamer {
    
    public enum VideoSet {
        ADVENTURE_TIME("adventureTime", "Adventure Time"),
        ALTERED_CARBON("alteredCarbon", "Altered Carbon"),
        AMERICAN_HORROR_STORY("americanHorrorStory", "American Horror Story"),
        AN_IDIOT_ABROAD("anIdiotAbroad", "An Idiot Abroad"),
        ASH_VS_EVIL_DEAD("ashVsEvilDead", "Ash vs Evil Dead"),
        BLACK_MIRROR("blackMirror", "Black Mirror"),
        BREAKING_BAD("breakingBad", "Breaking Bad"),
        DANGER_5("danger5", "Danger 5"),
        GAME_OF_THRONES("gameOfThrones", "Game Of Thrones"),
        HOUSE("house", "House"),
        HOUSE_OF_CARDS("houseOfCards", "House of Cards"),
        ITS_ALWAYS_SUNNY_IN_PHILADELPHIA("itsAlwaysSunnyInPhiladelphia", "It's Always Sunny in Philadelphia"),
        LEGION("legion", "Legion"),
        MR_ROBOT("mrRobot", "Mr. Robot"),
        RICK_AND_MORTY("rickAndMorty", "Rick and Morty"),
        SHERLOCK("sherlock", "Sherlock"),
        STARGATE_SG1("stargateSg1", "Stargate SG1"),
        STRANGER_THINGS("strangerThings", "Stranger Things"),
        THE_BOONDOCKS("theBoondocks", "The Boondocks"),
        THE_OFFICE("theOffice", "The Office"),
        TRAILER_PARK_BOYS("trailerParkBoys", "Trailer Park Boys"),
        TRUE_DETECTIVE("trueDetective", "True Detective"),
        TWIN_PEAKS("twinPeaks", "Twin Peaks"),
        WESTWORLD("westworld", "Westworld");
        
        String key;
        
        String name;
        
        VideoSet(String key, String name) {
            this.key = key;
            this.name = name;
        }
    }
    
    private static VideoSet activeVideoSet = VideoSet.AMERICAN_HORROR_STORY;
    
    private static final boolean doAll = true;
    
    private static Map<String, String> episodes = new HashMap<>();
    
    public static void main(String[] args) {
        if (doAll) {
            for (VideoSet videoSet : VideoSet.values()) {
                activeVideoSet = videoSet;
                System.out.println("***" + videoSet.name + "***");
                googleGridParser(new File("data/" + activeVideoSet.key + ".html"), new File("data/" + activeVideoSet.key + ".txt"));
                parseEpisodes(new File("data/" + activeVideoSet.key + ".txt"));
                renameVideos(new File("E:/Videos/" + activeVideoSet.name));
                System.out.println();
            }
        } else {
            googleGridParser(new File("data/" + activeVideoSet.key + ".html"), new File("data/" + activeVideoSet.key + ".txt"));
            parseEpisodes(new File("data/" + activeVideoSet.key + ".txt"));
            renameVideos(new File("E:/Videos/" + activeVideoSet.name));
        }
    }
    
    public static void googleGridParser(File in, File out) {
        Pattern textPattern = Pattern.compile("\\s*<div\\sclass=\"title\">(?<episode>S\\d+\\s?E\\d+)\\s?·\\s?(?<title>[^<]*)</div>\\s*");
        List<String> results = new ArrayList<>();
        List<String> episodes = new ArrayList<>();
        
        StringBuilder text = new StringBuilder();
        List<String> lines = Filesystem.readLines(in);
        for (String line : lines) {
            Matcher textMatcher = textPattern.matcher(line);
            if (textMatcher.matches() && !episodes.contains(textMatcher.group("episode"))) {
                results.add(textMatcher.group("episode") + " · " + textMatcher.group("title"));
                episodes.add(textMatcher.group("episode"));
            }
        }
        
        Filesystem.writeLines(out, results);
    }
    
    public static void parseEpisodes(File list) {
        List<String> lines = Filesystem.readLines(list);
        Pattern textPattern = Pattern.compile("(?<episode>S\\d+\\s?E\\d+)\\s?·\\s?(?<title>.+)");
        episodes.clear();
        for (String line : lines) {
            Matcher textMatcher = textPattern.matcher(line);
            if (textMatcher.matches()) {
                episodes.put(StringUtility.removeWhiteSpace(textMatcher.group("episode")), textMatcher.group("title").replaceAll("[?<>\"*]", "").replaceAll("[/\\\\:|]", "-").replace("&amp;", "&"));
            } else {
                System.err.println(line + " does not match");
            }
        }
    }
    
    public static void renameVideos(File dir) {
        List<File> videos = Filesystem.getFilesAndDirsRecursively(dir);
        Pattern videoNamePattern = Pattern.compile(activeVideoSet.name + "\\s-\\s" + "(?<episode>S\\d+E\\d+)(\\s-\\s(?<title>[^~]+))?\\.(?<fileExtension>.{3})");
        Pattern videoDoubleNamePattern = Pattern.compile(activeVideoSet.name + "\\s-\\s" + "(?<episode1>S\\d+E\\d+)(-(?<episode2>S\\d+E\\d+))?(\\s-\\s(?<title1>[^~]+)(\\s[+~]\\s(?<title2>[^~]+))?)?\\.(?<fileExtension>.{3})");
        for (File video : videos) {
            if (video.isDirectory() || !video.getAbsolutePath().contains(activeVideoSet.name + "\\" + "Season")) {
                continue;
            }
            String videoName = video.getName();
            Matcher videoNameMatcher = videoNamePattern.matcher(videoName);
            Matcher videoDoubleNameMatcher = videoDoubleNamePattern.matcher(videoName);
            if (videoNameMatcher.matches()) {
                if (episodes.containsKey(videoNameMatcher.group("episode"))) {
                    String newVideoName = activeVideoSet.name + " - " + videoNameMatcher.group("episode") + " - " + episodes.get(videoNameMatcher.group("episode")) + "." + videoNameMatcher.group("fileExtension");
                    File newVideo = new File(video.getParentFile(), newVideoName);
                    if (!newVideoName.equalsIgnoreCase(videoName)) {
                        if (!Filesystem.renameFile(video, newVideo)) {
                            System.out.println("Error Renaming: " + video.getAbsolutePath());
                        }
                    }
                } else {
                    System.out.println("No Key: " + video.getAbsolutePath());
                }
            } else if (videoDoubleNameMatcher.matches()) {
                if (episodes.containsKey(videoDoubleNameMatcher.group("episode1")) && episodes.containsKey(videoDoubleNameMatcher.group("episode2"))) {
                    String newVideoName = activeVideoSet.name + " - " + videoDoubleNameMatcher.group("episode1") + "-" + videoDoubleNameMatcher.group("episode2") + " - " + episodes.get(videoDoubleNameMatcher.group("episode1")) + " ~ " + episodes.get(videoDoubleNameMatcher.group("episode2")) + "." + videoDoubleNameMatcher.group("fileExtension");
                    File newVideo = new File(video.getParentFile(), newVideoName);
                    if (!newVideoName.equalsIgnoreCase(videoName)) {
                        if (!Filesystem.renameFile(video, newVideo)) {
                            System.out.println("Error Renaming: " + video.getAbsolutePath());
                        }
                    }
                } else {
                    System.out.println("No Key: " + video.getAbsolutePath());
                }
            } else {
                System.out.println("No Match: " + video.getAbsolutePath());
            }
        }
    }
    
}
