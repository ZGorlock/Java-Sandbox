/*
 * File:    FirefoxAddon.java
 * Package: main.model.site.firefox
 * Author:  Zachary Gill
 */

package main.model.site.firefox;

import java.util.Optional;

import commons.object.string.StringUtility;
import main.model.template.BrowserAddon;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.select.Elements;

public class FirefoxAddon extends BrowserAddon {
    
    //Constants
    
    public static final String BASE_URL = "https://addons.mozilla.org/en-US/firefox";
    
    public static final String ADDON_URL_KEY = "addon";
    
    
    //Constructors
    
    public FirefoxAddon(String urlOrId) {
        super(urlOrId, BASE_URL, ADDON_URL_KEY);
    }
    
    public FirefoxAddon() {
        super();
    }
    
    
    //Methods
    
    @Override
    protected void processPage() {
        super.processPage();
        
        this.name = page.getTextByClass("AddonTitle");
        
        this.downloadUrl = Optional.ofNullable(page.document)
                .map(e -> e.getElementsByAttributeValueContaining("href", "/firefox/downloads/"))
                .map(Elements::first)
                .map(e -> e.attr("href"))
                .filter(e -> !(this.version = StringUtility.lShear(FilenameUtils.getBaseName(e), (id.length() + 1))).isBlank())
                .orElse(null);
    }
    
    @Override
    protected String getAddonUrlKey() {
        return ADDON_URL_KEY;
    }
    
}
