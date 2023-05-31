/*
 * File:    DataScraper.java
 * Package: main.data.scraper
 * Author:  Zachary Gill
 */

package main.data.scraper;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import commons.access.Filesystem;
import commons.lambda.stream.collector.MapCollectors;
import commons.lambda.stream.mapper.Mappers;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import main.data.cache.LinkExtractor;

public abstract class DataScraper<T> {
    
    //Constants
    
    public static final boolean USE_DELAY = true;
    
    public static final Pattern URL_PATTERN = Pattern.compile("(?<url>(?<host>\\w+://[^/]+/)" + "(?<location>(?<path>(?:[\\w\\-]*(?:/|$))*)(?<name>[\\w\\-]+\\.\\w+|)))");
    
    public static final Pattern URL_EXTRACTOR_PATTERN = Pattern.compile("(?<=\")" + URL_PATTERN.pattern() + "(?=\")");
    
    
    //Methods
    
    public Map<String, File> fetchAll() {
        return tryFetchAll()
                .orElseGet(ErrorResponse.invoke(Collections.emptyMap(), "Failed to fetch all " + getTypeName(true)));
    }
    
    public Optional<Map<String, File>> tryFetchAll() {
        return tryEnumerateUrls()
                .flatMap(this::tryFetch);
    }
    
    public List<String> enumerateUrls() {
        return tryEnumerateUrls()
                .orElseGet(ErrorResponse.invoke(Collections.emptyList(), "Failed to enumerate urls"));
    }
    
    public Optional<List<String>> tryEnumerateUrls() {
        return LinkExtractor.extractLinks(getUrlExtractorPattern(), getUrlRoot());
    }
    
    public Map<String, File> fetch(List<String> urlList) {
        return tryFetch(urlList)
                .orElseGet(ErrorResponse.invoke("Failed to fetch " + getTypeName(true)));
    }
    
    public Optional<Map<String, File>> tryFetch(List<String> urlList) {
        return Optional.ofNullable(urlList)
                .filter(dataUrlList -> !ListUtility.isNullOrEmpty(dataUrlList))
                .stream().flatMap(Collection::stream)
                .map(Mappers.forEach(e -> Optional.ofNullable(e)
                        .map(url -> Optional.ofNullable(urlList).map(list -> (" " + (list.indexOf(url) + 1) + " / " + list.size() + " ")).orElse(""))
                        .map(separator -> StringUtility.pad(separator, 80, '-')).map(separator -> (System.lineSeparator() + separator))
                        .ifPresent(System.out::println)))
                .map(url -> tryFetch(url)
                        .map(data -> Map.entry(url, data))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(MapCollectors.toLinkedHashMap(), Optional::of));
    }
    
    public File fetch(String url) {
        return tryFetch(url)
                .orElseGet(ErrorResponse.invoke("Failed to fetch " + getTypeName() + ": " + url));
    }
    
    public Optional<File> tryFetch(String url) {
        return Optional.ofNullable(url)
                .map(x -> Mappers.perform(x, e -> System.out.println("Fetching " + getTypeName() + ": " + x)))
                .map(this::tryFetchCached)
                .filter(Optional::isPresent)
                .orElseGet(() -> tryCreateCache(url));
    }
    
    public File fetchCached(String url) {
        return tryFetchCached(url)
                .orElseGet(ErrorResponse.invoke("Failed to fetch cached " + getTypeName() + ": " + url));
    }
    
    public Optional<File> tryFetchCached(String url) {
        return Optional.of(url)
                .flatMap(this::getLocalFile)
                .map(x -> Mappers.perform(x, e -> System.out.println("Attempting to load cached " + getTypeName() + "...")))
                .filter(this::localFilePresent)
                .map(x -> Mappers.perform(x, e -> System.out.println("Loaded cached " + getTypeName() + ": " + x)));
    }
    
    public File createCache(String url, boolean overwrite) {
        return tryCreateCache(url, overwrite)
                .orElseGet(ErrorResponse.invoke("Failed to cache " + getTypeName() + ": " + url));
    }
    
    public File createCache(String url) {
        return createCache(url, false);
    }
    
