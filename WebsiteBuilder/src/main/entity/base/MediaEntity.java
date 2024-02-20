/*
 * File:    MediaEntity.java
 * Package: main.entity.base
 * Author:  Zachary Gill
 */

package main.entity.base;

import java.io.File;

import main.util.persistence.VariableUtil;

public abstract class MediaEntity extends RawEntity {
    
    //Constants
    
    public static final Permission FILENAME_SPACES = Permission.ALLOW;
    
    
    //Constructors
    
    protected MediaEntity(File media, boolean autoClean) {
        super(media, false);
        
        if (autoClean) {
            autoClean();
        }
    }
    
    protected MediaEntity(File media) {
        this(media, CLEAN.auto());
    }
    
    
    //Methods
    
    @Override
    protected boolean doFixName() {
        boolean result = false;
        if (FILENAME_SPACES.denied() && getFileName().contains(" ")) {
            result |= rename(normalizeFileName(getFileName()));
        }
        
        if (getFileName().matches(".*" + VariableUtil.get(0xe304) + "\\d+\\..*")) {
            result |= rename(getFileName().replaceAll(VariableUtil.get(0xe304) + "\\d+", ""));
        }
        if (getFileName().matches(".*" + VariableUtil.get(0xe9a3c) + ".*")) {
            result |= rename(getFileName().replaceAll(VariableUtil.get(0xe9a3c), ""));
        }
        if (getFileName().matches(".*\\." + VariableUtil.get(0x0797) + "[^.]*\\..*")) {
            result |= rename(getFileName().replaceAll("\\." + VariableUtil.get(0x0797) + "(?:" + VariableUtil.get(0xd7b6) + "|" + VariableUtil.get(0x511d) + ")?\\d*\\.", "."));
        }
        if (getFileName().matches(".*\\." + VariableUtil.get(0x7eb4) + "\\d*\\..*")) {
            result |= rename(getFileName().replaceAll("\\." + VariableUtil.get(0x7eb4) + "\\d*\\.", "."));
        }
        if (getFileName().matches(".*\\." + VariableUtil.get(0x3e7d) + "\\..*")) {
            result |= rename(getFileName().replaceAll("\\." + VariableUtil.get(0x3e7d) + "\\d*\\.", "."));
        }
        if (getFileName().matches(".*" + VariableUtil.get(0x7c78) + "(?:\\d+(?:x\\d+)?)?\\..*")) {
            result |= rename(getFileName().replaceAll(VariableUtil.get(0x7c78) + "(?:\\d+(?:x\\d+)?)?", ""));
        }
        if (getFileName().matches("^(" + VariableUtil.get(0xfec3) + "[^_]+)_.*(\\.[^.]+)$")) {
            result |= rename(getFileName().replace(" ", "_").replaceAll("^(" + VariableUtil.get(0xfec3) + "[^_]+)_.*(\\.[^.]+)$", "$1$2"));
        }
        return result;
    }
    
}
