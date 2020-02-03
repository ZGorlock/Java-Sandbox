/*
 * File:    TTS.java
 * Package:
 * Author:  Zachary Gill
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.sound.sampled.AudioInputStream;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.AudioPlayer;
import org.slf4j.LoggerFactory;

public class TTS {
    
    org.slf4j.Logger logger = LoggerFactory.getLogger("TTS");
    
    private MaryInterface marytts;
    
    private AudioPlayer ap;
    
    public TTS(String voiceName) {
        try {
            marytts = new LocalMaryInterface();
            marytts.setVoice(voiceName);
            ap = new AudioPlayer();
        } catch (MaryConfigurationException ex) {
            ex.printStackTrace();
        }
    }
    
    public void say(String input) {
        try {
            AudioInputStream audio = marytts.generateAudio(input);
            
            ap.setAudio(audio);
            ap.start();
        } catch (SynthesisException ex) {
            System.err.println("Error saying phrase.");
        }
    }
    
    public static void main(String[] args) {
        //TextToSpeech tts = new TextToSpeech;
        long a = System.currentTimeMillis();
        
        try {
            MaryInterface marytts = new LocalMaryInterface();
            long b = System.currentTimeMillis();
            System.out.println((b - a) / 1000);
            
            Set<String> voices = marytts.getAvailableVoices();
            List<String> voiceList = new ArrayList<String>(voices);
            for (String voice : voiceList) {
                System.out.println(voice);
                marytts.setVoice(voice);
                AudioInputStream audio = marytts.generateAudio("This is a sample text to speech voice.");
                AudioPlayer player = new AudioPlayer(audio);
                player.start();
                player.join();
            }
            long c = System.currentTimeMillis();
            System.out.println((c - b) / 1000);
            
        } catch (Exception e) {
            System.out.println("Caught Exception");
        }
    }
}
