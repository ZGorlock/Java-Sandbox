/*
 * File:    VideoProcessor.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import common.CmdLine;
import common.Filesystem;
import common.StringUtility;

@SuppressWarnings({"ConstantConditions", "UnnecessaryLocalVariable"})
public class VideoProcessor {
    
    //Constants
    
    public static final File videoDir = new File("E:\\Videos");
    
    public static final File workDir = videoDir;//new File("D:\\Work");
    
    public static final File log = new File("log/" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".txt");
    
    public static final File statsFile = new File("data/stats.txt");
    
    public static final File statsSpreadsheet = new File("data/stats.csv");
    
    public static final File dirStatsFile = new File("data/dirStats.txt");
    
    public static final File dirStatsSpreadsheet = new File("data/dirStats.csv");
    
    public static final List<String> videoFormats = List.of("mp4", "mkv", "m4v", "flv", "webm");
    
    public static final List<String> streamTypes = List.of("", "Video", "Audio", "Subtitle");
    
    public static final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    
    
    //Main Method
    
    public static void main(String[] args) {
//        processDir();

//        convertDirToMp4();
//        convertShowToMp4();

//        stripMetadataAndChapters();
//        stripMetadataAndChaptersInPlace();

//        addSubtitles();
//        extractSubtitles();

//        makePlaylists();
//        lossTest();
        
        Stats.produceStats();
        Stats.produceDirStats();
    }
    
    
    //Static Methods
    
    private static void processDir() {
        final File dir = workDir;
        final File out = new File(dir, "new");
        Filesystem.createDirectory(out);
        
        
        //input and output file formats
        final String inFormat = "mkv";
        final String outFormat = "mp4";
        
        
        //drop stream types
        final boolean removeStreamTypes = true;
        final Map<String, Boolean> saveStreamTypes = new LinkedHashMap<>();
        saveStreamTypes.put("video", true);
        saveStreamTypes.put("audio", true);
        saveStreamTypes.put("subtitle", false);
        
        
        //remove extra streams
        final boolean removeStreams = false;
        final Map<String, List<Integer>> saveStreams = new LinkedHashMap<>();
        saveStreams.put("video", List.of(0));
        saveStreams.put("audio", List.of(0));
        saveStreams.put("subtitle", List.of(0));
        
        
        //add subtitles from file
        final boolean addSubs = false;
        final boolean requireSubs = false;
        final String subFormat = "srt";
        
        
        //encode streams
        final boolean performTranscode = true;
        final Map<String, String> streamEncoders = new LinkedHashMap<>();
        
        streamEncoders.put("video", "copy");
//        streamEncoders.put("video", "libx265 -crf 26 -preset slower");
//        streamEncoders.put("video", "libx264 -vf scale=-1:720");
        
        streamEncoders.put("audio", "copy");
//        streamEncoders.put("audio", "aac -ac 6");
//        streamEncoders.put("audio", "libopus -ac 2");
        
        streamEncoders.put("subtitle", "mov_text");
        
        
        //modify target bitrate
        final boolean adjustBitrate = false;
        final Map<String, String> targetBitrates = new LinkedHashMap<>();
        targetBitrates.put("video", "475k");
        targetBitrates.put("audio", "80k");
        targetBitrates.put("", "500k");
        
        
        //additional params
        final String extraInParams = "";
        final String extraOutParams = "";
        
        
        //flags
        final boolean allowStreamChanges = true;
        final boolean allowReencoding = true;
        final boolean overwriteExisting = false;
        final boolean printCmd = false;
        final boolean runFFmpeg = true;
        
        
        //process videos
        for (File video : Filesystem.getFilesRecursively(dir)) {
            final File output = new File(video.getAbsolutePath()
                    .replace(dir.getAbsolutePath(), out.getAbsolutePath())
                    .replaceAll(("(?<=\\.)" + inFormat + "$"), (((outFormat == null) || outFormat.isEmpty()) ? inFormat : outFormat)));
            final File subs = new File(video.getParentFile(), video.getName()
                    .replaceAll(("(?<=\\.)" + inFormat + "$"), subFormat));
            
            if (!video.exists() || (output.exists() && !overwriteExisting) || (!subs.exists() && requireSubs) ||
                    (!inFormat.isEmpty() && !video.getName().endsWith(inFormat)) ||
                    (outFormat.isEmpty() || !output.getName().endsWith(outFormat)) ||
                    (!subFormat.isEmpty() && !subs.getName().endsWith(subFormat)) ||
                    (video.getAbsolutePath().contains("\\new\\") && (!dir.getName().equalsIgnoreCase("new") || (video.getAbsolutePath().contains("\\new\\new\\")))) ||
                    (!output.getParentFile().exists() && !Filesystem.createDirectory(output.getParentFile()))) {
                System.out.println("Skipping: " + video.getAbsolutePath());
                continue;
            }
            
            final String cmd = FFmpeg.buildCmd(
                    video, extraInParams,
                    allowStreamChanges,
                    removeStreamTypes, saveStreamTypes,
                    removeStreams, saveStreams,
                    addSubs, subs,
                    allowReencoding,
                    performTranscode, streamEncoders,
                    adjustBitrate, targetBitrates,
                    extraOutParams, output);
            
            if (printCmd) {
                System.out.println(cmd);
            }
            if (runFFmpeg) {
                FFmpeg.ffmpeg(cmd, true);
            }
        }
    }
    
    private static void convertDirToMp4() {
        File dir = workDir;
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
//            String params = "-map 0 -map -0:a:1 -map -0:a:2 -map -0:a:3 -map -0:s -c copy";
//            String params = "-c:a copy -c:s mov_text -b:v 2400k";
//            String params = "-c:v libx265 -c:a copy -c:s mov_text";
//            String params = "-c:v libx264 -c:a copy -crf 20";
            String params = "-c:v copy -c:a copy";
//            String params = "-c:v copy -c:a copy -c:s mov_text";
//            String params = "-c:v copy -c:a aac -c:s mov_text";
//            String params = "-c:v libx264 -c:a copy -c:s mov_text -crf 26";
//            String params = "-c:v libx265 -c:a copy -c:s mov_text -crf 27";
            
            String cmd = "-y -i \"" + f.getAbsolutePath() + "\" " + baseParams + " " + params + " \"" + output.getAbsolutePath() + "\"";
            FFmpeg.ffmpeg(cmd, true);
        }
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
            FFmpeg.ffmpeg(cmd, true);
        }
    }
    
    private static void stripMetadataAndChapters(File dir) {
        File source = new File(dir, "old");
        File dest = new File(dir.getAbsolutePath());
        
        for (File f : Filesystem.getFilesRecursively(source)) {
            if (f.getAbsolutePath().contains("\\old\\old\\") || !f.getName().endsWith("mp4")) {
                continue;
            }
            File output = new File(f.getAbsolutePath().replace("old\\", ""));
            if (!output.getParentFile().exists()) {
                Filesystem.createDirectory(output.getParentFile());
            }
            String cmd = "-y -i \"" + f.getAbsolutePath() + "\" -map_metadata -1 -map_chapters -1 -map 0 -c copy -c:s mov_text \"" + output.getAbsolutePath() + "\"";
            FFmpeg.ffmpeg(cmd, true);
        }
    }
    
    private static void stripMetadataAndChapters() {
//        stripMetadataAndChapters(videoDir);
        stripMetadataAndChapters(workDir);
    }
    
    private static void stripMetadataAndChaptersInPlace(File dir) {
        List<File> files = Filesystem.getFilesRecursively(dir, ".*\\.mp4");
        for (File f : files) {
            if (f.getAbsolutePath().contains("\\old\\")) {
                continue;
            }
            
            File newFile = new File(new File(f.getParentFile(), "old"), f.getName());
            if (!newFile.getParentFile().exists()) {
                Filesystem.createDirectory(newFile.getParentFile());
            }
            Filesystem.move(f, newFile);
        }
        
        List<File> dirs = Filesystem.getDirsRecursively(dir).stream().filter(e -> e.getName().equals("old")).collect(Collectors.toList());
        for (File doDir : dirs) {
            stripMetadataAndChapters(doDir.getParentFile());
        }
        
        files = Filesystem.getFilesRecursively(dir);
        for (File f : files) {
            if (f.getAbsolutePath().contains("\\old\\")) {
                if (new File(f.getAbsolutePath().replace("old\\", "")).exists()) {
                    Filesystem.deleteFile(f);
                } else {
                    System.out.println("Error, file not stripped: " + f.getAbsolutePath());
                }
            }
        }
        
        dirs = Filesystem.getDirsRecursively(dir).stream().filter(e -> e.getName().equals("old")).collect(Collectors.toList());
        for (File doDir : dirs) {
            Filesystem.deleteDirectory(doDir);
        }
    }
    
    private static void stripMetadataAndChaptersInPlace() {
        stripMetadataAndChaptersInPlace(workDir);
    }
    
    private static void addSubtitles() {
        File dir = workDir;
        File out = new File(dir, "new");
        Filesystem.createDirectory(out);
        
        String inFormat = ".mkv";
        String subFormat = ".ass";
        
        List<File> videos = Filesystem.listFiles(dir, x -> x.getName().endsWith(inFormat));
        List<File> subtitles = Filesystem.listFiles(dir, x -> x.getName().endsWith(subFormat));
        if (videos.size() != subtitles.size()) {
            File subsDir = new File(dir, "Subs");
            if (subsDir.exists()) {
                subtitles.clear();
                for (File subDir : Filesystem.getDirs(subsDir)) {
                    List<File> subs = Filesystem.getFiles(subDir);
                    if (subs.size() == 1) {
                        subtitles.add(subs.get(0));
                    } else {
                        File subChoice = Stream.of(2, 3, 1, 4).map(i -> i + "_English" + subFormat)
                                .filter(e -> subs.stream().anyMatch(f -> f.getName().equals(e))).findFirst()
                                .map(e -> new File(subDir, e)).orElse(null);
                        if (subChoice == null) {
                            return;
                        }
                        subtitles.add(subChoice);
                    }
                }
            } else {
                return;
            }
        }
        
        videos.sort(Comparator.comparing(File::getName));
        subtitles.sort(Comparator.comparing(File::getName));
        
        for (int i = 0; i < videos.size(); i++) {
            File output = new File(out, StringUtility.rShear(videos.get(i).getName(), 4) + inFormat);
            if (output.exists()) {
                continue;
            }
            String cmd = "-y -i \"" + videos.get(i).getAbsolutePath() + "\" -i \"" + subtitles.get(i).getAbsolutePath() + "\" -map_metadata -1 -map_chapters -1 " +
                    "-map 0 -map -0:s -map 1 " +
                    "-c:v copy -c:a copy -c:s copy " +
                    "\"" + output.getAbsolutePath() + "\"";
            FFmpeg.ffmpeg(cmd, true);
        }
    }
    
    private static void extractSubtitles() {
        File dir = workDir;
        File out = dir;
        Filesystem.createDirectory(out);
        
        String inFormat = ".mkv";
        String subFormat = ".srt";
        
        List<File> videos = Filesystem.getFilesRecursively(dir, ".*\\" + inFormat);
//        List<File> videos = Filesystem.listFiles(dir, x -> x.getName().endsWith(inFormat));
        
        String subParam = "-map 0:s:0 -c:s mov_text";
        
        for (File video : videos) {
            File output = new File(video.getAbsolutePath().replace(inFormat, subFormat));
            String cmd = "-y -i \"" + video.getAbsolutePath() + "\" -map_metadata -1 -map_chapters -1 " + subParam + " \"" + output.getAbsolutePath() + "\"";
            FFmpeg.ffmpeg(cmd, true);
            
            Filesystem.writeStringToFile(output, Filesystem.readFileToString(output).replace("\\h", ""));
        }
    }
    
    private static void makePlaylists() {
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
    
    private static void lossTest() {
        for (int i = 0; i < 1000; i++) {
            File f = new File("D:\\Temp\\New Folder\\in" + i + ".mp4");
            File f2 = new File("D:\\Temp\\New Folder\\in" + (i + 1) + ".mp4");
            String cmd = "-y -i \"" + f.getAbsolutePath() + "\" -c:v libx265 -crf 18 -c:a aac -c:s mov_text \"" + f2.getAbsolutePath() + "\"";
            FFmpeg.ffmpeg(cmd, true);
        }
    }
    
    
    //Inner Classes
    
    private static class FFmpeg {
        
        //Static Methods
        
        private static String executeFF(String program, String cmd, boolean printOutput) {
            final String ffCmd = program + " -hide_banner " + cmd;
            if (printOutput) {
                System.out.println(ffCmd);
            }
            
            CmdLine.printOutput = printOutput;
            final String cmdLog = CmdLine.executeCmd(ffCmd);
            CmdLine.printOutput = false;
            if (cmdLog.contains("Error") && !ffCmd.contains("Error")) {
                System.err.println("Error in " + program + ": " + ffCmd);
            }
            
            Filesystem.writeStringToFile(log, cmdLog + "\n" + StringUtility.fillStringOfLength('-', 120) + "\n\n", true);
            return cmdLog;
        }
        
        public static String ffmpeg(String cmd, boolean printOutput) {
            return executeFF("ffmpeg", cmd, printOutput);
        }
        
        public static String ffmpeg(String cmd) {
            return ffmpeg(cmd, false);
        }
        
        public static String ffprobe(String cmd, boolean printOutput) {
            return executeFF("ffprobe", cmd, printOutput);
        }
        
        public static String ffprobe(String cmd) {
            return ffprobe(cmd, false);
        }
        
        private static Map<String, Integer> countStreams(File video) {
            final Map<String, Integer> streamCounts = new HashMap<>();
            
            final List<String> streamsProbe = StringUtility.splitLines(ffprobe("-i \"" + video.getAbsolutePath() + "\" -show_streams"));
            for (String line : streamsProbe) {
                if (line.startsWith("codec_type=")) {
                    String type = line.substring(line.indexOf('=') + 1).toLowerCase();
                    streamCounts.putIfAbsent(type, 0);
                    streamCounts.replace(type, streamCounts.get(type) + 1);
                }
            }
            return streamCounts;
        }
        
        private static String buildCmd(
                File in, String extraInParams,
                boolean allowStreamChanges,
                boolean removeStreamTypes, Map<String, Boolean> saveStreamTypes,
                boolean removeStreams, Map<String, List<Integer>> saveStreams,
                boolean addSubs, File subs,
                boolean allowReencoding,
                boolean performTranscode, Map<String, String> streamEncoders,
                boolean adjustBitrate, Map<String, String> targetBitrates,
                String extraOutParams, File out) {
            
            final String inParams = "-i \"" + in.getAbsolutePath() + "\"" +
                    (subs.exists() ? (" -i \"" + subs.getAbsolutePath() + "\"") : "");
            
            final String baseParams = "-y -map_metadata -1 -map_chapters -1 -strict experimental";
            
            final String streamParams = !allowStreamChanges ? "-map 0" : String.join(" ",
                    !removeStreamTypes ? "-map 0" :
                            saveStreamTypes.entrySet().stream()
                                    .map(e -> ("-map " + (e.getValue() ? "" : "-") + "0:" + Character.toLowerCase(e.getKey().charAt(0))))
                                    .collect(Collectors.joining(" ")),
                    
                    !removeStreams ? "" :
                            countStreams(in).entrySet().stream()
                                    .flatMap(e -> IntStream.range(0, e.getValue())
                                            .filter(i -> !saveStreams.getOrDefault(e.getKey().toLowerCase(), Collections.emptyList()).contains(i))
                                            .mapToObj(i -> ("-map -0:" + Character.toLowerCase(e.getKey().charAt(0)) + ":" + i)))
                                    .collect(Collectors.joining(" ")),
                    
                    !(addSubs && subs.exists()) ? "" : "-map 1 -c:s mov_text"
            );
            
            final String encodeParams = !allowReencoding ? "-c copy" : String.join(" ",
                    !performTranscode ? "-c copy" :
                            streamEncoders.entrySet().stream()
                                    .filter(e -> ((e.getValue() != null) && !e.getValue().isEmpty()))
                                    .map(e -> ("-c:" + Character.toLowerCase(e.getKey().charAt(0)) + " " + e.getValue()))
                                    .collect(Collectors.joining(" ")),
                    
                    !adjustBitrate ? "" :
                            targetBitrates.entrySet().stream()
                                    .filter(e -> ((e.getValue() != null) && !e.getValue().isEmpty()))
                                    .map(e -> (e.getKey().isEmpty() ? ("-maxrate " + e.getValue() + " -bufsize " + e.getValue()) :
                                            ("-b:" + Character.toLowerCase(e.getKey().charAt(0)) + " " + e.getValue())))
                                    .collect(Collectors.joining(" "))
            );
            
            final String outParams = "\"" + out.getAbsolutePath() + "\"";
            
            return String.join(" ",
                            inParams, baseParams, extraInParams,
                            streamParams, encodeParams, extraOutParams, outParams)
                    .replaceAll("\\s+", " ").strip();
        }
        
    }
    
    private static class Stats {
        
        //Static Methods
        
        private static Map<String, Map<String, String>> produceStats() {
            return produceStats(workDir, statsFile, statsSpreadsheet);
//            return produceStats(videoDir, statsFile, statsSpreadsheet);
//            return produceStats(new File(videoDir, "Youtube"), statsFile, statsSpreadsheet);
        }
        
        public static Map<String, Map<String, String>> produceStats(File dir, File outputFile, File outputSpreadsheet) {
            final Map<String, Map<String, String>> stats = produceStatsHelper(dir, 1);
            final List<String> columns = List.of("Key", "Count", "Size", "Extension", "Length",
                    "Bitrate", "Streams", "Video Streams", "Audio Streams", "Subtitle Streams",
                    "Video Codec", "Video Language", "Video Bitrate", "Video Dimensions", "Video Framerate",
                    "Audio Codec", "Audio Language", "Audio Bitrate", "Audio Frequency", "Audio Layout", "Audio Format",
                    "Subtitle Codec", "Subtitle Language");
            
            validateStats(stats);
            outputStats(stats, columns, outputFile, outputSpreadsheet);
            
            return stats;
        }
        
        private static Map<String, Map<String, String>> produceStatsHelper(File dir, int depth) {
            final Map<String, Map<String, String>> stats = new LinkedHashMap<>();
            
            final Function<Double, String> durationStamp = (Double duration) ->
                    String.join("", " 0" + ((int) (duration / (60 * 60))),
                                    ":0" + ((int) ((duration / 60) % 60)),
                                    ":0" + ((int) (duration % 60)),
                                    ".0" + ((int) ((duration % 1) * 100)))
                            .replaceAll("(^|[:.])\\s*0?(\\d{2,})(?!\\d)", "$1$2");
            final Function<String, Double> durationUnstamp = (String stamp) -> {
                final String[] parts = stamp.split("[^\\d.]+");
                return Double.parseDouble(parts[2]) +
                        (Integer.parseInt(parts[1]) * 60) +
                        (Integer.parseInt(parts[0]) * (60 * 60));
            };
            
            final Map<String, String> dirStat = new LinkedHashMap<>();
            final String dirKey = StringUtility.spaces(3 * (depth - 1)) + "|-\\ " + dir.getAbsolutePath().replace(videoDir.getParentFile().getAbsolutePath(), "");
            stats.put(dirKey, dirStat);
            
            dirStat.put("Key", dirKey);
            dirStat.put("Name", dir.getName());
            
            final AtomicInteger count = new AtomicInteger(0);
            final List<Double> sizes = new ArrayList<>();
            final List<Double> durations = new ArrayList<>();
            final Map<String, List<Long>> bitRates = new HashMap<>();
            streamTypes.forEach(type -> bitRates.put(type, new ArrayList<>()));
            
            Filesystem.getFiles(dir).stream()
                    .filter(e -> videoFormats.contains(e.getName().replaceAll(".+\\.([^.]+)$", "$1").toLowerCase()))
                    .forEachOrdered(file -> {
                        
                        final Map<String, String> videoStats = new LinkedHashMap<>();
                        final String videoKey = StringUtility.spaces(3 * depth) + "|- " + file.getName();
                        stats.put(videoKey, videoStats);
                        
                        final double videoSize = (double) file.length() / (1024 * 1024);
                        
                        videoStats.put("Key", videoKey);
                        videoStats.put("Name", file.getName());
                        videoStats.put("Parent", dir.getName());
                        videoStats.put("Extension", file.getName().replaceAll(".+\\.([^.]+)$", "$1"));
                        videoStats.put("Size", decimalFormat.format(videoSize) + " MB");
                        
                        final Map<String, AtomicInteger> streams = new HashMap<>();
                        streamTypes.forEach(type -> streams.put(type, new AtomicInteger(0)));
                        
                        count.incrementAndGet();
                        sizes.add(videoSize);
                        
                        final List<String> probe = StringUtility.splitLines(FFmpeg.ffprobe("-loglevel quiet -show_streams -show_format \"" + file.getAbsolutePath() + "\""));
                        
                        final Map<String, String> streamStats = new LinkedHashMap<>();
                        final AtomicReference<String> section = new AtomicReference<>("");
                        final AtomicReference<String> streamType = new AtomicReference<>("");
                        
                        probe.forEach(line -> {
                            if (line.matches("^\\[/?[A-Z_]+]$")) {
                                final String currentSection = section.get();
                                final String currentStreamType = streamType.get();
                                
                                if (line.equals("[/" + section.get() + "]")) {
                                    if (currentSection.equals("STREAM") && streamTypes.contains(currentStreamType)) {
                                        streamStats.forEach((name, value) ->
                                                videoStats.put((currentStreamType + " " + name), Optional.ofNullable(videoStats.get(currentStreamType + " " + name))
                                                        .map(e -> (e.contains(" | ") ? "" : "[0] ") + e +
                                                                " | [" + e.split("\\s\\|\\s", -1).length + "] " + value)
                                                        .orElse(value)));
                                        
                                        streams.get("").incrementAndGet();
                                        streams.get(currentStreamType).incrementAndGet();
                                    }
                                    
                                    streamStats.clear();
                                    streamType.set("");
                                    section.set("");
                                    
                                } else if (section.get().isEmpty()) {
                                    section.set(line.replaceAll("[\\[\\]]", ""));
                                }
                                
                            } else if (!section.get().isEmpty()) {
                                final String[] lineParts = line.split("=");
                                final String key = lineParts[0];
                                final String value = lineParts[1];
                                
                                switch (section.get()) {
                                    case "FORMAT":
                                        switch (key) {
                                            case "duration":
                                                final double duration = Double.parseDouble(value);
                                                final String durationString = durationStamp.apply(duration);
                                                videoStats.put("Length", durationString);
                                                durations.add(duration);
                                                break;
                                            
                                            case "bit_rate":
                                                if (!value.equals("N/A")) {
                                                    final long bitRate = (Long.parseLong(value) / 1000);
                                                    final String bitRateString = bitRate + " kb/s";
                                                    videoStats.put("Bitrate", bitRateString);
                                                    bitRates.get("").add(bitRate);
                                                }
                                                break;
                                        }
                                        break;
                                    
                                    case "STREAM":
                                        switch (key) {
                                            case "codec_type":
                                                streamType.set(Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase());
                                                break;
                                            
                                            case "codec_name":
                                                final String codecName = value;
                                                streamStats.put("Codec", Optional.ofNullable(streamStats.get("Codec")).map(e -> (codecName + " (" + e + ")")).orElse(codecName));
                                                break;
                                            
                                            case "profile":
                                                if (!value.equals("unknown")) {
                                                    final String profile = value;
                                                    streamStats.put("Codec", Optional.ofNullable(streamStats.get("Codec")).map(e -> (e + " (" + profile + ")")).orElse(profile));
                                                }
                                                break;
                                            
                                            case "bit_rate":
                                                if (!value.equals("N/A")) {
                                                    final long bitRate = (Long.parseLong(value) / 1000);
                                                    final String bitRateString = bitRate + " kb/s";
                                                    streamStats.put("Bitrate", bitRateString);
                                                    bitRates.get(streamType.get()).add(bitRate);
                                                }
                                                break;
                                            
                                            case "TAG:language":
                                                final String language = value;
                                                streamStats.put("Language", language);
                                                break;
                                            
                                            default:
                                                switch (streamType.get()) {
                                                    case "Video":
                                                        switch (key) {
                                                            case "width":
                                                                final String width = value;
                                                                streamStats.put("Dimensions", Optional.ofNullable(streamStats.get("Dimensions")).map(e -> (width + "x" + e)).orElse(width));
                                                                break;
                                                            
                                                            case "height":
                                                                final String height = value;
                                                                streamStats.put("Dimensions", Optional.ofNullable(streamStats.get("Dimensions")).map(e -> (e + "x" + height)).orElse(height));
                                                                break;
                                                            
                                                            case "r_frame_rate":
                                                                final String rFrameRate = decimalFormat.format((double) Integer.parseInt(value.split("/")[0]) / Integer.parseInt(value.split("/")[1])) + " fps";
                                                                streamStats.put("Framerate", rFrameRate);
                                                                break;
                                                        }
                                                        break;
                                                    
                                                    case "Audio":
                                                        switch (key) {
                                                            case "sample_rate":
                                                                final String sampleRate = value + " Hz";
                                                                streamStats.put("Frequency", sampleRate);
                                                                break;
                                                            
                                                            case "channel_layout":
                                                                final String channelLayout = value;
                                                                streamStats.put("Layout", channelLayout);
                                                                break;
                                                            
                                                            case "sample_fmt":
                                                                final String sampleFmt = value;
                                                                streamStats.put("Format", sampleFmt);
                                                                break;
                                                        }
                                                        break;
                                                }
                                        }
                                        break;
                                }
                            }
                        });
                        
                        if (streams.get("").get() != streamTypes.stream().filter(e -> !e.isEmpty()).map(streams::get).mapToInt(AtomicInteger::get).sum()) {
                            System.err.println("Error parsing streams: " + file.getAbsolutePath());
                        }
                        streams.forEach((type, typeCount) ->
                                videoStats.put((type + " Streams").trim(), String.valueOf(typeCount)));
                    });
            
            Filesystem.getDirs(dir).stream()
                    .filter(e -> !e.getName().equalsIgnoreCase("old") &&
                            (!e.getName().equalsIgnoreCase("Youtube") || dir.getName().equals("Youtube")))
                    .forEachOrdered(subDir -> {
                        
                        final Map<String, Map<String, String>> subDirStats = produceStatsHelper(subDir, depth + 1);
                        final String subDirKey = StringUtility.spaces(3 * depth) + "|-\\ " + subDir.getAbsolutePath().replace(videoDir.getParentFile().getAbsolutePath(), "");
                        stats.putAll(subDirStats);
                        
                        final int subCount = Integer.parseInt(subDirStats.get(subDirKey).get("Count"));
                        final double subAverageSize = Double.parseDouble(subDirStats.get(subDirKey).get("Size").replaceAll(".+MB\\s\\((.+)\\)", "$1"));
                        final double subAverageDuration = durationUnstamp.apply(subDirStats.get(subDirKey).get("Length").replaceAll(".+\\s\\((.+)\\)", "$1"));
                        final Map<String, Long[]> subBitRates = new HashMap<>();
                        streamTypes.forEach(type -> subBitRates.put(type,
                                Arrays.stream(subDirStats.get(subDirKey).get((type + " Bitrate").trim()).split("\\D+")).map(Long::parseLong).toArray(Long[]::new)));
                        
                        count.addAndGet(subCount);
                        if (subCount > 0) {
                            IntStream.range(0, subCount).forEach(i ->
                                    sizes.add(subAverageSize));
                            IntStream.range(0, subCount).forEach(i ->
                                    durations.add(subAverageDuration));
                            subBitRates.forEach((type, typeBitRates) -> {
                                bitRates.get(type).add(typeBitRates[1]);
                                if (subCount > 1) {
                                    bitRates.get(type).add(typeBitRates[2]);
                                    IntStream.range(0, (subCount - 2)).forEach(i ->
                                            bitRates.get(type).add(typeBitRates[0]));
                                }
                            });
                        }
                    });
            
            dirStat.put("Count", String.valueOf(count));
            dirStat.put("Size", (decimalFormat.format(sizes.stream().mapToDouble(e -> e).sum()) + " MB" +
                    " (" + decimalFormat.format((count.get() > 0) ? (sizes.stream().mapToDouble(e -> e).sum() / count.get()) : 0) + ")"));
            dirStat.put("Length", (durationStamp.apply(durations.stream().mapToDouble(e -> e).sum()) +
                    " (" + durationStamp.apply((count.get() > 0) ? (durations.stream().mapToDouble(e -> e).sum() / count.get()) : 0) + ")"));
            bitRates.forEach((type, entries) ->
                    dirStat.put((type + " Bitrate").trim(), (((count.get() > 0) ? (entries.stream().mapToLong(e -> e).sum() / count.get()) : 0) + " kb/s" +
                            " [" + entries.stream().mapToLong(e -> e).min().orElse(0) + "," + entries.stream().mapToLong(e -> e).max().orElse(0) + "]")));
            
            return stats;
        }
        
        private static Map<String, Map<String, String>> produceDirStats() {
            return produceDirStats(workDir, dirStatsFile, dirStatsSpreadsheet);
//            return produceDirStats(videoDir, dirStatsFile, dirStatsSpreadsheet);
//            return produceDirStats(new File(videoDir, "Youtube"), dirStatsFile, dirStatsSpreadsheet);
        }
        
        public static Map<String, Map<String, String>> produceDirStats(File dir, File outputFile, File outputSpreadsheet) {
            final Map<String, Map<String, String>> dirStats = produceDirStatsHelper(dir, 1);
            final List<String> columns = List.of("Key", "Size", "Count", "Average Size");
            
            validateStats(dirStats);
            outputStats(dirStats, columns, outputFile, outputSpreadsheet);
            
            return dirStats;
        }
        
        private static Map<String, Map<String, String>> produceDirStatsHelper(File dir, int depth) {
            final Map<String, Map<String, String>> stats = new LinkedHashMap<>();
            
            final Map<String, String> dirStat = new LinkedHashMap<>();
            final String dirKey = StringUtility.spaces(3 * (depth - 1)) + "|-\\ " + dir.getAbsolutePath().replace(videoDir.getParentFile().getAbsolutePath(), "");
            stats.put(dirKey, dirStat);
            
            dirStat.put("Key", dirKey);
            dirStat.put("Name", dir.getName());
            
            final AtomicInteger count = new AtomicInteger(0);
            final AtomicLong size = new AtomicLong(0);
            
            Filesystem.getFiles(dir).stream()
                    .filter(e -> videoFormats.contains(e.getName().replaceAll(".+\\.([^.]+)$", "$1").toLowerCase()))
                    .forEachOrdered(file -> {
                        count.incrementAndGet();
                        size.addAndGet(file.length());
                    });
            
            Filesystem.getDirs(dir).stream()
                    .filter(e -> !e.getName().equalsIgnoreCase("old") &&
                            (!e.getName().equalsIgnoreCase("Youtube") || dir.getName().equals("Youtube")))
                    .forEachOrdered(subDir -> {
                        
                        final Map<String, Map<String, String>> subDirStats = produceDirStatsHelper(subDir, depth + 1);
                        final String subDirKey = StringUtility.spaces(3 * depth) + "|-\\ " + subDir.getAbsolutePath().replace(videoDir.getParentFile().getAbsolutePath(), "");
                        stats.putAll(subDirStats);
                        
                        count.addAndGet(Integer.parseInt(subDirStats.get(subDirKey).get("Count")));
                        size.addAndGet((long) (Double.parseDouble(subDirStats.get(subDirKey).get("Size").replace(" MB", "")) * (1024 * 1024)));
                    });
            
            dirStat.put("Count", String.valueOf(count.get()));
            dirStat.put("Size", (decimalFormat.format((double) size.get() / (1024 * 1024)) + " MB"));
            dirStat.put("Average Size", (decimalFormat.format((count.get() > 0) ? ((double) size.get() / (1024 * 1024) / count.get()) : 0) + " MB"));
            
            return stats;
        }
        
        private static void validateStats(Map<String, Map<String, String>> stats) {
            stats.forEach((key, stat) -> {
                if (stat.containsKey("Count")) {
                    if (stat.getOrDefault("Count", "0").equals("0")) {
                        System.out.println(StringUtility.padRight(("Directory is empty:"), 30, ' ') + key);
                    }
                    
                } else {
                    if (!key.endsWith(".mp4")) {
                        System.out.println(StringUtility.padRight(("Video Codec not mp4:"), 30, ' ') + key);
                    }
                    if (stat.containsKey("Video Codec") && !stat.get("Video Codec").startsWith("hevc")) {
                        System.out.println(StringUtility.padRight(("Video Codec not h265:"), 30, ' ') + key);
                    }
                    
                    streamTypes.stream().filter(e -> !e.isEmpty()).forEach(streamType -> {
                        if (stat.getOrDefault((streamType + " Streams"), "0").equals("0")) {
                            if (!streamType.equals("Subtitle")) {
                                System.out.println(StringUtility.padRight(("No " + streamType + " Streams:"), 30, ' ') + key);
                            }
                        } else {
                            if (!stat.getOrDefault((streamType + " Streams"), "0").equals("1")) {
                                System.out.println(StringUtility.padRight(("Multiple " + streamType + " Streams:"), 30, ' ') + key);
                            }
                            if (!stat.getOrDefault((streamType + " Language"), "und").matches("^(?:(?:\\[\\d+]\\s+)?(?:eng|und)(?:\\s+\\|\\s+)?)+$")) {
                                System.out.println(StringUtility.padRight(("Non-English " + streamType + " Stream:"), 30, ' ') + key);
                            }
                        }
                    });
                }
            });
        }
        
        private static void outputStats(Map<String, Map<String, String>> stats, List<String> columns, File outputFile, File outputSpreadsheet) {
            final List<String> txtOutput = new ArrayList<>();
            final List<String> csvOutput = new ArrayList<>();
            
            final Map<String, Integer> columnWidths = new HashMap<>();
            stats.forEach((key, subStats) -> {
                columnWidths.put("Key", Math.max(key.length(), columnWidths.getOrDefault("Key", 0)));
                subStats.forEach((name, value) ->
                        columnWidths.put(name, Math.max((name + ": " + value).length(), columnWidths.getOrDefault(name, 0))));
            });
            
            csvOutput.add(columns.stream()
                    .map(e -> ("\"" + e + "\"")).collect(Collectors.joining(",")));
            
            stats.forEach((key, stat) -> {
                txtOutput.add(String.format(
                        columns.stream()
                                .map(e -> Optional.ofNullable(columnWidths.get(e))
                                        .map(i -> -(i + 4)).map(String::valueOf)
                                        .orElse(""))
                                .map(e -> ("%" + e + "s")).collect(Collectors.joining()),
                        columns.stream()
                                .map(e -> Optional.ofNullable(stat.get(e))
                                        .map(e2 -> (e.equals("Key") ? e2 : (e + ": " + e2)))
                                        .orElse(""))
                                .toArray()));
                
                csvOutput.add(columns.stream()
                        .map(e -> Optional.ofNullable(stat.get(e)).orElse(""))
                        .map(e -> ("\"" + e + "\"")).collect(Collectors.joining(",")));
            });
            
            if (outputFile != null) {
                Filesystem.writeLines(outputFile, txtOutput);
            }
            if (outputSpreadsheet != null) {
                Filesystem.writeLines(outputSpreadsheet, csvOutput);
            }
        }
        
    }
    
}
