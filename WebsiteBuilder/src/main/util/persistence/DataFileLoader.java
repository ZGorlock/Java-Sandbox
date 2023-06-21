/*
 * File:    DataFileLoader.java
 * Package: main.util.persistence
 * Author:  Zachary Gill
 */

package main.util.persistence;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.access.Filesystem;
import commons.lambda.function.unchecked.UncheckedFunction;
import commons.lambda.stream.collector.MapCollectors;

public class DataFileLoader {
    
    //Constants
    
    public static final String COMMENT = "#";
    
    public static final String SEPARATOR = "|";
    
    
    //Static Methods
    
    public static Optional<String> read(File file, boolean raw) {
        return Optional.ofNullable(file)
                .map(Filesystem::readFileToString)
                .filter(line -> (raw || filter(line)));
    }
    
    public static Optional<String> read(File file) {
        return read(file, false);
    }
    
    private static boolean filter(String data) {
        return Optional.ofNullable(data)
                .filter(e -> !e.isBlank())
                .filter(e -> !e.trim().startsWith(COMMENT))
                .isPresent();
    }
    
    public static Optional<List<String>> readLines(File file, boolean raw) {
        return Optional.ofNullable(file)
                .map(Filesystem::readLines)
                .map(lines -> lines.stream()
                        .filter(line -> (raw || filter(line)))
                        .collect(Collectors.toList()));
    }
    
    public static Optional<List<String>> readLines(File file) {
        return readLines(file, false);
    }
    
    public static Stream<String> streamLines(File file, boolean raw) {
        return readLines(file, raw)
                .stream().flatMap(Collection::stream);
    }
    
    public static Stream<String> streamLines(File file) {
        return streamLines(file, false);
    }
    
    public static Optional<List<String[]>> readLineData(File file, boolean raw, int dataParts) {
        return readLines(file, raw)
                .map(lines -> lines.stream()
                        .map(e -> e.split(Pattern.quote(SEPARATOR), -1))
                        .filter(e -> ((dataParts < 0) || (e.length == dataParts)))
                        .map(e -> (raw ? e : Arrays.stream(e).map(String::strip).toArray(String[]::new)))
                        .collect(Collectors.toList()));
    }
    
    public static Optional<List<String[]>> readLineData(File file, boolean raw) {
        return readLineData(file, raw, -1);
    }
    
    public static Optional<List<String[]>> readLineData(File file, int dataParts) {
        return readLineData(file, false, dataParts);
    }
    
    public static Optional<List<String[]>> readLineData(File file) {
        return readLineData(file, false);
    }
    
    public static Stream<String[]> streamLineData(File file, boolean raw, int dataParts) {
        return readLineData(file, raw, dataParts)
                .stream().flatMap(Collection::stream);
    }
    
    public static Stream<String[]> streamLineData(File file, boolean raw) {
        return streamLineData(file, raw, -1);
    }
    
    public static Stream<String[]> streamLineData(File file, int dataParts) {
        return streamLineData(file, false, dataParts);
    }
    
    public static Stream<String[]> streamLineData(File file) {
        return streamLineData(file, false);
    }
    
    public static <K, V> Optional<List<Map.Entry<K, V>>> readLineDataEntries(File file, boolean raw,
            UncheckedFunction<String[], K> keyMapper,
            UncheckedFunction<String[], V> valueMapper) {
        return readLineData(file, raw, 2)
                .map(lines -> lines.stream()
                        .map(e -> Map.entry(keyMapper.apply(e), valueMapper.apply(e)))
                        .collect(Collectors.toList()));
    }
    
    public static <K, V> Optional<List<Map.Entry<K, V>>> readLineDataEntries(File file,
            UncheckedFunction<String[], K> keyMapper,
            UncheckedFunction<String[], V> valueMapper) {
        return readLineDataEntries(file, false, keyMapper, valueMapper);
    }
    
    public static Optional<List<Map.Entry<String, String>>> readLineDataEntries(File file, boolean raw) {
        return readLineDataEntries(file, raw, e -> e[0], e -> e[1]);
    }
    
    public static Optional<List<Map.Entry<String, String>>> readLineDataEntries(File file) {
        return readLineDataEntries(file, false);
    }
    
    public static <K, V> Stream<Map.Entry<K, V>> streamLineDataEntries(File file, boolean raw,
            UncheckedFunction<String[], K> keyMapper,
            UncheckedFunction<String[], V> valueMapper) {
        return readLineDataEntries(file, raw, keyMapper, valueMapper)
                .stream().flatMap(Collection::stream);
    }
    
    public static <K, V> Stream<Map.Entry<K, V>> streamLineDataEntries(File file,
            UncheckedFunction<String[], K> keyMapper,
            UncheckedFunction<String[], V> valueMapper) {
        return streamLineDataEntries(file, false, keyMapper, valueMapper);
    }
    
    public static Stream<Map.Entry<String, String>> streamLineDataEntries(File file, boolean raw) {
        return streamLineDataEntries(file, raw, e -> e[0], e -> e[1]);
    }
    
    public static Stream<Map.Entry<String, String>> streamLineDataEntries(File file) {
        return streamLineDataEntries(file, false);
    }
    
    public static <K, V> Optional<Map<K, V>> readLineDataMap(File file, boolean raw,
            UncheckedFunction<Map.Entry<String, String>, K> keyMapper,
            UncheckedFunction<Map.Entry<String, String>, V> valueMapper) {
        return readLineDataEntries(file, raw)
                .map(entries -> entries.stream()
                        .collect(MapCollectors.toLinkedHashMap(keyMapper, valueMapper)));
    }
    
    public static <K, V> Optional<Map<K, V>> readLineDataMap(File file,
            UncheckedFunction<Map.Entry<String, String>, K> keyMapper,
            UncheckedFunction<Map.Entry<String, String>, V> valueMapper) {
        return readLineDataMap(file, false, keyMapper, valueMapper);
    }
    
    public static Optional<Map<String, String>> readLineDataMap(File file, boolean raw) {
        return readLineDataMap(file, raw, Map.Entry::getKey, Map.Entry::getValue);
    }
    
    public static Optional<Map<String, String>> readLineDataMap(File file) {
        return readLineDataMap(file, false);
    }
    
    public static <K, V> Optional<Map<K, List<V>>> readGroupedDataMap(File file, boolean raw,
            UncheckedFunction<Map.Entry<String, String>, K> keyMapper,
            UncheckedFunction<Map.Entry<String, String>, V> valueMapper) {
        return readLineDataEntries(file, raw)
                .map(entries -> entries.stream()
                        .map(e -> Map.entry(keyMapper.apply(e), valueMapper.apply(e)))
                        .collect(Collectors.collectingAndThen(
                                Collectors.groupingBy(Map.Entry::getKey),
                                groupedEntries -> groupedEntries.entrySet().stream()
                                        .map(locationEntry -> Map.entry(
                                                locationEntry.getKey(),
                                                locationEntry.getValue().stream().map(Map.Entry::getValue).collect(Collectors.toList())))
                                        .collect(MapCollectors.toHashMap()))));
    }
    
    public static <K, V> Optional<Map<K, List<V>>> readGroupedDataMap(File file,
            UncheckedFunction<Map.Entry<String, String>, K> keyMapper,
            UncheckedFunction<Map.Entry<String, String>, V> valueMapper) {
        return readGroupedDataMap(file, false, keyMapper, valueMapper);
    }
    
}
