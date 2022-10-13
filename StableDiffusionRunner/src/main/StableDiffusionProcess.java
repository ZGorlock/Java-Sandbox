/*
 * File:    StableDiffusionProcess.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
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
        final BiFunction<String, Map.Entry<String, String>, String> fieldReplacer = (String line, Map.Entry<String, String> field) ->
                line.replaceAll(("(?<=\\s)set\\s" + field.getKey() + "=.*$"), ("set " + field.getKey() + '=' + Matcher.quoteReplacement(field.getValue())));
        
        final Map<String, String> configuration = new HashMap<>();
        configuration.putAll(getFieldMap());
        configuration.putAll(StableDiffusionConstants.getFieldMap());
        
        Filesystem.writeLines(SCRIPT, Filesystem.readLines(BASE_SCRIPT).stream()
                .map(e -> configuration.entrySet().stream()
                        .reduce(Map.entry("", e), (line, field) ->
                                Map.entry("", fieldReplacer.apply(line.getValue(), field)))
                        .getValue())
                .collect(Collectors.toList()));
    }
    
    private Map<String, String> getFieldMap() {
        return Map.of(
                "prompt_text", prompt,
                "guidance_scale", String.valueOf(scale),
                "num_iterations", String.valueOf(iterations),
                "image_width", String.valueOf(width),
                "image_height", String.valueOf(height)
        );
    }
    
    public void execute() throws Exception {
        CmdLine.executeCmd(SCRIPT.getAbsolutePath().replace("\\", "/"));
    }
    
}