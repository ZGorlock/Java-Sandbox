/*
 * File:    BrowserAddon.java
 * Package: main.model.template
 * Author:  Zachary Gill
 */

package main.model.template;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

import commons.object.collection.ListUtility;
import main.model.BaseModel;

public abstract class BrowserAddon extends BaseModel {
    
    //Fields
    
    public String id;
    
    public String name;
    
    public String version;
    
    public String downloadUrl;
    
    
    //Constructors
    
    protected BrowserAddon(String urlOrId, String baseUrl, String itemUrlKey) {
        super(buildUrl(baseUrl, itemUrlKey, urlOrId));
    }
    
    protected BrowserAddon() {
        super();
    }
    
    
    //Methods
    
    @Override
    protected void processPage() {
        super.processPage();
        
        this.id = Optional.of(getUrlPattern(getAddonUrlKey()).matcher(url)).filter(Matcher::matches)
                .map(e -> e.group("id")).orElse(null);
    }
    
    @Override
    protected List<List<Map.Entry<String, Optional<String>>>> getDisplayFields() {
        return ListUtility.addAndGet(super.getDisplayFields(),
                List.of(
                        Map.entry("Name", Optional.ofNullable(name)),
                        Map.entry("ID", Optional.ofNullable(id)),
                        Map.entry("Version", Optional.ofNullable(version)),
                        Map.entry("Download Url", Optional.ofNullable(downloadUrl))
                ));
    }
    
    protected abstract String getAddonUrlKey();
    
    
    //Static Methods
    
    public static String buildUrl(String baseUrl, String addonIdKey, String urlOrId) {
        return urlOrId.startsWith(baseUrl) ? urlOrId :
                String.join("/", baseUrl, addonIdKey, urlOrId);
    }
    
}
