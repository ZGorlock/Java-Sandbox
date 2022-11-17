/*
 * File:    StableDiffusionParametersTag.java
 * Package: main.tag
 * Author:  Zachary Gill
 */

package main.tag;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.access.Filesystem;
import commons.lambda.function.checked.CheckedFunction;
import commons.object.string.StringUtility;

public class StableDiffusionParametersTag extends Tag {
    
    //Fields
    
    public String prompt;
    
    public String negativePrompt;
    
    public Integer steps;
    
    public String sampler;
    
    public Float cfgScale;
    
    public Long seed;
    
    public Integer width;
    
    public Integer height;
    
    public String modelHash;
    
    public Float denoisingStrength;
    
    public Integer clipSkip;
    
    public Integer ensd;
    
    public Integer firstPassWidth;
    
    public Integer firstPassHeight;
    
    
    //Constructors
    
    public StableDiffusionParametersTag(String text) {
        super("parameters", text);
    }
    
    public StableDiffusionParametersTag(String prompt, String negativePrompt, Integer steps, String sampler, Float cfgScale, Long seed, Integer width, Integer height, String modelHash, Float denoisingStrength, Integer clipSkip, Integer ensd, Integer firstPassWidth, Integer firstPassHeight) {
        this("");
        
        this.prompt = prompt;
        this.negativePrompt = negativePrompt;
        this.steps = steps;
        this.sampler = sampler;
        this.cfgScale = cfgScale;
        this.seed = seed;
        this.width = width;
        this.height = height;
        this.modelHash = modelHash;
        this.denoisingStrength = denoisingStrength;
        this.clipSkip = clipSkip;
        this.ensd = ensd;
        this.firstPassWidth = firstPassWidth;
        this.firstPassHeight = firstPassHeight;
        
        this.text = build();
    }
    
    
    //Methods
    
    public String build() {
        return String.join(System.lineSeparator(),
                Stream.of(
                        ((prompt != null) ? (prompt) : null),
                        ((negativePrompt != null) ? ("Negative prompt: " + negativePrompt) : null)
                ).filter(Objects::nonNull).collect(Collectors.joining(System.lineSeparator())),
                Stream.of(
                        ((steps != null) ? ("Steps: " + steps) : null),
                        ((sampler != null) ? ("Sampler: " + sampler) : null),
                        ((cfgScale != null) ? ("CFG scale: " + cfgScale) : null),
                        ((seed != null) ? ("Seed: " + seed) : null),
                        (((width != null) && (height != null)) ? ("Size: " + width + 'x' + height) : null),
                        ((modelHash != null) ? ("Model hash: " + modelHash) : null),
                        ((denoisingStrength != null) ? ("Denoising strength: " + denoisingStrength) : null),
                        ((clipSkip != null) ? ("Clip skip: " + clipSkip) : null),
                        ((ensd != null) ? ("ENSD: " + ensd) : null),
                        (((firstPassWidth != null) && (firstPassHeight != null)) ? ("First pass size: " + firstPassWidth + 'x' + firstPassHeight) : null)
                ).filter(Objects::nonNull).collect(Collectors.joining(", ")));
    }
    
    
    //Static Methods
    
    public static StableDiffusionParametersTag readConf(File logFile) {
        final Map<String, String> info = Filesystem.readLines(logFile).stream()
                .map(e -> Map.entry(e.replaceAll(":\\s+(.*)$", ""), e.replaceAll("^[^:]+:\\s+", "")))
                .filter(e -> !StringUtility.isNullOrBlank(e.getValue()))
                .collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue().strip()));
        
        return new StableDiffusionParametersTag(
                Optional.ofNullable(info.get("prompt")).map(String::strip).orElse(null),
                null,
                Optional.ofNullable(info.get("ddim steps")).map((CheckedFunction<String, Integer>) Integer::parseInt).orElse(30),
                Optional.ofNullable(info.get("sampler")).map(String::strip).orElse("plms"),
                Optional.ofNullable(info.get("scale")).map((CheckedFunction<String, Float>) Float::parseFloat).orElse(7.5f),
                Optional.ofNullable(info.get("custom seed")).map((CheckedFunction<String, Long>) Long::parseLong).orElse(null),
                Optional.ofNullable(info.get("width")).map((CheckedFunction<String, Integer>) Integer::parseInt).orElse(512),
                Optional.ofNullable(info.get("height")).map((CheckedFunction<String, Integer>) Integer::parseInt).orElse(512),
                "7460a6fa",
                null,
                null,
                null,
                null,
                null
        );
    }
    
}
