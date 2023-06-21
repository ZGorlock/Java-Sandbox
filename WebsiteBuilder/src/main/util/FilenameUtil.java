/*
 * File:    FilenameUtil.java
 * Package: main.util
 * Author:  Zachary Gill
 */

package main.util;

import java.io.File;
import java.text.Normalizer;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.math.number.BoundUtility;

public final class FilenameUtil {
    
    //Constants
    
    /**
     * A map of reserved filename characters to their replacement substitutions.
     * <pre>
     * | Source   | Target    | Unicode   |
     * |----------|-----------|-----------|
     * | NUL      |           |           |
     * | \        | ＼        | U+FF3C    |
     * | /        | ／        | U+FF0F    |
     * | :        | ：        | U+FF1A    |
     * | *        | ＊        | U+FF0A    |
     * | ?        | ？        | U+FF1F    |
     * | "        | ＂        | U+FF02    |
     * | <        | ＜        | U+FF1C    |
     * | >        | ＞        | U+FF1E    |
     * | |        | ｜        | U+FF5C    |
     * </pre>
     */
    public static final Map<String, String> RESERVED_CHAR_REPLACEMENTS = Stream.concat(
                    Stream.of(Map.entry("\0", "")),
                    Stream.of('\\', '/', ':', '*', '?', '\"', '<', '>', '|')
                            .map(c -> Map.entry(String.valueOf(c), String.valueOf(convertAsciiToFullWidthUnicode(c)))))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    
    
    //Static Methods
    
    public static String cleanTitle(String title) {
        return Optional.ofNullable(title)
                .map(FilenameUtil::normalizeTitle)
                .map(FilenameUtil::replaceReservedFilenameCharacters)
                .map(FilenameUtil::normalizeSpecialCharacters)
                .orElse(null);
    }
    
    public static String restoreTitle(String title) {
        return Optional.ofNullable(title)
                .map(FilenameUtil::normalizeTitle)
                .map(FilenameUtil::restoreReservedFilenameCharacters)
                .map(FilenameUtil::revertCustomTitleAdjustments)
                .orElse(null);
    }
    
    public static String normalizeTitle(String title) {
        return Optional.ofNullable(title).orElse("")
                .replace("–", "-")
                .replaceAll("(?<!\\s)-\\s", ": ")
                .replaceAll("[`’]", "'")
                .replaceAll("\\s+", " ")
                .strip();
    }
    
    public static String formatSearchTitle(String title) {
        return Optional.ofNullable(title)
                .map(FilenameUtil::restoreTitle)
                .map(e -> e.replaceAll("[^a-zA-Z0-9\\s\\-&'.,!:]", ""))
                .map(String::strip)
                .map(e -> e.replaceAll("\\s+", "+"))
                .orElse(null);
    }
    
    public static String performCustomTitleAdjustments(String title) {
        return Optional.ofNullable(title).orElse("")
                .replace("<->", "↔").replace("->", "→").replace("<-", "←");
    }
    
    public static String revertCustomTitleAdjustments(String title) {
        return Optional.ofNullable(title).orElse("")
                .replace("↔", "<->").replace("→", "->").replace("←", "<-");
    }
    
    public static String replaceReservedFilenameCharacters(String title) {
        return RESERVED_CHAR_REPLACEMENTS.entrySet().stream()
                .reduce(title, (s, e) -> s.replace(e.getKey(), e.getValue()), (s1, s2) -> s1);
    }
    
    public static String restoreReservedFilenameCharacters(String title) {
        return RESERVED_CHAR_REPLACEMENTS.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .reduce(title, (s, e) -> s.replace(e.getValue(), e.getKey()), (s1, s2) -> s1);
    }
    
    public static String stripReservedFilenameCharacters(String title) {
        return RESERVED_CHAR_REPLACEMENTS.entrySet().stream()
                .reduce(title, (s, e) -> s.replace(e.getKey(), ""), (s1, s2) -> s1);
    }
    
    public static String normalizeSpecialCharacters(String title) {
        return Normalizer.normalize(Optional.ofNullable(title).orElse(""), Normalizer.Form.NFC)
                .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS}+", "")
                .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS_SUPPLEMENT}+", "")
                .strip();
    }
    
    private static String stripNonAsciiCharacters(String title) {
        return Optional.ofNullable(title).orElse("")
                .replaceAll("[\r\n\t \\s]+", " ")
                .replaceAll("\\p{Cntrl}&&[^\r\n\t]", "")
                .replaceAll("[^\\x00-\\xFF]", "")
                .strip();
    }
    
    private static Character convertAsciiToFullWidthUnicode(Character asciiChar) {
        return Optional.ofNullable(asciiChar)
                .map(Integer::valueOf)
                .filter(i -> BoundUtility.inBounds(i, 0x21, 0x7E))
                .map(i -> (i + 0xFEE0))
                .map(i -> (char) i.intValue())
                .orElse(asciiChar);
    }
    
    public static String normalizeFilename(String title) {
        return Optional.ofNullable(title)
                .map(FilenameUtil::normalizeSpecialCharacters)
                .map(FilenameUtil::stripReservedFilenameCharacters)
                .map(FilenameUtil::stripNonAsciiCharacters)
                .orElse(null);
    }
    
    public static String getTitle(File file) {
        return Optional.ofNullable(file)
                .map(File::getName)
                .map(e -> e.replaceAll("\\.[^.]+$", ""))
                .orElse(null);
    }
    
    public static String getExtension(File file) {
        return Optional.ofNullable(file)
                .map(File::getName)
                .map(e -> (e.contains(".") ? e.replaceAll("^.*(\\.[^.]+)$", "$1") : ""))
                .map(String::toLowerCase)
                .orElse(null);
    }
    
    public static File setExtension(File file, String extension) {
        return new File(file.getParentFile(), file.getName()
                .replaceAll("\\.[^.]+$", extension));
    }
    
}
