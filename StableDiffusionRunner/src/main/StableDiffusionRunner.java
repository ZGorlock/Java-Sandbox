/*
 * File:    StableDiffusionRunner.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.util.List;

public class StableDiffusionRunner {
    
    //Static Fields
    
    private static final List<StableDiffusionProcess> processes = List.of(
            new StableDiffusionProcess("prompt",
                    7.0f, 10, 512, 512)

//            , new StableDiffusionProcess("another prompt", 
//                    15.0f, 2, 512, 768)
//            , new StableDiffusionProcess("an even other prompt", 
//                    10.0f, 1, 768, 512)
    );
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        for (StableDiffusionProcess process : processes) {
            run(process);
        }
    }
    
    
    //Static Methods
    
    private static void run(StableDiffusionProcess process) throws Exception {
        process.prepareScript();
        process.execute();
    }
    
}
