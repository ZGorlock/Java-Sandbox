/*
 * File:    DataMirror.java
 * Package: main.data.cache
 * Author:  Zachary Gill
 */

package main.data.cache;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import commons.access.Filesystem;
import commons.access.Project;
import main.data.scraper.page.category.DungeonPageScraper;
import main.data.scraper.page.category.MonsterPageScraper;
import main.data.scraper.page.category.SubDungeonPageScraper;
import main.data.scraper.resource.category.ImageResourceScraper;

public final class DataMirror {
    
    //Constants
    
    public static final File DATA_DIR = Project.DATA_DIR;
    
    public static final File MIRROR_DIR = new File(DATA_DIR, ".mirror");
    
    public static final File HOST_FILE = new File(MIRROR_DIR, "host.txt");
    
    public static final String PROTOCOL = "https";
    
    public static final String HOST_BASE = Filesystem.readFileToString(HOST_FILE);
    
    public static final String HOST_STATIC = HOST_BASE.replaceFirst("\\.", "-static.");
    
    public static final String URL_BASE = (PROTOCOL + "://" + HOST_BASE + "/");
    
    public static final String URL_STATIC = (PROTOCOL + "://" + HOST_STATIC + "/");
    
    public static final File DIR_BASE = new File(MIRROR_DIR, HOST_BASE);
    
    public static final File DIR_STATIC = new File(MIRROR_DIR, HOST_STATIC);
    
    
    //Static Fields
    
    public static final MonsterPageScraper monsterPageScraper = new MonsterPageScraper();
    
    public static final DungeonPageScraper dungeonPageScraper = new DungeonPageScraper();
    
    public static final SubDungeonPageScraper subDungeonPageScraper = new SubDungeonPageScraper();
    
    public static final ImageResourceScraper imageResourceScraper = new ImageResourceScraper();
    
    public static final Map<String, File> monsterPages = new LinkedHashMap<>();
    
    public static final Map<String, File> dungeonPages = new LinkedHashMap<>();
    
    public static final Map<String, File> subDungeonPages = new LinkedHashMap<>();
    
    public static final Map<String, File> imageResources = new LinkedHashMap<>();
    
    
    //Main Methods
    
    public static void main(String[] args) {
        sync();
    }
    
    
    //Static Methods
    
    public static void sync() {
        final Runnable printSeparator = () ->
                System.out.println("\n\n" + ("=".repeat(120) + "\n").repeat(2) + "\n");
        
        final Consumer<Runnable> syncer = (Runnable categorySyncer) -> {
            printSeparator.run();
            categorySyncer.run();
        };
        
        syncer.accept(DataMirror::syncMonsterPages);
        syncer.accept(DataMirror::syncSubDungeonPages);
        
        syncer.accept(DataMirror::syncDungeonPages);
        syncer.accept(DataMirror::syncSubDungeonPages);
        
        syncer.accept(DataMirror::syncImageResources);
    }
    
    public static void syncMonsterPages() {
        System.out.println("Syncing Monster Pages...\n\n");
        monsterPages.clear();
        monsterPages.putAll(monsterPageScraper.fetchAll());
    }
    
    public static void syncSubDungeonPages() {
        System.out.println("Syncing Sub-Dungeon Pages...\n\n");
        subDungeonPages.clear();
        subDungeonPages.putAll(subDungeonPageScraper.fetchAll());
    }
    
    public static void syncDungeonPages() {
        System.out.println("Syncing Dungeon Pages...\n\n");
        dungeonPages.clear();
        dungeonPages.putAll(dungeonPageScraper.fetchAll());
    }
    
    public static void syncImageResources() {
        System.out.println("Syncing Image Resources...\n\n");
        imageResources.clear();
        imageResources.putAll(imageResourceScraper.fetchAll());
    }
    
}
