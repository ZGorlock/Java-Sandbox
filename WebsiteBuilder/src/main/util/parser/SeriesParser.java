/*
 * File:    SeriesParser.java
 * Package: main.util.parser
 * Author:  Zachary Gill
 */

package main.util.parser;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import commons.access.Filesystem;
import commons.access.Internet;
import commons.access.Project;
import commons.lambda.stream.collector.MapCollectors;
import commons.object.string.StringComparisonUtility;
import commons.object.string.StringUtility;
import main.entity.shortcut.Shortcut;
import main.entity.video.show.SeriesInfo;
import main.util.FilenameUtil;
import main.util.HtmlParseUtil;
import main.util.persistence.LocationUtil;
import main.util.persistence.VariableUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class SeriesParser extends DataParser {
    
    //Static Methods
    
    public static Map<String, String> parseSeriesIndex(File html) {
        return parseHtmlLinkMap(html, VariableUtil.get(0x2c09), VariableUtil.get(0xd8ab), true)
                .entrySet().stream()
                .filter(e -> !e.getKey().matches(VariableUtil.get(0x919a)))
                .sorted(Map.Entry.comparingByKey()).collect(MapCollectors.toLinkedHashMap());
    }
    
    public static Map<String, Optional<File>> parseSeriesIndexFromSource(File html) {
        return Optional.ofNullable(html).filter(File::exists)
                .map(SeriesParser::parseSeriesIndex)
                .filter(e -> Filesystem.writeLines(
                        new File(html.getParentFile().getParentFile(), html.getName().replace(".html", ".txt")),
                        e.keySet()))
                .map(e -> Shortcut.createShortcuts(LocationUtil.getLocation(0xc45f, 0xe7e1, 0x4877), e))
                //.map(Mappers.forEach(e -> fixSeriesFolderNames(html)))
                .orElseGet(HashMap::new);
    }
    
    public static Map<String, Optional<File>> parseSeriesIndexFromSource() {
        return Optional.ofNullable(VariableUtil.get(0x6f8b)).map(e -> new File(Project.DATA_DIR, e))
                .map(SeriesParser::parseSeriesIndexFromSource)
                .orElseGet(HashMap::new);
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
        //info.poster = Optional.ofNullable(info.posterUrl)
        //        .map(e -> Internet.downloadFile(info.posterUrl, new File(Project.TMP_DIR, (Shortcut.getShortcutId(info.posterUrl) + Shortcut.DEFAULT_SHORTCUT_EXTENSION))))
        //        .map(Picture::loadPicture).orElse(null);
        
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
                        .map(SeriesParser::fetchSeriesInfo)
                        
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
                        .flatMap(SeriesParser::searchSeries)
                        .map(SeriesParser::fetchSeriesInfo)
                        
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
    
}
