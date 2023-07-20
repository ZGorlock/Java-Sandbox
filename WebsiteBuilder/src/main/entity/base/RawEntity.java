/*
 * File:    RawEntity.java
 * Package: main.entity.base
 * Author:  Zachary Gill
 */

package main.entity.base;

import java.io.File;
import java.util.stream.Stream;

import commons.access.Filesystem;
import commons.object.string.StringUtility;

public abstract class RawEntity extends Entity {
    
    //Constant
    
    public static final Permission FIX_NAME = Permission.AUTO;
    
    public static final Permission FIX_FORMAT = Permission.AUTO;
    
    public static final Permission REMOVE_DUPLICATES = Permission.AUTO;
    
    public static final Permission CLEAN_ENTITY_FILE = Permission.AUTO;
    
    
    //Constructors
    
    protected RawEntity(File source, boolean autoClean) {
        super(source, false);
        
        if (autoClean) {
            autoClean();
        }
    }
    
    protected RawEntity(File source) {
        this(source, CLEAN.auto());
    }
    
    
    //Methods
    
    @Override
    protected boolean doCleanName() {
        return Stream.of(
                super.doCleanName(),
                FIX_NAME.auto() && fixName(),
                FIX_FORMAT.auto() && fixFormat()
        ).reduce(Boolean.FALSE, Boolean::logicalOr);
    }
    
    public boolean fixName() {
        if (FIX_NAME.denied() || !permitFixName() || !permitProcessing()) {
            return false;
        }
        return doFixName();
    }
    
    protected boolean permitFixName() {
        return FIX_NAME.allowed() &&
                !getSource().isDirectory() && !getFileName().startsWith(META_PREFIX);
    }
    
    protected boolean doFixName() {
        return false;
    }
    
    public boolean fixFormat() {
        if (FIX_FORMAT.denied() || !permitFixFormat() || !permitProcessing()) {
            return false;
        }
        return doFixFormat();
    }
    
    protected boolean permitFixFormat() {
        return FIX_FORMAT.allowed() &&
                !getSource().isDirectory() && !getFileName().startsWith(META_PREFIX);
    }
    
    protected boolean doFixFormat() {
        return false;
    }
    
    @Override
    protected boolean doCleanFile() {
        return Stream.of(
                super.doCleanFile(),
                REMOVE_DUPLICATES.auto() && removeDuplicates(),
                CLEAN_ENTITY_FILE.auto() && cleanEntityFile()
        ).reduce(Boolean.FALSE, Boolean::logicalOr);
    }
    
    public boolean removeDuplicates() {
        if (REMOVE_DUPLICATES.denied() || !permitRemoveDuplicates() || !permitProcessing(true)) {
            return false;
        }
        return doRemoveDuplicates();
    }
    
    protected boolean permitRemoveDuplicates() {
        return REMOVE_DUPLICATES.allowed();
    }
    
    protected boolean doRemoveDuplicates() {
        final String baseName = getFileName().replaceAll("^(.+)[\\s_]?\\(\\d+\\)(\\.[^.]+)$", "$1$2");
        if (!baseName.equals(getFileName())) {
            final File baseFile = new File(getParentFile(), baseName);
            if (baseFile.exists()) {
                if (baseFile.length() >= getSize()) {
                    delete();
                } else {
                    System.err.println(StringUtility.format("Deleting: '{}'", baseFile.getAbsolutePath()));
                    Filesystem.deleteFile(baseFile);
                    rename(baseFile);
                }
            } else {
                rename(baseFile);
            }
            return true;
        }
        return false;
    }
    
    public boolean cleanEntityFile() {
        if (CLEAN_ENTITY_FILE.denied() || !permitCleanEntityFile() || !permitProcessing(false)) {
            return false;
        }
        return doCleanEntityFile();
    }
    
    protected boolean permitCleanEntityFile() {
        return CLEAN_ENTITY_FILE.allowed() &&
                !getSource().isDirectory() && !getFileName().startsWith(META_PREFIX);
    }
    
    protected boolean doCleanEntityFile() {
        return false;
    }
    
}
