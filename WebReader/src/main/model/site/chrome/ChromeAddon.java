/*
 * File:    ChromeAddon.java
 * Package: main.model.site.chrome
 * Author:  Zachary Gill
 */

package main.model.site.chrome;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import commons.access.Internet;
import commons.object.collection.ListUtility;
import main.model.template.BrowserAddon;
import org.jsoup.select.Elements;

public class ChromeAddon extends BrowserAddon {
    
    //Constants
    
    public static final String BASE_URL = "https://chrome.google.com/webstore";
    
    public static final String ADDON_URL_KEY = "detail";
    
    public static final String CHROME_VERSION = "104.0.5112.79";
    
    
    //Fields
    
    public String blobUrl;
    
    
    //Constructors
    
    public ChromeAddon(String urlOrId) {
        super(urlOrId, BASE_URL, ADDON_URL_KEY);
    }
    
    public ChromeAddon() {
        super();
    }
    
    
    //Methods
    
    @Override
    protected void processPage() {
        super.processPage();
        
        this.name = page.getTextByClass("e-f-w");
        
        this.blobUrl = "https://clients2.google.com/service/update2/crx?" +
                "prodversion=" + CHROME_VERSION + "&acceptformat=crx2,crx3&" +
                "x=id%3D" + id + "%26uc";
        
        this.downloadUrl = Optional.ofNullable(Internet.getHtml(blobUrl))
                .map(e -> e.getElementsByTag("updatecheck"))
                .map(Elements::first)
                .filter(e -> !(version = e.attr("version")).isBlank())
                .map(e -> e.attr("codebase"))
                .orElse(null);
    }
    
    @Override
    protected List<List<Map.Entry<String, Optional<String>>>> getDisplayFields() {
        return ListUtility.addAndGet(super.getDisplayFields(),
                List.of(
                        Map.entry("Blob Url", Optional.ofNullable(blobUrl))
                ));
    }
    
    @Override
    protected String getAddonUrlKey() {
        return ADDON_URL_KEY;
    }
    
}
