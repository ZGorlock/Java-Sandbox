/*
 * File:    DungeonPageScraper.java
 * Package: main.data.scraper.page.category
 * Author:  Zachary Gill
 */

package main.data.scraper.page.category;

import java.util.regex.Pattern;

import main.data.scraper.page.PageScraper;

public class DungeonPageScraper extends PageScraper {
    
    //Constants
    
    public static final String PAGE_CATEGORY_DUNGEON = "dungeon";
    
    public static final Pattern URL_PATTERN_DUNGEON = Pattern.compile("(?<url>(?<host>" + "/|" + Pattern.quote(URL_ROOT_PAGE) + ")" + "(?<location>(?<path>" + PAGE_CATEGORY_DUNGEON + "/)" + "(?<name>\\d+)" + "/?))");
    
    public static final Pattern URL_PATTERN_EXTRACTOR_DUNGEON = Pattern.compile("(?<=\")" + URL_PATTERN_DUNGEON.pattern() + "(?=\")");
    
    
    //Getters
    
    @Override
    protected String getPageCategory() {
        return PAGE_CATEGORY_DUNGEON;
    }
    
    @Override
    protected Pattern getUrlPattern() {
        return URL_PATTERN_DUNGEON;
    }
    
    @Override
    protected Pattern getUrlExtractorPattern() {
        return URL_PATTERN_EXTRACTOR_DUNGEON;
    }
    
}
