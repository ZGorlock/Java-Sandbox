/*
 * File:    SettingGroup.java
 * Package: dla.resource.setting
 * Author:  Zachary Gill
 */


import java.util.ArrayList;
import java.util.List;

/**
 * Stores a group of setting objects.
 */
public class SettingGroup
{
    
    //Logger
    
    //Fields
    
    /**
     * The name of the setting group.
     */
    private String name = "";
    
    /**
     * The list of settings within the setting group.
     */
    private final List<Setting> settings = new ArrayList<>();
    
    /**
     * The list of setting groups within the setting group.
     */
    private final List<SettingGroup> settingGroups = new ArrayList<>();
    
    
    //Constructor
    
    /**
     * Constructor for a SettingGroup.
     *
     * @param name     The name of the setting group.
     * @param settings The list of settings to add to the setting group.
     */
    public SettingGroup(String name, List<Setting> settings, List<SettingGroup> settingGroups)
    {
        this.name = name;
        this.settings.addAll(settings);
        this.settingGroups.addAll(settingGroups);
    }
    
    /**
     * Constructor for a SettingGroup.
     *
     * @param name The name of the setting group.
     */
    public SettingGroup(String name)
    {
        this.name = name;
    }
    
    /**
     * Constructor for a SettingGroup.
     */
    public SettingGroup()
    {
    }
    
    
    //Methods
    
