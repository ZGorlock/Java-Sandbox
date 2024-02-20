/*
 * File:    CollectionParser.java
 * Package: main.util.parser
 * Author:  Zachary Gill
 */

package main.util.parser;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import commons.access.Filesystem;
import commons.access.Project;
import commons.lambda.stream.collector.MapCollectors;
import main.util.persistence.VariableUtil;

public class CollectionParser extends DataParser {
    
    //Static Methods
    
    public static Map<String, String> parseCollection(File html) {
        return parseHtmlLinkMap(html, VariableUtil.get(0x119f), VariableUtil.get(0x80d4));
    }
    
    public static Map<String, String> parseCollectionFromSource(File html) {
        return Optional.ofNullable(html).filter(File::exists)
                .map(CollectionParser::parseCollection)
                .filter(e -> Filesystem.writeLines(
                        new File(html.getParentFile().getParentFile(), html.getName().replace(".html", ".txt")),
                        e.keySet()))
                .orElseGet(HashMap::new);
    }
    
    public static Map<String, Map<String, String>> parseCollectionsFromSource() {
        return Optional.ofNullable(VariableUtil.get(0xf606)).map(e -> new File(Project.DATA_DIR, e))
                .map(Filesystem::getFiles)
                .stream().flatMap(Collection::stream)
                .map(f -> Map.entry(f.getName(), parseCollectionFromSource(f)))
                .collect(MapCollectors.toLinkedHashMap());
    }
    
}
