/*
 * File:    StableDiffusionProcess.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.stream.Collectors;

import commons.access.CmdLine;
import commons.access.Filesystem;

public class StableDiffusionProcess {
    
    //Constants
    
    private static final File BASE_SCRIPT = new File("resources", "stable-diffusion.bat");
    
    private static final File SCRIPT = new File("tmp", "stable-diffusion.bat");
    
    private static final float DEFAULT_SCALE = 7.5f;
    
    private static final int DEFAULT_ITERATIONS = 5;
    
    private static final int DEFAULT_WIDTH = 512;
    
    private static final int DEFAULT_HEIGHT = 512;
    
    
    //Fields
    
    public String prompt;
    
    public float scale;
    
    public int iterations;
    
    public int width;
    
    public int height;
    
    
    //Constructors
    
    public StableDiffusionProcess(String prompt, float scale, int iterations, int width, int height) {
        this.prompt = prompt;
        this.scale = scale;
        this.iterations = iterations;
        this.width = width;
        this.height = height;
    }
    
    public StableDiffusionProcess(String prompt) {
        this(prompt, DEFAULT_SCALE, DEFAULT_ITERATIONS, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    
    //Methods
    
    public void prepareScript() {
        Filesystem.writeLines(SCRIPT,
                Filesystem.readLines(BASE_SCRIPT).stream().map(e -> e
                                .replaceAll("^(?<indent>\\s*)set prompt_text=(?<oldValue>.*)$", ("$1set prompt_text=" + prompt))
                                .replaceAll("^(?<indent>\\s*)set guidance_scale=(?<oldValue>.*)$", ("$1set guidance_scale=" + scale))
                                .replaceAll("^(?<indent>\\s*)set num_iterations=(?<oldValue>.*)$", ("$1set num_iterations=" + iterations))
                                .replaceAll("^(?<indent>\\s*)set image_width=(?<oldValue>.*)$", ("$1set image_width=" + width))
                                .replaceAll("^(?<indent>\\s*)set image_height=(?<oldValue>.*)$", ("$1set image_height=" + height)))
                        .collect(Collectors.toList()));
    }
    
    public void execute() throws Exception {
        CmdLine.executeCmd(SCRIPT.getAbsolutePath().replace("\\", "/"));
    }
    
}