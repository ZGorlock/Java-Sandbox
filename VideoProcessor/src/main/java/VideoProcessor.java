/*
 * File:    VideoProcessor.java
 * Package:
 * Author:  Zachary Gill
 */

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.CmdLine;
import common.Filesystem;
import common.StringUtility;

public class VideoProcessor {
    
    public static final File videoDir = new File("E:\\Videos");
    
    public static final File log = new File("log/" + new SimpleDateFormat("YYYY-MM-dd").format(new Date()) + ".txt");
    
    public static final File statsFile = new File("stats.csv");
    
    public static final File dirStatsFile = new File("stats.dir.csv");
    
    public static void main(String[] args) {
//        convertShowToMp4();
//        convertDirToMp4();
//        stripMetadataAndChapters();
//        addSubtitles();
//        lossTest();
//        Map<String, Map<String, String>> stats = produceStats();
//        Map<String, Map<String, String>> dirStats = produceDirStats();
        makePlaylists();
    }
    
    private static String ffmpeg(String cmd, boolean printOutput) {
        cmd = "ffmpeg -hide_banner " + cmd;
        if (printOutput) {
            System.out.println(cmd);
        }
        CmdLine.printOutput = printOutput;
        String cmdLog = CmdLine.executeCmd(cmd);
        CmdLine.printOutput = false;
        if (cmdLog.contains("Error") && !cmd.contains("Error")) {
            System.err.println("Error in ffmpeg: " + cmd);
        }
        Filesystem.writeStringToFile(log, cmdLog + "\n" + StringUtility.fillStringOfLength('-', 120) + "\n\n", true);
        return cmdLog;
    }
    
    private static String ffmpeg(String cmd) {
        return ffmpeg(cmd, false);
    }
    
    private static Map<String, Map<String, String>> produceStats() {
        Map<String, Map<String, String>> stats = produceStatsHelper(videoDir, 1);
//        Map<String, Map<String, String>> stats = produceStatsHelper(new File(videoDir, "The Blacklist"), 1);
        
        List<String> csvOutput = new ArrayList<>();
        List<String> output = new ArrayList<>();
        
        Map<String, Integer> columnWidths = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : stats.entrySet()) {
            columnWidths.putIfAbsent("Key", 0);
            int keyLength = entry.getKey().length();
            if (columnWidths.get("Key") < keyLength) {
                columnWidths.replace("Key", keyLength);
            }
            for (Map.Entry<String, String> statEntry : entry.getValue().entrySet()) {
                columnWidths.putIfAbsent(statEntry.getKey(), 0);
                int length = (statEntry.getKey() + ": " + statEntry.getValue()).length();
                if (columnWidths.get(statEntry.getKey()) < length) {
                    columnWidths.replace(statEntry.getKey(), length);
                }
            }
        }
        
