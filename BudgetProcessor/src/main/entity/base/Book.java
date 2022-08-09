/*
 * File:    Book.java
 * Package: main.entity.base
 * Author:  Zachary Gill
 */

package main.entity.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public abstract class Book {
    
    //Fields
    
    public String title;
    
    public final List<Page> pages = new ArrayList<>();
    
    protected Workbook workbook;
    
    
    //Constructors
    
    protected Book(Book book) {
        this.title = book.title;
        this.pages.addAll(book.pages);
        this.workbook = book.workbook;
        
        parse();
    }
    
    protected Book(File workbookFile) {
        this.title = workbookFile.getName().replaceAll("\\.[^.]+$", "");
        
        try {
            load(workbookFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        parse();
    }
    
    protected Book() {
    }
    
    
    //Getters
    
    protected int pageCount() {
        return pages.size();
    }
    
    
    //Methods
    
    public void load(File workbookFile) throws IOException {
        final FileInputStream workbookStream = new FileInputStream(workbookFile);
        load(new XSSFWorkbook(workbookStream));
    }
    
    private void load(Workbook workbook) {
        this.workbook = workbook;
        
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Page page = new Page();
            page.load(workbook.getSheetAt(i));
            addPage(page);
        }
    }
    
    protected void parse() {
    }
    
    public void save(File workbookFile) throws IOException, FileNotFoundException {
        final FileOutputStream out = new FileOutputStream(workbookFile);
        workbook.write(out);
    }
    
    public void close() throws IOException {
        workbook.close();
    }
    
    protected void addPage(Page page) {
        page.book = this;
        page.pageIndex = pages.size();
        pages.add(page);
    }
    
}
