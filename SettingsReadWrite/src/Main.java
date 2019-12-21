/*
 * File:    Main.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */

import javax.management.InvalidAttributeValueException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;

public class Main
{
    
    public static void main(String[] args) {
    
    }
    
    
    
    /**
     * Loads settings from a setting file.
     *
     * @param settingFile The setting file.
     * @return The loaded settings.
     *
     * @throws InvalidAttributeValueException When there is a problem with the Settings file.
     */
    public static SettingGroup readSettingsFromFile(String settingFile) throws InvalidAttributeValueException
    {
        File file = new File(settingFile);
        if (!file.exists()) {
            return new SettingGroup();
        }
        
        try {
            JSONParser parser = new JSONParser();
            JSONObject settingData = (JSONObject) parser.parse(new FileReader(settingFile));
            
            if ((settingData.get("name") == null) || (settingData.get("settings") == null)) {
                throw new NoSuchElementException();
            }
            
            String name = (String) settingData.get("name");
            JSONArray settingList = (JSONArray) settingData.get("settings");
            
            SettingGroup settings = readSettingsFromGroup(settingList);
            settings.setName(name);
            return settings;
            
        } catch (FileNotFoundException e) {
            logger.error("Setting file does not exist: {}", settingFile);
            logger.error(DLA.stackTrace(e));
            throw new InvalidAttributeValueException();
        } catch (IOException e) {
            logger.error("Setting file could not be opened: {}", settingFile);
            logger.error(DLA.stackTrace(e));
            throw new InvalidAttributeValueException();
        } catch (ParseException e) {
            logger.error("Setting file could not be parsed: {}", settingFile);
            logger.error(DLA.stackTrace(e));
            throw new InvalidAttributeValueException();
        } catch (NoSuchElementException e) {
            logger.error("Setting file does not contain all necessary elements: {}", settingFile);
            logger.error(DLA.stackTrace(e));
            throw new InvalidAttributeValueException();
        }
    }
    
    /**
     * Loads settings from a setting group.
     *
     * @param settingsArray The JSON settings array.
     * @return The loaded settings.
     *
     * @throws NoSuchElementException When there is a problem with the Settings file.
     */
    public static SettingGroup readSettingsFromGroup(JSONArray settingsArray) throws NoSuchElementException
    {
        SettingGroup settings = new SettingGroup();
        
        for (Object settingArrayElement : settingsArray) {
            JSONObject settingElement = (JSONObject) settingArrayElement;
            
            if (settingElement.get("settings") == null) { //setting
                if ((settingElement.get("name") == null) || (settingElement.get("type") == null)) {
                    throw new NoSuchElementException();
                }
                
                String name = (String) settingElement.get("name");
                String type = (String) settingElement.get("type");
                String value = (settingElement.get("value") != null) ? (String) settingElement.get("value") : "";
                
                settings.addSetting(new Setting(name, type, value));
                
            } else { //setting group
                if ((settingElement.get("name") == null)) {
                    throw new NoSuchElementException();
                }
                
                String name = (String) settingElement.get("name");
                JSONArray elementSettings = (JSONArray) settingElement.get("settings");
                
                SettingGroup subSettings = readSettingsFromGroup(elementSettings);
                subSettings.setName(name);
                
                settings.addSettingGroup(subSettings);
            }
        }
        
        return settings;
    }
    
    /**
     * Loads a requested Setting from a setting file.
     *
     * @param settingFile The setting file.
     * @param settingName The name of the requested Setting.
     * @return The requested Setting, or null if it does not exist.
     */
    public static Setting readSettingFromFile(String settingFile, String settingName)
    {
        File file = new File(settingFile);
        if (!file.exists()) {
            return null;
        }
        
        try {
            JSONParser parser = new JSONParser();
            JSONObject settingData = (JSONObject) parser.parse(new FileReader(settingFile));
            
            if (settingData.get("settings") == null) {
                throw new NoSuchElementException();
            }
            JSONArray settingList = (JSONArray) settingData.get("settings");
            
            return readSettingFromGroup(settingList, settingName);
            
        } catch (IOException | ParseException | NoSuchElementException ignored) {
        }
        
        return null;
    }
    
