/*
 * File:    WebsiteBuilder.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import commons.access.Filesystem;
import commons.access.Internet;
import commons.lambda.stream.collector.MapCollectors;
import commons.object.string.StringComparisonUtility;
import commons.object.string.StringUtility;
import main.entity.base.Entity;
import main.entity.image.picture.PictureAlbum;
import main.entity.shortcut.Shortcut;
import main.entity.shortcut.subreddit.SubredditRegistry;
import main.entity.video.clip.ClipLibrary;
import main.entity.video.show.SeriesInfo;
import main.util.FilenameUtil;
import main.util.HtmlParseUtil;
import main.util.persistence.LocationUtil;
import main.util.persistence.VariableUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WebsiteBuilder {
    
    //Constants
    
    public static final File BASE_DIR = LocationUtil.getRootLocation();
    
    public static final File OUTPUT_DIR = LocationUtil.getLocation(0x255e);
    
    public static final File TMP_DIR = LocationUtil.getLocation(0x8605);
    
    public static final boolean SAFE_MODE = false;
    
    public static final boolean TEST_MODE = false;
    
    public static final boolean REPROCESS_ALL = false;
    
    
    //Enums
    
    private enum FolderType {
        IMAGE,
        VIDEO,
        SUBREDDIT,
        WEBSITE
    }
    
    
    //Main Methods
    
    public static void main(String[] args) throws Exception {
        System.out.println("Done");

//        final Map<String, String> usersLinkMap = parseUsers(new File(Project.TMP_DIR, "users.html"));
//        final Map<String, Optional<File>> userShortcuts = Shortcut.createShortcuts(LocationUtil.getLocation(0xc45f, 0x19c1), usersLinkMap);

//        final Map<String, String> redditIndexLinkMap = parseRedditIndex(new File(Project.TMP_DIR, "redditIndex.html"));
//        final Map<String, Optional<File>> redditIndexShortcuts = Shortcut.createShortcuts(LocationUtil.getLocation(0xc45f, 0x11ee, 0x26d5), redditIndexLinkMap);

//        final Map<String, String> seriesLinkMap = parseSeriesList(new File(Project.TMP_DIR, "seriesList.html"));
//        fixSeriesFolderNames(LocationUtil.getLocation(0x4df6, 0xe7e1));

//        PictureAlbum a = PictureAlbum.loadAlbum(LocationUtil.getLocation(0x06a7, 0xa356));
//        ClipLibrary l = ClipLibrary.loadLibrary(LocationUtil.getLocation(0x06a7, 0x4df6));
//        SubredditRegistry r = SubredditRegistry.loadRegistry(LocationUtil.getLocation(0x06a7, 0xc45f));

//        final List<PictureAlbum> galleries = loadGalleries();
//        final List<ClipLibrary> clipLibraries = loadClipLibraries();
//        final List<SubredditRegistry> subredditLibraries = loadSubredditRegistry();

//        IntStream.range(0, 10).mapToObj(i -> clipLibraries.get(0).generateTitleInLexicon()).forEach(System.out::println);
        
        System.out.println("Done");
    }
    
    
    //Static Methods
    
    private static List<File> getSourceFolders(FolderType type) {
        return Filesystem.getDirs(BASE_DIR,
                d -> !d.getName().startsWith(Entity.META_PREFIX));
    }
    
    private static List<SubredditRegistry> loadSubredditRegistry() {
        return LocationUtil.getEntityLocations(SubredditRegistry.class).stream()
                .map(SubredditRegistry::loadRegistry)
                .collect(Collectors.toList());
    }
    
    private static List<PictureAlbum> loadGalleries() {
        return LocationUtil.getEntityLocations(PictureAlbum.class).stream()
                .map(PictureAlbum::loadAlbum)
                .collect(Collectors.toList());
    }
    
    private static List<ClipLibrary> loadClipLibraries() {
        return LocationUtil.getEntityLocations(ClipLibrary.class).stream()
                .map(ClipLibrary::loadLibrary)
                .collect(Collectors.toList());
    }
    
    private static Map<String, String> parseHtmlLinkMap(File html, String linkLocator, String urlPrefix) {
        final Optional<String> linkAttr = Optional.of(linkLocator)
                .filter(e -> !e.isBlank())
                .map(e -> e.split("\\s+[>+~]\\s+")).filter(e -> (e.length > 0))
                .map(e -> e[e.length - 1]).filter(e -> e.contains("["))
                .map(e -> e.replaceAll(".*\\[\\^?(.+?)[=^$*~\\]][^\\[\\]]*$", "$1"));
        
        return Optional.ofNullable(html).map(Filesystem::readFileToString).map(Jsoup::parse)
                .map(e -> e.select(linkLocator))
                .stream().flatMap(Collection::stream)
                .map(e -> Map.entry(
                        FilenameUtil.cleanTitle(e.text()),
                        linkAttr.map(e::attr).orElseGet(() -> e.attr("href"))))
                .filter(e -> !e.getKey().isBlank())
                .filter(e -> !e.getValue().isBlank())
                .collect(MapCollectors.toLinkedHashMap(
                        Map.Entry::getKey,
                        e -> (urlPrefix + e.getValue())));
    }
    
    private static Map<String, String> parseUsers(File html) {
        return parseHtmlLinkMap(html, VariableUtil.get(0xb013), VariableUtil.get(0x80d4));
    }
    
    private static Map<String, String> parseSeriesList(File html) {
        return parseHtmlLinkMap(html, VariableUtil.get(0x2c09), VariableUtil.get(0xd8ab))
                .entrySet().stream()
                .filter(e -> !e.getKey().matches(VariableUtil.get(0x919a)))
                .sorted(Map.Entry.comparingByKey()).collect(MapCollectors.toLinkedHashMap());
    }
    
    private static Map<String, String> parseRedditIndex(File html) {
        final Stack<String> path = new Stack<>();
        return Optional.ofNullable(html).map(Filesystem::readFileToString).map(Jsoup::parse)
                .map(e -> e.selectFirst(VariableUtil.get(0xe09b)))
                .map(index -> index.children().select("h1, h2, h3, h4, h5, " + VariableUtil.get(0x631d)))
                .stream().flatMap(Collection::stream)
                .map(e -> (e.tagName().replaceAll("\\d", "").equals("h") ? "./".repeat(Integer.parseInt(e.tagName().replaceAll("\\D", ""))) : "") +
                        e.ownText().replaceAll("/r/", ""))
                .filter(e -> {
                    if (e.startsWith("./")) {
                        while (path.size() >= e.replaceAll("[^/]", "").length()) {
                            path.pop();
                        }
                        path.push(e.replaceAll("^(\\./)+", ""));
                        return false;
                    }
                    return true;
                })
                .map(e -> path.stream().collect(Collectors.joining("/", "", ("/" + e))))
                .map(e -> Map.entry(e, (VariableUtil.get(0x916d) + e.substring(e.lastIndexOf('/') + 1))))
                .collect(MapCollectors.toLinkedHashMap());
    }
    
    private static SeriesInfo fetchSeriesInfo(String seriesUrl) {
        final SeriesInfo info = new SeriesInfo(seriesUrl);
        
        final Document seriesDoc = Optional.ofNullable(seriesUrl)
                .map(Internet::getHtml)
                .orElse(null);
        
        info.tags = HtmlParseUtil.select(seriesDoc, 0xb643).map(Element::text).collect(Collectors.toList());
        info.rating = HtmlParseUtil.text(seriesDoc, 0x8f78).orElse(null);
        info.description = HtmlParseUtil.text(seriesDoc, 0x976e).orElse(null);
        info.title = HtmlParseUtil.text(seriesDoc, 0x1e7e).orElse(null);
        
        final Map<String, String> details = HtmlParseUtil.select(seriesDoc, 0x84bb)
                .map(e -> Map.entry(
                        HtmlParseUtil.text(e, 0x0de8).orElse(""),
                        HtmlParseUtil.text(e, 0x2191).orElse("")))
                .filter(e -> !e.getKey().isBlank()).filter(e -> !e.getValue().isBlank())
                .collect(MapCollectors.toLinkedHashMap());
        
        info.originalTitle = details.get("Original title");
        info.studio = details.get("Studio");
        info.firstAirDate = details.get("First air date");
        info.lastAirDate = details.get("Last air date");
        info.episodeCount = details.get("Episodes");
        info.status = details.get("Status");
        
        info.posterUrl = HtmlParseUtil.attr(seriesDoc, 0x702b, "data-src").orElse(null);
//        info.poster = Optional.ofNullable(info.posterUrl)
//                .map(e -> Internet.downloadFile(info.posterUrl, new File(TMP_DIR, info.posterUrl.replaceAll("^.+/([^/]+)$", "$1"))))
//                .map(Picture::loadPicture).orElse(null);
        
        return info;
    }
    
    private static Optional<String> searchSeries(String title) {
        System.out.println();
        System.out.println(title);
        
        final String search = FilenameUtil.formatSearchTitle(title);
        final String searchUrl = String.format("%s?s=%s", VariableUtil.get(0x70e1), search);
        
        System.out.println(StringUtility.format("Search: '{}'", searchUrl));
        
        final Document searchDoc = Optional.ofNullable(searchUrl)
                .map(Internet::getHtml)
                .orElseThrow();
        
        final Optional<Element> searchResult = Optional.of(searchDoc)
                .map(e -> e.select(VariableUtil.get(0x4f8c)))
                .flatMap(e -> e.stream()
                        .filter(e2 -> !e2.ownText().isBlank())
                        .sorted(Comparator.comparingDouble(o -> -StringComparisonUtility.stringCompare(title, o.ownText(), true, false)))
                        .limit(1).findFirst());
        
        final Optional<String> searchResultTitle = searchResult
                .map(Element::ownText)
                .filter(e -> !e.isEmpty());
        
        if (searchResultTitle.isPresent()) {
            if (StringComparisonUtility.stringCompare(title, searchResultTitle.get(), true, false) < 0.75) {
                System.err.println(StringUtility.format("Searching for: \"{}\" returned: \"{}\"", title, searchResultTitle.get()));
                return Optional.empty();
            }
            System.out.println(StringUtility.format("Searching for: \"{}\" returned: \"{}\"", title, searchResultTitle.get()));
        } else {
            System.err.println(StringUtility.format("Searching for: \"{}\" returned no result", title));
            return Optional.empty();
        }
        
        final Optional<String> searchResultUrl = searchResult
                .map(e -> e.attr("href"))
                .filter(e -> !e.isEmpty());
        
        if (searchResultUrl.isPresent()) {
            System.out.println(StringUtility.format("Found url: \"{}\"", searchResultUrl.get()));
        } else {
            System.err.println("Found no url");
            return Optional.empty();
        }
        
        return searchResultUrl;
    }
    
    private static void fixSeriesShortcutNames(File shortcutDir) {
        Optional.of(shortcutDir)
                .map(Filesystem::getFiles)
                .stream().flatMap(Collection::stream)
                
                .forEach(shortcut -> Optional.of(shortcut)
                        .map(Shortcut::readUrlFromShortcut)
                        .map(WebsiteBuilder::fetchSeriesInfo)
                        
                        .ifPresent(info -> Optional.of(info)
                                .map(e -> e.title).map(FilenameUtil::cleanTitle)
                                .filter(newTitle -> !FilenameUtil.getTitle(shortcut).equals(newTitle))
                                
                                .ifPresent(newTitle -> {
                                    System.out.println(String.join(System.lineSeparator(),
                                            "Original: " + FilenameUtil.getTitle(shortcut),
                                            "Remote:   " + info.title,
                                            "Cleaned:  " + newTitle, ""));
                                    Filesystem.renameFile(shortcut, new File(shortcut.getParentFile(), (newTitle + ".url")));
                                })));
    }
    
    private static void fixSeriesFolderNames(File folderDir) {
        Optional.of(folderDir)
                .map(Filesystem::getDirs)
                .stream().flatMap(Collection::stream)
                
                .forEach(folder -> Optional.of(folder)
                        .map(File::getName)
                        .flatMap(WebsiteBuilder::searchSeries)
                        .map(WebsiteBuilder::fetchSeriesInfo)
                        
                        .ifPresent(info -> Optional.of(info)
                                .map(e -> e.title).map(FilenameUtil::cleanTitle)
                                .filter(newTitle -> !folder.getName().equals(newTitle))
                                
                                .ifPresent(newTitle -> {
                                    System.out.println(String.join(System.lineSeparator(),
                                            "Original: " + folder.getName(),
                                            "Remote:   " + info.title,
                                            "Cleaned:  " + newTitle, ""));
                                    Filesystem.renameDirectory(folder, new File(folder.getParentFile(), newTitle));
                                })));
    }
    
    private static void testUpgradeSampleImages() {
        File redir = LocationUtil.getLocation(0x8605);
        List<File> redo = Filesystem.getFiles(redir);
        
        File good = new File(redir, "A");
        Filesystem.createDirectory(good);
        
        File old = new File(redir, "B");
        Filesystem.createDirectory(old);
        
        for (File f : redo) {
            if (f.getName().contains("__" + VariableUtil.get(0x164a))) {
                
                String fileName = f.getName();
                String hash = fileName.replaceAll(".*-([^\\-_]+\\.[^.]+)$", "$1");
                String title = fileName.replaceAll((VariableUtil.get(0x164a) + "-"), "");
                
                String url = VariableUtil.get(0x47ea) + StringUtility.lSnip(hash, 2) + "/" + StringUtility.lSnip(StringUtility.lShear(hash, 2), 2) + "/" + title;
                String saveName = title;//.replaceAll("^_+", "");//.replaceAll("__", "__");
                File save = new File(good, saveName);
                
                int g = 4;
                File downloaded = Internet.downloadFile(url, save);
                
                if (downloaded == null) {
                    url = url.replace(".jpg", ".tmp").replace(".png", ".jpg").replace(".tmp", ".png");
                    saveName = saveName.replace(".jpg", ".tmp").replace(".png", ".jpg").replace(".tmp", ".png");
                    save = new File(good, saveName);
                    downloaded = Internet.downloadFile(url, save);
                }
                
                if (downloaded != null) {
                    File oldDone = new File(old, f.getName());
                    Filesystem.moveFile(f, oldDone);
                }
            }
        }
    }
    
}
