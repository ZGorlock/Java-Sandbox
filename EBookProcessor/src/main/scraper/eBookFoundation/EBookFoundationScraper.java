/*
 * File:    EBookFoundationScraper.java
 * Package: main.scraper.eBookFoundation
 * Author:  Zachary Gill
 */

package main.scraper.eBookFoundation;

import java.io.File;
import java.util.Optional;

import commons.access.Filesystem;
import commons.access.Internet;
import commons.access.Project;
import main.scraper.eBookFoundation.page.BooksByLanguagePage;
import main.scraper.eBookFoundation.page.BooksBySubjectPage;
import main.scraper.eBookFoundation.page.EBookFoundationPage;

public class EBookFoundationScraper {
    
    //Constants
    
    private static final File DEST_DIR = new File("E:\\Documents\\eBooks\\Programming");
    
    private static final String SOURCE_REPO = "https://raw.githubusercontent.com/EbookFoundation/free-programming-books/main/books/";
    
    private static final String BOOKS_BY_LANGUAGE = "free-programming-books-langs.md";
    
    private static final String BOOKS_BY_SUBJECT = "free-programming-books-subjects.md";
    
    private static final File BOOKS_BY_LANGUAGE_OUTPUT = new File(DEST_DIR, "Language");
    
    private static final File BOOKS_BY_SUBJECT_OUTPUT = new File(DEST_DIR, "Subjects");
    
    
    //Main Method
    
    public static void main(String[] args) {
//        scrapeBooksByLanguage();
        scrapeBooksBySubject();
    }
    
    
    //Static Methods
    
    private static void scrapeBooksByLanguage() {
        final File pageFile = fetchRawPageData(BOOKS_BY_LANGUAGE);
        final BooksByLanguagePage page = new BooksByLanguagePage(pageFile);
        
        scrapePageEbooks(page, BOOKS_BY_LANGUAGE_OUTPUT);
    }
    
    private static void scrapeBooksBySubject() {
        final File pageData = fetchRawPageData(BOOKS_BY_SUBJECT);
        final BooksBySubjectPage page = new BooksBySubjectPage(pageData);
        
        scrapePageEbooks(page, BOOKS_BY_SUBJECT_OUTPUT);
    }
    
    private static void scrapePageEbooks(EBookFoundationPage page, File outputDir) {
        page.categories.forEach((category, books) -> {
            final File categoryDir = new File(outputDir, category);
            books.entrySet().stream()
                    .filter(e -> e.getValue().toLowerCase().endsWith(".pdf"))
                    .forEach(e -> {
                        final File bookOutput = new File(categoryDir, (e.getKey()
                                .replaceAll("\\s*\\*\\(:card_file_box:\\s*archived\\)\\*\\s*", "")
                                .replaceAll("\\([^)]+\\)$", "")
                                .replaceAll("[\\\\/:*?\"<>|]", "")
                                .strip() + ".pdf"));
                        
                        if (bookOutput.exists() && (bookOutput.length() > 20000)) {
                            return;
                        }
                        
                        System.out.println("Downloading: " + e.getKey());
                        System.out.println("           : " + e.getValue());
                        System.out.println("           : " + bookOutput.getAbsolutePath());
                        System.out.println();
                        
                        final File download = Internet.downloadFile(e.getValue());
                        if ((download != null) && download.exists() && (download.length() > 20000)) {
                            Filesystem.moveFile(download, bookOutput);
                        } else {
                            int g = 5;
                        }
                    });
        });
    }
    
    private static File fetchRawPageData(String page) {
        final String rawPageUrl = SOURCE_REPO + page;
        
        System.out.println("Fetching: " + rawPageUrl);
        
        return Optional.of(rawPageUrl)
                .flatMap(url -> Optional.of(page)
                        .map(fileName -> new File(Project.TMP_DIR, fileName))
                        .map(file -> Optional.ofNullable(Internet.downloadFile(url, file)).orElse(file))
                        .filter(File::exists))
                .orElse(null);
    }
    
}
