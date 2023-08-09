/*
 * File:    BooksBySubjectPage.java
 * Package: main.scraper.eBookFoundation.page
 * Author:  Zachary Gill
 */

package main.scraper.eBookFoundation.page;

import java.io.File;
import java.util.List;

public class BooksBySubjectPage extends EBookFoundationPage {
    
    //Constructors
    
    public BooksBySubjectPage(List<String> pageContent) {
        super(pageContent);
    }
    
    public BooksBySubjectPage(String pageData) {
        super(pageData);
    }
    
    public BooksBySubjectPage(File pageFile) {
        super(pageFile);
    }
    
}
