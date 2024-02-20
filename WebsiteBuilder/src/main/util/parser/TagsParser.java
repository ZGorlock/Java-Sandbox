/*
 * File:    TagsParser.java
 * Package: main.util.parser
 * Author:  Zachary Gill
 */

package main.util.parser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import commons.access.Filesystem;
import commons.access.Project;
import main.entity.shortcut.Shortcut;
import main.util.persistence.LocationUtil;
import main.util.persistence.VariableUtil;

public class TagsParser extends DataParser {
    
    //Static Methods
    
    public static Map<String, String> parseTags(File html) {
        return parseHtmlLinkMap(html, VariableUtil.get(0x774c), VariableUtil.get(0x80d4), true);
    }
    
    public static Map<String, Optional<File>> parseTagsFromSource(File html) {
        return Optional.ofNullable(html).filter(File::exists)
                .map(TagsParser::parseTags)
                .filter(e -> Filesystem.writeLines(
                        new File(html.getParentFile().getParentFile(), html.getName().replace(".html", ".txt")),
                        e.keySet()))
                .map(e -> Shortcut.createShortcuts(LocationUtil.getLocation(0xc45f, 0x19c1, 0x11ee), e))
                .orElseGet(HashMap::new);
    }
    
    public static Map<String, Optional<File>> parseTagsFromSource() {
        return Optional.ofNullable(VariableUtil.get(0x8779)).map(e -> new File(Project.DATA_DIR, e))
                .map(TagsParser::parseTagsFromSource)
                .orElseGet(HashMap::new);
    }
    
}
