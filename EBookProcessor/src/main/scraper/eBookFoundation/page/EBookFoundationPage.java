/*
 * File:    EBookFoundationPage.java
 * Package: main.scraper.eBookFoundation.page
 * Author:  Zachary Gill
 */

package main.scraper.eBookFoundation.page;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.access.Filesystem;
import commons.object.collection.map.BiMap;
import commons.object.string.StringUtility;

public abstract class EBookFoundationPage {
    
    //Fields
    
    public List<String> pageContent;
    
    public BiMap<String, String> indices;
    
    public Map<String, Map<String, String>> categories;
    
    
    //Constructors
    
    public EBookFoundationPage(List<String> pageContent) {
        this.pageContent = pageContent;
        this.indices = parseIndex();
        this.categories = parseCategories(this.indices);
    }
    
    public EBookFoundationPage(String pageData) {
        this(Optional.ofNullable(pageData)
                .map(StringUtility::splitLines)
                .orElseGet(Collections::emptyList));
    }
    
    public EBookFoundationPage(File pageFile) {
        this(Optional.ofNullable(pageFile)
                .map(Filesystem::readFileToString)
                .orElse(null));
    }
    
    
    //Methods
    
    public BiMap<String, String> parseIndex() {
        final BiMap<String, String> indices = new BiMap<>();
        
        int i = 0;
        for (; i < pageContent.size(); i++) {
            final String line = pageContent.get(i);
            
            if (line.strip().equals("### Index")) {
                i += 2;
                break;
            }
        }
        
        final Pattern indexPattern = Pattern.compile("^\\s*\\*\\s+\\[(?<title>[^]]+)]\\s*\\((?<anchor>#?[^)]+)\\).*$");
        
        String current = null;
        for (; i < pageContent.size(); i++) {
            final String line = pageContent.get(i);
            
            final Matcher indexMatcher = indexPattern.matcher(line);
            if (!indexMatcher.matches()) {
                break;
            }
            
            String title = indexMatcher.group("title")
                    .replaceAll("[\\\\/]", " - ")
                    .replaceAll("^\\d+\\s*-\\s*", "")
                    .replaceAll("\\s+", " ");
            String anchor = indexMatcher.group("anchor");
            
            boolean isSub = line.matches("^\\s+.*$");
            if (isSub) {
                if (current != null) {
                    title = current + "/" + title;
                }
            } else {
                current = title;
            }
            
            if (anchor.startsWith("#")) {
                if (indices.containsValue(anchor.replace("#", ""))) {
                    int g = 4;
                } else {
                    indices.put(title, anchor.replace("#", ""));
                }
            }
        }
        
        return indices;
    }
    
    public Map<String, Map<String, String>> parseCategories(BiMap<String, String> indices) {
        final Map<String, Map<String, String>> categories = new LinkedHashMap<>();
        
        String current = null;
        int i = 0;
        for (; i < pageContent.size(); i++) {
            final String line = pageContent.get(i);
            
            if (line.isBlank()) {
                continue;
            }
            
            if (line.strip().startsWith("###") && !line.strip().equals("### Index")) {
                final String category = line
                        .replaceAll("^\\s*#+\\s*", "")
                        .replaceAll("^\\d+\\s*-\\s*", "")
                        .replaceAll("[\\\\/.()]", "")
                        .replaceAll("<a id=\"[^\"]+\"></a>", "");
                final String anchor = line
                        .replaceAll("^\\s*#+\\s*", "")
                        .replaceAll("^.*<a id=\"([^\"]+)\"></a>.*", "$1")
                        .replaceAll("[\\\\/.()]", "")
                        .replaceAll("\\s", "-").toLowerCase();
                
                String indexCategory = indices.inverseGet(anchor);
                if (indexCategory == null) {
                    indices.put(category, anchor);
                    indexCategory = category;
                }
                
                current = indexCategory;
                categories.put(current, new LinkedHashMap<>());
                continue;
            }
            
            if (current == null) {
                continue;
            }
            
            if (line.strip().startsWith("*")) {
                final Map.Entry<String, String> book = parseBook(line);
                if (book != null) {
                    categories.get(current).put(book.getKey(), book.getValue());
                }
            }
            
        }
        
        return categories;
    }
    
    public Map.Entry<String, String> parseBook(String line) {
        final Pattern bookPattern = Pattern.compile("^\\s*\\*\\s+\\[(?<title>[^]]+(?:\\[[^]]+])*)]\\s*\\((?<link>[^)]+)\\)(?<extra>.*|)$");
        final Matcher bookMatcher = bookPattern.matcher(line);
        if (!bookMatcher.matches()) {
            return null;
        }
        
        final String title = bookMatcher.group("title") + bookMatcher.group("extra");
        final String link = bookMatcher.group("link");
        
        return Map.entry(title, link);
    }
    
}
