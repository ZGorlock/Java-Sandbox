/*
 * File:    Line.java
 * Package: main.entity.base
 * Author:  Zachary Gill
 */

package main.entity.base;

import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;

public class Line extends Data {
    
    //Fields
    
    protected Page page;
    
    protected int lineIndex;
    
    private org.apache.poi.ss.usermodel.Row row;
    
    
    //Constructors
    
    protected Line(Line line) {
        this.data.addAll(line.data);
        this.page = line.page;
        this.lineIndex = line.lineIndex;
        this.row = line.row;
        
        parse();
    }
    
    protected Line() {
    }
    
    
    //Methods
    
    protected void load(org.apache.poi.ss.usermodel.Row row) {
        this.row = row;
        
        for (int cell = 0; cell < row.getPhysicalNumberOfCells(); cell++) {
            data.add(Optional.ofNullable(row.getCell(cell)).map(Cell::toString).orElse(null));
        }
    }
    
    protected void parse() {
    }
    
}
