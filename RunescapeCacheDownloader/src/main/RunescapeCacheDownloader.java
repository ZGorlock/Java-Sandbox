/*
 * File:    RunescapeCacheDownloader.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import commons.access.CmdLine;
import commons.access.Filesystem;
import commons.access.Internet;
import commons.access.Project;
import commons.object.collection.ListUtility;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class RunescapeCacheDownloader {
    
    //Constants
    
    private static final String GITHUB_URL = "https://github.com/abextm/osrs-cache";
    
    private static final File FLAT_CACHE_DIR = new File("F:/Datasets/Gaming/OSRS Historical Game Cache Archive/AbexTM's FlatCache Archive/");
    
    private static final File CACHE_DUMP_DIR = new File("F:/Datasets/Gaming/OSRS Historical Game Cache Archive/AbexTM's Cache Dump Archive/");
    
    private static final File VERSION_CACHE = new File(Project.DATA_DIR, "versions.txt");
    
    
    //Static Fields
    
    private static final List<String> versions = new ArrayList<>();
    
    
    //Main Method
    
    public static void main(String[] args) {
        listVersions();
        downloadCaches();
        createCacheDumps();
    }
    
    
    //Static Methods
    
    private static List<String> listVersions() {
        versions.addAll(Filesystem.readLines(VERSION_CACHE));
        versions.addAll(scrapeGithubReleases());
        
        ListUtility.removeDuplicates(versions);
        Collections.sort(versions, Comparator.reverseOrder());
        Filesystem.writeLines(VERSION_CACHE, versions);
        
        return versions;
    }
    
    private static List<String> scrapeGithubReleases() {
        final List<String> releases = new ArrayList<>();
        
        for (int page = 1; page < Integer.MAX_VALUE; page++) {
            final Document html = Optional.of(GITHUB_URL + "/releases" + ((page > 1) ? ("?page=" + page) : ""))
                    .map(Internet::getHtml).orElseThrow();
            
            final List<String> releasePage = Optional.of(html)
                    .map(e -> e.select("main > turbo-frame > div > div > div > section"))
                    .stream().flatMap(Collection::stream)
                    .map(e -> e.selectFirst(":root > div > div > div.Box > div.Box-body *> a.Link--primary"))
                    .filter(Objects::nonNull)
                    .map(Element::text)
                    .collect(Collectors.toList());
            
            for (String release : releasePage) {
                if (versions.contains(release)) {
                    return releases;
                }
                System.out.println("New Release: " + release);
                versions.add(release);
            }
            
            if (html.selectFirst("a.next_page") == null) {
                break;
            }
        }
        
        return releases;
    }
    
    private static void downloadCaches() {
        for (String version : versions) {
            final File flatCache = downloadFlatCache(version);
            final File cacheDump = downloadCacheDump(version);
            
            if ((flatCache != null) && (cacheDump != null)) {
                fixCacheDates(flatCache, cacheDump);
            }
        }
    }
    
    private static File downloadFlatCache(String version) {
        final String url = GITHUB_URL + "/archive/refs/tags/" + version + ".tar.gz";
        
        final File out = new File(FLAT_CACHE_DIR, (version + ".tar.gz"));
        if (!Filesystem.isEmpty(out)) {
            return null;
        }
        return performDownload(url, out);
    }
    
    private static File downloadCacheDump(String version) {
        final String url = GITHUB_URL + "/releases/download/" + version + "/dump-" + version + ".tar.gz";
        
        final File out = new File(CACHE_DUMP_DIR, ("dump-" + version + ".tar.gz"));
        if (!Filesystem.isEmpty(out)) {
            return null;
        }
        return performDownload(url, out);
    }
    
    private static File performDownload(String url, File out) {
        final String cmd = "wget -S -O \"" + out.getAbsolutePath() + "\" \"" + url + "\"";
        System.out.println(cmd);
        
        final String response = CmdLine.executeCmd(cmd);
        
        if (!Filesystem.isEmpty(out)) {
            return out;
        }
        return null;
    }
    
    private static void fixCacheDates(File sourceCache, File targetCache) {
        if (!Filesystem.exists(sourceCache) || !Filesystem.exists(targetCache)) {
            return;
        }
        
        final Map<String, FileTime> cacheDates = Filesystem.readDates(sourceCache);
        final Date lastModified = new Date(cacheDates.get("lastModifiedTime").toMillis());
        
        Filesystem.setLastModifiedTime(targetCache, lastModified);
    }
    
    private static void createCacheDumps() {
        for (String version : versions) {
            final File flatCache = new File(FLAT_CACHE_DIR, (version + ".tar.gz"));
            final File cacheDump = new File(CACHE_DUMP_DIR, ("dump-" + version + ".tar.gz"));
            
            if (flatCache.exists() && !cacheDump.exists()) {
                produceDump(version);
            }
        }
    }
    
    private static void produceDump(String version) {
        File versionDir = new File(Project.TMP_DIR, version);
        Filesystem.createDirectory(versionDir);
        
        File flatCacheSource = new File(FLAT_CACHE_DIR, (version + ".tar.gz"));
        File flatCache = new File(versionDir, flatCacheSource.getName());
        Filesystem.copyFile(flatCacheSource, flatCache);
        
        File cacheDir = new File(versionDir, "osrs-cache-" + version);
        CmdLine.executeCmd("winrar x -ibck \"" + flatCache.getAbsolutePath() + "\" \"" + flatCache.getParentFile().getAbsolutePath() + "\"");
        
        File dumpDir = new File(versionDir, "dump");
        Filesystem.createDirectory(dumpDir);
        
        File packerJar = new File(Project.RESOURCES_DIR, "packer-1.10.22-shaded.jar");
        String dumpCategories = "underlays,kits,overlays,inventories,object_defs,enums,npc_defs,item_defs,sequences,var_players,var_bits,param_defs,interface_defs,models_raw,models,sprites,texture_defs,rs2asm,structs,binary,_18,_19,_20,dbtable,dbrow,dbtable_index";
        String packerCmd = "java -jar \"" + packerJar.getAbsolutePath() + "\" dump " + dumpCategories + " \"" + cacheDir.getAbsolutePath() + "/\" \"" + dumpDir.getAbsolutePath() + "\"";
        CmdLine.executeCmd(packerCmd);
        
        File dump = new File(versionDir, "dump-" + version + ".tar.gz");
        CmdLine.executeCmd("winrar a -ibck -ma5 -m5 -md64m -cfg- -mt8 -ep1 -df -y \"" + dump.getAbsolutePath() + "\" \"" + dumpDir.getAbsolutePath() + "\"");
        
        fixCacheDates(flatCache, dump);
        
        File cacheDumpOutput = new File(CACHE_DUMP_DIR, dump.getName());
        Filesystem.moveFile(dump, cacheDumpOutput);
        
        Filesystem.deleteDirectory(versionDir);
    }
    
}