        for (Map.Entry<String, Map<String, String>> entry : stats.entrySet()) {
            String key = entry.getKey();
            Map<String, String> value = entry.getValue();

//                if (value.containsKey("Video Codec") && !value.get("Video Codec").startsWith("hevc")) {
//                    System.out.println("Video Codec not h265: " + key);
//                }
            if (value.containsKey("Audio Streams") && !value.get("Audio Streams").equalsIgnoreCase("1")) {
                System.out.println("Multiple Audio Streams: " + key);
            }
            if (value.containsKey("Subtitle Streams") && !value.get("Subtitle Streams").equalsIgnoreCase("1") && !value.get("Subtitle Streams").equalsIgnoreCase("0")) {
                System.out.println("Multiple Subtitle Streams: " + key);
            }
            if (value.containsKey("Video Language") && !value.get("Video Language").contains("eng") && !value.get("Video Language").contains("und")) {
                System.out.println("Non-English Video Stream: " + key);
            }
            if (value.containsKey("Audio Language") && !value.get("Audio Language").contains("eng") && !value.get("Audio Language").contains("und")) {
                System.out.println("Non-English Audio Stream: " + key);
            }
            if (value.containsKey("Subtitle Language") && !value.get("Subtitle Language").contains("eng") && !value.get("Subtitle Language").contains("und")) {
                System.out.println("Non-English Subtitle Stream: " + key);
            }
            
            String cvsLine;
            if (entry.getValue().containsKey("Count")) {
                cvsLine = "\"" + key + "\"," +
                        "\"" + (value.containsKey("Size") ? ("Size: " + value.get("Size")) : "") + "\"," +
                        "\"" + (value.containsKey("Extension") ? ("Extension: " + value.get("Extension")) : "") + "\"," +
                        "\"" + (value.containsKey("Count") ? ("Count: " + value.get("Count")) : "") + "\"," +
                        "\"" + (value.containsKey("Min Video Bitrate") ? ("Min Video Bitrate: " + value.get("Min Video Bitrate")) : "") + "\"," +
                        "\"" + (value.containsKey("Max Video Bitrate") ? ("Max Video Bitrate: " + value.get("Max Video Bitrate")) : "") + "\"," +
                        "\"" + (value.containsKey("Min Audio Bitrate") ? ("Min Audio Bitrate: " + value.get("Min Audio Bitrate")) : "") + "\"," +
                        "\"" + (value.containsKey("Max Audio Bitrate") ? ("Max Audio Bitrate: " + value.get("Max Audio Bitrate")) : "") + "\"";
            } else {
                cvsLine = "\"" + key + "\"," +
                        "\"" + (value.containsKey("Size") ? ("Size: " + value.get("Size")) : "") + "\"," +
                        "\"" + (value.containsKey("Extension") ? ("Extension: " + value.get("Extension")) : "") + "\"," +
                        "\"" + (value.containsKey("Length") ? ("Length: " + value.get("Length")) : "") + "\"," +
                        "\"" + (value.containsKey("Bitrate") ? ("Bitrate: " + value.get("Bitrate")) : "") + "\"," +
                        "\"" + (value.containsKey("Streams") ? ("Streams: " + value.get("Streams")) : "") + "\"," +
                        "\"" + (value.containsKey("Video Streams") ? ("Video Streams: " + value.get("Video Streams")) : "") + "\"," +
                        "\"" + (value.containsKey("Audio Streams") ? ("Audio Streams: " + value.get("Audio Streams")) : "") + "\"," +
                        "\"" + (value.containsKey("Subtitle Streams") ? ("Subtitle Streams: " + value.get("Subtitle Streams")) : "") + "\"," +
                        "\"" + (value.containsKey("Video Codec") ? ("Video Codec: " + value.get("Video Codec")) : "") + "\"," +
                        "\"" + (value.containsKey("Dimensions") ? ("Dimensions: " + value.get("Dimensions")) : "") + "\"," +
                        "\"" + (value.containsKey("Video Bitrate") ? ("Video Bitrate: " + value.get("Video Bitrate")) : "") + "\"," +
                        "\"" + (value.containsKey("Framerate") ? ("Framerate: " + value.get("Framerate")) : "") + "\"," +
                        "\"" + (value.containsKey("Video Language") ? ("Video Language: " + value.get("Video Language")) : "") + "\"," +
                        "\"" + (value.containsKey("Audio Codec") ? ("Audio Codec: " + value.get("Audio Codec")) : "") + "\"," +
                        "\"" + (value.containsKey("Frequency") ? ("Frequency: " + value.get("Frequency")) : "") + "\"," +
                        "\"" + (value.containsKey("Channel") ? ("Channel: " + value.get("Channel")) : "") + "\"," +
                        "\"" + (value.containsKey("Audio Bitrate") ? ("Audio Bitrate: " + value.get("Audio Bitrate")) : "") + "\"," +
                        "\"" + (value.containsKey("Audio Language") ? ("Audio Language: " + value.get("Audio Language")) : "") + "\"," +
                        "\"" + (value.containsKey("Subtitle Codec") ? ("Subtitle Codec: " + value.get("Subtitle Codec")) : "") + "\"," +
                        "\"" + (value.containsKey("Subtitle Language") ? ("Subtitle Language: " + value.get("Subtitle Language")) : "") + "\"";
            }
            csvOutput.add(cvsLine);
            
            String line;
            if (entry.getValue().containsKey("Count")) {
                line = String.format("%-" + columnWidths.get("Key") + "s  " +
                                "%-" + columnWidths.get("Size") + "s  " +
                                "%-" + columnWidths.get("Count") + "s  " +
                                "%-" + columnWidths.get("Min Video Bitrate") + "s  " +
                                "%-" + columnWidths.get("Max Video Bitrate") + "s  " +
                                "%-" + columnWidths.get("Min Audio Bitrate") + "s  " +
                                "%-" + columnWidths.get("Max Audio Bitrate") + "s",
                        key,
                        value.containsKey("Size") ? ("Size: " + value.get("Size")) : "",
                        value.containsKey("Extension") ? ("Extension: " + value.get("Extension")) : "",
                        value.containsKey("Count") ? ("Count: " + value.get("Count")) : "",
                        value.containsKey("Min Video Bitrate") ? ("Min Video Bitrate: " + value.get("Min Video Bitrate")) : "",
                        value.containsKey("Max Video Bitrate") ? ("Max Video Bitrate: " + value.get("Max Video Bitrate")) : "",
                        value.containsKey("Min Audio Bitrate") ? ("Min Audio Bitrate: " + value.get("Min Audio Bitrate")) : "",
                        value.containsKey("Max Audio Bitrate") ? ("Max Audio Bitrate: " + value.get("Max Audio Bitrate")) : "");
            } else {
                line = String.format("%-" + columnWidths.get("Key") + "s  " +
                                "%-" + columnWidths.get("Size") + "s  " +
                                "%-" + columnWidths.get("Extension") + "s  " +
                                "%-" + columnWidths.get("Length") + "s  " +
                                "%-" + columnWidths.get("Bitrate") + "s  " +
                                "%-" + columnWidths.get("Streams") + "s  " +
                                "%-" + columnWidths.get("Video Streams") + "s  " +
                                "%-" + columnWidths.get("Audio Streams") + "s  " +
                                "%-" + columnWidths.get("Subtitle Streams") + "s  " +
                                "%-" + columnWidths.get("Video Codec") + "s  " +
                                "%-" + columnWidths.get("Dimensions") + "s  " +
                                "%-" + columnWidths.get("Video Bitrate") + "s  " +
                                "%-" + columnWidths.get("Framerate") + "s  " +
                                "%-" + columnWidths.get("Video Language") + "s  " +
                                "%-" + columnWidths.get("Audio Codec") + "s  " +
                                "%-" + columnWidths.get("Frequency") + "s  " +
                                "%-" + columnWidths.get("Channel") + "s  " +
                                "%-" + columnWidths.get("Audio Bitrate") + "s  " +
                                "%-" + columnWidths.get("Audio Language") + "s  " +
                                "%-" + columnWidths.get("Subtitle Codec") + "s  " +
                                "%-" + columnWidths.get("Subtitle Language") + "s",
                        key,
                        value.containsKey("Size") ? ("Size: " + value.get("Size")) : "",
                        value.containsKey("Extension") ? ("Extension: " + value.get("Extension")) : "",
                        value.containsKey("Length") ? ("Length: " + value.get("Length")) : "",
                        value.containsKey("Bitrate") ? ("Bitrate: " + value.get("Bitrate")) : "",
                        value.containsKey("Streams") ? ("Streams: " + value.get("Streams")) : "",
                        value.containsKey("Video Streams") ? ("Video Streams: " + value.get("Video Streams")) : "",
                        value.containsKey("Audio Streams") ? ("Audio Streams: " + value.get("Audio Streams")) : "",
                        value.containsKey("Subtitle Streams") ? ("Subtitle Streams: " + value.get("Subtitle Streams")) : "",
                        value.containsKey("Video Codec") ? ("Video Codec: " + value.get("Video Codec")) : "",
                        value.containsKey("Dimensions") ? ("Dimensions: " + value.get("Dimensions")) : "",
                        value.containsKey("Video Bitrate") ? ("Video Bitrate: " + value.get("Video Bitrate")) : "",
                        value.containsKey("Framerate") ? ("Framerate: " + value.get("Framerate")) : "",
                        value.containsKey("Video Language") ? ("Video Language: " + value.get("Video Language")) : "",
                        value.containsKey("Audio Codec") ? ("Audio Codec: " + value.get("Audio Codec")) : "",
                        value.containsKey("Frequency") ? ("Frequency: " + value.get("Frequency")) : "",
                        value.containsKey("Channel") ? ("Channel: " + value.get("Channel")) : "",
                        value.containsKey("Audio Bitrate") ? ("Audio Bitrate: " + value.get("Audio Bitrate")) : "",
                        value.containsKey("Audio Language") ? ("Audio Language: " + value.get("Audio Language")) : "",
                        value.containsKey("Subtitle Codec") ? ("Subtitle Codec: " + value.get("Subtitle Codec")) : "",
                        value.containsKey("Subtitle Language") ? ("Subtitle Language: " + value.get("Subtitle Language")) : "");
            }
            output.add(line);
        }
        Filesystem.writeLines(statsFile, csvOutput);
        Filesystem.writeLines(new File(statsFile.getAbsolutePath().replace(".csv", ".txt")), output);
        