    public Optional<File> tryCreateCache(String url, boolean overwrite) {
        return Optional.of(url)
                .map(x -> Mappers.perform(x, e -> System.out.println("Attempting to download remote " + getTypeName() + "...")))
                .map(this::scrapeData)
                .flatMap(data -> getLocalFile(url)
                        .filter(localFile -> (overwrite || localFileNotPresent(localFile)) && Filesystem.createDirectory(localFile.getParentFile()))
                        .map(x -> Mappers.perform(x, e -> System.out.println("Saving cached " + getTypeName() + ": " + x)))
                        .filter(localFile -> saveData(localFile, data)))
                .map(Mappers.forEach(e -> delay(getDelayRms())));
    }
    
    public Optional<File> tryCreateCache(String url) {
        return tryCreateCache(url, false);
    }
    
    public T scrapeData(String url) {
        return tryScrapeData(url)
                .orElseGet(ErrorResponse.invoke("Failed to scrape " + getTypeName() + ": " + url));
    }
    
    public Optional<T> tryScrapeData(String url) {
        return Optional.of(url)
                .map(x -> Mappers.perform(x, e -> System.out.println("Scraping " + getTypeName() + ": " + x)))
                .map(this::downloadData);
    }
    
    protected T downloadData(String url) {
        return tryDownloadData(url)
                .orElseGet(ErrorResponse.invoke("Failed to download " + getTypeName() + " from: " + url));
    }
    
    protected abstract Optional<T> tryDownloadData(String url);
    
    protected boolean saveData(File file, T data) {
        return trySaveData(file, data)
                .orElseGet(ErrorResponse.invoke(false, "Failed to save " + getTypeName() + ": " +
                        Optional.ofNullable(file).map(File::getAbsolutePath).orElse(null)));
    }
    
    protected abstract Optional<Boolean> trySaveData(File file, T data);
    
    public Optional<File> getLocalFile(String sourceUrl) {
        return Optional.ofNullable(sourceUrl)
                .map(getUrlPattern()::matcher).filter(Matcher::matches)
                .flatMap(urlMatcher -> getLocalFile(
                        Optional.ofNullable(urlMatcher.group("path")).filter(e -> !e.isBlank()).orElseGet(this::getCategory),
                        Optional.ofNullable(urlMatcher.group("name")).filter(e -> !e.isBlank()).orElse(null)));
    }
    
    public Optional<File> getLocalFile(String sourcePath, String sourceName) {
        return Optional.ofNullable(sourceName)
                .flatMap(this::getLocalFileName)
                .flatMap(localName -> locateLocalFile(sourcePath, localName));
    }
    
    public Optional<String> getLocalFileName(String sourceName) {
        return Optional.ofNullable(sourceName)
                .map(name -> name.replaceAll("\\.[^.]+$", ""))
                .map(nameKey -> Optional.of(nameKey)
                        .flatMap(this::formatLocalFileName)
                        .orElse(nameKey))
                .map(nameKey -> (nameKey + "." + getContentType()));
    }
    
    protected abstract Optional<String> formatLocalFileName(String nameKey);
    
    protected abstract Optional<File> locateLocalFile(String localPath, String localName);
    
    protected abstract boolean localFilePresent(File localFile);
    
    protected abstract boolean localFileNotPresent(File localFile);
    
    
    //Getters
    
    protected String getTypeName(boolean plural) {
        return "data";
    }
    
    protected final String getTypeName() {
        return getTypeName(false);
    }
    
    protected abstract String getCategory();
    
    protected abstract String getUrlRoot();
    
    protected abstract Pattern getUrlPattern();
    
    protected abstract Pattern getUrlExtractorPattern();
    
    protected abstract String getContentType();
    
    protected abstract int getDelayRms();
    
    
    //Static Methods
    
    public static void delay(int delayMs) {
        if (!USE_DELAY) {
            return;
        }
        
        final int delay = (int) (Math.random() * (delayMs / 10)) + delayMs;
        System.out.println("Sleeping " + delay + "ms...");
        
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {
        }
    }
    
}
