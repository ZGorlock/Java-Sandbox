/*
 * File:    LinkExtractor.java
 * Package: main.data.cache
 * Author:  Zachary Gill
 */

package main.data.cache;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import commons.access.Filesystem;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;

public final class LinkExtractor {
    
    //Static Methods
    
    public static Optional<List<String>> extractLinks(Pattern linkExtractorPattern, String rootUrl) {
        return DataCache.getAllPageFiles()
                .flatMap(pageList -> extractPageLinks(pageList, linkExtractorPattern))
                .flatMap(urlList -> formatExtractedLinks(urlList, rootUrl));
    }
    
    public static Optional<List<String>> extractLinks(Pattern linkExtractorPattern) {
        return extractLinks(linkExtractorPattern, null);
    }
    
    public static Optional<List<String>> extractPageLinks(List<File> pages, Pattern linkExtractorPattern) {
        return Optional.ofNullable(pages)
                .filter(pageList -> !ListUtility.isNullOrEmpty(pageList))
                .stream().flatMap(Collection::stream)
                .map(page -> extractPageLinks(page, linkExtractorPattern))
                .filter(Optional::isPresent).map(Optional::get)
                .flatMap(Collection::stream).distinct()
                .collect(Collectors.collectingAndThen(Collectors.toList(), Optional::of));
    }
    
    public static Optional<List<String>> extractPageLinks(File page, Pattern linkExtractorPattern) {
        return Optional.ofNullable(page)
                .map(Filesystem::readFileToString)
                .map(linkExtractorPattern::matcher)
                .map(Matcher::results)
                .map(results -> results
                        .map(MatchResult::group)
                        .collect(Collectors.toList()));
    }
    
    public static Optional<List<String>> formatExtractedLinks(List<String> urls, String rootUrl) {
        return Optional.ofNullable(urls)
                .map(urlList -> urlList.stream()
                        .filter(url -> !StringUtility.isNullOrBlank(url))
                        .map(url -> url.replaceAll("^(?:.+://[^/]+)?/", Optional.ofNullable(rootUrl).orElse("$1/")))
                        .distinct().collect(Collectors.toList()));
    }
    
}
