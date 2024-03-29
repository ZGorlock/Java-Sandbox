/*
 * File:    Shortcut.java
 * Package: main.entity.shortcut
 * Author:  Zachary Gill
 */

package main.entity.shortcut;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import commons.access.Filesystem;
import commons.lambda.stream.collector.MapCollectors;
import commons.object.string.StringUtility;
import main.entity.base.LinkEntity;
import main.util.FilenameUtil;

public abstract class Shortcut extends LinkEntity {
    
    //Constants
    
    public static final String DEFAULT_SHORTCUT_EXTENSION = ".url";
    
    public static final Permission FIX_NAME = Permission.AUTO;
    
    public static final Permission FIX_FORMAT = Permission.AUTO;
    
    public static final Permission CLEAN_SHORTCUT = Permission.AUTO;
    
    public static final Pattern SHORTCUT_CONTENT_PATTERN = Pattern.compile("(?s)^.*\\bURL=(?<url>\\S+)\\s+.*$");
    
    public static final Pattern SHORTCUT_URL_PATTERN = Pattern.compile("^" +
            "(?<protocol>\\w+://|)" +
            "(?<domain>(?:\\.?+[\\w\\-]+)+)" +
            "(?<path>/(?:[^/&#?]+/)*?)" +
            "(?<id>[^/&#?]+)/?+(?:#/)?(?=[^/]*$)" +
            "(?<params>(?:[?&][^?&]+)*)" +
            "(?<fragment>(?:#[^#]*)?)" +
            "$");
    
    
    //Fields
    
    protected String url;
    
    protected String name;
    
    
    //Constructors
    
    protected Shortcut(File shortcut, boolean autoClean) {
        super(shortcut, false);
        
        this.url = shortcut.isDirectory() ? "" : Optional.ofNullable(readUrlFromShortcut(shortcut)).orElse("");
        this.name = getFileTitle();
        
        if (autoClean) {
            autoClean();
        }
    }
    
    protected Shortcut(File shortcut) {
        this(shortcut, CLEAN.auto());
    }
    
    
    //Methods
    
    @Override
    protected boolean permitFixName() {
        return super.permitFixName() && FIX_NAME.allowed();
    }
    
    @Override
    protected boolean doFixName() {
        if (!getFileName().equals(getShortcutName())) {
            rename(new File(getParentFile(), getShortcutName()));
        } else {
            return false;
        }
        return true;
    }
    
    @Override
    protected boolean permitFixFormat() {
        return super.permitFixFormat() && FIX_FORMAT.allowed();
    }
    
    @Override
    protected boolean doFixFormat() {
        final File defaultSource = FilenameUtil.setExtension(getSource(), DEFAULT_SHORTCUT_EXTENSION);
        if (".lnk".equals(getFileType())) {
            System.err.println(StringUtility.format("Should be converted to {}: '{}'", DEFAULT_SHORTCUT_EXTENSION, getSource().getAbsolutePath()));
        } else if (!DEFAULT_SHORTCUT_EXTENSION.equals(getFileType())) {
            System.err.println(StringUtility.format("Unrecognized format: '{}'", getSource().getAbsolutePath()));
        } else {
            return false;
        }
        return true;
    }
    
    @Override
    protected boolean permitCleanEntityFile() {
        return super.permitCleanEntityFile() && CLEAN_SHORTCUT.allowed() &&
                DEFAULT_SHORTCUT_EXTENSION.equals(getFileType());
    }
    
    @Override
    protected boolean doCleanEntityFile() {
        if (!getContent().equals(getShortcutText())) {
            write();
        } else {
            return false;
        }
        return true;
    }
    
    @Override
    public void write(String newUrl) {
        if (getContent().equals(getShortcutContentsFromUrl(newUrl))) {
            return;
        }
        if (!getUrl().equals(newUrl)) {
            if (getUrl().isEmpty()) {
                System.err.println(StringUtility.format("Writing: '{}' : '{}'", getSource().getAbsolutePath(), newUrl));
            } else {
                System.err.println(StringUtility.format("Re-Writing: '{}' : '{}' -> '{}'", getSource().getAbsolutePath(), getUrl(), newUrl));
            }
        }
        writeQuietly(getShortcutContentsFromUrl(newUrl));
    }
    
    public void write() {
        write(getUrl());
    }
    
    @Override
    public String toString() {
        return getUrl();
    }
    
    
    //Getters
    
    public String getUrl() {
        return url;
    }
    
    public String getName() {
        return name;
    }
    
    public String getShortcutText() {
        return getShortcutContentsFromUrl(getUrl());
    }
    
    public String getShortcutName() {
        return getName() + DEFAULT_SHORTCUT_EXTENSION;
    }
    
    
    //Static Methods
    
