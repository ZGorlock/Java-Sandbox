/*
 * File:    PngTagger.java
 * Package: main.tagger
 * Author:  Zachary Gill
 */

package main.tagger;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import commons.access.Filesystem;
import main.tag.Tag;
import org.w3c.dom.Node;

public class PngTagger {
    
    //Static Methods
    
    public static void tagPng(File image, Node tag, File output) throws Exception {
        if (output == null) {
            output = new File(new File(image.getParentFile(), "out"), image.getName());
            Filesystem.createDirectory(output.getParentFile());
        }
        
        try (FileInputStream fileInputStream = new FileInputStream(image);
             FileOutputStream fileOutputStream = new FileOutputStream(output)) {
            
            final ImageInputStream imageInputStream = ImageIO.createImageInputStream(fileInputStream);
            final ImageReader reader = ImageIO.getImageReaders(imageInputStream).next();
            reader.setInput(imageInputStream, true);
            
            final IIOMetadata metadata = reader.getImageMetadata(0);
            metadata.mergeTree(metadata.getNativeMetadataFormatName(), tag);
            
            final IIOMetadata streamMetadata = reader.getStreamMetadata();
            
            final BufferedImage data = reader.read(0);
            imageInputStream.flush();
            
            final ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(fileOutputStream);
            final ImageWriter writer = ImageIO.getImageWriter(reader);
            writer.setOutput(imageOutputStream);
            
            final ImageWriteParam writeParams = writer.getDefaultWriteParam();
            writeParams.setCompressionMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
            
            writer.write(streamMetadata, new IIOImage(data, null, metadata), writeParams);
            writer.dispose();
            imageOutputStream.flush();
        }
        
        final Map<String, FileTime> dates = Filesystem.readDates(image);
        Filesystem.writeDates(output, dates);
    }
    
    public static void tagPng(File image, Node tag) throws Exception {
        tagPng(image, tag, null);
    }
    
    public static void tagPng(File image, Tag tag, File output) throws Exception {
        tagPng(image, tag.node(), output);
    }
    
    public static void tagPng(File image, Tag tag) throws Exception {
        tagPng(image, tag.node(), null);
    }
    
    public static void tagPng(File image, String tagKeyword, String tagText, File output) throws Exception {
        tagPng(image, new Tag(tagKeyword, tagText), output);
    }
    
    public static void tagPng(File image, String tagKeyword, String tagText) throws Exception {
        tagPng(image, tagKeyword, tagText, null);
    }
    
}
