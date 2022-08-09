/*
 * File:    Page.java
 * Package: main.entity.base
 * Author:  Zachary Gill
 */

package main.entity.base;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;

public class Page {
    
    //Fields
    
    public String title;
    
    public final List<Line> lines = new ArrayList<>();
    
    public final List<Column> columns = new ArrayList<>();
    
    protected Book book;
    
    protected int pageIndex;
    
    private Sheet sheet;
    
    
    //Constructors
    
    protected Page(Page page) {
        this.title = page.title;
        this.lines.addAll(page.lines);
        this.columns.addAll(page.columns);
        this.book = page.book;
        this.pageIndex = page.pageIndex;
        this.sheet = page.sheet;
        
        parse();
    }
    
    protected Page() {
    }
    
    
    //Getters
    
    protected int lineCount() {
        return lines.size();
    }
    
    protected int columnCount() {
        return columns.size();
    }
    
    
    //Methods
    
    protected void load(Sheet sheet) {
        this.sheet = sheet;
        
        this.title = sheet.getSheetName();
        sheet.rowIterator().forEachRemaining(row -> {
            Line line = new Line();
            line.load(row);
            addLine(line);
        });
    }
    
    protected void parse() {
    }
    
    protected void addLine(Line line) {
        line.page = this;
        line.lineIndex = lines.size();
        lines.add(line);
        
        if ((line.lineIndex == 0) && columns.isEmpty()) {
            line.data.forEach(this::addHeader);
        }
        for (int cell = 0; cell < line.data.size(); cell++) {
            if (columns.size() <= cell) {
                addHeader("", line.lineIndex);
            }
            columns.get(cell).data.add(line.data.get(cell));
        }
    }
    
    protected void addHeader(String header) {
        final Column column = new Column();
        column.header = header;
        column.columnIndex = columns.size();
        columns.add(column);
    }
    
    protected void addHeader(String header, int lineIndex) {
        addHeader(header);
        
        Column column = columns.get(columns.size() - 1);
        for (int i = 0; i < lineIndex; i++) {
            column.data.add(null);
        }
    }
    
}
