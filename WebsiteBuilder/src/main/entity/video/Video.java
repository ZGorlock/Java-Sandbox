/*
 * File:    Video.java
 * Package: main.entity.video
 * Author:  Zachary Gill
 */

package main.entity.video;

import java.io.File;
import java.util.List;
import javax.naming.OperationNotSupportedException;

import commons.access.Filesystem;
import commons.io.file.media.FFmpeg;
import commons.object.string.StringUtility;
import main.entity.base.MediaEntity;
import main.util.FilenameUtil;
import main.util.persistence.VariableUtil;

public abstract class Video extends MediaEntity {
    
    //Constants
    
    public static final String DEFAULT_VIDEO_EXTENSION = ".mp4";
    
    public static final Permission FIX_NAME = Permission.AUTO;
    
    public static final Permission FIX_FORMAT = Permission.AUTO;
    
    public static final Permission CLEAN_VIDEO = Permission.DENY;
    
    
    //Constructors
    
    protected Video(File video, boolean autoClean) {
        super(video, false);
        
        if (autoClean) {
            autoClean();
        }
    }
    
    protected Video(File video) {
        this(video, CLEAN.auto());
    }
    
    
    //Methods
    
    @Override
    protected boolean permitFixName() {
        return super.permitFixName() && FIX_NAME.allowed();
    }
    
    @Override
    protected boolean doFixName() {
        boolean result = super.doFixName();
        if (isMobile()) {
            System.err.println(StringUtility.format("Mobile file detected: '{}'", getSource().getAbsolutePath()));
        }
        return result;
    }
    
    @Override
    protected boolean permitFixFormat() {
        return super.permitFixFormat() && FIX_FORMAT.allowed();
    }
    
    @Override
    protected boolean doFixFormat() {
        final File defaultSource = FilenameUtil.setExtension(getSource(), DEFAULT_VIDEO_EXTENSION);
        if (".mpeg4".equals(getFileType())) {
            rename(defaultSource);
        } else if (List.of(".mkv", ".flv", ".webp", ".webm", ".gif").contains(getFileType())) {
            System.err.println(StringUtility.format("Should be converted to {}: '{}'", DEFAULT_VIDEO_EXTENSION, getSource().getAbsolutePath()));
        } else if (!DEFAULT_VIDEO_EXTENSION.equals(getFileType())) {
            System.err.println(StringUtility.format("Unrecognized format: '{}'", getSource().getAbsolutePath()));
        } else {
            return false;
        }
        return true;
    }
    
    @Override
    protected boolean permitCleanEntityFile() {
        return super.permitCleanEntityFile() && CLEAN_VIDEO.allowed() &&
                DEFAULT_VIDEO_EXTENSION.equals(getFileType());
    }
    
    @Override
    protected boolean doCleanEntityFile() {
        try {
            final File tmpSource = Filesystem.getTemporaryFile(getFileType());
            FFmpeg.stripMediaFile(getSource(), true, true, tmpSource);
            replace(tmpSource, getSource());
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }
    
    protected boolean isMobile() {
        return getFileName().matches("(?i)^.*\b" + VariableUtil.get(0x8dd4) + "\b.*$");
    }
    
    @Override
    public void write(String newContent) {
        throw new RuntimeException(new OperationNotSupportedException());
    }
    
    @Override
    public void writeQuietly(String newContent) {
        throw new RuntimeException(new OperationNotSupportedException());
    }
    
    
    //Static Methods
    
    public static List<File> findVideosInFolder(File videoDir) {
        return Filesystem.getFilesRecursively(videoDir,
                f -> true);
    }
    
    public static List<File> findVideoDirectoriesInFolder(File videoDir) {
        return Filesystem.getDirsRecursively(videoDir,
                d -> true);
    }
    
}
