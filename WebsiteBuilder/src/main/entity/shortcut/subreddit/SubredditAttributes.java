/*
 * File:    SubredditAttributes.java
 * Package: main.entity.shortcut.subreddit
 * Author:  Zachary Gill
 */

package main.entity.shortcut.subreddit;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.lambda.stream.collector.MapCollectors;
import commons.object.string.StringUtility;
import main.entity.base.Entity;

public class SubredditAttributes {
    
    //Enums
    
    public enum Attribute {
        MULTI,
        HIDDEN,
        FAVORITE,
        BANNED,
        PRIVATE,
        ALIVE
    }
    
    
    //Fields
    
    protected final Map<Attribute, Boolean> attributeMap = Arrays.stream(Attribute.values())
            .collect(MapCollectors.mapEachTo(() -> false));
    
    
    //Constructors
    
    protected SubredditAttributes(Entity subredditEntity) {
        final String entityPath = Optional.ofNullable(subredditEntity.getSource())
                .map(source -> Optional.of(source)
                        .filter(File::isFile).map(File::getParentFile)
                        .orElse(source))
                .map(StringUtility::fileString).orElse("");
        
        this.attributeMap.put(Attribute.MULTI, subredditEntity.getSource().isFile() && subredditEntity.getFileName().startsWith(Entity.META_PREFIX));
        this.attributeMap.put(Attribute.HIDDEN, entityPath.contains("/" + Entity.META_PREFIX));
        this.attributeMap.put(Attribute.FAVORITE, entityPath.contains("/" + SubredditCategory.FAVORITE_FOLDER_NAME));
        this.attributeMap.put(Attribute.BANNED, entityPath.contains("/" + SubredditCategory.BANNED_FOLDER_NAME));
        this.attributeMap.put(Attribute.PRIVATE, entityPath.contains("/" + SubredditCategory.PRIVATE_FOLDER_NAME));
        this.attributeMap.put(Attribute.ALIVE, (!attributeMap.get(Attribute.BANNED) && !attributeMap.get(Attribute.PRIVATE)));
    }
    
    
    //Methods
    
    public String titleTag() {
        return Stream.of(Attribute.FAVORITE, Attribute.BANNED, Attribute.PRIVATE)
                .filter(attributeMap::get)
                .map(Enum::name).map(String::toLowerCase)
                .map(e -> ('[' + e + ']'))
                .collect(Collectors.joining(" "));
    }
    
    //Getters
    
    public Map<Attribute, Boolean> getAttributeMap() {
        return attributeMap;
    }
    
    public boolean isMulti() {
        return getAttributeMap().get(Attribute.MULTI);
    }
    
    public boolean isHidden() {
        return getAttributeMap().get(Attribute.HIDDEN);
    }
    
    public boolean isFavorite() {
        return getAttributeMap().get(Attribute.FAVORITE);
    }
    
    public boolean isBanned() {
        return getAttributeMap().get(Attribute.BANNED);
    }
    
    public boolean isPrivate() {
        return getAttributeMap().get(Attribute.PRIVATE);
    }
    
    public boolean isAlive() {
        return getAttributeMap().get(Attribute.ALIVE);
    }
    
}
