/*
 * File:    ResourceScraper.java
 * Package: main.data.scraper.resource
 * Author:  Zachary Gill
 */

package main.data.scraper.resource;

import java.io.File;
import java.util.Optional;
import java.util.regex.Pattern;

import main.data.cache.DataCache;
import main.data.cache.DataMirror;
import main.data.scraper.DataScraper;

public abstract class ResourceScraper<T> extends DataScraper<T> {
    
    //Constants
    
    public static final String URL_ROOT_RESOURCE = DataMirror.URL_STATIC;
    
    public static final Pattern URL_PATTERN_RESOURCE = Pattern.compile("(?<url>(?<host>" + Pattern.quote(URL_ROOT_RESOURCE) + ")" + "(?<location>(?<path>(?:[\\w\\-]*(?:/|$))*)(?<name>[\\w\\-]+\\.\\w+|)))");
    
    public static final Pattern URL_EXTRACTOR_PATTERN_RESOURCE = Pattern.compile("(?<=\")" + URL_PATTERN_RESOURCE.pattern() + "(?=\")");
    
    public static final int DELAY_RMS_RESOURCE = 250;
    
    
    //Methods
    
    @Override
    protected Optional<String> formatLocalFileName(String nameKey) {
        return Optional.ofNullable(nameKey);
    }
    
    @Override
    protected final Optional<File> locateLocalFile(String localPath, String localName) {
        return DataCache.getResourceFile(localPath, localName);
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
        return "resource" + (plural ? "s" : "");
    }
    
    @Override
    protected final String getCategory() {
        return null;
    }
    
    @Override
    protected String getUrlRoot() {
        return URL_ROOT_RESOURCE;
    }
    
    @Override
    protected Pattern getUrlPattern() {
        return URL_PATTERN_RESOURCE;
    }
    
    @Override
    protected Pattern getUrlExtractorPattern() {
        return URL_EXTRACTOR_PATTERN_RESOURCE;
    }
    
    @Override
    protected int getDelayRms() {
        return DELAY_RMS_RESOURCE;
    }
    
}
