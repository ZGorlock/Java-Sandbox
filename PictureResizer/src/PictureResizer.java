/*
 * File:    PictureResizer.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import common.Filesystem;

public class PictureResizer {
    
//    private static File directory = new File("E:/Documents/Taxidermy/Specimens");
    private static File directory = new File("C:\\Users\\Zack\\Desktop\\New folder");
    
    public static void main(String[] args) throws Exception {
        List<File> pictures;
        if (directory != null) {
            List<File> files = Filesystem.getFilesRecursively(directory);
            pictures = new ArrayList<>();
            for (File f : files) {
                if (f.getName().toLowerCase().endsWith(".jpg")) {
                    pictures.add(f);
                }
            }
        } else {
            pictures = Filesystem.getFiles(new File("data"));
        }
        Filesystem.createDirectory(new File("output"));
        for (File picture : pictures) {
            BufferedImage image = ImageIO.read(picture);
            Image newImage = image.getScaledInstance(image.getWidth(), image.getHeight(), Image.SCALE_DEFAULT);
            image.getGraphics().drawImage(newImage, 0, 0, null);
            File output = new File("output", picture.getName().replaceAll("(\\.[JPG][jpg])+", ".jpg"));
            if (ImageIO.write(image, "JPG", output)) {
                if (directory != null) {
                    Filesystem.deleteFile(picture);
                    if (Filesystem.move(output, picture)) {
                        Filesystem.deleteFile(output);
                    }
                }
            }
        }
    }
    
}
