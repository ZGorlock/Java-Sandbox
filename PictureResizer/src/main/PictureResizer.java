/*
 * File:    PictureResizer.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import common.Filesystem;
import common.ImageMetadataUtility;
import common.StringUtility;

public class PictureResizer {
    
    //Constants
    
    private static final File DATA_DIR = new File("data");
    
    private static final File TMP_DIR = new File("tmp");
    
    private static final File WORK_DIR = new File(Filesystem.getUserDirectory(), "Desktop/New Folder");
    
    private static final String BACKUP_NAME = "original";
    
    private static final List<String> PICTURE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff");
    
    
    //Static Fields
    
    private static final boolean preserveMetadata = false; //when inactive, file dates will be preserved if preserveDates is enabled but 'Date Taken' will not
    
    private static final boolean preserveDates = true; //when active, file dates will be preserved but 'Date Taken' will not
    
    private static final boolean preserveOrientation = true; //when active, and if metadata is not being preserved, pictures will be rotated to fix the orientation described by their metadata
    
    private static final boolean printMetadata = false;
    
    private static final boolean saveBackup = true;
    
    private static boolean allowEdits = true; //when inactive, cropping, scaling, and dimension limiting will be disabled
    
    private static final boolean crop = false; //occurs before scaling; aspect ratio will not be preserved
    
    private static final int cropOffTop = 0;
    
    private static final int cropOffLeft = 0;
    
    private static final int cropOffRight = 0;
    
    private static final int cropOffBottom = 0;
    
    private static final boolean scale = false; //occurs after cropping and before limiting dimensions
    
    private static final double scaleWidth = 1.0;
    
    private static final double scaleHeight = 1.0;
    
    private static final boolean limitDimensions = false; //occurs after cropping and scaling; aspect ratio will be preserved
    
    private static final int limitMaxWidth = 0;
    
    private static final int limitMaxHeight = 0;
    
    private static final int limitMaxDimension = 0; //when non-zero the minimum of this and either cropMaxWidth and cropMaxHeight with be used for that dimension
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        final List<File> pictures = getPictures();
        processPictures(pictures);
    }
    
    
    //Static Methods
    
    @SuppressWarnings("ConstantValue")
    private static List<File> getPictures() throws Exception {
        return Stream.of(DATA_DIR, WORK_DIR)
                .filter(Objects::nonNull).filter(File::exists)
                .map(Filesystem::getFilesRecursively).flatMap(Collection::stream)
                .filter(e -> !e.getAbsolutePath().matches("(?i).*[\\\\/]" + BACKUP_NAME + "[\\\\/].*"))
                .filter(e -> PICTURE_TYPES.contains(Filesystem.getFileType(e).toLowerCase()))
                .collect(Collectors.toList());
    }
    
    private static void processPictures(List<File> pictures) throws Exception {
        prepareDirectories();
        
        pictures.forEach(picture -> {
            System.out.println("Processing: " + picture.getAbsolutePath());
            try {
                processPicture(picture);
            } catch (Exception e) {
                System.err.println("Failed to process: " + picture.getAbsolutePath());
                e.printStackTrace(System.err);
            }
        });
    }
    
    @SuppressWarnings("ConstantValue")
    private static void prepareDirectories() throws Exception {
        Stream.of(TMP_DIR, DATA_DIR).filter(Objects::nonNull)
                .filter(dir -> !Filesystem.createDirectory(dir))
                .forEach(dir -> {
                    System.err.println("Failed to create directory: " + dir.getAbsolutePath());
                    throw new RuntimeException();
                });
        
        if (saveBackup) {
            Stream.of(DATA_DIR, WORK_DIR).filter(Objects::nonNull)
                    .filter(File::exists).map(e -> new File(e, BACKUP_NAME))
                    .forEach(backupDir -> {
                        if (!backupDir.exists()) {
                            if (!Filesystem.createDirectory(backupDir)) {
                                System.err.println("Failed to create backup directory: " + backupDir.getAbsolutePath());
                                throw new RuntimeException();
                            }
                        } else if (!Filesystem.directoryIsEmpty(backupDir)) {
                            final String oldBackupTimestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Filesystem.readDates(backupDir).get("creationTime"));
                            final File oldBackupDir = new File(backupDir.getParentFile(), backupDir.getName() + "-" + oldBackupTimestamp);
                            final File saveOldBackupDir = new File(backupDir, backupDir.getName());
                            Filesystem.rename(backupDir, oldBackupDir);
                            Filesystem.moveDirectory(oldBackupDir, saveOldBackupDir);
                        }
                    });
        }
    }
    
    private static void processPicture(File picture) throws Exception {
        final String type = Filesystem.getFileType(picture).toLowerCase();
        final File tmp = new File(TMP_DIR, picture.getName().replaceAll("(?<=\\.)[^.]+$", type));
        final File output = new File(picture.getParentFile(), tmp.getName());
        
        if (saveBackup) {
            backupImage(picture);
        }
        
        printMetadata("Input", picture);
        
        processImage(picture, tmp);
        replaceImage(picture, tmp, output);
        
        printMetadata("Output", picture);
    }
    
    private static boolean processImage(File source, File target) {
        if ((source == null) || !source.exists()) {
            return false;
        }
        
        try {
            IIOImage imageData;
            IIOMetadata streamMetadata;
            
            try (final ImageInputStream imageInputStream = ImageIO.createImageInputStream(source)) {
                final ImageReader reader = ImageIO.getImageReaders(imageInputStream).next();
                try {
                    reader.setInput(imageInputStream);
                    
                    final ImageReadParam readParams = reader.getDefaultReadParam();
                    
                    imageData = reader.readAll(0, readParams);
                    streamMetadata = reader.getStreamMetadata();
                    
                } finally {
                    imageInputStream.flush();
                    reader.dispose();
                }
            }
            
            if (allowEdits) {
                final BufferedImage originalImage = (BufferedImage) imageData.getRenderedImage();
                final BufferedImage image = prepareImage(source);
                
                imageData.setRenderedImage(image);
            }
            
            if (!preserveMetadata) {
                imageData.setMetadata(null);
                imageData.setThumbnails(null);
                streamMetadata = null;
            }
            
            try (final ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(target)) {
                final ImageWriter writer = ImageIO.getImageWritersBySuffix(Filesystem.getFileType(target)).next();
                try {
                    writer.setOutput(imageOutputStream);
                    
                    final ImageWriteParam writeParams = writer.getDefaultWriteParam();
                    writeParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    writeParams.unsetCompression();
                    
                    writer.write(streamMetadata, imageData, writeParams);
                    
                } finally {
                    imageOutputStream.flush();
                    writer.dispose();
                }
            }
            
            return true;
            
        } catch (Exception e) {
            if (Filesystem.logFilesystem()) {
                System.err.println("Filesystem: Unable to clean image file: " + source.getAbsolutePath());
            }
            return false;
        }
    }
    
    @Deprecated
    private static void processPicturePreserveMetadata(File source, File target) throws Exception {
        try (FileInputStream fileInputStream = new FileInputStream(source);
             FileOutputStream fileOutputStream = new FileOutputStream(target)) {
            
            final ImageInputStream imageInputStream = ImageIO.createImageInputStream(fileInputStream);
            final ImageReader reader = ImageIO.getImageReaders(imageInputStream).next();
            reader.setInput(imageInputStream, true);
            
            final IIOMetadata metadata = reader.getImageMetadata(0);
            final IIOMetadata streamMetadata = reader.getStreamMetadata();
            
            final BufferedImage originalImage = reader.read(0);
            imageInputStream.flush();
            
            final BufferedImage image = prepareImage(source);
            
            final ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(fileOutputStream);
            final ImageWriter writer = ImageIO.getImageWriter(reader);
            writer.setOutput(imageOutputStream);
            
            final ImageWriteParam writeParams = writer.getDefaultWriteParam();
            writeParams.setCompressionMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
            
            writer.write(streamMetadata, new IIOImage(image, null, metadata), writeParams);
            writer.dispose();
            imageOutputStream.flush();
        }
    }
    
    @Deprecated
    private static void processPictureLoseMetadata(File source, File target) throws Exception {
        final BufferedImage originalImage = ImageIO.read(source);
        final BufferedImage image = prepareImage(source);
        
        ImageIO.write(image, Filesystem.getFileType(source).toLowerCase(), target);
    }
    
    private static BufferedImage prepareImage(BufferedImage image) throws Exception {
        if (crop) {
            image = crop(image);
        }
        if (scale) {
            image = scale(image);
        }
        if (limitDimensions) {
            image = limitDimensions(image);
        }
        return image;
    }
    
    private static BufferedImage prepareImage(File picture) throws Exception {
        BufferedImage image = ImageIO.read(picture);
        if (preserveOrientation && !preserveMetadata) {
            image = orient(image, getMetadataTag(picture, "Exif IFD0", "Orientation"));
        }
        return prepareImage(image);
    }
    
    public static BufferedImage cropImage(BufferedImage image, Rectangle rect) {
        final int newWidth = (int) rect.getWidth();
        final int newHeight = (int) rect.getHeight();
        
        final AffineTransform transform = new AffineTransform();
        transform.translate((int) -rect.getMinX(), (int) -rect.getMinY());
        final AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        
        final BufferedImage cropped = new BufferedImage(newWidth, newHeight, image.getType());
        return transformOp.filter(image, cropped);
    }
    
    public static BufferedImage cropImage(BufferedImage image, int cropOffLeft, int cropOffTop, int cropOffRight, int cropOffBottom) {
        final Rectangle rect = new Rectangle(
                cropOffLeft, cropOffTop,
                (image.getWidth() - cropOffLeft - cropOffRight),
                (image.getHeight() - cropOffTop - cropOffBottom));
        
        return cropImage(image, rect);
    }
    
    private static BufferedImage crop(BufferedImage image) {
        return cropImage(image, cropOffLeft, cropOffTop, cropOffRight, cropOffBottom);
    }
    
    public static BufferedImage scaleImage(BufferedImage image, double scaleX, double scaleY) {
        if ((scaleX <= 0.0) || (scaleY <= 0.0)) {
            return image;
        }
        
        final int newWidth = (int) (image.getWidth() * scaleX);
        final int newHeight = (int) (image.getHeight() * scaleY);
        
        final AffineTransform transform = new AffineTransform();
        transform.scale(scaleX, scaleY);
        final AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
        
        final BufferedImage scaled = new BufferedImage(newWidth, newHeight, image.getType());
        return transformOp.filter(image, scaled);
    }
    
    public static BufferedImage scaleImage(BufferedImage image, double scale) {
        return scaleImage(image, scale, scale);
    }
    
    private static BufferedImage scale(BufferedImage image) {
        return scaleImage(image, scaleWidth, scaleHeight);
    }
    
    private static BufferedImage limitDimensions(BufferedImage image) {
        final int maxWidth = Math.min(((limitMaxWidth > 0) ? limitMaxWidth : Integer.MAX_VALUE), ((limitMaxDimension > 0) ? limitMaxDimension : Integer.MAX_VALUE));
        final int maxHeight = Math.min(((limitMaxHeight > 0) ? limitMaxHeight : Integer.MAX_VALUE), ((limitMaxDimension > 0) ? limitMaxDimension : Integer.MAX_VALUE));
        
        final double scale = Math.min(((double) maxWidth / image.getWidth()), ((double) maxHeight / image.getHeight()));
        return scaleImage(image, scale);
    }
    
    public static BufferedImage rotateImage(BufferedImage image, int quadrants) {
        final int newWidth = (((quadrants % 2) == 0) ? image.getWidth() : image.getHeight());
        final int newHeight = (((quadrants % 2) == 0) ? image.getHeight() : image.getWidth());
        
        final AffineTransform transform = new AffineTransform();
        transform.translate((newWidth / 2.0), (newHeight / 2.0));
        transform.quadrantRotate(quadrants);
        transform.translate((-image.getWidth() / 2.0), (-image.getHeight() / 2.0));
        final AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        
        final BufferedImage rotated = new BufferedImage(newWidth, newHeight, image.getType());
        return transformOp.filter(image, rotated);
    }
    
    public static BufferedImage flipImage(BufferedImage image, boolean horizontally, boolean vertically) {
        final int newWidth = image.getWidth();
        final int newHeight = image.getHeight();
        
        final AffineTransform transform = new AffineTransform();
        transform.translate((newWidth / 2.0), (newHeight / 2.0));
        transform.scale((horizontally ? -1 : 1), (vertically ? -1 : 1));
        transform.translate((-image.getWidth() / 2.0), (-image.getHeight() / 2.0));
        final AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        
        final BufferedImage flipped = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        return transformOp.filter(image, flipped);
    }
    
    private static BufferedImage orient(BufferedImage image, ImageMetadataUtility.MetadataTag orientationTag) {
        if (orientationTag == null) {
            return image;
        }
        
        final String data = orientationTag.value.toUpperCase().replaceAll(".*\\(\\s*|\\s*\\).*", "");
        final String mirror = data.contains("MIRROR") ? data.replaceAll(".*MIRROR\\s(\\w+).*", "$1") : "";
        final String rotate = data.contains("ROTATE") ? ((data.contains("CCW") ? "-" : "") + data.replaceAll(".*ROTATE\\s(\\d+).*", "$1")) : "";
        
        BufferedImage oriented = image;
        if (!mirror.isEmpty() && !mirror.contains(" ")) {
            final boolean horizontal = mirror.contains("HORIZONTAL");
            final boolean vertical = mirror.contains("VERTICAL");
            oriented = flipImage(oriented, horizontal, vertical);
        }
        if (!rotate.isEmpty() && !rotate.contains(" ")) {
            final int rotation = (Integer.parseInt(rotate) / 90);
            oriented = rotateImage(image, rotation);
        }
        return oriented;
    }
    
    private static void backupImage(File picture) throws Exception {
        final File backupDir = new File(picture.getParentFile(), BACKUP_NAME);
        final File backup = new File(backupDir, picture.getName());
        if (!Filesystem.copyFile(picture, backup)) {
            System.err.println("Failed to create backup: " + backup.getAbsolutePath());
            throw new Exception();
        }
    }
    
    private static void replaceImage(File source, File tmp, File target) throws Exception {
        if (preserveDates) {
            final Map<String, FileTime> dates = Filesystem.readDates(source);
            Filesystem.writeDates(tmp, dates);
        }
        
        Filesystem.move(tmp, target, true);
    }
    
    public static ImageMetadataUtility.MetadataTag getMetadataTag(File picture, String directory, String name) {
        return ImageMetadataUtility.getMetadataTag(picture, directory, name);
    }
    
    public static List<ImageMetadataUtility.MetadataTag> getMetadata(File picture) {
        return ImageMetadataUtility.getMetadata(picture);
    }
    
    private static void printMetadata(String title, File picture) {
        if (printMetadata) {
            System.out.println(StringUtility.spaces(4) + title + " Metadata:");
            getMetadata(picture).stream()
                    .map(String::valueOf).map(e -> (StringUtility.spaces(8) + e))
                    .forEach(System.out::println);
        }
    }
    
    private static List<File> findPicturesWithOrientationMetadata(File dir) {
        return Filesystem.getFilesRecursively(dir).stream()
                .filter(file -> PICTURE_TYPES.contains(Filesystem.getFileType(file).toLowerCase()))
                .filter(picture -> Optional.ofNullable(getMetadataTag(picture, "Exif IFD0", "Orientation"))
                        .map(e -> e.value)
                        .filter(e -> !e.contains("Unknown")).filter(e -> !e.contains("(0)"))
                        .map(e -> !e.isBlank()).orElse(false))
                .collect(Collectors.toList());
    }
    
}
