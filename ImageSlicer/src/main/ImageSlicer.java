/*
 * File:    ImageSlicer.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

import commons.access.Filesystem;

public class ImageSlicer {
    
    //Constants
    
    private static final File DATA_DIR = new File("data");
    
    private static final File OUTPUT_DIR = new File(DATA_DIR, "out");
    
    private static final List<String> PICTURE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff", "webp");
    
    private static final Pattern IMAGE_TITLE_PATTERN = Pattern.compile("^(?<name>.+)_(?<h>\\d+)x(?<v>\\d+)\\.(?<ext>[^.]+)$");
    
    
    //Static Fields
    
    private static final int fixedHorizontal = 1;
    
    private static final int fixedVertical = 1;
    
    private static final int fixedSubWidth = 0;
    
    private static final int fixedSubHeight = 0;
    
    private static final int borderLeft = 0;
    
    private static final int borderTop = 0;
    
    private static final int borderRight = 0;
    
    private static final int borderBottom = 0;
    
    private static final boolean filterBlank = true;
    
    
    //Main Method
    
    public static void main(String[] args) {
        final List<File> imageFiles = listInputImages();
        
        for (File imageFile : imageFiles) {
            
            String imageTitle = imageFile.getName();
            int imageHorizontal = fixedHorizontal;
            int imageVertical = fixedVertical;
            
            final Matcher titleMatcher = IMAGE_TITLE_PATTERN.matcher(imageFile.getName());
            if (titleMatcher.matches()) {
                imageTitle = titleMatcher.group("name") + '.' + titleMatcher.group("ext");
                imageHorizontal = Integer.parseInt(titleMatcher.group("h"));
                imageVertical = Integer.parseInt(titleMatcher.group("v"));
            }
            
            final BufferedImage image = loadImage(imageFile);
            sliceImage(image, imageTitle, imageHorizontal, imageVertical);
        }
    }
    
    private static void sliceImage(BufferedImage image, String title, int horizontal, int vertical) {
        final int width = image.getWidth() - borderLeft - borderRight;
        final int height = image.getHeight() - borderTop - borderBottom;
        
        final int subWidth = (fixedSubWidth > 0) ? fixedSubWidth : (width / horizontal);
        final int subHeight = (fixedSubHeight > 0) ? fixedSubHeight : (height / vertical);
        
        for (int x = 0; x < horizontal; x++) {
            for (int y = 0; y < vertical; y++) {
                
                final BufferedImage subImage = new BufferedImage(subWidth, subHeight, BufferedImage.TYPE_4BYTE_ABGR);
                
                final Graphics2D subImageGraphics = (Graphics2D) subImage.getGraphics();
                subImageGraphics.drawImage(image,
                        0, 0, subWidth, subHeight,
                        ((x * subWidth) + borderLeft), ((y * subHeight) + borderTop),
                        (((x + 1) * subWidth) + borderLeft), (((y + 1) * subHeight) + borderTop),
                        new Color(0, 0, 0, 0), null);
                
                if (filterImage(subImage)) {
                    continue;
                }
                
                final File subImageFile = new File(OUTPUT_DIR, title
                        .replaceAll("\\.([^.]+)$", ("_" + x + "_" + y + ".$1")));
                saveImage(subImageFile, subImage);
            }
        }
    }
    
    private static boolean filterImage(BufferedImage image) {
        if (!filterBlank) {
            return false;
        }
        
        final int color = image.getRGB(0, 0);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRGB(x, y) != color) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static List<File> listInputImages() {
        return Filesystem.getFiles(DATA_DIR).stream()
                .filter(file -> Optional.of(file)
                        .map(Filesystem::getFileType)
                        .map(String::toLowerCase).map(fileType -> fileType.replace(".", ""))
                        .map(PICTURE_TYPES::contains)
                        .orElse(false))
                .collect(Collectors.toList());
    }
    
    private static BufferedImage loadImage(File imageFile) {
        try {
            return ImageIO.read(imageFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static boolean saveImage(File imageFile, BufferedImage imageData) {
        try {
            return imageFile.mkdirs() && ImageIO.write(imageData, Filesystem.getFileType(imageFile), imageFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