    /**
     * Determines if a setting is contained within the setting group.
     *
     * @param name The name of the setting.
     * @return Whether the setting is contained within the setting group or not.
     */
    public boolean containsSetting(String name)
    {
        for (Setting setting : settings) {
            if (setting.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        
        for (SettingGroup settingGroup : settingGroups) {
            if (settingGroup.containsSetting(name)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Determines if a setting group is contained within the setting group.
     *
     * @param name The name of the setting group.
     * @return Whether the setting group is contained within the setting group or not.
     */
    public boolean containsSettingGroup(String name)
    {
        for (SettingGroup settingGroup : settingGroups) {
            if (settingGroup.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        
        for (SettingGroup settingGroup : settingGroups) {
            if (settingGroup.containsSettingGroup(name)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Retrieves a setting from the setting group.
     *
     * @param name The name of the setting to retrieve.
     * @return The setting or null if it does not exist.
     */
    public Setting getSetting(String name)
    {
        for (Setting setting : settings) {
            if (setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }
    
        for (SettingGroup settingGroup : settingGroups) {
            if (settingGroup.containsSetting(name)) {
                return settingGroup.getSetting(name);
            }
        }
        
        return null;
    }
    
    /**
     * Retrieves a setting group from the setting group.
     *
     * @param name The name of the setting group to retrieve.
     * @return The setting group or null if it does not exist.
     */
    public SettingGroup getSettingGroup(String name)
    {
        for (SettingGroup settingGroup : settingGroups) {
            if (settingGroup.getName().equalsIgnoreCase(name)) {
                return settingGroup;
            }
        }
        
        for (SettingGroup settingGroup : settingGroups) {
            if (settingGroup.containsSetting(name)) {
                return settingGroup.getSettingGroup(name);
            }
        }
        
        return null;
    }
    
    /**
     * Adds a setting to the setting group.
     *
     * @param setting The setting to add to the setting group.
     * @return Whether the setting was added or not.
     */
    public boolean addSetting(Setting setting)
    {
        if (!containsSetting(setting.getName())) {
            settings.add(setting);
            return true;
        }
        return false;
    }
    
    /**
     * Removes a setting from the setting group.
     *
     * @param setting The setting to remove from the setting group.
     * @return Whether the setting was removed or not.
     */
    public boolean removeSetting(Setting setting)
    {
        return settings.remove(setting);
    }
    
    /**
     * Removes a setting from the setting group.
     *
     * @param name The name of the setting to remove from the setting group.
     * @return Whether the setting was removed or not.
     */
    public boolean removeSetting(String name)
    {
        int index = -1;
        for (int i = 0; i < settings.size(); i++) {
            if (settings.get(i).getName().equalsIgnoreCase(name)) {
                index = i;
                break;
            }
        }
        
        if (index > -1) {
            settings.remove(index);
            return true;
        }
        return false;
    }
    
    /**
     * Adds a setting group to the setting group.
     *
     * @param settingGroup The setting group to add to the setting group.
     * @return Whether the setting group was added or not.
     */
    public boolean addSettingGroup(SettingGroup settingGroup)
    {
        if (!containsSettingGroup(settingGroup.getName())) {
            settingGroups.add(settingGroup);
            return true;
        }
        return false;
    }
    
    /**
     * Removes a setting group from the setting group.
     *
     * @param settingGroup The setting group to remove from the setting group.
     * @return Whether the setting group was removed or not.
     */
    public boolean removeSettingGroup(SettingGroup settingGroup)
    {
        return settingGroups.remove(settingGroup);
    }
    
    /**
     * Removes a setting group from the setting group.
     *
     * @param name The name of the setting group to remove from the setting group.
     * @return Whether the setting group was removed or not.
     */
    public boolean removeSettingGroup(String name)
    {
        int index = -1;
        for (int i = 0; i < settingGroups.size(); i++) {
            if (settingGroups.get(i).getName().equalsIgnoreCase(name)) {
                index = i;
                break;
            }
        }
    
        if (index > -1) {
            settingGroups.remove(index);
            return true;
        }
        return false;
    }
    
    /**
     * Adds a setting to a setting group within the setting group.
     *
     * @param setting   The setting to add to the setting group.
     * @param groupName The name of the setting group to add the setting to.
     * @return Whether the setting was added or not.
     */
    public boolean addSettingToGroup(Setting setting, String groupName)
    {
        SettingGroup group = getSettingGroup(groupName);
        return (group != null) && group.addSetting(setting);
    }
    
    /**
     * Removes a setting from a setting group within the setting group.
     *
     * @param setting   The setting to remove from the setting group.
     * @param groupName The name of the setting group to remove the setting from.
     * @return Whether the setting was removed or not.
     */
    public boolean removeSettingFromGroup(Setting setting, String groupName)
    {
        SettingGroup group = getSettingGroup(groupName);
        return (group != null) && group.removeSetting(setting);
    }
    
    /**
     * Removes a setting from a setting group within the setting group.
     *
     * @param setting   The name of the setting to remove from the setting group.
     * @param groupName The name of the setting group to remove the setting from.
     * @return Whether the setting was removed or not.
     */
    public boolean removeSettingFromGroup(String setting, String groupName)
    {
        SettingGroup group = getSettingGroup(groupName);
        return (group != null) && group.removeSetting(setting);
    }
    
    /**
     * Adds a setting group to a setting group within the setting group.
     *
     * @param settingGroup The setting group to add to the setting group.
     * @param groupName    The name of the setting group to add the setting group to.
     * @return Whether the setting group was added or not.
     */
    public boolean addSettingGroupToGroup(SettingGroup settingGroup, String groupName)
    {
        SettingGroup group = getSettingGroup(groupName);
        return (group != null) && group.addSettingGroup(settingGroup);
    }
    
    /**
     * Removes a setting group from a setting group within the setting group.
     *
     * @param settingGroup The setting group to remove from the setting group.
     * @param groupName    The name of the setting group to remove the setting from.
     * @return Whether the setting group was removed or not.
     */
    public boolean removeSettingFromGroup(SettingGroup settingGroup, String groupName)
    {
        SettingGroup group = getSettingGroup(groupName);
        return (group != null) && group.removeSettingGroup(settingGroup);
    }
    
    /**
     * Removes a setting group from a setting group within the setting group.
     *
     * @param settingGroup The name of the setting group to remove from the setting group.
     * @param groupName    The name of the setting group to remove the setting from.
     * @return Whether the setting group was removed or not.
     */
    public boolean removeSettingGroupFromGroup(String settingGroup, String groupName)
    {
        SettingGroup group = getSettingGroup(groupName);
        return (group != null) && group.removeSettingGroup(settingGroup);
    }
    
    
    //Getters
    
    /**
     * Returns the name of the setting group.
     *
     * @return The name of the setting group.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns the list of settings within the setting group.
     *
     * @return The list of settings within the setting group.
     */
    public List<Setting> getSettings()
    {
        return settings;
    }
    
    /**
     * Returns the list of setting groups within the setting group.
     *
     * @return The list of setting groups within the setting group.
     */
    public List<SettingGroup> getSettingGroups()
    {
        return settingGroups;
    }
    
    
    //Setters
    
    /**
     * Sets the name of the setting group.
     *
     * @param name The new name of the setting group.
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
}
