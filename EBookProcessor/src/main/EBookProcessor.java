/*
 * File:    EBookProcessor.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.access.Filesystem;
import main.ebook.EbookMetadata;
import main.ebook.EbookUtils;
import main.util.FilenameUtil;

public class EBookProcessor {
    
    //Constants
    
    private static final File SOURCE_DIR = new File("C:\\Users\\Zack\\Desktop\\New folder");
    
    private static final File WORK_DIR = new File(SOURCE_DIR, "work");
    
    private static final File OUTPUT_DIR = new File(SOURCE_DIR, "out");
    
    private static final String EPUB_FILE = "epub";
    
    private static final String MOBI_FILE = "mobi";
    
    private static final Pattern RAW_EBOOK_TEXT_TITLE_PATTERN = Pattern.compile("^pg\\d+\\.\\w+$");
    
    private static final Pattern RAW_EBOOK_IMGS_TITLE_PATTERN = Pattern.compile("^pg\\d+-images\\.\\w+$");
    
    
    //Main Method
    
    public static void main(String[] args) {
        countEbooks();
        
        if (Filesystem.directoryIsEmpty(WORK_DIR)) {
            filterEbooks();
        }
        
        if (!Filesystem.directoryIsEmpty(WORK_DIR)) {
            processEbooks();
        }
    }
    
    private static void filterEbooks() {
//        List<File> ebookFiles = listEbooks(SOURCE_DIR, false, false);
//        List<File> ebookFiles = listEbooks(SOURCE_DIR, true, false);
//        List<File> ebookFiles = listEbooks(SOURCE_DIR, false, true);
//        List<File> ebookFiles = listEbooks(SOURCE_DIR, true, true);
        List<File> ebookFiles = listEbooks(SOURCE_DIR);
        
        Map<String, List<File>> ebooksMap = ebookFiles.stream()
                .collect(Collectors.groupingBy(e -> e.getParentFile().getName()));
        
        List<File> ebooksTodo = ebooksMap.values().stream()
                .map(EBookProcessor::pickPreferred)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        ebooksTodo.forEach(e -> Filesystem.copyFile(e, new File(WORK_DIR, e.getName())));
    }
    
    private static void processEbooks() {
        List<File> ebooks = Filesystem.getFiles(WORK_DIR);
        
        Map<File, EbookMetadata> meta = new HashMap<>();

//        int limit = 100;
        for (File ebook : ebooks) {
//            if (limit-- <= 0) {
//                break;
//            }
            
            EbookMetadata metadata = EbookUtils.getMetadata(ebook);
            meta.put(ebook, metadata);
        }
        
        meta.forEach((ebook, metadata) -> {
            File shelved = shelveEbook(ebook, metadata);
            if (!shelved.exists()) {
                Filesystem.copy(ebook, shelved);
            }
        });
        
        int g = 5;
    }
    
    private static File shelveEbook(File ebook, EbookMetadata metadata) {
        String title = Optional.ofNullable(metadata).map(EbookMetadata::getTitle).map(FilenameUtil::normalizeFilename).orElse("Unknown");
        String author = Optional.ofNullable(metadata).map(EbookMetadata::getAuthor).map(FilenameUtil::normalizeFilename).orElse("Unknown");
        String language = Optional.ofNullable(metadata).map(EbookMetadata::getLanguage).map(FilenameUtil::normalizeFilename).orElse("Unknown");
        
        String path = Filesystem.generatePath(true, OUTPUT_DIR.getAbsolutePath(), language, author);
        File shelf = new File(path);
        
        return new File(shelf, (title + '.' + Filesystem.getFileType(ebook)));
    }
    
    private static void countEbooks() {
        List<File> epubNoImg = listEbooks(SOURCE_DIR, false, false);
        List<File> epubImg = listEbooks(SOURCE_DIR, true, false);
        List<File> mobiNoImg = listEbooks(SOURCE_DIR, false, true);
        List<File> mobiImg = listEbooks(SOURCE_DIR, true, true);
        
        System.out.println("EPUB + NO_IMAGES: " + epubNoImg.size());
        System.out.println("EPUB +    IMAGES: " + epubImg.size());
        System.out.println("MOBI + NO_IMAGES: " + mobiNoImg.size());
        System.out.println("MOBI +    IMAGES: " + mobiImg.size());
    }
    
    private static List<File> listEbooks(File ebookDir, FileFilter filter) {
        return Filesystem.getFilesRecursively(ebookDir, filter);
    }
    
    private static List<File> listEbooks(File ebookDir) {
        return listEbooks(ebookDir, f -> ((f.getName().endsWith(MOBI_FILE) || f.getName().endsWith(EPUB_FILE)) &&
                (RAW_EBOOK_IMGS_TITLE_PATTERN.matcher(f.getName()).matches() || RAW_EBOOK_TEXT_TITLE_PATTERN.matcher(f.getName()).matches())));
    }
    
    private static List<File> listEbooks(File ebookDir, boolean images, boolean mobi) {
        return listEbooks(ebookDir, f -> (f.getName().endsWith(mobi ? MOBI_FILE : EPUB_FILE) &&
                (images ? RAW_EBOOK_IMGS_TITLE_PATTERN : RAW_EBOOK_TEXT_TITLE_PATTERN).matcher(f.getName()).matches()));
    }
    
    private static File pickPreferred(List<File> ebookFiles) {
        return Stream.of(("-images." + EPUB_FILE), ("." + EPUB_FILE), ("-images." + MOBI_FILE), ("." + MOBI_FILE))
                .map(type -> ebookFiles.stream().filter(e -> e.getName().endsWith(type)).findFirst())
                .filter(Optional::isPresent).map(Optional::get)
                .findFirst().orElse(null);
    }
    
}
