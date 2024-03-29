/*
 * File:    DataParser.java
 * Package: main.util.parser
 * Author:  Zachary Gill
 */

package main.util.parser;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import commons.access.Filesystem;
import commons.lambda.stream.collector.MapCollectors;
import main.entity.shortcut.Shortcut;
import main.util.FilenameUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public abstract class DataParser {
    
    //Static Methods
    
    protected static Map<String, String> parseHtmlLinkMap(File html, String linkLocator, String urlPrefix, boolean useOriginalTitles) {
        final String linkAttr = Optional.of(linkLocator)
                .filter(e -> !e.isBlank())
                .map(e -> e.split("\\s+[>+~]\\s+")).filter(e -> (e.length > 0))
                .map(e -> e[e.length - 1]).filter(e -> e.contains("["))
                .map(e -> e.replaceAll(".*\\[\\^?(.+?)[=^$*~\\]][^\\[\\]]*$", "$1"))
                .orElse(null);
        
        final Function<Element, String> linkExtractor = (Element element) ->
                Optional.ofNullable(element)
                        .map(e -> Optional.ofNullable(linkAttr)
                                .map(element::attr)
                                .orElseGet(() -> element.attr("href")))
                        .map(String::strip).filter(e -> !e.isBlank())
                        .map(e -> (urlPrefix + e))
                        .orElse("");
        
        final Function<Element, String> nameExtractor = (Element element) ->
                Optional.ofNullable(element)
                        .map(Element::text)
                        .map(FilenameUtil::cleanTitle)
                        .map(String::strip).filter(e -> !e.isBlank())
                        .filter(e -> useOriginalTitles)
                        .orElseGet(() -> Optional.ofNullable(element)
                                .map(linkExtractor)
                                .map(Shortcut::getShortcutId)
                                .orElse(""));
        
        final Function<Element, Map.Entry<String, String>> extractor = (Element element) ->
                Optional.ofNullable(element)
                        .map(e -> Map.entry(
                                nameExtractor.apply(e),
                                linkExtractor.apply(e)))
                        .filter(e -> !e.getKey().isBlank())
                        .filter(e -> !e.getValue().isBlank())
                        .orElse(null);
        
        return Optional.ofNullable(html).map(Filesystem::readFileToString).map(Jsoup::parse)
                .map(e -> e.select(linkLocator))
                .stream().flatMap(Collection::stream)
                .map(extractor)
                .filter(Objects::nonNull)
                .collect(MapCollectors.toLinkedHashMap());
    }
    
    protected static Map<String, String> parseHtmlLinkMap(File html, String linkLocator, String urlPrefix) {
        return parseHtmlLinkMap(html, linkLocator, urlPrefix, false);
    }
    
}
