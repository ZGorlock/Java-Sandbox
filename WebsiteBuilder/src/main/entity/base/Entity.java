/*
 * File:    Entity.java
 * Package: main.entity.base
 * Author:  Zachary Gill
 */

package main.entity.base;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import commons.access.Filesystem;
import commons.object.string.StringUtility;
import main.WebsiteBuilder;
import main.util.FilenameUtil;
import main.util.persistence.LastProcessUtil;
import main.util.persistence.VariableUtil;

public abstract class Entity {
    
    //Constant
    
    public static final boolean LOG_ENTITY = false;
    
    public static final Permission CLEAN = Permission.AUTO;
    
    public static final Permission CLEAN_NAME = Permission.AUTO;
    
    public static final Permission CLEAN_FILE = Permission.AUTO;
    
    public static final Permission FILENAME_SPACES = Permission.ALLOW;
    
    public static final String META_PREFIX = VariableUtil.get(0xbd70);
    
    
    //Enums
    
    public enum Permission {
        
        //Values
        
        DENY(true, false, false),
        ALLOW(false, true, false),
        AUTO(false, true, true);
        
        
        //Fields
        
        private final boolean denied;
        
        private final boolean allowed;
        
        private final boolean auto;
        
        
        //Constructors
        
        Permission(boolean denied, boolean allowed, boolean auto) {
            this.denied = denied;
            this.allowed = allowed;
            this.auto = auto;
        }
        
        
        //Getters
        
        public boolean denied() {
            return denied;
        }
        
        public boolean allowed() {
            return allowed;
        }
        
        public boolean auto() {
            return auto;
        }
        
    }
    
    
    //Fields
    
    protected File source;
    
    protected String fileName;
    
    protected String fileTitle;
    
    protected String fileType;
    
    protected File parentFile;
    
    protected String parentFilePath;
    
    
    //Constructors
    
    protected Entity(File source, boolean autoClean) {
        if (LOG_ENTITY) {
            System.out.println(StringUtility.format("Loading: '{}'", source.getAbsolutePath()));
        }
        
        init(source);
        
        if (autoClean) {
            autoClean();
        }
    }
    
    protected Entity(File source) {
        this(source, CLEAN.auto());
    }
    
    
    //Methods
    
    private void init(File source) {
        this.source = source;
        this.fileName = this.source.getName();
        this.fileTitle = FilenameUtil.getTitle(this.source);
        this.fileType = FilenameUtil.getExtension(this.source);
        this.parentFile = this.source.getParentFile();
        this.parentFilePath = StringUtility.fileString(this.parentFile);
    }
    
    protected final boolean autoClean() {
        if (!CLEAN.auto() || !permitAutoClean() || !permitProcessing()) {
            return false;
        }
        return doAutoClean();
    }
    
    protected boolean permitAutoClean() {
        return CLEAN.auto();
    }
    
    protected final boolean doAutoClean() {
        return clean();
    }
    
    public final boolean clean() {
        if (CLEAN.denied() || !permitClean() || !permitProcessing()) {
            return false;
        }
        return doClean();
    }
    
    protected boolean permitClean() {
        return CLEAN.allowed();
    }
    
    protected boolean doClean() {
        return Stream.of(
                CLEAN_NAME.auto() && cleanName(),
                CLEAN_FILE.auto() && cleanFile()
        ).anyMatch(e -> e);
    }
    
    protected boolean cleanName() {
        if (CLEAN_NAME.denied() || !permitCleanName() || !permitProcessing()) {
            return false;
        }
        return doCleanName();
    }
    
    protected boolean permitCleanName() {
        return CLEAN_NAME.allowed();
    }
    
    protected boolean doCleanName() {
        return false;
    }
    
    protected boolean cleanFile() {
        if (CLEAN_FILE.denied() || !permitCleanFile() || !permitProcessing()) {
            return false;
        }
        return doCleanFile();
    }
    
    protected boolean permitCleanFile() {
        return CLEAN_FILE.allowed();
    }
    
    protected boolean doCleanFile() {
        return false;
    }
    
