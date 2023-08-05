/*
 * File:    EbookUtils.java
 * Package: main.ebook
 * Author:  Zachary Gill
 */

package main.ebook;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import commons.access.CmdLine;
import commons.access.Filesystem;
import commons.object.string.StringUtility;

public final class EbookUtils {
    
    //Static Methods
    
    @SuppressWarnings("UnnecessaryLocalVariable")
    public static EbookMetadata getMetadata(File ebook) {
        String rawMetadata = extractMetadata(ebook);
        
        Map<String, String> metadataMap = parseMetadataMap(rawMetadata);
        metadataMap.put("_Raw", rawMetadata);
        metadataMap.put("_Source", ebook.getAbsolutePath());
        
        EbookMetadata metadata = wrapMetadata(metadataMap);
        return metadata;
    }
    
    public static String extractMetadata(File ebook, boolean export) {
        File opf = new File(ebook.getParentFile(), (ebook.getName() + ".opf"));
        if (export && opf.exists()) {
            return Filesystem.readFileToString(opf);
        }
        
        String cmd = "ebook-meta \"" + ebook.getAbsolutePath() + "\"" +
                (export ? (" --to-opf=\"" + opf.getAbsolutePath() + "\"") : "");
        
        System.out.println(cmd);
        return CmdLine.executeCmd(cmd);
    }
    
    public static String extractMetadata(File ebook) {
        return extractMetadata(ebook, false);
    }
    
    private static Map<String, String> parseMetadataMap(String response) {
        return StringUtility.splitLines(response).stream()
                .map(e -> e.split("\\s+:\\s+")).filter(e -> (e.length == 2))
                .map(e -> Map.entry(e[0].strip(), e[1].strip()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    private static EbookMetadata wrapMetadata(Map<String, String> metadataMap) {
        EbookMetadata metadata = new EbookMetadata(metadataMap);
        checkMetadataMap(metadataMap);
        return metadata;
    }
    
    private static List<String> checkMetadataMap(Map<String, String> metadataMap) {
        List<String> newTags = metadataMap.keySet().stream()
                .filter(e -> !EbookMetadata.KNOWN_META_TAGS.contains(e))
                .collect(Collectors.toList());
        
        if (!newTags.isEmpty()) {
            int g = 5;
        }
        return newTags;
    }
    
}
