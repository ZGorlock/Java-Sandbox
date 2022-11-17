/*
 * File:    Tag.java
 * Package: main.tag
 * Author:  Zachary Gill
 */

package main.tag;

import javax.imageio.metadata.IIOMetadataNode;

public class Tag {
    
    //Fields
    
    protected String keyword;
    
    protected String text;
    
    
    //Constructors
    
    public Tag(String keyword, String text) {
        this.keyword = keyword;
        this.text = text;
    }
    
    public Tag() {
    }
    
    
    //Getters
    
    public String getKeyword() {
        return keyword;
    }
    
    public String getText() {
        return text;
    }
    
    
    //Methods
    
    public IIOMetadataNode node() {
        final IIOMetadataNode textTagEntry = new IIOMetadataNode("tEXtEntry");
        textTagEntry.setAttribute("keyword", keyword);
        textTagEntry.setAttribute("value", text);
        
        final IIOMetadataNode textTag = new IIOMetadataNode("tEXt");
        textTag.appendChild(textTagEntry);
        
        final IIOMetadataNode root = new IIOMetadataNode("javax_imageio_png_1.0");
        root.appendChild(textTag);
        
        return root;
    }
    
}
