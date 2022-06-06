/*
 * File:    HomePictureOrganizer.java
 * Package: main 
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

public class HomePictureOrganizer {
    
    //Constants
    
    private static final File pictureDir = new File("todo");
    
    private static final File outputDir = new File("output");
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        try {
            File[] pictures = pictureDir.listFiles();
            if (pictures == null || pictures.length == 0) {
                return;
            }
            
            for (File picture : pictures) {
                String dateTaken = getDateTaken(picture);
                File dateDir = new File(outputDir, dateTaken);
                if (!dateDir.exists() && !dateDir.mkdir()) {
                    continue;
                }
                
                File moved = new File(dateDir, picture.getName());
                Files.move(picture.toPath(), moved.toPath());
            }
            
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    
    //Methods
    
    /**
     * Returns the date the image was taken.
     *
     * @param image The image file.
     * @return The date the image was taken, or the 'last modified time' if that metadata is not present.
     */
    public static String getDateTaken(File image) {
        if (image == null) {
            return null;
        }
        
        MetadataTag dateTaken;
        switch (getFileType(image).toLowerCase()) {
            case "jpg":
            case "jpeg":
                dateTaken = getMetadataTag(image, "Date/Time Original");
                if (dateTaken != null) {
                    break;
                }
            case "png":
            case "gif":
            case "bmp":
            case "tif":
            case "tiff":
            case "wbmp":
                dateTaken = getMetadata(image).stream()
                        .filter(e -> e.name.equals("Textual Data") && e.value.startsWith("Creation Time: "))
                        .findFirst().orElse(null);
                if (dateTaken != null) {
                    dateTaken.value = dateTaken.value.substring(15);
                }
                break;
            default:
                return "NO DATE";
        }
        
        if (dateTaken != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            try {
                return new SimpleDateFormat("MM-dd-yyyy").format(sdf.parse(dateTaken.value));
            } catch (ParseException ignored) {
            }
        }
        return "NO DATE";
    }
    
    /**
     * Returns the file type of a file.
     *
     * @param file The file.
     * @return The file type of the file.
     */
    public static String getFileType(File file) {
        return (file.getName().contains(".")) ?
               file.getName().substring(file.getName().lastIndexOf('.') + 1) : "";
    }
    
    /**
     * Extracts the metadata tags from a file.
     *
     * @param file      The file.
     * @param directory The metadata directory to get metadata from.
     * @return The list of metadata tags from the file, or an empty list if there is an error or the metadata cannot be found, or null if the file does not exist.
     */
    public static List<MetadataTag> getMetadata(File file, String directory) {
        if ((file == null) || !file.exists()) {
            return null;
        }
        
        List<MetadataTag> metadataTags = new ArrayList<>();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            
            for (Directory dir : metadata.getDirectories()) {
                if ((directory != null) && !dir.getName().equals(directory)) {
                    continue;
                }
                
                for (Tag tag : dir.getTags()) {
                    metadataTags.add(
                            new MetadataTag(dir.getName(), tag.getTagName(), tag.getDescription(), tag.getTagType()));
                }
                
                if (dir.hasErrors()) {
                    for (String error : dir.getErrors()) {
                        metadataTags.add(
                                new MetadataTag(dir.getName(), "ERROR", error, Integer.MIN_VALUE));
                    }
                }
            }
            
        } catch (Exception ignored) {
        }
        
        return metadataTags;
    }
    
    /**
     * Extracts the metadata tags from a file.
     *
     * @param file The file.
     * @return The list of metadata tags from the file, or an empty list if there is an error or the metadata cannot be found.
     * @see #getMetadata(File, String)
     */
    public static List<MetadataTag> getMetadata(File file) {
        return getMetadata(file, null);
    }
    
    /**
     * Extracts a metadata tag from a file.
     *
     * @param file      The file.
     * @param directory The metadata tag directory.
     * @param name      The metadata tag name.
     * @return The specified metadata tag from the file, or null if it does not exist.
     */
    public static MetadataTag getMetadataTag(File file, String directory, String name) {
        return getMetadata(file, directory).stream()
                .filter(e -> e.name.equals(name))
                .findFirst().orElse(null);
    }
    
    /**
     * Extracts a metadata tag from a file.
     *
     * @param file The file.
     * @param name The metadata tag name.
     * @return The specified metadata tag from the file, or null if it does not exist.
     * @see #getMetadataTag(File, String, String)
     */
    public static MetadataTag getMetadataTag(File file, String name) {
        return getMetadataTag(file, null, name);
    }
    
    
    //Inner Classes
    
    /**
     * Defines a Metadata Tag.
     */
    public static class MetadataTag {
        
        //Fields
        
        /**
         * The directory of the Metadata Tag.
         */
        public String directory;
        
        /**
         * The name of the Metadata Tag.
         */
        public String name;
        
        /**
         * The value of the Metadata Tag.
         */
        public String value;
        
        /**
         * The type of the Metadata Tag.
         */
        public int type;
        
        
        //Constructors
        
        /**
         * Creates a new Metadata Tag.
         *
         * @param directory The directory of the Metadata Tag.
         * @param name      The name of the Metadata Tag.
         * @param value     The value of the Metadata Tag.
         * @param type      The type of the Metadata Tag.
         */
        public MetadataTag(String directory, String name, String value, int type) {
            this.directory = directory;
            this.name = name;
            this.value = value;
            this.type = type;
        }
        
        /**
         * The default no-argument constructor for a Metadata Tag.
         */
        public MetadataTag() {
        }
        
        
        //Methods
        
        /**
         * Returns the string representation of a Metadata Tag.
         *
         * @return The string representation of a Metadata Tag.
         */
        @Override
        public String toString() {
            return "[" + directory + "] " + name + " : " + value;
        }
        
        /**
         * Determines if another Metadata Tag is equal to this Metadata Tag.
         *
         * @param o The other Metadata Tag.
         * @return Whether the other Metadata Tag is equal to this Metadata Tag or not.
         */
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MetadataTag)) {
                return false;
            }
            MetadataTag other = (MetadataTag) o;
            
            return directory.equals(other.directory) &&
                    name.equals(other.name) &&
                    value.equals(other.value) &&
                    (type == other.type);
        }
        
    }
    
}
