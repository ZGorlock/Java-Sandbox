/*
 * File:    SphinxTraining.java
 * Package:
 * Author:  Zachary Gill
 */

/**
 * Performs training for Sphinx speech recognition.
 */
public class SphinxTraining {
    
    //Fields
    
    private String sphinxDirectory = "";
    
    private String transcriptionFile = "";
    
    private String fileIdsFile = "";
    
    
    //Constructors
    
    public SphinxTraining(String sphinxDirectory, String transcriptionFile, String fileIdsFile) {
        this.sphinxDirectory = sphinxDirectory;
        this.transcriptionFile = transcriptionFile;
        this.fileIdsFile = fileIdsFile;
    }
    
}
