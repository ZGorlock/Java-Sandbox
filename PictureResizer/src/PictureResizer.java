/*
 * File:    PictureResizer.java
 * Package:
 * Author:  Zachary Gill
 */

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import common.Filesystem;

public class PictureResizer {
    
    //Constants
    
    private static final boolean preserveMetadata = false; //when inactive, file dates will be preserved if preserveDates is enabled but 'Date Taken' will not
    
    private static final boolean preserveDates = true; //when active, file dates will be preserved but 'Date Taken' will not
    
    private static final boolean saveBackup = true;
    
    private static final boolean limitDimensions = false; //aspect ratio will be preserved
    
    private static final int maxDimension = 3072;
    
    private static final boolean crop = false;
    
    private static final int cropOffTop = 31;
    
    private static final int cropOffLeft = 0;
    
    private static final int cropOffRight = 0;
    
    private static final int cropOffBottom = 0;
    
    private static final File dataDir = new File("data");
    
    private static final List<String> pictureTypes = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff");
    
    
    //Static Fields
    
    private static File directory = new File("C:/Users/Zack/Desktop/New folder");
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        processPictures(getPictures(directory));
    }
    
    
    //Methods
    
    private static List<File> getPictures(File directory) throws Exception {
        return Filesystem.getFilesRecursively(directory != null ? directory : dataDir).stream()
                .filter(e -> pictureTypes.contains(Filesystem.getFileType(e).toLowerCase()))
                .collect(Collectors.toList());
    }
    
    private static void processPictures(List<File> pictures) throws Exception {
        Filesystem.createDirectory(new File("tmp"));
        
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
    
    private static void processPicture(File picture) throws Exception {
        String type = Filesystem.getFileType(picture).toLowerCase();
        File tmp = new File("tmp", picture.getName().replaceAll("(?<=\\.)[^.]+$", type));
        File output = new File(picture.getParentFile(), tmp.getName());
        
        //System.out.println("INPUT");
        //readMetadata(picture);
        
        if (saveBackup) {
            backupImage(picture);
        }
        
        if (preserveMetadata) {
            processPicturePreserveMetadata(picture, tmp);
        } else {
            processPictureLoseMetadata(picture, tmp);
        }
        
        replaceImage(picture, tmp, output);
        
        //System.out.println("OUTPUT:");
        //readMetadata(output);
    }
    
    private static void processPicturePreserveMetadata(File source, File target) throws Exception {
        try (FileInputStream fileInputStream = new FileInputStream(source);
             FileOutputStream fileOutputStream = new FileOutputStream(target)) {
            
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(fileInputStream);
            ImageReader reader = ImageIO.getImageReaders(imageInputStream).next();
            reader.setInput(imageInputStream, true);
            IIOMetadata metadata = reader.getImageMetadata(0);
            IIOMetadata streamMetadata = reader.getStreamMetadata();
            BufferedImage image = reader.read(0);
            imageInputStream.flush();
            
            image = preProcessImage(image);
            
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(fileOutputStream);
            ImageWriter writer = ImageIO.getImageWriter(reader);
            writer.setOutput(imageOutputStream);
            ImageWriteParam writeParams = writer.getDefaultWriteParam();
            writeParams.setCompressionMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
            
            writer.write(streamMetadata, new IIOImage(image, null, metadata), writeParams);
            writer.dispose();
            imageOutputStream.flush();
        }
    }
    
    private static void processPictureLoseMetadata(File source, File target) throws Exception {
        BufferedImage image = ImageIO.read(source);
        
        image = preProcessImage(image);
        
        ImageIO.write(image, Filesystem.getFileType(source).toLowerCase(), target);
    }
    
    private static BufferedImage preProcessImage(BufferedImage image) {
        if (crop) {
            image = cropImage(image);
        }
        if (limitDimensions) {
            image = scaleImage(image, maxDimension, maxDimension);
        }
        return image;
    }
    
    private static BufferedImage cropImage(BufferedImage image, Rectangle rect) {
        BufferedImage cropped = new BufferedImage((int) rect.getWidth(), (int) rect.getHeight(), image.getType());
        Graphics g = cropped.getGraphics();
        g.drawImage(image, 0, 0, (int) rect.getWidth(), (int) rect.getHeight(), (int) rect.getX(), (int) rect.getY(), (int) (rect.getX() + rect.getWidth()), (int) (rect.getY() + rect.getHeight()), null);
        g.dispose();
        return cropped;
    }
    
    private static BufferedImage cropImage(BufferedImage image) {
        Rectangle rect = new Rectangle(cropOffLeft, cropOffTop, (image.getWidth() - cropOffLeft - cropOffRight), (image.getHeight() - cropOffTop - cropOffBottom));
        return cropImage(image, rect);
    }
    
    public static BufferedImage scaleImage(BufferedImage image, double scale) {
        if ((scale <= 0.0) || (scale >= 1.0)) {
            return image;
        }
        
        AffineTransform transform = new AffineTransform();
        transform.scale(scale, scale);
        AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
        BufferedImage scaled = new BufferedImage((int) (image.getWidth() * scale), (int) (image.getHeight() * scale), image.getType());
        return transformOp.filter(image, scaled);
    }
    
    public static BufferedImage scaleImage(BufferedImage image, int maxWidth, int maxHeight) {
        if ((image.getWidth() <= maxWidth) && (image.getHeight() <= maxHeight)) {
            return image;
        }
        
        double scale = Math.min(((double) maxWidth / image.getWidth()), ((double) maxHeight / image.getHeight()));
        return scaleImage(image, scale);
    }
    
    private static void backupImage(File picture) throws Exception {
        File backupDir = new File(picture.getParentFile(), "original");
        if (!backupDir.exists() && !Filesystem.createDirectory(backupDir)) {
            System.err.println("Failed to create backup directory: " + backupDir.getAbsolutePath());
            throw new Exception();
        }
        File backup = new File(backupDir, picture.getName());
        if (!Filesystem.copyFile(picture, backup)) {
            System.err.println("Failed to create backup: " + backup.getAbsolutePath());
            throw new Exception();
        }
    }
    
    private static void replaceImage(File source, File tmp, File target) throws Exception {
        Map<String, FileTime> dates = readDates(source);
        Filesystem.move(tmp, target, true);
        writeDates(target, dates);
    }
    
    private static Map<String, FileTime> readDates(File image) throws Exception {
        Map<String, FileTime> dates = new HashMap<>();
        if (!preserveDates) {
            return dates;
        }
        
        List<String> attributes = Arrays.asList("lastModifiedTime", "lastAccessTime", "creationTime");
        for (String attribute : attributes) {
            dates.put(attribute, (FileTime) Files.getAttribute(image.toPath(), attribute));
        }
        return dates;
    }
    
    private static void writeDates(File image, Map<String, FileTime> dates) throws Exception {
        if (!preserveDates) {
            return;
        }
        
        List<String> attributes = Arrays.asList("lastModifiedTime", "lastAccessTime", "creationTime");
        for (String attribute : attributes) {
            FileTime date = dates.get(attribute);
            if (date == null) {
                continue;
            }
            Files.setAttribute(image.toPath(), attribute, date);
        }
    }
    
    private static List<MetadataTag> readMetadata(File picture) throws Exception {
        Metadata metadata = ImageMetadataReader.readMetadata(picture);
        List<MetadataTag> metadataTags = parseMetadata(metadata);
        metadataTags.forEach(System.out::println);
        return metadataTags;
    }
    
    private static List<MetadataTag> parseMetadata(Metadata metadata) {
        List<MetadataTag> metadataTags = new ArrayList<>();
        
        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                metadataTags.add(new MetadataTag(directory.getName(), tag.getTagName(), tag.getDescription(), tag.getTagType()));
            }
            
            if (directory.hasErrors()) {
                for (String error : directory.getErrors()) {
                    metadataTags.add(new MetadataTag(directory.getName(), "ERROR", error, Integer.MIN_VALUE));
                }
            }
        }
        return metadataTags;
    }
    
    
    //Inner Classes
    
    public static class MetadataTag {
        
        String directory;
        
        String tag;
        
        String value;
        
        int type;
        
        public MetadataTag(String directory, String tag, String value, int type) {
            this.directory = directory;
            this.tag = tag;
            this.value = value;
            this.type = type;
        }
        
        @Override
        public String toString() {
            return "[" + directory + "] " + tag + " : " + value;
        }
    }
    
}
