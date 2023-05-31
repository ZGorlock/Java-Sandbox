/*
 * File:    PageScraper.java
 * Package: main.data.scraper.page
 * Author:  Zachary Gill
 */

package main.data.scraper.page;

import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.access.Filesystem;
import commons.access.Internet;
import commons.object.string.StringUtility;
import main.data.cache.DataCache;
import main.data.cache.DataMirror;
import main.data.scraper.DataScraper;
import main.data.scraper.ErrorResponse;
import org.jsoup.nodes.Element;

public abstract class PageScraper extends DataScraper<String> {
    
    //Constants
    
    public static final String URL_ROOT_PAGE = DataMirror.URL_BASE;
    
    public static final Pattern URL_PATTERN_PAGE = Pattern.compile("(?<url>(?<host>" + Pattern.quote(URL_ROOT_PAGE) + ")" + "(?<location>(?<path>(?:[\\w\\-]*(?:/|$))*)(?<name>[\\w\\-]+\\.\\w+|)))");
    
    public static final Pattern URL_EXTRACTOR_PATTERN_PAGE = Pattern.compile("(?<=\")" + URL_PATTERN_PAGE.pattern() + "(?=\")");
    
    public static final String CONTENT_TYPE_PAGE = "html";
    
    public static final int DELAY_RMS_PAGE = 1000;
    
    
    //Methods
    
    public File fetch(int id) {
        return tryFetch(id)
                .orElseGet(ErrorResponse.invoke());
    }
    
    public Optional<File> tryFetch(int id) {
        return getPageUrl(id)
                .flatMap(this::tryFetch);
    }
    
    public File fetchCached(int id) {
        return tryFetchCached(id)
                .orElseGet(ErrorResponse.invoke());
    }
    
    public Optional<File> tryFetchCached(int id) {
        return getPageUrl(id)
                .flatMap(this::tryFetchCached);
    }
    
    public File createCache(int id, boolean overwrite) {
        return tryCreateCache(id, overwrite)
                .orElseGet(ErrorResponse.invoke());
    }
    
    public File createCache(int id) {
        return createCache(id, false);
    }
    
    public Optional<File> tryCreateCache(int id, boolean overwrite) {
        return getPageUrl(id)
                .flatMap(url -> tryCreateCache(url, overwrite));
    }
    
    public Optional<File> tryCreateCache(int id) {
        return tryCreateCache(id, false);
    }
    
    public String scrapePageData(int id) {
        return tryScrapePageData(id)
                .orElseGet(ErrorResponse.invoke());
    }
    
    public Optional<String> tryScrapePageData(int id) {
        return getPageUrl(id)
                .flatMap(this::tryScrapeData);
    }
    
    @Override
    protected Optional<String> tryDownloadData(String url) {
        return Optional.of(url)
                .map(Internet::getHtml)
                .map(Element::html);
    }
    
    @Override
    protected Optional<Boolean> trySaveData(File file, String data) {
        return Optional.ofNullable(data)
                .flatMap(fileData -> Optional.ofNullable(file)
                        .map(outputFile -> Filesystem.writeStringToFile(outputFile, fileData)));
    }
    
    public Optional<String> getPageUrl(int id) {
        return Optional.of(id)
                .map(idNum -> (getUrlRoot() + getPageCategory() + '/' + idNum));
    }
    
    public Optional<Integer> getPageId(String url) {
        return Optional.of(url)
                .map(getUrlPattern()::matcher).filter(Matcher::matches)
                .map(monsterUrlMatcher -> monsterUrlMatcher.group("name"))
                .filter(e -> e.matches("^\\d+$")).map(Integer::parseInt);
    }
    
    public Optional<File> getLocalFile(int id) {
        return getLocalFileId(id)
                .flatMap(localName -> getLocalFile(getCategory(), localName));
    }
    
    public Optional<String> getLocalFileId(int id) {
        return Optional.of(id)
                .map(idNum -> StringUtility.padZero(idNum, 5));
    }
    
    @Override
    protected Optional<String> formatLocalFileName(String nameKey) {
        return Optional.of(nameKey)
                .filter(e -> e.matches("^\\d+$")).map(Integer::parseInt)
                .flatMap(this::getLocalFileId);
    }
    
    @Override
    protected final Optional<File> locateLocalFile(String localPath, String localName) {
        return DataCache.getPageFile(localPath, localName);
    }
    
    @Override
    protected boolean localFilePresent(File localFile) {
        return DataCache.localFileExists(localFile);
    }
    
    @Override
    protected boolean localFileNotPresent(File localFile) {
        return DataCache.localFileNotExists(localFile);
    }
    
    
    //Getters
    
    @Override
    protected String getTypeName(boolean plural) {
        return "page" + (plural ? "s" : "");
    }
    
    @Override
    protected final String getCategory() {
        return getPageCategory();
    }
    
    protected abstract String getPageCategory();
    
    @Override
    protected String getUrlRoot() {
        return URL_ROOT_PAGE;
    }
    
    @Override
    protected Pattern getUrlPattern() {
        return URL_PATTERN_PAGE;
    }
    
    @Override
    protected Pattern getUrlExtractorPattern() {
        return URL_EXTRACTOR_PATTERN_PAGE;
    }
    
    @Override
    protected String getContentType() {
        return CONTENT_TYPE_PAGE;
    }
    
    @Override
    protected int getDelayRms() {
        return DELAY_RMS_PAGE;
    }
    
}
