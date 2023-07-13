/*
 * File:    EbookMetadata.java
 * Package: main.ebook
 * Author:  Zachary Gill
 */

package main.ebook;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EbookMetadata {
    
    //Constants
    
    public static List<String> KNOWN_META_TAGS = Arrays.asList(
            "Title", "Author(s)",
            "Languages", "Published", "Rights",
            "Identifiers", "Tags",
            "_Raw", "_Source"
    );
    
    
    //Fields
    
    private Map<String, String> meta;
    
    private String raw;
    
    private File source;
    
    private String title;
    
    private String author;
    
    private String language;
    
    private String published;
    
    private String copyright;
    
    private String identifier;
    
    private List<String> tags;
    
    
    //Constructors
    
    public EbookMetadata(Map<String, String> metadataMap) {
        setMeta(metadataMap);
        
        Optional.ofNullable(getMeta()).map(e -> e.get("_Raw")).ifPresent(this::setRaw);
        Optional.ofNullable(getMeta()).map(e -> e.get("_Source")).map(File::new).ifPresent(this::setSource);
        
        Optional.ofNullable(getMeta()).map(e -> e.get("Title")).ifPresent(this::setTitle);
        Optional.ofNullable(getMeta()).map(e -> e.get("Author(s)")).map(e -> e.replaceAll("\\s*\\[[^]]+]", "")).ifPresent(this::setAuthor);
        Optional.ofNullable(getMeta()).map(e -> e.get("Languages")).ifPresent(this::setLanguage);
        
        Optional.ofNullable(getMeta()).map(e -> e.get("Published")).map(e -> e.replaceAll("T.+$", "")).ifPresent(this::setPublished);
        
        Optional.ofNullable(getMeta()).map(e -> e.get("Rights")).ifPresent(this::setCopyright);
        Optional.ofNullable(getMeta()).map(e -> e.get("Identifiers")).ifPresent(this::setIdentifier);
        
        Optional.ofNullable(getMeta()).map(e -> e.get("Tags")).map(e -> e.split("\\s*,\\s*")).map(Arrays::asList).ifPresent(this::setTags);
    }
    
    
    //Getters
    
    public Map<String, String> getMeta() {
        return meta;
    }
    
    public String getRaw() {
        return raw;
    }
    
    public File getSource() {
        return source;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public String getPublished() {
        return published;
    }
    
    public String getCopyright() {
        return copyright;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    
    //Setters
    
    public void setMeta(Map<String, String> meta) {
        this.meta = meta;
    }
    
    public void setRaw(String raw) {
        this.raw = raw;
    }
    
    public void setSource(File source) {
        this.source = source;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public void setPublished(String published) {
        this.published = published;
    }
    
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
}
