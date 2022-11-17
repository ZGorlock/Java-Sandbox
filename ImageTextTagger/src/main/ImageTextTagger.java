/*
 * File:    ImageTextTagger.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;

import commons.access.Filesystem;
import main.tag.StableDiffusionParametersTag;
import main.tagger.PngTagger;

public class ImageTextTagger {
    
    //Constants
    
    private static final File inputDir = new File("E:/Downloads/work");
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
//        PngTagger.tagPng(new File(inputDir, "a.png"), "testKey", "testValue");
        
        tagOldStableDiffusionImages();
    }
    
    
    //Static Methods
    
    private static void tagOldStableDiffusionImages() throws Exception {
        final File conf = Filesystem.getFiles(inputDir, f -> f.getName().endsWith(".txt")).get(0);
        for (File image : Filesystem.getFiles(inputDir, f -> f.getName().endsWith(".png"))) {
            PngTagger.tagPng(image, StableDiffusionParametersTag.readConf(conf));
        }
    }
    
}
