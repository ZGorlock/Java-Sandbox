/*
 * File:    Column.java
 * Package: main.entity.base
 * Author:  Zachary Gill
 */

package main.entity.base;

import java.util.List;

public class Column extends Data {
    
    //Fields
    
    public String header;
    
    protected Page page;
    
    public int columnIndex;
    
    
    //Constructors
    
    protected Column(String header) {
        this.header = header;
    }
    
    protected Column(String header, List<String> data) {
        this(header);
        
        this.data.addAll(data);
    }
    
    protected Column() {
    }
    
}
