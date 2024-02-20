/*
 * File:    LinkEntity.java
 * Package: main.entity.base
 * Author:  Zachary Gill
 */

package main.entity.base;

import java.io.File;

public abstract class LinkEntity extends RawEntity {
    
    //Constructors
    
    protected LinkEntity(File link, boolean autoClean) {
        super(link, false);
        
        if (autoClean) {
            autoClean();
        }
    }
    
    protected LinkEntity(File link) {
        this(link, CLEAN.auto());
    }
    
}
