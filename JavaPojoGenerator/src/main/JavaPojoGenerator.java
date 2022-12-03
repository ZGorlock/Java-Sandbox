/*
 * File:    JavaPojoGenerator.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaPojoGenerator {
    
    //Constants
    
    private static final File INPUT = new File("data", "input.txt");
    
    private static final File OUTPUT = new File("data", "output.txt");
    
    private static final boolean GENERATE_FIELDS = true;
    
    private static final boolean GENERATE_GETTERS = true;
    
    private static final boolean GENERATE_SETTERS = true;
    
    private static final boolean GENERATE_JAVADOCS = true;
    
    private static final String INDENT = "    ";
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        List<String> input = Files.readAllLines(INPUT.toPath())
                .stream().map(e -> e.replaceAll("//.*$", "")).map(String::strip)
                .filter(e -> !e.isEmpty()).collect(Collectors.toList());
        
        Map<String, String> fields = loadFields(input);
        
        List<String> fieldOutput = generateFields(fields);
        List<String> getterOutput = generateGetters(fields);
        List<String> setterOutput = generateSetters(fields);
        
        List<String> output = Stream.of(fieldOutput, getterOutput, setterOutput).flatMap(Collection::stream)
                .map(e -> INDENT + e).collect(Collectors.toList());
        
        Files.write(OUTPUT.toPath(), output);
    }
    
    
    //Static Methods
    
    private static Map<String, String> loadFields(List<String> input) {
        Map<String, String> fields = new LinkedHashMap<>();
        
        StringBuilder javadoc = new StringBuilder();
        StringBuilder field = new StringBuilder();
        
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i).equals("/**")) {
                for (i = i + 1; i < input.size(); i++) {
                    if (input.get(i).equals("*/")) {
                        break;
                    }
                    javadoc.append((javadoc.length() == 0) ? "" : System.lineSeparator())
                            .append(input.get(i).replaceAll("\\*\\s*", ""));
                }
                continue;
            }
            
            field.append((field.length() == 0) ? "" : System.lineSeparator())
                    .append(input.get(i).replaceAll(";$", ""));
            
            if (input.get(i).endsWith(";")) {
                fields.put(field.toString(), javadoc.toString());
                
                field = new StringBuilder();
                javadoc = new StringBuilder();
            }
        }
        
        return fields;
    }
    
    private static List<String> generateFields(Map<String, String> fields) {
        List<String> fieldOutput = new ArrayList<>();
        if (!GENERATE_FIELDS) {
            return fieldOutput;
        }
        
        fieldOutput.add("");
        fieldOutput.add("");
        fieldOutput.add("//Fields");
        
        for (Map.Entry<String, String> field : fields.entrySet()) {
            fieldOutput.add("");
            
            if (GENERATE_JAVADOCS && !field.getValue().isBlank()) {
                fieldOutput.add("/**");
                fieldOutput.add(" * " + field.getValue());
                fieldOutput.add(" */");
            }
            
            fieldOutput.add(field.getKey() + ";");
        }
        
        return fieldOutput;
    }
    
    private static List<String> generateGetters(Map<String, String> fields) {
        List<String> getterOutput = new ArrayList<>();
        if (!GENERATE_GETTERS) {
            return getterOutput;
        }
        
        getterOutput.add("");
        getterOutput.add("");
        getterOutput.add("//Getters");
        
        for (Map.Entry<String, String> field : fields.entrySet()) {
            getterOutput.add("");
            
            String[] fieldParts = field.getKey().split(" ");
            String fieldName = fieldParts[fieldParts.length - 1];
            String fieldType = fieldParts[fieldParts.length - 2];
            
            if (GENERATE_JAVADOCS && !field.getValue().isBlank()) {
                getterOutput.add("/**");
                getterOutput.add(" * Returns " + field.getValue().substring(0, 1).toLowerCase() + field.getValue().substring(1));
                getterOutput.add(" *");
                getterOutput.add(" * @return " + field.getValue());
                getterOutput.add(" */");
            }
            
            getterOutput.add(field.getKey().replace(fieldName,
                    "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1) + "() {"));
            getterOutput.add(INDENT + "return " + fieldName + ";");
            getterOutput.add("}");
        }
        
        return getterOutput;
    }
    
    private static List<String> generateSetters(Map<String, String> fields) {
        List<String> setterOutput = new ArrayList<>();
        if (!GENERATE_SETTERS) {
            return setterOutput;
        }
        
        setterOutput.add("");
        setterOutput.add("");
        setterOutput.add("//Setters");
        
        for (Map.Entry<String, String> field : fields.entrySet()) {
            setterOutput.add("");
            
            String[] fieldParts = field.getKey().split(" ");
            String fieldName = fieldParts[fieldParts.length - 1];
            String fieldType = fieldParts[fieldParts.length - 2];
            
            if (GENERATE_JAVADOCS && !field.getValue().isBlank()) {
                setterOutput.add("/**");
                setterOutput.add(" * Sets " + field.getValue().substring(0, 1).toLowerCase() + field.getValue().substring(1));
                setterOutput.add(" *");
                setterOutput.add(" * @param " + fieldName + " " + field.getValue());
                setterOutput.add(" */");
            }
            
            setterOutput.add(field.getKey().replace(fieldType, "void").replace(fieldName,
                    "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1) + "(" + fieldType + " " + fieldName + ") {"));
            setterOutput.add(INDENT + "this." + fieldName + " = " + fieldName + ";");
            setterOutput.add("}");
        }
        
        return setterOutput;
    }
    
}
