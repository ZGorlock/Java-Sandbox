/*
 * File:    FreeTTS.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */

import java.beans.PropertyVetoException;
import java.util.Locale;
import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.EngineStateError;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;

public class FreeTTS
{
    SynthesizerModeDesc desc;
    Synthesizer synthesizer;
    Voice voice;
    
    public void init(String voiceName) throws EngineException, EngineStateError {
        if (desc == null) {
            System.setProperty("freetts.voices",
                    "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
                    //"com.sun.speech.freetts.en.us.cmu_time_awb.AlanVoiceDirectory");
                    //"de.dfki.lt.freetts.en.us.MbrolaVoiceDirectory");
            desc = new SynthesizerModeDesc(Locale.US);
            Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
            synthesizer = Central.createSynthesizer(desc);
            synthesizer.allocate();
            synthesizer.resume();
            SynthesizerModeDesc smd = (SynthesizerModeDesc) synthesizer.getEngineModeDesc();
            Voice[] voices = smd.getVoices();
            Voice voice = null;
            for (int i = 0; i < voices.length; i++) {
                System.out.println(voices[i].getName());
                if (voices[i].getName().equals(voiceName)) {
                    voice = voices[i];
                    break;
                }
            }
            synthesizer.getSynthesizerProperties().setVoice(voice);
        }
    }
    
    public void terminate() throws EngineException, EngineStateError {
        synthesizer.deallocate();
    }
    
    public void doSpeak(String speakText) throws EngineException, AudioException, IllegalArgumentException, InterruptedException {
        synthesizer.speakPlainText(speakText, null);
        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
    }
    
    public static void main(String[] args) throws Exception {
        FreeTTS su = new FreeTTS();
        su.init("kevin16");
        su.doSpeak("This is an example text to speech voice! Testing things like ABC, and 50%, it is 5:30 pm, anti-aliasing");
        su.terminate();
    }
}