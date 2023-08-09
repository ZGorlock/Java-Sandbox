/*
 * File:    BooksByLanguagePage.java
 * Package: main.scraper.eBookFoundation.page
 * Author:  Zachary Gill
 */

package main.scraper.eBookFoundation.page;

import java.io.File;
import java.util.List;

public class BooksByLanguagePage extends EBookFoundationPage {
    
    //Constructors
    
    public BooksByLanguagePage(List<String> pageContent) {
        super(pageContent);
    }
    
    public BooksByLanguagePage(String pageData) {
        super(pageData);
    }
    
    public BooksByLanguagePage(File pageFile) {
        super(pageFile);
    }
    
}