    /**
     * Loads a requested Setting from a setting group.
     *
     * @param settingsArray The JSON settings array.
     * @param settingName  The name of the requested Setting.
     * @return The requested Setting, or null if it does not exist.
     */
    public static Setting readSettingFromGroup(JSONArray settingsArray, String settingName)
    {
        try {
            for (Object settingArrayElement : settingsArray) {
                JSONObject settingElement = (JSONObject) settingArrayElement;
                
                if (settingElement.get("settings") == null) { //setting
                    if ((settingElement.get("name") == null) || (settingElement.get("type") == null)) {
                        throw new NoSuchElementException();
                    }
                    
                    String name = (String) settingElement.get("name");
                    String type = (String) settingElement.get("type");
                    String value = (settingElement.get("value") != null) ? (String) settingElement.get("value") : "";
                    
                    if (name.equals(settingName)) {
                        return new Setting(name, type, value);
                    }
                    
                } else { //setting group
                    JSONArray elementSettings = (JSONArray) settingElement.get("settings");
                    
                    Setting ret = readSettingFromGroup(elementSettings, settingName);
                    if (ret != null) {
                        return ret;
                    }
                }
            }
            
        } catch (NoSuchElementException ignored) {
        }
        
        return null;
    }
    
    /**
     * Prints settings to a setting file.
     *
     * @param settings    The settings to print to the setting file.
     * @param settingFile The setting file.
     */
    public static void printSettingsToFile(SettingGroup settings, String settingFile)
    {
        File file = new File(settingFile);
        if (!file.exists()) {
            Filesystem.createFile(file);
        }
        
        if (!Filesystem.writeStringToFile(file, printSettingGroupToString(settings))) {
            logger.warn("Unable to print Settings to file: {}", settingFile);
        }
    }
    
    /**
     * Prints a setting group to a string.
     *
     * @param settingGroup The setting group to print to the string.
     * @return The string.
     */
    public static String printSettingGroupToString(SettingGroup settingGroup) {
        return printSettingGroupToString(settingGroup, 0);
    }
    
    /**
     * Prints a setting group to a string.
     *
     * @param settingGroup The setting group to print to the string.
     * @param depth        The recursive depth of the setting group.
     * @return The string.
     */
    private static String printSettingGroupToString(SettingGroup settingGroup, int depth)
    {
        String indent1 = StringUtility.fillStringOfLength(' ', (depth * 2) * SETTING_FILE_INDENT);
        String indent2 = StringUtility.fillStringOfLength(' ', ((depth * 2) + 1) * SETTING_FILE_INDENT);
        String indent3 = StringUtility.fillStringOfLength(' ', ((depth * 2) + 2) * SETTING_FILE_INDENT);
        String indent4 = StringUtility.fillStringOfLength(' ', ((depth * 2) + 3) * SETTING_FILE_INDENT);
        
        StringBuilder settingData = new StringBuilder();
        settingData.append(indent1).append('{').append(System.lineSeparator());
        settingData.append(indent2).append("\"name\":\"").append(settingGroup.getName()).append("\",");
        settingData.append(indent2).append("\"settings\":[");
        
        boolean firstSetting = true;
        for (Setting setting : settingGroup.getSettings()) {
            if (firstSetting) {
                firstSetting = false;
            } else {
                settingData.append(',');
            }
            settingData.append(System.lineSeparator());
            
            settingData.append(indent3).append('{').append(System.lineSeparator());
            settingData.append(indent4).append("\"name\":\"").append(setting.getName()).append("\",").append(System.lineSeparator());
            settingData.append(indent4).append("\"type\":\"").append(setting.getType()).append("\",").append(System.lineSeparator());
            settingData.append(indent4).append("\"value\":\"").append(setting.getStringValue().replace("\\", "\\\\")).append('"').append(System.lineSeparator());
            settingData.append(indent3).append('}');
        }
        
        for (SettingGroup setting : settingGroup.getSettingGroups()) {
            if (firstSetting) {
                firstSetting = false;
            } else {
                settingData.append(',');
            }
            settingData.append(System.lineSeparator());
            
            settingData.append(printSettingGroupToString(setting, depth + 1));
        }
        
        settingData.append(System.lineSeparator());
        settingData.append(indent2).append(']').append(System.lineSeparator());
        settingData.append(indent1).append('}');
        
        return settingData.toString();
    }
    
}
