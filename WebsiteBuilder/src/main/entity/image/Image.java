/*
 * File:    Image.java
 * Package: main.entity.image
 * Author:  Zachary Gill
 */

package main.entity.image;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.naming.OperationNotSupportedException;

import commons.access.Filesystem;
import commons.access.Internet;
import commons.io.file.media.image.ImageUtility;
import commons.object.string.StringUtility;
import main.WebsiteBuilder;
import main.entity.base.RawEntity;
import main.util.FilenameUtil;
import main.util.persistence.VariableUtil;

public abstract class Image extends RawEntity {
    
    //Constants
    
    public static final String DEFAULT_IMAGE_EXTENSION = ".jpg";
    
    public static final Permission FIX_NAME = Permission.AUTO;
    
    public static final Permission FIX_FORMAT = Permission.AUTO;
    
    public static final Permission UPGRADE_SAMPLE = Permission.AUTO;
    
    public static final Permission CLEAN_IMAGE = Permission.AUTO;
    
    
    //Fields
    
    protected Dimension dimensions;
    
    
    //Constructors
    
    protected Image(File image, boolean autoClean) {
        super(image, false);
        
        this.dimensions = ImageUtility.getDimensions(image);
        
        if (autoClean) {
            autoClean();
        }
    }
    
    protected Image(File image) {
        this(image, CLEAN.auto());
    }
    
    
    //Methods
    
    @Override
    protected boolean permitFixName() {
        return super.permitFixName() && FIX_NAME.allowed();
    }
    
    @Override
    protected boolean doFixName() {
        boolean result = false;
        if (FILENAME_SPACES.denied() && getFileName().contains(" ")) {
            result |= rename(getFileName().replaceAll("\\s+", "_"));
        }
        
        if (isSample()) {
            System.err.println(StringUtility.format("Sample file detected: '{}'", getSource().getAbsolutePath()));
            if (getFileName().startsWith("__")) {
                if (UPGRADE_SAMPLE.auto() && upgradeSample()) {
                    result |= rename(getFileName().replaceAll("^_+", "").replaceFirst("__", VariableUtil.get(0x2f4d)));
                }
            } else {
                result |= rename(getFileName().replaceAll("^sample[_\\-\\s]", ""));
            }
        }
        
        if (getFileName().matches(".*\\.pic[^.]*\\..*")) {
            result |= rename(getFileName().replaceAll("\\.pic(?:small|large)?\\d*\\.", "."));
        }
        if (getFileName().matches(".*\\.mov\\d*\\..*")) {
            result |= rename(getFileName().replaceAll("\\.mov\\d*\\.", "."));
        }
        if (getFileName().matches("^(imgur_[^_]+)_.*(\\.[^.]+)$")) {
            result |= rename(getFileName().replace(" ", "_").replaceAll("^(imgur_[^_]+)_.*(\\.[^.]+)$", "$1$2"));
        }
        
        return result;
    }
    
    @Override
    protected boolean permitFixFormat() {
        return super.permitFixFormat() && FIX_FORMAT.allowed();
    }
    
    @Override
    protected boolean doFixFormat() {
        final File defaultSource = FilenameUtil.setExtension(getSource(), DEFAULT_IMAGE_EXTENSION);
        if (List.of(".jpeg", ".jfif").contains(getFileType())) {
            rename(defaultSource);
        } else if (List.of(".png", ".bmp", ".webp").contains(getFileType())) {
            convert(defaultSource);
        } else if (!List.of(DEFAULT_IMAGE_EXTENSION, ".gif").contains(getFileType())) {
            System.err.println(StringUtility.format("Unrecognized format: '{}'", getSource().getAbsolutePath()));
        } else {
            return false;
        }
        return true;
    }
    
    @Override
    protected boolean permitCleanEntityFile() {
        return super.permitCleanEntityFile() && CLEAN_IMAGE.allowed() &&
                DEFAULT_IMAGE_EXTENSION.equals(getFileType());
    }
    
