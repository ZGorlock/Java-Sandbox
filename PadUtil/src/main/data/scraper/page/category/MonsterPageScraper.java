/*
 * File:    MonsterPageScraper.java
 * Package: main.data.scraper.page.category
 * Author:  Zachary Gill
 */

package main.data.scraper.page.category;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import commons.access.Internet;
import commons.lambda.stream.mapper.Mappers;
import main.data.scraper.ErrorResponse;
import main.data.scraper.page.PageScraper;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MonsterPageScraper extends PageScraper {
    
    //Constants
    
    public static final String PAGE_CATEGORY_MONSTER = "monster";
    
    public static final Pattern URL_PATTERN_MONSTER = Pattern.compile("(?<url>(?<host>" + "/|" + Pattern.quote(URL_ROOT_PAGE) + ")" + "(?<location>(?<path>monster/|card/|)" + "(?<name>\\d+)" + "/?))");
    
    public static final Pattern URL_PATTERN_EXTRACTOR_MONSTER = Pattern.compile("(?<=\")" + URL_PATTERN_MONSTER.pattern() + "(?=\")");
    
    
    //Methods
    
    @Override
    public Optional<List<String>> tryEnumerateUrls() {
        return tryScrapePageCount()
                .stream().flatMapToInt(e -> IntStream.rangeClosed(1, e)).boxed()
                .map(this::getPageUrl)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Optional::of));
    }
    
    @Override
    public Optional<String> getPageUrl(int id) {
        return Optional.of(id)
                .map(idNum -> (getUrlRoot() + idNum));
    }
    
    
    //Getters
    
    @Override
    protected String getPageCategory() {
        return PAGE_CATEGORY_MONSTER;
    }
    
    @Override
    protected Pattern getUrlPattern() {
        return URL_PATTERN_MONSTER;
    }
    
    @Override
    protected Pattern getUrlExtractorPattern() {
        return URL_PATTERN_EXTRACTOR_MONSTER;
    }
    
    
    //Static Methods
    
    public static int scrapePageCount() {
        return tryScrapePageCount()
                .orElseGet(ErrorResponse.invoke(-1, "Failed to scrape page count"));
    }
    
    public static Optional<Integer> tryScrapePageCount() {
        return Optional.of(URL_ROOT_PAGE)
                .map(pageId -> Mappers.perform(pageId, e -> System.out.println("Scraping page count: " + pageId)))
                .map(Internet::getHtml)
                .map(document -> document.getElementsByClass("mx-2 flex-grow-1 text-center"))
                .map(Elements::first).map(Element::text)
                .map(text -> text.replaceAll("(?i)results", "")).map(String::strip)
                .filter(text -> text.matches("^\\d+$"))
                .map(Integer::parseInt);
    }
    
}
