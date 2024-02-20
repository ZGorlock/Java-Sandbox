/*
 * File:    IndexParser.java
 * Package: main.util.parser
 * Author:  Zachary Gill
 */

package main.util.parser;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import commons.access.Filesystem;
import commons.access.Project;
import commons.lambda.stream.collector.MapCollectors;
import main.entity.shortcut.Shortcut;
import main.util.persistence.LocationUtil;
import main.util.persistence.VariableUtil;
import org.jsoup.Jsoup;

public class IndexParser extends DataParser {
    
    //Static Methods
    
    public static Map<String, String> parseIndex(File html) {
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
    
    public static Map<String, Optional<File>> parseIndexFromSource(File html) {
        return Optional.ofNullable(html).filter(File::exists)
                .map(IndexParser::parseIndex)
                .filter(e -> Filesystem.writeLines(
                        new File(html.getParentFile().getParentFile(), html.getName().replace(".html", ".txt")),
                        e.keySet()))
                .map(e -> Shortcut.createShortcuts(LocationUtil.getLocation(0xc45f, 0x11ee, 0x26d5), e))
                .orElseGet(HashMap::new);
    }
    
    public static Map<String, Optional<File>> parseIndexFromSource() {
        return Optional.ofNullable(VariableUtil.get(0x1e02)).map(e -> new File(Project.DATA_DIR, e))
                .map(IndexParser::parseIndexFromSource)
                .orElseGet(HashMap::new);
    }
    
}
