/*
 * File:    CategoriesParser.java
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

public class CategoriesParser extends DataParser {
    
    //Static Methods
    
    public static Map<String, String> parseCategories(File html) {
        return parseHtmlLinkMap(html, VariableUtil.get(0x4b3b), VariableUtil.get(0x80d4), true);
    }
    
    public static Map<String, Optional<File>> parseCategoriesFromSource(File html) {
        return Optional.ofNullable(html).filter(File::exists)
                .map(CategoriesParser::parseCategories)
                .filter(e -> Filesystem.writeLines(
                        new File(html.getParentFile().getParentFile(), html.getName().replace(".html", ".txt")),
                        e.keySet()))
                .map(e -> Shortcut.createShortcuts(LocationUtil.getLocation(0xc45f, 0x19c1, 0x9a2a), e))
                .orElseGet(HashMap::new);
    }
    
    public static Map<String, Optional<File>> parseCategoriesFromSource() {
        return Optional.ofNullable(VariableUtil.get(0xfe59)).map(e -> new File(Project.DATA_DIR, e))
                .map(CategoriesParser::parseCategoriesFromSource)
                .orElseGet(HashMap::new);
    }
    
}
