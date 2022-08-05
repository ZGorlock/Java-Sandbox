/*
 * File:    VideoRenamer.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import common.Filesystem;

public class VideoRenamer {
    
    //Enums
    
    private enum EpisodeSource {
        WIKIPEDIA,
        GOOGLE
    }
    
    
    //Constants
    
    public static final File VIDEO_DIR = new File("E:\\Videos");
    
    public static final File DATA_DIR = new File("data");
    
    public static final File RESOURCES_DIR = new File("resources");
    
    private static final AtomicReference<String> ACTIVE_SHOW = new AtomicReference<>("");
    
    private static final Map<String, String> EPISODES = new HashMap<>();
    
    private static final EpisodeSource EPISODE_SOURCE = EpisodeSource.WIKIPEDIA;
    
    private static final boolean PRINT_PARSE_RESULTS = false;
    
    private static final boolean REPARSE_SOURCES = true;
    
    private static final boolean ALLOW_RENAMING = false;
    
    private static final boolean DO_ALL = true;
    
    private static final String DO_SHOW = "Stranger Things";
    
    
    //Static Functions
    
    private static final Supplier<String> activeShow = ACTIVE_SHOW::get;
    
    private static final Supplier<List<String>> allShows = () ->
            Stream.of(RESOURCES_DIR, DATA_DIR)
                    .flatMap(e -> Filesystem.getFiles(e).stream())
                    .map(e -> e.getName().replaceAll("\\.[^.]+$", ""))
                    .filter(e -> e.matches("^[A-Z].*"))
                    .distinct().collect(Collectors.toList());
    
    private static final Supplier<File> activeShowEpisodeDataFile = () ->
            new File(DATA_DIR, activeShow.get() + ".html");
    
    private static final Supplier<File> activeShowEpisodeListFile = () ->
            new File(RESOURCES_DIR, activeShow.get() + ".txt");
    
    private static final Supplier<File> activeShowDir = () ->
            new File(VIDEO_DIR, activeShow.get());
    
    
    //Main Method
    
    public static void main(String[] args) {
        Stream.of(DO_ALL ? allShows.get() : List.of(DO_SHOW))
                .flatMap(Collection::stream)
                .forEachOrdered(show -> {
                    ACTIVE_SHOW.set(show);
                    processShow();
                });
    }
    
    
    //Static Methods
    
    private static void processShow() {
        System.out.println(System.lineSeparator() +
                "*** " + activeShow.get() + " ***");
        
        parseEpisodes();
        loadEpisodes();
        renameVideos();
    }
    
    private static void parseEpisodes() {
        System.out.println("--- PARSING EPISODES ---");
        
        if (REPARSE_SOURCES || !activeShowEpisodeListFile.get().exists()) {
            switch (EPISODE_SOURCE) {
                case WIKIPEDIA:
                    wikipediaParser();
                    break;
                case GOOGLE:
                    googleGridParser();
                    break;
            }
        }
    }
    
    private static void wikipediaParser() {
        final Pattern pageTitlePattern = Pattern.compile(".*<title>(?<title>.+)\\s+-\\s+Wikipedia</title>.*");
        final Pattern pageSectionPattern = Pattern.compile(".*<h2><span\\s+class=\"mw-headline\"\\s+id=\"[^\"]+\">(?<section>.+?)</span>.+");
        
        final Pattern seasonTitlePattern = Pattern.compile("(?<seasonTitle>" +
                "(?<type>\\S+)\\s+" +
                "(?<season>[^\\s:<(]+)(?:\\s+(?<modifier>[^\\s:<(]+))?" +
                "(?:\\s*:\\s*(?<name>.+?))?" +
                "(?:\\s+\\((?<year>\\d+(?:\\D\\d+)?)\\))?)");
        final Pattern seasonPattern = Pattern.compile(".*" +
                "<h[34]>(?:<span\\s+id=\"[^\"]+\"></span>)?" +
                "<span\\s+class=\"mw-headline\"\\s+id=\"[^\"]+\">" + seasonTitlePattern + "(?:\\s*<span(?:\\s[^<>]+?)?>.*</span>)*</span>.*");
        
        final Pattern episodeSeqPattern = Pattern.compile("(?<sequence>\\d+(?:(?:[-–+]|<hr\\s*/>)\\d+)*)");
        final Pattern episodeNumPattern = Pattern.compile("(?<episode>.+?)");
        final Pattern episodeTitlePattern = Pattern.compile("(?<title>.+?)");
        final Pattern episodePattern = Pattern.compile(".*" +
                "<th\\s+scope=\"row\"\\s+rowspan=\"\\d+\"\\s+id=\"[^\"]+\"\\s+style=\"text-align:\\w+\">" + episodeSeqPattern + "</th>" +
                "(?:<td\\s+(?:style=\"text-align:\\w+\"|rowspan=\"\\d+\")>" + episodeNumPattern + "</td>)?" +
                "<td\\s+class=\"summary\"(?:\\s+rowspan=\"\\d+\")?\\s+style=\"text-align:\\w+\">" + episodeTitlePattern + "</td>.*");
        
        final List<String> episodes = new ArrayList<>();
        final Map<String, Boolean> episodeCache = new HashMap<>();
        
        int type = -1;
        boolean parsingSection = false;
        boolean parsingSeason = false;
        int currentSeason = 0;
        int currentEpisode = 0;
        int sequenceOffset = 0;
        EpisodeTitle queued = new EpisodeTitle();
        
        String line = null;
        try {
            for (String dataLine : Filesystem.readLines(activeShowEpisodeDataFile.get())) {
                line = dataLine.strip()
                        .replaceAll("&(?:nbsp|#(?:32|160));", " ").replace("&amp;", "&")
                        .replaceAll("</?(?:i|em|b|strong|u|small|mark|del|ins)(?:\\s+[^>]+)?>", "")
                        .replaceAll("<(su[pb])[\\s>].+?</\\1>", "")
                        .replaceAll("<span\\s+class=\"nowrap\">([^<>]+)</span>", "$1");
                
                if (queued.isValid()) {
                    if (episodeCache.replace(queued.getEpisodeId(), true) == null) {
                        for (int part = (queued.parts.isEmpty() ? 0 : 1); part <= queued.parts.size(); part++) {
                            episodes.add(queued.getEpisodeTitle(part));
                        }
                    } else {
                        throw new RuntimeException("Parser found duplicated Episode key: " + queued.getEpisodeId());
                    }
                    
                    queued = new EpisodeTitle();
                    queued.season = currentSeason;
                }
                
                final Matcher episodeMatcher = episodePattern.matcher(line);
                if (episodeMatcher.matches()) {
                    final String sequenceMatch = episodeMatcher.group("sequence");
                    final String episodeMatch = episodeMatcher.group("episode");
                    final String titleMatch = episodeMatcher.group("title");
                    
                    if (!parsingSeason) {
                        continue;
                    }
                    if (currentSeason == 0) {
                        throw new RuntimeException("Parser failed to locate Season: 1");
                    }
                    
                    final List<String> titleParts = Arrays.stream(titleMatch.strip()
                                    .replaceAll("(?:</?(?:a|br|hr|link|span|style)(?:\\s.+?(?:>[^<]+</style)?)?>)", "")
                                    .replaceAll("\"([^()\"]+\\S)\\(([^()\"]+)\\)\"", "\"$1-$2\"")
                                    .replaceAll("\"([^()\"]+)\\s\\(([^()\"]+)\\)\\s([^()\"]+)\"", "\"$1 '$2' $3\"")
                                    .split("\"\"|\\s+\\("))
                            .map(String::strip).map(e -> e.replaceAll("^[(\"]+\\s*|\\s*[\")]+$", ""))
                            .collect(Collectors.toList());
                    
                    final List<Integer> episodeSet = Arrays.stream(
                                    Optional.ofNullable(episodeMatch).orElse(sequenceMatch).strip().split("\\D+"))
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());
                    
                    if (titleParts.isEmpty() || titleParts.size() > 2) {
                        throw new RuntimeException("Parse found invalid title: '" + titleMatch + "'");
                    }
                    
                    queued.title = titleParts.get(0)
                            .replaceAll("\"\\s+\\([^)]+\\)$", "")
                            .replaceAll("(?i)Pt\\.\\s*", "Part ")
                            .replaceAll("(?:(?<!-)|\\s*[,:])\\s+(Part\\s+|Conclusion$)", " - $1")
                            .replaceAll("(?i)(>?\"?)(?:Episode|Chapter)\\s+(?:\\d+|One|Two|Three|Four|Five|Six|Seven|Eight|Nine|Ten):", "$1");
                    queued.subTitle = (titleParts.size() > 1) ? titleParts.get(1) : "";
                    
                    if (!queued.subTitle.isEmpty()) {
                        if (queued.subTitle.matches("(?i).*episode.*") ||
                                queued.subTitle.toLowerCase().replaceAll("[aeiouy]", "").contains(queued.title.toLowerCase().replaceAll("[aeiouy]", ""))) {
                            queued.subTitle = "";
                        } else if (queued.title.equalsIgnoreCase("Pilot") || queued.title.matches("(?i)(?:Episode|Chapter|Part)\\s+\\d+")) {
                            queued.title = queued.subTitle;
                            queued.subTitle = "";
                        }
                    }
                    
                    for (int episodeNum = episodeSet.get(0); episodeNum <= episodeSet.get(episodeSet.size() - 1); episodeNum++) {
                        
                        if (!((episodeNum == 0) && (currentEpisode == 0)) && (++currentEpisode != episodeNum)) {
                            if ((episodeMatch == null) && (currentSeason > 1) && (episodeNum == (episodes.size() + 1))) {
                                currentEpisode = episodeNum;
                                sequenceOffset = currentEpisode - 1;
                            } else {
                                throw new RuntimeException("Parser failed to locate Season: " + currentSeason + " Episode: " + currentEpisode);
                            }
                        }
                        
                        if (queued.episode < 0) {
                            queued.episode = episodeNum - sequenceOffset;
                        }
                        if (episodeSet.size() > 1) {
                            queued.addPart();
                        }
                    }
                    
                    continue;
                }
                
                final Matcher seasonMatcher = seasonPattern.matcher(line);
                if (seasonMatcher.matches()) {
                    final String seasonTitleMatch = seasonMatcher.group("seasonTitle");
                    final String typeMatch = seasonMatcher.group("type");
                    final String seasonMatch = seasonMatcher.group("season");
                    final String modifierMatch = seasonMatcher.group("modifier");
                    final String nameMatch = seasonMatcher.group("name");
                    final String yearMatch = seasonMatcher.group("year");
                    
                    if (!parsingSection) {
                        continue;
                    }
                    if ((currentSeason > 0) && (currentEpisode == 0)) {
                        if (type == 2) {
                            currentSeason = 0;
                            if (seasonMatch.equalsIgnoreCase("overview")) {
                                currentSeason = 1;
                                continue;
                            }
                        } else {
                            throw new RuntimeException("Parser failed to locate any episodes in Season: " + currentSeason);
                        }
                    }
                    
                    int seasonNum = (seasonMatch.strip().matches("\\d+")) ? Integer.parseInt(seasonMatch.strip()) :
                            List.of("zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten").indexOf(seasonMatch.toLowerCase());
                    
                    if (seasonNum < 0) {
                        if (seasonMatch.equalsIgnoreCase("season") && typeMatch.matches("\\d+")) {
                            seasonNum = currentSeason + 1;
                        } else if (type == 2) {
                            seasonNum = 1;
                        } else {
                            parsingSeason = false;
                            continue;
                        }
                    }
                    if (++currentSeason != seasonNum) {
                        if (seasonMatcher.group("modifier") != null) {
                            currentSeason--;
                            parsingSeason = false;
                            continue;
                        }
                        throw new RuntimeException("Parser failed to locate Season: " + currentSeason);
                    }
                    
                    parsingSeason = true;
                    queued.season = currentSeason;
                    currentEpisode = 0;
                    
                    if (PRINT_PARSE_RESULTS) {
                        System.out.println(seasonTitleMatch);
                    }
                    continue;
                } else if (line.contains("<h3>")) {
                    if (type != 2) {
                        parsingSeason = false;
                    }
                    continue;
                }
                
                final Matcher pageSectionMatcher = pageSectionPattern.matcher(line);
                if (pageSectionMatcher.matches()) {
                    final String sectionMatch = pageSectionMatcher.group("section");
                    
                    if (sectionMatch.strip().matches("(?i)Episode(?:s|\\sList)")) {
                        parsingSection = true;
                        if (type == 2) {
                            parsingSeason = true;
                            currentSeason = 1;
                            queued.season = 1;
                        }
                    } else if (parsingSection) {
                        break;
                    }
                    
                    continue;
                }
                
                final Matcher pageTitleMatcher = pageTitlePattern.matcher(line);
                if (pageTitleMatcher.matches()) {
                    final String titleMatch = pageTitleMatcher.group("title");
                    
                    final String pageTitle = titleMatch.strip()
                            .replace(":", " -")
                            .replaceAll("-(\\d+)", "$1");
                    type = (pageTitle.matches("(?i)List\\s+of\\s+" + activeShow.get() + "(?:\\s+\\(.+\\))?\\s+episodes")) ? 1 :
                            (pageTitle.matches("(?i)" + activeShow.get() + "(?:\\s+\\(.+\\))?")) ? 2 : -1;
                    
                    if (type == -1) {
                        throw new RuntimeException("Parser failed to determine Wikipedia page type");
                    }
                }
                
            }
        } catch (Exception e) {
            System.out.println(System.lineSeparator() + line);
            throw e;
        }
        
        if (episodes.isEmpty()) {
            throw new RuntimeException("Parser failed to locate any episodes");
        } else {
            if (PRINT_PARSE_RESULTS) {
                System.out.println("Parsed " + episodes.size() + " episodes (" + currentSeason + " seasons)");
            }
        }
        
        Collections.sort(episodes);
        Filesystem.writeLines(activeShowEpisodeListFile.get(), episodes);
    }
    
    private static void googleGridParser() {
        final Pattern episodeTitlePattern = Pattern.compile("\\s*<div\\sclass=\"title\">(?<episode>.+)\\s*·\\s*(?<title>.*)</div>\\s*");
        
        final List<String> episodes = new ArrayList<>();
        final Map<String, Boolean> episodeCache = new HashMap<>();
        
        for (String line : Filesystem.readLines(activeShowEpisodeDataFile.get())) {
            
            final Matcher episodeTitleMatcher = episodeTitlePattern.matcher(line);
            if (episodeTitleMatcher.matches()) {
                final String episodeMatch = episodeTitleMatcher.group("episode");
                final String titleMatch = episodeTitleMatcher.group("title");
                
                if (episodeCache.replace(episodeMatch.strip(), true) == null) {
                    episodes.add(EpisodeTitle.generateEpisodeTitle(episodeMatch.strip(), titleMatch.strip()));
                }
            }
        }
        
        Collections.sort(episodes);
        Filesystem.writeLines(activeShowEpisodeListFile.get(), episodes);
    }
    
    private static void loadEpisodes() {
        System.out.println("--- LOADING EPISODES ---");
        
        final Pattern episodeTitlePattern = Pattern.compile("^" + EpisodeTitle.EPISODE_ID_PATTERN + EpisodeTitle.TITLE_SEPARATOR + EpisodeTitle.TITLE_PATTERN + "$");
        
        EPISODES.clear();
        for (String line : Filesystem.readLines(activeShowEpisodeListFile.get())) {
            
            final Matcher episodeTitleMatcher = episodeTitlePattern.matcher(line);
            if (episodeTitleMatcher.matches()) {
                final String episodeMatch = episodeTitleMatcher.group("episode");
                final String titleMatch = episodeTitleMatcher.group("title");
                
                EPISODES.put(episodeMatch, EpisodeTitle.cleanTitle(titleMatch));
                
            } else {
                throw new RuntimeException(line + " does not match");
            }
        }
    }
    
    private static void renameVideos() {
        System.out.println("--- RENAMING VIDEOS ---");
        
        for (File video : Filesystem.getFilesAndDirsRecursively(activeShowDir.get())) {
            final String videoName = video.getName();
            if (video.isDirectory() || !videoName.endsWith(".mp4") ||
                    !video.getAbsolutePath().contains(activeShow.get() + File.separator + "Season")) {
                continue;
            }
            
            final Matcher videoNameMatcher = EpisodeTitle.EPISODE_TITLE_PATTERN.matcher(videoName);
            if (videoNameMatcher.matches()) {
                final String episode = videoNameMatcher.group("episode");
                final String episode2 = videoNameMatcher.group("episode2");
                final String fileType = videoNameMatcher.group("fileType");
                final boolean doubleEpisode = (episode2 != null);
                
                if (EPISODES.containsKey(episode) && (!doubleEpisode || EPISODES.containsKey(episode2))) {
                    final String newVideoName = !doubleEpisode ?
                            EpisodeTitle.generateEpisodeTitle(true, episode, EPISODES.get(episode), fileType) :
                            EpisodeTitle.generateDoubleEpisodeTitle(true, episode, episode2, EPISODES.get(episode), EPISODES.get(episode2), fileType);
                    final File newVideo = new File(video.getParentFile(), newVideoName);
                    
                    if (!newVideoName.equalsIgnoreCase(videoName)) {
                        System.out.println((ALLOW_RENAMING ? "Renaming: " : "Would have Renamed: ") + video.getName());
                        System.out.println((ALLOW_RENAMING ? "      To: " : "                To: ") + newVideo.getName());
                        
                        if (ALLOW_RENAMING) {
                            if (!Filesystem.renameFile(video, newVideo)) {
                                System.err.println("Failed to rename: " + video.getAbsolutePath() + " to:" + newVideo.getAbsolutePath());
                            }
                        }
                    }
                    
                } else {
                    if (!videoName.contains("E00 - ") && videoName.contains("Viewer Special") && videoName.contains("Behind The Scenes")) {
                        System.err.println("Failed to lookup episode title for: " + video.getAbsolutePath());
                    }
                }
            } else {
                System.err.println("Failed to parse file name of: " + video.getAbsolutePath());
            }
        }
    }
    
    
    //Inner Classes
    
    private static class EpisodeTitle {
        
        //Constants
        
        public static final char TITLE_SEPARATOR_CHAR = '-';
        
        public static final String TITLE_SEPARATOR = " " + TITLE_SEPARATOR_CHAR + " ";
        
        public static final String EPISODE_ID_SEPARATOR = "";
        
        public static final char DOUBLE_TITLE_SEPARATOR_CHAR = '~';
        
        public static final String DOUBLE_TITLE_SEPARATOR = " " + DOUBLE_TITLE_SEPARATOR_CHAR + " ";
        
        public static final String DOUBLE_EPISODE_ID_SEPARATOR = "-";
        
        public static final Pattern SHOW_NAME_PATTERN = Pattern.compile("(?<show>.+)");
        
        public static final Pattern TITLE_PATTERN = Pattern.compile("(?<title>[^" + DOUBLE_TITLE_SEPARATOR_CHAR + "]+?)");
        
        public static final Pattern EPISODE_ID_PATTERN = Pattern.compile("(?<episode>S(?<seasonNum>\\d{2})" + EPISODE_ID_SEPARATOR + "E(?<episodeNum>\\d{2}(?:\\.\\d+)?))");
        
        public static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("(?<fileExtension>\\.(?<fileType>\\w{3}))");
        
        public static final Pattern EPISODE_TITLE_PATTERN = Pattern.compile(
                "^" + SHOW_NAME_PATTERN +
                        "(?:" + TITLE_SEPARATOR + EPISODE_ID_PATTERN + ("(?:" + DOUBLE_EPISODE_ID_SEPARATOR + EPISODE_ID_PATTERN.pattern().replaceAll("(?=>)", "2") + ")?") + ")" +
                        "(?:" + TITLE_SEPARATOR + TITLE_PATTERN + ("(?:" + DOUBLE_TITLE_SEPARATOR + TITLE_PATTERN.pattern().replaceAll("(?=>)", "2") + ")?") + ")?" +
                        FILE_EXTENSION_PATTERN + "?$");
        
        
        //Fields
        
        public int season = -1;
        
        public int episode = -1;
        
        public String title = "";
        
        public String subTitle = "";
        
        final List<EpisodeTitle> parts = new ArrayList<>();
        
        
        //Constructors
        
        public EpisodeTitle(int season, int episode, String title, String subTitle) {
            this.season = season;
            this.episode = episode;
            this.title = title;
            this.subTitle = subTitle;
        }
        
        public EpisodeTitle() {
        }
        
        
        //Methods
        
        public String getSeasonKey() {
            return generateSeasonKey(season);
        }
        
        public String getEpisodeKey() {
            return generateEpisodeKey(episode);
        }
        
        public String getEpisodeId() {
            return generateEpisodeId(season, episode);
        }
        
        public String getTitle() {
            return generateTitle(title, subTitle);
        }
        
        public String getEpisodeTitle(boolean prependShow) {
            return generateEpisodeTitle(prependShow, getEpisodeId(), getTitle());
        }
        
        public String getEpisodeTitle() {
            return getEpisodeTitle(false);
        }
        
        public String getEpisodeTitle(boolean prependShow, int part) {
            return ((part > 0) ? parts.get(part - 1) : this).getEpisodeTitle(prependShow);
        }
        
        public String getEpisodeTitle(int part) {
            return getEpisodeTitle(false, part);
        }
        
        public void addPart() {
            parts.add(new EpisodeTitle(
                    season, (episode + parts.size()), title, generateTitle(this.subTitle, ("Part " + (parts.size() + 1)))));
        }
        
        public boolean isValid() {
            return (season > 0) && (episode >= 0) && !title.isEmpty();
        }
        
        
        //Static Methods
        
        public static String generateSeasonKey(int season) {
            return "S" + ((season < 10) ? "0" : "") + season;
        }
        
        public static String generateEpisodeKey(int episode) {
            return "E" + ((episode < 10) ? "0" : "") + episode;
        }
        
        public static String generateEpisodeId(int season, int episode) {
            return generateSeasonKey(season) + generateEpisodeKey(episode);
        }
        
        public static String generateTitle(String title, String subTitle) {
            return title + ((title.isEmpty() || subTitle.isEmpty()) ? "" : TITLE_SEPARATOR) + subTitle;
        }
        
        public static String generateEpisodeTitle(boolean prependShow, String episodeId, String title, String fileExtension) {
            return Optional.of((prependShow ? (activeShow.get() + TITLE_SEPARATOR) : "") +
                            episodeId + TITLE_SEPARATOR + title +
                            Optional.ofNullable(fileExtension).map(e -> ((e.startsWith(".") ? "" : ".") + e)).orElse(""))
                    .map(EpisodeTitle::cleanTitle).orElse("");
        }
        
        public static String generateEpisodeTitle(boolean prependShow, String episodeId, String title) {
            return generateEpisodeTitle(prependShow, episodeId, title, null);
        }
        
        public static String generateEpisodeTitle(String episodeId, String title) {
            return generateEpisodeTitle(false, episodeId, title);
        }
        
        public static String generateDoubleEpisodeTitle(boolean prependShow, String episodeId1, String episodeId2, String title1, String title2, String fileExtension) {
            final String title = (title1.replaceAll("\\s+-\\s+Part\\s\\d+$", "").equals(title2.replaceAll("\\s+-\\s+Part\\s\\d+$", ""))) ?
                    title1.replaceAll("\\s+-\\s+Part\\s\\d+$", "") :
                    title1 + DOUBLE_TITLE_SEPARATOR + title2;
            return generateEpisodeTitle(prependShow, (episodeId1 + DOUBLE_EPISODE_ID_SEPARATOR + episodeId2), title, fileExtension);
        }
        
        public static String generateDoubleEpisodeTitle(boolean prependShow, String episodeId1, String episodeId2, String title1, String title2) {
            return generateDoubleEpisodeTitle(prependShow, episodeId1, episodeId2, title1, title2, null);
        }
        
        public static String generateDoubleEpisodeTitle(String episodeId1, String episodeId2, String title1, String title2) {
            return generateDoubleEpisodeTitle(false, episodeId1, episodeId2, title1, title2);
        }
        
        public static String cleanTitle(String title) {
            return Normalizer.normalize(title, Normalizer.Form.NFC)
                    .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS}+", "")
                    .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS_SUPPLEMENT}+", "")
                    .strip()
                    
                    .replaceAll("(?i)&amp;", "&")
                    .replaceAll("(?i)&(?:nbsp|#(?:32|160));", " ")
                    .strip()
                    
                    .replace("×", "x")
                    .replace("÷", "%")
                    .replace("‰", "%")
                    .replaceAll("[⋯…]", "...")
                    .replace("ˆ", "^")
                    .replaceAll("[›»]", ">")
                    .replaceAll("[‹«]", "<")
                    .replaceAll("[•·]", "*")
                    .strip()
                    
                    .replaceAll("[‚„¸]", ",")
                    .replaceAll("[`´‘’]", "'")
                    .replaceAll("[“”]", "\"")
                    .replaceAll("[¦︱︲]", "|")
                    .replaceAll("[᐀゠⸗]", "=")
                    .replaceAll("[¬¨－﹣﹘⸻⸺¯−₋⁻―—–‒‑‐᠆־]", "-")
                    .replaceAll("[⁓֊〜〰]", "~")
                    .replaceAll("[™©®†‡§¶]", "")
                    .strip()
                    
                    .replaceAll("[\r\n\t ]+", " ")
                    .replaceAll("\\p{Cntrl}&&[^\r\n\t]", "")
                    .replaceAll("[^\\x00-\\xFF]", "")
                    .strip()
                    
                    .replaceAll("(\\d{1,2}):(\\d{2}):(\\d{2})", "$1-$2-$3")
                    .replaceAll("(\\d{1,2}):(\\d{2})", "$1-$2")
                    .replaceAll("(\\d{1,2})/(\\d{1,2})", "$1-$2")
                    .strip()
                    
                    .replaceAll("[:;|/\\\\]", " - ")
                    .replaceAll("[?<>*]", "")
                    .replace("\"", "'")
                    .strip()
                    
                    .replaceAll("\\++", "+")
                    .replaceAll("-+", "-")
                    .replace("+-", "+ -")
                    .replaceAll("(?:-\\s+)+", "- ")
                    .replaceAll("(?:\\+\\s+)+", "+ ")
                    .replaceAll("(?:\\s+-\\s+)+", " - ")
                    .replaceAll("^\\s*(?:[\\-+\"]+\\s+)+|(?:\\s+[\\-+\"]+)+\\s*$", "")
                    .strip()
                    
                    .replaceAll("!(?:\\s*!)+|(?:\\s*!)+$", "!")
                    .replaceAll("\\?(?:\\s*\\?)+|(?:\\s*\\?)+$", "?")
                    .replaceAll("\\$(?:\\s*\\$)+", Matcher.quoteReplacement("$"))
                    .replaceAll("\\s+", " ")
                    .strip();
        }
        
    }
    
}
