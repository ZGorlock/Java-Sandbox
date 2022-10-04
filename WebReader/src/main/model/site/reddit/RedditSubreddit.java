/*
 * File:    RedditSubreddit.java
 * Package: main.model.site.reddit
 * Author:  Zachary Gill
 */

package main.model.site.reddit;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

import commons.object.collection.ListUtility;
import main.model.BaseModel;
import org.jsoup.nodes.Node;

public class RedditSubreddit extends BaseModel {
    
    //Constants
    
    public static final String BASE_URL = "https://www.reddit.com";
    
    public static final String SUB_ID_KEY = "r";
    
    
    //Fields
    
    public String subName;
    
    public Boolean isBanned;
    
    public Boolean isPrivate;
    
    
    //Constructors
    
    public RedditSubreddit(String urlOrSubName) {
        super(buildUrl(urlOrSubName));
    }
    
    public RedditSubreddit() {
        super();
    }
    
    
    //Methods
    
    @Override
    protected void processPage() {
        super.processPage();
        
        this.subName = Optional.of(getUrlPattern(SUB_ID_KEY).matcher(url)).filter(Matcher::matches)
                .map(e -> e.group("id")).orElse(null);
        
        Optional.ofNullable(page.document)
                .map(e -> e.getElementById("AppRouter-main-content"))
                .map(Node::toString)
                .map(e -> e.replaceAll("<!--.*?-->", ""))
                .ifPresent(e -> {
                    this.isBanned = e.contains(">r/" + subName + " has been banned from Reddit<");
                    this.isPrivate = e.contains(">r/" + subName + " is a private community<");
                });
    }
    
    @Override
    protected List<List<Map.Entry<String, Optional<String>>>> getDisplayFields() {
        return ListUtility.addAndGet(super.getDisplayFields(),
                List.of(
                        Map.entry("Sub Name", Optional.ofNullable(subName)),
                        Map.entry("Is Banned", Optional.ofNullable(isBanned).map(Object::toString)),
                        Map.entry("Is Private", Optional.ofNullable(isPrivate).map(Object::toString))
                ));
    }
    
    
    //Static Methods
    
    public static String buildUrl(String urlOrSubName) {
        return urlOrSubName.startsWith(BASE_URL) ? urlOrSubName :
                String.join("/", BASE_URL, SUB_ID_KEY, urlOrSubName);
    }
    
}
