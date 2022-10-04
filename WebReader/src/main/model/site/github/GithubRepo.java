/*
 * File:    GithubRepo.java
 * Package: main.model.site.github
 * Author:  Zachary Gill
 */

package main.model.site.github;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.object.collection.ListUtility;
import main.model.BaseModel;

public class GithubRepo extends BaseModel {
    
    //Constants
    
    public static final String BASE_URL = "https://github.com";
    
    
    //Fields
    
    public String name;
    
    public String author;
    
    public String description;
    
    public String link;
    
    public String lastUpdated;
    
    public String license;
    
    public String stars;
    
    public String watchers;
    
    public String forks;
    
    public String users;
    
    public String releases;
    
    
    //Constructors
    
    public GithubRepo(String author, String repo) {
        super(buildUrl(author, repo));
    }
    
    public GithubRepo(String url) {
        super(cleanUrl(url));
    }
    
    public GithubRepo() {
        super();
    }
    
    
    //Methods
    
    @Override
    protected void processPage() {
        super.processPage();
        
        Optional.of(getUrlPattern().matcher(url)).filter(Matcher::matches)
                .ifPresent(e -> {
                    this.author = e.group("author");
                    this.name = e.group("name");
                });
        
        this.description = page.getTextByClass("f4 my-3");
        this.link = page.getTextByAttributeValue("role", "link");
        this.lastUpdated = page.getTextByTag("relative-time");
        this.license = page.getTextByAttributeValue("data-analytics-event", "{\"category\":\"Repository Overview\",\"action\":\"click\",\"label\":\"location:sidebar;file:license\"}");
        this.stars = page.getTextByAttributeValue("href", String.join("/", "", author, name, "stargazers"));
        this.watchers = page.getTextByAttributeValue("href", String.join("/", "", author, name, "watchers"));
        this.forks = page.getTextByAttributeValue("href", String.join("/", "", author, name, "network", "members"));
        this.users = page.getTextByAttributeValue("href", String.join("/", "", author, name, "network", "dependents"));
        this.releases = page.getTextByClass("Link--primary no-underline", "Counter");
    }
    
    @Override
    protected List<List<Map.Entry<String, Optional<String>>>> getDisplayFields() {
        return ListUtility.addAndGet(super.getDisplayFields(),
                List.of(
                        Map.entry("Name", Optional.ofNullable(name)),
                        Map.entry("Author", Optional.ofNullable(author)),
                        Map.entry("Link", Optional.ofNullable(link)),
                        Map.entry("License", Optional.ofNullable(license)),
                        Map.entry("Releases", Optional.ofNullable(releases)),
                        Map.entry("Last Updated", Optional.ofNullable(lastUpdated)),
                        Map.entry("Stars", Optional.ofNullable(stars)),
                        Map.entry("Watchers", Optional.ofNullable(watchers)),
                        Map.entry("Forks", Optional.ofNullable(forks)),
                        Map.entry("Users", Optional.ofNullable(users)),
                        Map.entry("Description", Optional.ofNullable(description))
                ));
    }
    
    public Pattern getUrlPattern() {
        return Pattern.compile("^" +
                Pattern.quote(BASE_URL) +
                "/(?<author>[^/?#]+)" +
                "/(?<name>[^/?#]+)" +
                ".*$");
    }
    
    public GithubLatestRelease getLatestRelease() {
        return new GithubLatestRelease(url);
    }
    
    //Static Methods
    
    public static String buildUrl(String author, String repo) {
        return String.join("/", BASE_URL, author, repo);
    }
    
    public static String cleanUrl(String url) {
        return url.replaceAll("^((?:[^/]*/*){4}).*$", "$1");
    }
    
}