        return stats;
    }
    
    private static Map<String, Map<String, String>> produceStatsHelper(File dir, int depth) {
        DecimalFormat df = new DecimalFormat("#.##");
        Pattern stream = Pattern.compile("\\s*Stream\\s#(?<stream>\\d+:\\d+).*:.*");
        Pattern duration = Pattern.compile("\\s*Duration:\\s(?<length>\\d+:\\d+:\\d+\\.\\d+),\\s.*bitrate:\\s(?<bitrate>\\d+\\skb/s).*");
        Pattern videoStream = Pattern.compile("\\s*Stream\\s#\\d+:\\d+(?<language>.*):\\sVideo:\\s(?<codec>[^,]+),\\s[^,(]*(\\([^,]+(,[^,]+)*\\))?,\\s(?<dimensions>\\d+x\\d+)(\\s.*)?,\\s(?<bitrate>\\d+\\skb/s),.*\\s(?<framerate>\\d+(\\.\\d+)?\\sfps),\\s.*");
        Pattern audioStream = Pattern.compile("\\s*Stream\\s#\\d+:\\d+(?<language>.*):\\sAudio:\\s(?<codec>[^,]+),\\s(?<frequency>\\d+\\sHz),\\s(?<channel>[^,]+),.*\\s(?<bitrate>\\d+\\skb/s).*");
        Pattern subtitleStream = Pattern.compile("\\s*Stream\\s#\\d+:\\d+(?<language>.*):\\sSubtitle:\\s(?<codec>[^\\s]+)\\s.*");
        
        Map<String, Map<String, String>> stats = new LinkedHashMap<>();
        
        Map<String, String> vStats = new LinkedHashMap<>();
        String vName = StringUtility.spaces(3 * (depth - 1)) + "|-\\ " + dir.getAbsolutePath().replace(videoDir.getParentFile().getAbsolutePath(), "");
        stats.put(vName, vStats);
        vStats.put("Name", dir.getName());
        vStats.put("Size", "0.0 MB");
        vStats.put("Count", "0");
        vStats.put("Min Video Bitrate", "9999999 kb/s");
        vStats.put("Max Video Bitrate", "-9999999 kb/s");
        vStats.put("Min Audio Bitrate", "9999999 kb/s");
        vStats.put("Max Audio Bitrate", "-9999999 kb/s");
        
        for (File f : Filesystem.getFiles(dir)) {
            if (f.getName().endsWith("txt") || f.getName().endsWith("ini")) {
                System.out.println("Not MP4: " + f.getAbsolutePath());
                continue;
            }
            Map<String, String> fStats = new LinkedHashMap<>();
            String fName = StringUtility.spaces(3 * depth) + "|- " + f.getName();
            stats.put(fName, fStats);
            
            fStats.put("Name", f.getName());
            fStats.put("Parent", dir.getName());
            vStats.put("Count", String.valueOf(Integer.parseInt(vStats.get("Count")) + 1));
            fStats.put("Extension", StringUtility.rSnip(f.getName(), 3));
            double size = (double) f.length() / (1024 * 1024);
            fStats.put("Size", df.format(size) + " MB");
            vStats.put("Size", df.format(Double.parseDouble(StringUtility.rShear(vStats.get("Size"), " MB".length())) + size) + " MB");
            fStats.put("Streams", "0");
            fStats.put("Video Streams", "0");
            fStats.put("Audio Streams", "0");
            fStats.put("Subtitle Streams", "0");
            
            if (!f.getName().endsWith("mp4")) {
                System.out.println("Not MP4: " + f.getAbsolutePath());
                continue;
            }
            List<String> details = StringUtility.splitLines(ffmpeg("-i \"" + f.getAbsolutePath() + "\""));
            
            for (String line : details) {
                Matcher streamMatcher = stream.matcher(line);
                Matcher durationMatcher = duration.matcher(line);
                Matcher videoStreamMatcher = videoStream.matcher(line);
                Matcher audioStreamMatcher = audioStream.matcher(line);
                Matcher subtitleStreamMatcher = subtitleStream.matcher(line);
                if (streamMatcher.matches()) {
                    fStats.put("Streams", String.valueOf(Integer.parseInt(fStats.get("Streams")) + 1));
                }
                
                try {
                    if (durationMatcher.matches()) {
                        fStats.put("Length", durationMatcher.group("length"));
                        fStats.put("Bitrate", durationMatcher.group("bitrate"));
                        
                    } else if (videoStreamMatcher.matches()) {
                        fStats.put("Video Streams", String.valueOf(Integer.parseInt(fStats.get("Video Streams")) + 1));
                        fStats.put("Video Codec", videoStreamMatcher.group("codec"));
                        fStats.put("Dimensions", videoStreamMatcher.group("dimensions"));
                        fStats.put("Video Bitrate", videoStreamMatcher.group("bitrate"));
                        fStats.put("Framerate", videoStreamMatcher.group("framerate"));
                        fStats.put("Video Language", videoStreamMatcher.group("language"));
                        
                        int bitrate = Integer.parseInt(videoStreamMatcher.group("bitrate").replace(" kb/s", ""));
                        int minBitrate = Integer.parseInt(StringUtility.rShear(vStats.get("Min Video Bitrate"), " kb/s".length()));
                        int maxBitrate = Integer.parseInt(StringUtility.rShear(vStats.get("Max Video Bitrate"), " kb/s".length()));
                        if (bitrate < minBitrate) {
                            vStats.put("Min Video Bitrate", bitrate + " kb/s");
                        }
                        if (bitrate > maxBitrate) {
                            vStats.put("Max Video Bitrate", bitrate + " kb/s");
                        }
                        
                    } else if (audioStreamMatcher.matches()) {
                        fStats.put("Audio Streams", String.valueOf(Integer.parseInt(fStats.get("Audio Streams")) + 1));
                        fStats.put("Audio Codec", audioStreamMatcher.group("codec"));
                        fStats.put("Frequency", audioStreamMatcher.group("frequency"));
                        fStats.put("Channel", audioStreamMatcher.group("channel"));
                        fStats.put("Audio Bitrate", audioStreamMatcher.group("bitrate"));
                        fStats.put("Audio Language", audioStreamMatcher.group("language"));
                        
                        int bitrate = Integer.parseInt(audioStreamMatcher.group("bitrate").replace(" kb/s", ""));
                        int minBitrate = Integer.parseInt(StringUtility.rShear(vStats.get("Min Audio Bitrate"), " kb/s".length()));
                        int maxBitrate = Integer.parseInt(StringUtility.rShear(vStats.get("Max Audio Bitrate"), " kb/s".length()));
                        if (bitrate < minBitrate) {
                            vStats.put("Min Audio Bitrate", bitrate + " kb/s");
                        }
                        if (bitrate > maxBitrate) {
                            vStats.put("Max Audio Bitrate", bitrate + " kb/s");
                        }
                        
                    } else if (subtitleStreamMatcher.matches()) {
                        fStats.put("Subtitle Streams", String.valueOf(Integer.parseInt(fStats.get("Subtitle Streams")) + 1));
                        fStats.put("Subtitle Codec", subtitleStreamMatcher.group("codec"));
                        fStats.put("Subtitle Language", subtitleStreamMatcher.group("language"));
                        
                    }
                } catch (Exception e) {
                    int x = 4;
                }
            }
            
            if (Integer.parseInt(fStats.get("Streams")) != (Integer.parseInt(fStats.get("Video Streams")) + Integer.parseInt(fStats.get("Audio Streams")) + Integer.parseInt(fStats.get("Subtitle Streams")))) {
                System.err.println("Error: " + f.getAbsolutePath());
            }
        }
        
        for (File d : Filesystem.getDirs(dir)) {
            if (d.getName().equalsIgnoreCase("old") ||
                    d.getName().equalsIgnoreCase("Youtube")) {
                continue;
            }
            
            Map<String, Map<String, String>> dStats = produceStatsHelper(d, depth + 1);
            String dName = StringUtility.spaces(3 * depth) + "|-\\ " + d.getAbsolutePath().replace(videoDir.getParentFile().getAbsolutePath(), "");
            
            vStats.put("Size", df.format(Double.parseDouble(StringUtility.rShear(vStats.get("Size"), " MB".length())) + Double.parseDouble(StringUtility.rShear(dStats.get(dName).get("Size"), " MB".length()))) + " MB");
            vStats.put("Count", df.format(Integer.parseInt(vStats.get("Count")) + Integer.parseInt(dStats.get(dName).get("Count"))));
            
            int dMinVideoBitrate = Integer.parseInt(StringUtility.rShear(dStats.get(dName).get("Min Video Bitrate"), " kb/s".length()));
            int dMaxVideoBitrate = Integer.parseInt(StringUtility.rShear(dStats.get(dName).get("Max Video Bitrate"), " kb/s".length()));
            int minVideoBitrate = Integer.parseInt(StringUtility.rShear(vStats.get("Min Video Bitrate"), " kb/s".length()));
            int maxVideoBitrate = Integer.parseInt(StringUtility.rShear(vStats.get("Max Video Bitrate"), " kb/s".length()));
            if (dMinVideoBitrate < minVideoBitrate) {
                vStats.put("Min Video Bitrate", dMinVideoBitrate + " kb/s");
            }
            if (dMaxVideoBitrate > maxVideoBitrate) {
                vStats.put("Max Video Bitrate", dMaxVideoBitrate + " kb/s");
            }
            
            int dMinAudioBitrate = Integer.parseInt(StringUtility.rShear(dStats.get(dName).get("Min Audio Bitrate"), " kb/s".length()));
            int dMaxAudioBitrate = Integer.parseInt(StringUtility.rShear(dStats.get(dName).get("Max Audio Bitrate"), " kb/s".length()));
            int minAudioBitrate = Integer.parseInt(StringUtility.rShear(vStats.get("Min Audio Bitrate"), " kb/s".length()));
            int maxAudioBitrate = Integer.parseInt(StringUtility.rShear(vStats.get("Max Audio Bitrate"), " kb/s".length()));
            if (dMinAudioBitrate < minAudioBitrate) {
                vStats.put("Min Audio Bitrate", dMinAudioBitrate + " kb/s");
            }
            if (dMaxAudioBitrate > maxAudioBitrate) {
                vStats.put("Max Audio Bitrate", dMaxAudioBitrate + " kb/s");
            }
            
            stats.putAll(dStats);
        }
        
        return stats;
    }
    
    private static Map<String, Map<String, String>> produceDirStats() {
        DecimalFormat df = new DecimalFormat("#.##");
        final boolean justSize = false;
        Map<String, Map<String, String>> stats = produceDirStatsHelper(videoDir, 1);
        
        List<String> cvsOutput = new ArrayList<>();
        List<String> output = new ArrayList<>();
        
        Map<String, Integer> columnWidths = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : stats.entrySet()) {
            columnWidths.putIfAbsent("Key", 0);
            int keyLength = entry.getKey().length();
            if (columnWidths.get("Key") < keyLength) {
                columnWidths.replace("Key", keyLength);
            }
            for (Map.Entry<String, String> statEntry : entry.getValue().entrySet()) {
                columnWidths.putIfAbsent(statEntry.getKey(), 0);
                int length = (statEntry.getKey() + ": " + statEntry.getValue()).length();
                if (columnWidths.get(statEntry.getKey()) < length) {
                    columnWidths.replace(statEntry.getKey(), length);
                }
            }
        }
        
        for (Map.Entry<String, Map<String, String>> entry : stats.entrySet()) {
            String key = entry.getKey();
            Map<String, String> value = entry.getValue();
            
            String cvsLine = "\"" + key + "\"," +
                    "\"" + (value.containsKey("Size") ? ("Size: " + value.get("Size")) : "") + "\"," +
                    "\"" + (value.containsKey("Count") ? ("Count: " + value.get("Count")) : "") + "\"," +
                    "\"" + (value.containsKey("Average Size") ? ("Size: " + value.get("Size")) : "") + "\"";
            cvsOutput.add(cvsLine);
            
            value.put("Size", df.format(Double.parseDouble(StringUtility.rShear(value.get("Size"), " MB".length())) / 1000.0) + " GB");
            String line;
            if (justSize) {
                line = String.format("%-" + columnWidths.get("Key") + "s  " +
                                "%-" + columnWidths.get("Size") + "s  ",
                        key,
                        value.containsKey("Size") ? ("Size: " + value.get("Size")) : "");
            } else {
                line = String.format("%-" + columnWidths.get("Key") + "s  " +
                                "%-" + columnWidths.get("Size") + "s  " +
                                "%-" + columnWidths.get("Count") + "s  " +
                                "%-" + columnWidths.get("Average Size") + "s  ",
                        key,
                        value.containsKey("Size") ? ("Size: " + value.get("Size")) : "",
                        value.containsKey("Count") ? ("Count: " + value.get("Count")) : "",
                        value.containsKey("Average Size") ? ("Average Size: " + value.get("Average Size")) : "");
            }
            output.add(line);
        }
        Filesystem.writeLines(dirStatsFile, cvsOutput);
        Filesystem.writeLines(new File(dirStatsFile.getAbsolutePath().replace(".csv", ".txt")), output);
        
        return stats;
    }
    
    private static Map<String, Map<String, String>> produceDirStatsHelper(File dir, int depth) {
        DecimalFormat df = new DecimalFormat("#.##");
        Map<String, Map<String, String>> stats = new LinkedHashMap<>();
        
        Map<String, String> dStats = new LinkedHashMap<>();
        String dName = StringUtility.spaces(3 * (depth - 1)) + "|-\\ " + dir.getAbsolutePath().replace(videoDir.getParentFile().getAbsolutePath(), "");
        stats.put(dName, dStats);
        dStats.put("Size", "0.0 MB");
        dStats.put("Count", "0");
        dStats.put("Average Size", "0.0 MB");
        
        for (File f : Filesystem.getFiles(dir)) {
            if (f.getName().endsWith("txt") || f.getName().endsWith("ini") || !f.getParentFile().getAbsolutePath().contains("Season")) {
                continue;
            }
            
            double size = (double) f.length() / (1024 * 1024);
            dStats.put("Count", String.valueOf(Integer.parseInt(dStats.get("Count")) + 1));
            dStats.put("Size", df.format(Double.parseDouble(StringUtility.rShear(dStats.get("Size"), " MB".length())) + size) + " MB");
        }
        
        for (File d : Filesystem.getDirs(dir)) {
            if (d.getName().equalsIgnoreCase("old") ||
                    d.getName().equalsIgnoreCase("Youtube") ||
                    (depth > 1 && !d.getAbsolutePath().contains("Season"))) {
                continue;
            }
            
            Map<String, Map<String, String>> fStats = produceDirStatsHelper(d, depth + 1);
            String fName = StringUtility.spaces(3 * depth) + "|-\\ " + d.getAbsolutePath().replace(videoDir.getParentFile().getAbsolutePath(), "");
            
            if (!fStats.isEmpty()) {
                stats.putAll(fStats);
                
                dStats.put("Count", df.format(Integer.parseInt(dStats.get("Count")) + Integer.parseInt(fStats.get(fName).get("Count"))));
                dStats.put("Size", df.format(Double.parseDouble(StringUtility.rShear(dStats.get("Size"), " MB".length())) + Double.parseDouble(StringUtility.rShear(fStats.get(fName).get("Size"), " MB".length()))) + " MB");
            }
        }
        
        dStats.put("Average Size", df.format(Double.parseDouble(StringUtility.rShear(dStats.get("Size"), " MB".length())) / Integer.parseInt(dStats.get("Count"))) + " MB");
        
        if (Integer.parseInt(dStats.get("Count")) == 0) {
            return new HashMap<>();
        }
        return stats;
    }
    
    private static void convertShowToMp4() {
        String show = "MythBusters";
        boolean reencode = false;
        boolean copySubtitles = true;
        
        File source = new File(videoDir, "old\\" + show);
        File dest = new File(videoDir, show);
        Filesystem.createDirectory(dest);
        
        for (File f : Filesystem.getFilesRecursively(source)) {
            if (f.getAbsolutePath().contains("\\new\\")) {
                continue;
            }
            File output = new File(StringUtility.rShear(f.getAbsolutePath().replace("old\\" + show, show), 4) + ".mp4");
            if (output.exists()) {
                continue;
            }
            if (!output.getParentFile().exists()) {
                Filesystem.createDirectory(output.getParentFile());
            }
            String cmd = "-y -i \"" + f.getAbsolutePath() + "\" -map_metadata -1 -map_chapters -1 " + (reencode ? "" : ("-map 0 -map -0:s:1 -map -0:s:2 -c copy " + (copySubtitles ? "-c:s mov_text " : ""))) + "\"" + output.getAbsolutePath() + "\"";
            ffmpeg(cmd, true);
        }
    }
    
    private static void convertDirToMp4() {
        File dir = new File("E:\\Downloads\\The Midnight Gospel");
        File out = new File(dir, "new");
        Filesystem.createDirectory(out);
        
        for (File f : Filesystem.getFilesRecursively(dir)) {
            if (f.getAbsolutePath().contains("\\new\\") &&
                    (!dir.getName().equalsIgnoreCase("new") ||
                            (f.getAbsolutePath().contains("\\new\\new\\")))) {
                continue;
            }
            File newDir = new File(out, f.getParentFile().getAbsolutePath().replace(dir.getAbsolutePath(), ""));
            if (!newDir.exists()) {
                Filesystem.createDirectory(newDir);
            }
            File output = new File(newDir, StringUtility.rShear(f.getName(), 4) + ".mp4");
            if (output.exists()) {
                continue;
            }
            
            String baseParams = "-map_metadata -1 -map_chapters -1";
//            String params = "-map 0 -map -0:s:0 -map -0:s:2 -map -0:s:3 -map -0:s:4 -map -0:s:5 -c copy -c:s mov_text";
//            String params = "-map 0 -map -0:s:0 -map -0:a:0 -c copy -c:s mov_text";
//            String params = "-map 0 -map -0:a:0 -map -0:s:0 -0:s:1 -c:v copy -c:a copy";
//            String params = "-map 0 -map -0:v:1 -map -0:v:2 -map -0:v:3 -c copy -c:s mov_text";
//            String params = "-c:a copy -c:s mov_text -b:v 2400k";
//            String params = "-c:v libx265 -c:a copy -c:s mov_text";
//            String params = "-c:v libx264 -c:a copy -crf 20";
//            String params = "-c:v copy -c:a copy";
//            String params = "-c:v copy -c:a copy -c:s mov_text";
            String params = "-c:v copy -c:a aac -c:s mov_text";
//            String params = "-c:v libx264 -c:a copy -c:s mov_text -crf 26";
//            String params = "-c:v libx265 -c:a copy -c:s mov_text -crf 27";
            
            String cmd = "-y -i \"" + f.getAbsolutePath() + "\" " + baseParams + " " + params + " \"" + output.getAbsolutePath() + "\"";
            ffmpeg(cmd, true);
        }
    }
    
    private static void stripMetadataAndChapters() {
        File source = new File(videoDir, "old");
        File dest = videoDir;
        
        for (File f : Filesystem.getFilesRecursively(source)) {
            if (f.getAbsolutePath().contains("\\old\\old\\") || !f.getName().endsWith("mp4")) {
                continue;
            }
            File output = new File(f.getAbsolutePath().replace("old\\", ""));
            String cmd = "-y -i \"" + f.getAbsolutePath() + "\" -map_metadata -1 -map_chapters -1 -map 0 -c copy -c:s mov_text \"" + output.getAbsolutePath() + "\"";
            ffmpeg(cmd, true);
        }
    }
    
    private static void addSubtitles() {
        File dir = new File("E:\\Videos\\old\\Better Call Saul\\Season 5");
        File out = new File(dir, "new");
        Filesystem.createDirectory(out);
        
        List<File> videos = Filesystem.listFiles(dir, x -> x.getName().endsWith(".mp4"));
        List<File> subtitles = Filesystem.listFiles(dir, x -> x.getName().endsWith(".srt"));
        if (videos.size() != subtitles.size()) {
            return;
        }
        
        videos.sort(Comparator.comparing(File::getName));
        subtitles.sort(Comparator.comparing(File::getName));
        
        for (int i = 0; i < videos.size(); i++) {
            File output = new File(out, StringUtility.rShear(videos.get(i).getName(), 4) + ".mp4");
            if (output.exists()) {
                continue;
            }
            String cmd = "-y -i \"" + videos.get(i).getAbsolutePath() + "\" -i \"" + subtitles.get(i).getAbsolutePath() + "\" -map_metadata -1 -map_chapters -1 -c:v copy -c:a copy -c:s mov_text \"" + output.getAbsolutePath() + "\"";
            ffmpeg(cmd, true);
        }
    }
    
    private static void makePlaylists() {
        List<File> shows = Filesystem.getDirs(videoDir);
        for (File show : shows) {
            if (show.getName().equalsIgnoreCase("Youtube") ||
                    show.getName().equalsIgnoreCase("Short Films") ||
                    show.getName().equalsIgnoreCase("To Watch")) {
                continue;
            }
            List<String> showPlaylist = new ArrayList<>();
            List<File> seasons = Filesystem.getDirs(show);
            seasons.sort(Comparator.comparingInt(o -> Integer.parseInt(StringUtility.trim(StringUtility.rSnip(o.getName(), 2)))));
            for (File season : seasons) {
                List<String> seasonPlaylist = new ArrayList<>();
                List<File> episodes = Filesystem.getFiles(season);
                for (File episode : episodes) {
                    seasonPlaylist.add(episode.getAbsolutePath());
                    showPlaylist.add(episode.getAbsolutePath());
                }
                Filesystem.writeLines(new File(show, season.getName() + ".m3u"), seasonPlaylist);
            }
            Filesystem.writeLines(new File(show, show.getName() + ".m3u"), showPlaylist);
        }
    }
    
    private static void lossTest() {
        for (int i = 0; i < 1000; i++) {
            File f = new File("D:\\Temp\\New Folder\\in" + i + ".mp4");
            File f2 = new File("D:\\Temp\\New Folder\\in" + (i + 1) + ".mp4");
            String cmd = "-y -i \"" + f.getAbsolutePath() + "\" -c:v libx265 -crf 18 -c:a aac -c:s mov_text \"" + f2.getAbsolutePath() + "\"";
            ffmpeg(cmd, true);
        }
    }
    
}