    @SuppressWarnings("ConstantValue")
    protected boolean permitProcessing(boolean permitDuplicateProcessing) {
        return (!WebsiteBuilder.SAFE_MODE || WebsiteBuilder.TEST_MODE) &&
                (WebsiteBuilder.REPROCESS_ALL || permitDuplicateProcessing || needsProcessing());
    }
    
    protected boolean permitProcessing() {
        return permitProcessing(false);
    }
    
    protected boolean needsProcessing(Class<? extends Entity> type, boolean defaultAutoClean) {
        return Optional.ofNullable(LastProcessUtil.getLastProcessDate(type))
                .flatMap(last -> Optional.ofNullable(Filesystem.getLastModifiedTime(getSource())).map(last::before))
                .orElse(defaultAutoClean);
    }
    
    protected boolean needsProcessing(Class<? extends Entity> type) {
        return needsProcessing(type, CLEAN.auto());
    }
    
    protected boolean needsProcessing() {
        return needsProcessing(null);
    }
    
    public void write(String newContent) {
        System.err.println(StringUtility.format("{}Writing: '{}'", (getSource().exists() ? "Re-" : ""), getSource().getAbsolutePath()));
        writeQuietly(newContent);
    }
    
    public void writeQuietly(String newContent) {
        if (!WebsiteBuilder.TEST_MODE) {
            Filesystem.writeStringToFile(getSource(), newContent);
        }
    }
    
    public void rename(File newSource) {
        System.err.println(StringUtility.format("Renaming: '{}' to '{}'", getSource().getAbsolutePath(), newSource.getAbsolutePath()));
        renameQuietly(newSource);
    }
    
    public void renameQuietly(File newSource) {
        if (!WebsiteBuilder.TEST_MODE) {
            if (Filesystem.safeReplace(getSource(), newSource)) {
                init(newSource);
            }
        }
    }
    
    public void move(File newSource) {
        System.err.println(StringUtility.format("Moving: '{}' to '{}'", getSource().getAbsolutePath(), newSource.getAbsolutePath()));
        moveQuietly(newSource);
    }
    
    public void moveQuietly(File newSource) {
        if (!WebsiteBuilder.TEST_MODE) {
            Filesystem.move(getSource(), newSource);
            init(newSource);
        }
    }
    
    public boolean replace(File tmpSource, File newSource) {
        System.err.println(newSource.equals(getSource()) ?
                StringUtility.format("Rewriting: '{}'", getSource().getAbsolutePath()) :
                StringUtility.format("Replacing: '{}' with: '{}'", getSource().getAbsolutePath(), newSource.getAbsolutePath()));
        return replaceQuietly(tmpSource, newSource);
    }
    
    public boolean replace(File newSource) {
        return replace(newSource, getSource());
    }
    
    public boolean replaceQuietly(File tmpFile, File newSource) {
        if (!WebsiteBuilder.TEST_MODE) {
            if (Filesystem.safeReplace(tmpFile, newSource)) {
                if (!newSource.equals(getSource())) {
                    delete();
                    init(newSource);
                }
                return true;
            }
        }
        return false;
    }
    
    public boolean replaceQuietly(File tmpSource) {
        return replaceQuietly(tmpSource, getSource());
    }
    
    public void delete() {
        System.err.println(StringUtility.format("Deleting: '{}'", getSource().getAbsolutePath()));
        deleteQuietly();
    }
    
    public void deleteQuietly() {
        if (!WebsiteBuilder.TEST_MODE) {
            Filesystem.deleteFile(getSource());
        }
    }
    
    protected int count() {
        return 1;
    }
    
    @Override
    public String toString() {
        return getSource().getAbsolutePath();
    }
    
    
    //Getters
    
    public File getSource() {
        return source;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public String getFileTitle() {
        return fileTitle;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public File getParentFile() {
        return parentFile;
    }
    
    public String getParentFilePath() {
        return parentFilePath;
    }
    
    public String getContent() {
        return readContent(getSource());
    }
    
    public Long getSize() {
        return getSource().length();
    }
    
    
    //Static Methods
    
    public static String readContent(File source) {
        return Filesystem.readFileToString(source);
    }
    
    public static String normalizeFileName(String fileName) {
        return FilenameUtil.normalizeFilename(fileName)
                .replaceAll("\\s", (FILENAME_SPACES.allowed() ? " " : ""));
    }
    
}
