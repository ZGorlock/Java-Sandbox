/*
 * File:    Training.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.util.Scanner;

public class Training {
    
    /**
     * Entry to run the program
     */
    public static void main(String[] args) {
        
        WaveRecording recording = new WaveRecording("data/text.wav");
        
        recording.start();
        
        Scanner s = new Scanner(System.in);
        String line = "";
        while (!line.equals("stop")) {
            line = s.nextLine();
        }
        
        recording.stop();
    }
}
