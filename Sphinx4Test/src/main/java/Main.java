/*
 * File:    Main.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import sun.security.krb5.Config;


public class Main
{
    
    
    public static void main(String[] args) throws Exception
    {
        Configuration configuration = new Configuration();

        // Set path to acoustic model.
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        // Set path to dictionary.
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        // Set language model.
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
        
        configuration.setUseGrammar(false);
    
    
        //Recognizer object, Pass the Configuration object
        LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
    
        //Start Recognition Process (The bool parameter clears the previous cache if true)
        recognizer.startRecognition(true);
    
        //Creating SpeechResult object
        SpeechResult result;
    
        //Check if recognizer recognized the speech
        while ((result = recognizer.getResult()) != null) {
            //recognizer.stopRecognition();
        
            //Get the recognized speech
            String command = result.getHypothesis();
            System.out.println(command);
            
//            recognizer.stopRecognition();
//            recognizer.startRecognition(true);
            
//            //Match recognized speech with our commands
//            if(command.equalsIgnoreCase("open file manager")) {
//                System.out.println("File Manager Opened!");
//            } else if (command.equalsIgnoreCase("close file manager")) {
//                System.out.println("File Manager Closed!");
//            } else if (command.equalsIgnoreCase("open browser")) {
//                System.out.println("Browser Opened!");
//            } else if (command.equalsIgnoreCase("close browser")) {
//                System.out.println("Browser Closed!");
//            }
            //recognizer.startRecognition(true);
        }
    
    }
    
}
