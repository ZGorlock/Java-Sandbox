/*
 * File:    WebsiteBuilder.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import commons.access.Filesystem;
import main.entity.base.Entity;
import main.entity.image.picture.PictureAlbum;
import main.entity.shortcut.subreddit.SubredditRegistry;
import main.entity.video.clip.ClipLibrary;
import main.util.persistence.LocationUtil;

public class WebsiteBuilder {
    
    //Constants
    
    public static final File BASE_DIR = LocationUtil.getRootLocation();
    
    public static final File OUTPUT_DIR = LocationUtil.getLocation(0x255e);
    
    public static final File TMP_DIR = LocationUtil.getLocation(0x8605);
    
    public static final boolean SAFE_MODE = false;
    
    public static final boolean TEST_MODE = false;
    
    public static final boolean REPROCESS_ALL = false;
    
    
    //Enums
    
    private enum FolderType {
        IMAGE,
        VIDEO,
        SUBREDDIT,
        WEBSITE
    }
    
    
    //Main Methods
    
    public static void main(String[] args) throws Exception {
        System.out.println("Start");
        
        //UsersParser.parseUsersFromSource();
        //CategoriesParser.parseCategoriesFromSource();
        //TagsParser.parseTagsFromSource();
        //CollectionParser.parseCollectionsFromSource();
        //SeriesParser.parseSeriesIndexFromSource();
        //IndexParser.parseIndexFromSource();
        
        //PictureAlbum a = PictureAlbum.loadAlbum(LocationUtil.getLocation(0x06a7, 0xa356));
        //ClipLibrary l = ClipLibrary.loadLibrary(LocationUtil.getLocation(0x06a7, 0x4df6));
        //SubredditRegistry r = SubredditRegistry.loadRegistry(LocationUtil.getLocation(0x06a7, 0xc45f));
        //
        //final List<PictureAlbum> galleries = loadGalleries();
        //final List<ClipLibrary> clipLibraries = loadClipLibraries();
        //final List<SubredditRegistry> subredditLibraries = loadSubredditRegistry();
        
        System.out.println("Done");
    }
    
    
    //Static Methods
    
    private static List<File> getSourceFolders(FolderType type) {
        return Filesystem.getDirs(BASE_DIR,
                d -> !d.getName().startsWith(Entity.META_PREFIX));
    }
    
    private static List<SubredditRegistry> loadSubredditRegistry() {
        return LocationUtil.getEntityLocations(SubredditRegistry.class).stream()
                .map(SubredditRegistry::loadRegistry)
                .collect(Collectors.toList());
    }
    
    private static List<PictureAlbum> loadGalleries() {
        return LocationUtil.getEntityLocations(PictureAlbum.class).stream()
                .map(PictureAlbum::loadAlbum)
                .collect(Collectors.toList());
    }
    
    private static List<ClipLibrary> loadClipLibraries() {
        return LocationUtil.getEntityLocations(ClipLibrary.class).stream()
                .map(ClipLibrary::loadLibrary)
                .collect(Collectors.toList());
    }
    
}