    public static String readUrlFromShortcut(File shortcut) {
        return Optional.ofNullable(shortcut)
                .filter(Filesystem::exists)
                .filter(e -> e.getName().toLowerCase().endsWith(DEFAULT_SHORTCUT_EXTENSION))
                .map(Shortcut::readContent)
                .map(Shortcut::getUrlFromShortcutContents)
                .orElse(null);
    }
    
    public static String getUrlFromShortcutContents(String shortcutContents) {
        return Optional.ofNullable(shortcutContents)
                .filter(e -> !StringUtility.isNullOrBlank(e))
                .map(SHORTCUT_CONTENT_PATTERN::matcher).filter(Matcher::matches)
                .map(e -> e.group("url"))
                .orElse(null);
    }
    
    public static String getShortcutContentsFromUrl(String url) {
        return "[InternetShortcut]\nURL=" + url + "\n";
    }
    
    public static List<File> findShortcutsInFolder(File shortcutDir) {
        return Filesystem.getFiles(shortcutDir,
                f -> f.getName().endsWith(DEFAULT_SHORTCUT_EXTENSION));
    }
    
    public static List<File> findShortcutsInFolderRecursively(File shortcutDir) {
        return Filesystem.getFilesRecursively(shortcutDir,
                f -> f.getName().endsWith(DEFAULT_SHORTCUT_EXTENSION));
    }
    
    public static List<File> findShortcutDirectoriesInFolder(File shortcutDir) {
        return Filesystem.getDirsRecursively(shortcutDir,
                d -> true);
    }
    
    public static List<File> findShortcutDirectoriesInFolderRecursively(File shortcutDir) {
        return Filesystem.getDirs(shortcutDir,
                d -> true);
    }
    
    public static File createShortcut(File dest, String url) {
        return ((!dest.exists() || (dest.length() == 0)) &&
                Filesystem.writeStringToFile(dest, getShortcutContentsFromUrl(url))) ? dest : null;
    }
    
    public static Map<String, Optional<File>> createShortcuts(File dir, Map<String, String> linkMap) {
        final Map<String, String> existingLinks = findShortcutsInFolder(dir).stream()
                .collect(MapCollectors.toLinkedHashMap(
                        e -> e.getName().replace(DEFAULT_SHORTCUT_EXTENSION, ""),
                        Shortcut::readUrlFromShortcut));
        
        final Map<String, String> newLinks = linkMap.entrySet().stream()
                .filter(link -> Optional.ofNullable(existingLinks)
                        .map(currentMap -> currentMap.get(link.getKey()))
                        .map(currentUrl -> !currentUrl.equals(link.getValue()) &&
                                Filesystem.renameFile(
                                        new File(dir, (link.getKey() + DEFAULT_SHORTCUT_EXTENSION)),
                                        new File(dir, (getShortcutId(currentUrl) + DEFAULT_SHORTCUT_EXTENSION))))
                        .orElse(true))
                .filter(link -> Optional.ofNullable(existingLinks)
                        .map(Map::entrySet).stream().flatMap(Collection::stream)
                        .filter(current -> current.getValue().equals(link.getValue()))
                        .findFirst()
                        .map(current -> !current.getKey().equals(link.getKey()) &&
                                !Filesystem.renameFile(
                                        new File(dir, (current.getKey() + DEFAULT_SHORTCUT_EXTENSION)),
                                        new File(dir, (link.getKey() + DEFAULT_SHORTCUT_EXTENSION))))
                        .orElse(true))
                .collect(MapCollectors.toLinkedHashMap());
        
        return newLinks.entrySet().stream()
                .map(e -> Map.entry(e.getKey(),
                        Optional.of(e.getValue())
                                .map(e2 -> new File(dir, (e.getKey() + DEFAULT_SHORTCUT_EXTENSION)))
                                .map(e2 -> Shortcut.createShortcut(e2, e.getValue()))))
                .collect(MapCollectors.toLinkedHashMap());
    }
    
    public static String getShortcutId(String shortcutUrl) {
        return Optional.ofNullable(shortcutUrl)
                .filter(e -> !StringUtility.isNullOrBlank(e))
                .map(SHORTCUT_URL_PATTERN::matcher).filter(Matcher::matches)
                .map(e -> e.group("id")).filter(e -> !StringUtility.isNullOrBlank(e))
                .orElse(null);
    }
    
    public static String getShortcutId(File shortcutFile) {
        return getShortcutId(readUrlFromShortcut(shortcutFile));
    }
    
    public static List<String> enumerateShortcutUrls(List<File> shortcuts) {
        return shortcuts.stream()
                .map(Shortcut::readUrlFromShortcut)
                .filter(Objects::nonNull).distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }
    
    public static List<String> enumerateShortcutUrlsInFolder(File shortcutDir) {
        return enumerateShortcutUrls(findShortcutsInFolder(shortcutDir));
    }
    
    public static List<String> enumerateShortcutUrlsInFolderRecursively(File shortcutDir) {
        return enumerateShortcutUrls(findShortcutsInFolderRecursively(shortcutDir));
    }
    
}
