/*
 * File:    GalleryDownloader.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.List;
import java.util.Optional;

import commons.access.CmdLine;
import commons.access.Filesystem;
import commons.access.Internet;
import commons.access.OperatingSystem;
import commons.access.Project;
import commons.object.string.StringUtility;
import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Element;

public class GalleryDownloader {
    
    //Constants
    
    private static final String GITHUB_URL = "https://github.com/mikf/gallery-dl/";
    
    private static final File LOCAL_EXECUTABLE = new File("gallery-dl" + (OperatingSystem.isWindows() ? ".exe" : ""));
    
    private static final File MAIN_DIR = new File(FileUtils.getUserDirectory(), "gallery-dl");
    
    private static final File CONF_JSON = new File(MAIN_DIR, "config.json");
    
    private static final File EXECUTABLE = new File(MAIN_DIR, LOCAL_EXECUTABLE.getName());
    
    
    //Static Fields
    
    private static final List<String> queue = List.of(
            "https://www.reddit.com/r/ProgrammerHumor/"
    );
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        init();
        
        for (String queued : queue) {
            download(queued);
        }
    }
    
    
    //Static Methods
    
    private static String download(String url) {
        return CmdLine.executeCmd(new String[] {StringUtility.quote(LOCAL_EXECUTABLE.getAbsolutePath()), url});
    }
    
    private static void init() throws Exception {
        if (!Internet.isOnline()) {
            throw new RuntimeException("Internet access is required");
        }
        if (!getGalleryDlExecutable()) {
            throw new RuntimeException("Unable to find gallery-dl");
        }
        if (!Filesystem.createDirectory(MAIN_DIR)) {
            throw new RuntimeException("Unable to create main directory: " + MAIN_DIR.getAbsolutePath());
        }
        if (!CONF_JSON.exists() && !Filesystem.copyFile(new File(Project.RESOURCES_DIR, CONF_JSON.getName()), CONF_JSON)) {
            throw new RuntimeException("Unable to initialize configuration file: " + CONF_JSON.getAbsolutePath());
        }
    }
    
    private static boolean getGalleryDlExecutable() throws Exception {
        boolean exists = LOCAL_EXECUTABLE.exists();
        String currentVersion = !exists ? "" : CmdLine.executeCmd(
                        new String[] {StringUtility.quote(LOCAL_EXECUTABLE.getAbsolutePath()), "--version"})
                .replaceAll("\r?\n", "").replaceAll("\\[\\*].*$", "").strip();
        String latestVersion = Optional.ofNullable(Internet.getHtml(GITHUB_URL + "releases/"))
                .map(e -> e.getElementsByClass("Link--primary"))
                .filter(e -> !e.isEmpty()).map(e -> e.get(0)).map(Element::text).orElse("");
        
        if ((!exists || !currentVersion.equals(latestVersion)) && !latestVersion.isEmpty()) {
            if (Internet.downloadFile((GITHUB_URL + "releases/download/" + latestVersion + "/" + LOCAL_EXECUTABLE.getName()), LOCAL_EXECUTABLE) != null) {
                Filesystem.copyFile(LOCAL_EXECUTABLE, EXECUTABLE, true);
            }
        }
        
        return LOCAL_EXECUTABLE.exists();
    }
    
}