    @Override
    protected boolean doCleanEntityFile() {
        try {
            System.out.println(StringUtility.format("Cleaning image: '{}'", getSource().getAbsolutePath()));
            ImageUtility.cleanImageFile(getSource(), true);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }
    
    protected boolean isSample() {
        return getFileName().matches("(?i)(?:^|.*[\\W_\\-])" + VariableUtil.get(0x164a) + "[\\W_\\-].*$");
    }
    
    public boolean upgradeSample() {
        if (UPGRADE_SAMPLE.denied() || !permitUpgradeSample() || !permitProcessing(false)) {
            return false;
        }
        return doUpgradeSample();
    }
    
    protected boolean permitUpgradeSample() {
        return UPGRADE_SAMPLE.allowed() &&
                isSample();
    }
    
    protected boolean doUpgradeSample() {
        try {
            final String fileName = getFileName();
            final String title = fileName
                    .replaceAll(("(?i)" + VariableUtil.get(0x164a) + "(?!\\.)[\\W_\\-]"), "");
            final String hash = fileName
                    .replaceAll(("(?i)(?:^|.*[\\W_\\-])([0-9a-f]{4,})[\\W_]" + VariableUtil.get(0x164a) + "[\\W_\\-].*$"), "$1")
                    .replaceAll("(?i)(?:^|.*[\\W_\\-])" + VariableUtil.get(0x164a) + "[\\W_\\-]([0-9a-f]{4,})[\\W_\\-].*$", "$1");
            
            String url;
            if (fileName.startsWith("__")) {
                url = VariableUtil.get(0x47ea) + StringUtility.lSnip(hash, 2) + "/" + StringUtility.lSnip(StringUtility.lShear(hash, 2), 2) + "/" + title;
            } else {
                return false;
            }
            
            System.out.println(StringUtility.format("Attempting to Upgrading sample image: '{}'", getSource().getAbsolutePath()));
            
            File save = new File(WebsiteBuilder.TMP_DIR, title);
            File downloaded = Internet.downloadFile(url, save);
            
            if (downloaded == null) {
                url = url.replace(".jpg", ".tmp").replace(".png", ".jpg").replace(".tmp", ".png");
                save = new File(WebsiteBuilder.TMP_DIR, title.replace(".jpg", ".tmp").replace(".png", ".jpg").replace(".tmp", ".png"));
                downloaded = Internet.downloadFile(url, save);
            }
            
            if ((downloaded != null) && !Filesystem.isEmpty(save) && (save.length() >= getSize()) && (ImageUtility.loadImage(save) != null)) {
                replace(save);
            } else {
                System.err.println(StringUtility.format("Failed to upgrade sample image: '{}'", getSource().getAbsolutePath()));
            }
            
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }
    
    @Override
    public void write(String newContent) {
        throw new RuntimeException(new OperationNotSupportedException());
    }
    
    @Override
    public void writeQuietly(String newContent) {
        throw new RuntimeException(new OperationNotSupportedException());
    }
    
    public BufferedImage read() {
        return ImageUtility.loadImage(getSource());
    }
    
    public boolean write(BufferedImage image, File newSource) {
        System.err.println(!getSource().exists() ? StringUtility.format("Writing: '{}'", getSource().getAbsolutePath()) :
                !newSource.equals(getSource()) ? StringUtility.format("Converting: '{}' to: '{}'", getSource().getAbsolutePath(), newSource.getAbsolutePath()) :
                        StringUtility.format("Re-Writing: '{}' to: '{}'", getSource().getAbsolutePath(), newSource.getAbsolutePath()));
        return writeQuietly(image, newSource);
    }
    
    public boolean write(BufferedImage image) {
        return write(image, getSource());
    }
    
    public boolean writeQuietly(BufferedImage image, File newSource) {
        final File tmpFile = Filesystem.getTemporaryFile(Filesystem.getFileType(newSource));
        return ImageUtility.saveImage(image, tmpFile) && replace(tmpFile, newSource);
    }
    
    public boolean writeQuietly(BufferedImage image) {
        return writeQuietly(image, getSource());
    }
    
    public boolean convert(File newSource) {
        System.out.println(StringUtility.format("Converting to {}: '{}'", Filesystem.getFileType(newSource), getSource().getAbsolutePath()));
        if (!write(read(), newSource)) {
            System.err.println(StringUtility.format("Failed to convert: '{}'", getSource().getAbsolutePath()));
            return false;
        }
        return true;
    }
    
    
    //Getters
    
    public Dimension getDimensions() {
        return dimensions;
    }
    
    public int getWidth() {
        return dimensions.width;
    }
    
    public int getHeight() {
        return dimensions.height;
    }
    
    
    //Static Methods
    
    public static List<File> findImagesInFolder(File imageDir) {
        return Filesystem.getFilesRecursively(imageDir,
                f -> true);
    }
    
    public static List<File> findImageDirectoriesInFolder(File imageDir) {
        return Filesystem.getDirsRecursively(imageDir,
                d -> true);
    }
    
}
