/*
 * File:    GithubLatestRelease.java
 * Package: main.model.site.github
 * Author:  Zachary Gill
 */

package main.model.site.github;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import commons.lambda.stream.collector.MapCollectors;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import main.model.BaseModel;
import org.jsoup.nodes.Element;

public class GithubLatestRelease extends BaseModel {
    
    //Constants
    
    public static final String URL_EXTENSION = "releases";
    
    public static final String BASE_URL = String.join("/", GithubRepo.BASE_URL, URL_EXTENSION);
    
    
    //Fields
    
    public String version;
    
    public String releaseDate;
    
    public Map<String, String> assets;
    
    public String executable;
    
    public String winExecutable;
    
    
    //Constructors
    
    public GithubLatestRelease(String author, String repo) {
        super(buildUrl(author, repo));
    }
    
    public GithubLatestRelease(String url) {
        super(cleanUrl(url));
    }
    
    public GithubLatestRelease() {
        super();
    }
    
    
    //Methods
    
    @Override
    protected void processPage() {
        super.processPage();
        
        this.version = Optional.ofNullable(page.getTextByClass("Box-body", "Link--primary"))
                .map(e -> e.replaceAll("\\s-\\s.*", "")).orElse(null);
        this.releaseDate = page.getTextByTag("relative-time");
        
        this.assets = Optional.ofNullable(page.getElementByClass("Box-footer", "Box Box--condensed mt-3"))
                .map(e -> e.element).map(e -> e.getElementsByClass("Truncate"))
                .map(e -> e.stream().collect(MapCollectors.toLinkedHashMap(
                        Element::text, e2 -> (GithubRepo.BASE_URL + e2.attr("href")))))
                .orElse(null);
        
        Optional.ofNullable(assets)
                .ifPresent(e -> {
                    this.winExecutable = e.entrySet().stream()
                            .filter(e2 -> e2.getKey().endsWith(".exe"))
                            .sorted(Comparator.comparingInt(o -> o.getKey().length())).map(Map.Entry::getValue)
                            .limit(1).findFirst().orElse(null);
                    this.executable = e.entrySet().stream()
                            .filter(e2 -> e2.getKey().startsWith(Optional.ofNullable(winExecutable)
                                    .map(e3 -> e3.replaceAll(".*/([^/.]+).exe$", "$1")).orElse("")))
                            .sorted(Comparator.comparingInt(o -> o.getKey().length())).map(Map.Entry::getValue)
                            .limit(1).findFirst().orElse(null);
                });
    }
    
    @Override
    protected List<List<Map.Entry<String, Optional<String>>>> getDisplayFields() {
        return ListUtility.addAndGet(super.getDisplayFields(),
                List.of(
                        Map.entry("Latest Version", Optional.ofNullable(version)),
                        Map.entry("Release Date", Optional.ofNullable(releaseDate)),
                        Map.entry("Windows Executable", Optional.ofNullable(winExecutable)),
                        Map.entry("Executable", Optional.ofNullable(executable)),
                        Map.entry("Assets", Optional.ofNullable(assets)
                                .map(e -> System.lineSeparator() + e.entrySet().stream()
                                        .map(e2 -> String.join("",
                                                StringUtility.spaces(4),
                                                e2.getKey() + ": ",
                                                StringUtility.spaces(e2.getKey().isBlank() ? 0 : (e.keySet().stream().mapToInt(String::length).max().orElse(0) - e2.getKey().length())),
                                                e2.getValue()))
                                        .collect(Collectors.joining(System.lineSeparator()))))
                ));
    }
    
    
    //Static Methods
    
    public static String buildUrl(String author, String repo) {
        return String.join("/", GithubRepo.BASE_URL, author, repo, URL_EXTENSION);
    }
    
    public static String cleanUrl(String url) {
        return GithubRepo.cleanUrl(url) + URL_EXTENSION;
    }
    
}
