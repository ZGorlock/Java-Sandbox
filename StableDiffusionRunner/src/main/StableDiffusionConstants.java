/*
 * File:    StableDiffusionConstants.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.util.Map;
import java.util.Optional;

public final class StableDiffusionConstants {
    
    //Constants
    
    public static final String BASE_DIR = "%UserProfile%\\stable-diffusion";
    
    public static final String CONDA_DIR = "C:\\python\\Miniconda3";
    
    public static final String MODEL_DIR = "F:\\Datasets\\Neural Networks\\Stable Diffusion";
    
    public static final String OUTPUT_DIR = "%base_dir%\\output";
    
    public static final String CONDA_ENV = "ldm";
    
    public static final String BRANCH_NAME = "stable-diffusion-main";
    
    public static final String MODEL_FILE_NAME = "sd-v1-4.ckpt";
    
    public static final String CONFIG_FILE = null;
    
    public static final boolean USE_OPTIMIZED_SCRIPT = true;
    
    public static final String PROMPT_FILE = "%base_dir%\\prompt.txt";
    
    
    //Static Methods
    
    @SuppressWarnings("ConstantConditions")
    public static Map<String, String> getFieldMap() {
        return Map.of(
                "base_dir", Optional.ofNullable(BASE_DIR).orElse(""),
                "conda_dir", Optional.ofNullable(CONDA_DIR).orElse(""),
                "model_dir", Optional.ofNullable(MODEL_DIR).orElse(""),
                "output_dir", Optional.ofNullable(OUTPUT_DIR).orElse(""),
                "conda_env", Optional.ofNullable(CONDA_ENV).orElse(""),
                "branch_name", Optional.ofNullable(BRANCH_NAME).orElse(""),
                "model_file", Optional.ofNullable(MODEL_FILE_NAME).map(e -> ("%model_dir%\\" + e)).orElse(""),
                "config_file", Optional.ofNullable(CONFIG_FILE).orElse(""),
                "use_optimized_script", String.valueOf(USE_OPTIMIZED_SCRIPT),
                "prompt_file", Optional.ofNullable(PROMPT_FILE).orElse(""));
    }
    
}
