/*
 * File:    WaveRecording.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 * Captures a WAV recording.
 */
public class WaveRecording {
    
    //Constants
    
    /**
     * The number of milliseconds to wait before returning the status of a started recording.
     */
    public static final int RECORDING_THREAD_STATUS_DELAY = 100;
    
    
    //Fields
    
    /**
     * The WAV file to store the recording to.
     */
    private File wavFile = null;
    
    /**
     * The line from which audio data is captured.
     */
    private TargetDataLine line = null;
    
    /**
     * The recording thread.
     */
    private Thread recording = null;
    
    
    //Constructors
    
    /**
     * The constructor for a WaveRecording.
     *
     * @param file The file to produce the recording in.
     */
    public WaveRecording(String file) {
        wavFile = new File(file);
    }
    
    
    //Methods
    
    /**
     * Opens the target data line and begins capturing the recording.
     *
     * @return Whether the recording was successfully started or not.
     */
    public boolean start() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            
            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Your microphone is not supported or your system does not allow audio capture");
                return false;
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            
            recording = new Thread(() -> {
                try {
                    // start capturing
                    AudioInputStream ais = new AudioInputStream(line);
                    
                    // start recording
                    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, wavFile);
                } catch (IOException e) {
                    System.out.println("There was an error capturing the recording"); //TODO stacktrace?
                    stop();
                }
            });
            
        } catch (LineUnavailableException e) {
            System.out.println("You do not have a microphone installed"); //TODO stacktrace?
            return false;
        }
        
        recording.start();
        
        try {
            Thread.sleep(RECORDING_THREAD_STATUS_DELAY);
        } catch (InterruptedException ignored) {
        }
        return recording.isAlive();
    }
    
    /**
     * Closes the target data line to finish capturing and recording.
     */
    public void stop() {
        line.stop();
        line.close();
        
        try {
            recording.join();
        } catch (InterruptedException e) {
            System.out.println("Unable to shutdown recording thread"); //TODO stacktrace?
        }
    }
    
    
    //Functions
    
    /**
     * Defines an audio format.
     *
     * @return The audio format.
     */
    private static AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
    
}
