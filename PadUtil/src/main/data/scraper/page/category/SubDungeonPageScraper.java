/*
 * File:    SubDungeonPageScraper.java
 * Package: main.data.scraper.page.category
 * Author:  Zachary Gill
 */

package main.data.scraper.page.category;

import java.util.Optional;
import java.util.regex.Pattern;

import commons.object.string.StringUtility;
import main.data.scraper.page.PageScraper;

public class SubDungeonPageScraper extends PageScraper {
    
    //Constants
    
    public static final String PAGE_CATEGORY_SUB_DUNGEON = "sub-dungeon";
    
    public static final Pattern URL_PATTERN_SUB_DUNGEON = Pattern.compile("(?<url>(?<host>" + "/|" + Pattern.quote(URL_ROOT_PAGE) + ")" + "(?<location>(?<path>" + PAGE_CATEGORY_SUB_DUNGEON + "/)" + "(?<name>\\d+)" + "/?))");
    
    public static final Pattern URL_PATTERN_EXTRACTOR_SUB_DUNGEON = Pattern.compile("(?<=\")" + URL_PATTERN_SUB_DUNGEON.pattern() + "(?=\")");
    
    
    //Methods
    
    @Override
    public Optional<String> getLocalFileId(int id) {
        return Optional.of(id)
                .map(mainId -> (mainId / 1000)).flatMap(super::getLocalFileId)
                .map(pageId -> (pageId + "_" + StringUtility.padZero((id % 1000), 3)));
    }
    
    
    //Getters
    
    @Override
    protected String getPageCategory() {
        return PAGE_CATEGORY_SUB_DUNGEON;
    }
    
    @Override
    protected Pattern getUrlPattern() {
        return URL_PATTERN_SUB_DUNGEON;
    }
    
    @Override
    protected Pattern getUrlExtractorPattern() {
        return URL_PATTERN_EXTRACTOR_SUB_DUNGEON;
    }
    
}
