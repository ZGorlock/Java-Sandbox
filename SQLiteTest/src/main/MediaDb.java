/*
 * File:    MediaDb.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import commons.access.CmdLine;
import commons.access.Filesystem;
import commons.io.console.ProgressBar;
import commons.io.file.media.FFmpeg;
import commons.io.file.media.image.ImageUtility;
import commons.object.collection.map.BiMap;
import org.apache.commons.codec.digest.DigestUtils;

@SuppressWarnings({"UnnecessaryLocalVariable", "ConstantValue"})
public class MediaDb {
    
    //Constants
    
    public static final String SQLITE_PROTOCOL = "jdbc:sqlite";
    
    public static final List<String> EXCLUDE_TYPES = List.of("sqlite", "sqlite3", "db", "txt", "ini", "url", "part");
    
    private static final boolean FORCE_CHECK_HASHES_ON_SYNC = false;
    
    
    //Static Fields
    
    private static final File db = new File("data/media.sqlite");
    
    private static Connection sqliteDb;
    
    private static Statement sqlite;
    
    
    //Main Methods
    
    public static void main(String[] args) throws Exception {
        sqliteDb = DriverManager.getConnection(SQLITE_PROTOCOL + ':' + db.getPath());
        
        sqlite = sqliteDb.createStatement();
        sqlite.setQueryTimeout(30);
        
        try {
            
            //initDb();
            //runSql();
            
            //ingestMedia();
            //pruneMedia();
            //syncMedia();
            
            //fixVideos();
            fixImages();
            //fixHashes();
            
            //moveMedia();
            //convertMedia();
            
        } finally {
            sqlite.close();
            sqliteDb.close();
        }
    }
    
    
    //Static Methods
    
    private static void initDb() throws Exception {
        sqlite.executeUpdate("CREATE TABLE IF NOT EXISTS media (\n" +
                "   id      INTEGER PRIMARY KEY             ,\n" +
                "   name    TEXT    NOT NULL                ,\n" +
                "   type    TEXT    NOT NULL                ,\n" +
                "   path    TEXT    NOT NULL                ,\n" +
                "   hash    TEXT                            ,\n" +
                "   size    INTEGER NOT NULL                ,\n" +
                "   width   INTEGER             DEFAULT -1  ,\n" +
                "   height  INTEGER             DEFAULT -1  ,\n" +
                "   length  INTEGER             DEFAULT -1   \n" +
                ");");
        
        sqlite.executeUpdate("ALTER TABLE media ADD COLUMN IF NOT EXISTS\n" +
                "bitrate INTEGER\n" +
                "GENERATED ALWAYS AS (max((size / length), -1))\n" +
                "VIRTUAL;");
        sqlite.executeUpdate("ALTER TABLE media ADD COLUMN IF NOT EXISTS\n" +
                "file TEXT\n" +
                "GENERATED ALWAYS AS (path || \"\\\" || name || \".\" || type)\n" +
                "VIRTUAL;");
        
        //sqlite.executeUpdate("CREATE INDEX IF NOT EXISTS\n" +
        //        "idx_media_file ON media(file);");
        //sqlite.executeUpdate("CREATE INDEX IF NOT EXISTS\n" +
        //        "idx_media_hash ON media(hash);");
        
        //sqlite.executeUpdate("CREATE TRIGGER tr_media_hash IF NOT EXISTS\n" +
        //        "AFTER UPDATE OF type, size, width, height, length ON media\n" +
        //        "FOR EACH ROW WHEN (\n" +
        //        "   ((OLD.hash = NEW.hash)          AND (OLD.hash != ''))   AND (\n" +
        //        "       ((OLD.type != NEW.type)     AND (OLD.type != ''))   OR\n" +
        //        "       ((OLD.size != NEW.size)     AND (OLD.size != -1))   OR\n" +
        //        "       ((OLD.width != NEW.width)   AND (OLD.width != -1))  OR\n" +
        //        "       ((OLD.height != NEW.height) AND (OLD.height != -1)) OR\n" +
        //        "       ((OLD.length != NEW.length) AND (OLD.length != -1))   \n" +
        //        "   )\n" +
        //        ")\n" +
        //        "BEGIN\n" +
        //        "    UPDATE media SET hash = '' WHERE id = NEW.id;\n" +
        //        "END;");
    }
    
    private static void runSql(File sqlFile) throws Exception {
        List<String> sqlStatements = Filesystem.readLines(sqlFile);
        
        ProgressBar progress = new ProgressBar("Running SQL", sqlStatements.size());
        progress.setAutoPrint(true);
        
        //boolean active = false;
        
        for (String sql : sqlStatements) {
            progress.addOne();
            
            //if (!(active |= sql.matches(".*id=100000;$"))) {
            //    continue;
            //}
            //active &= !sql.matches(".*id=200000;$");
            
            progress.log(sql);
            sqlite.executeUpdate(sql);
        }
        
        progress.complete();
    }
    
    private static void runSql() throws Exception {
        runSql(new File("data/sql/run.sql"));
    }
    
    private static void ingestMedia(File dir) throws Exception {
        List<File> files = Filesystem.getFilesRecursively(dir,
                f -> !EXCLUDE_TYPES.contains(Filesystem.getFileType(f).toLowerCase()));
        
        ProgressBar progress = new ProgressBar("Ingesting Media", files.size());
        progress.setAutoPrint(true);
        
        for (File file : files) {
            progress.addOne();
            
            if (!file.exists()) {
                continue;
            }
            
            String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
            String type = file.getName().substring(file.getName().lastIndexOf('.') + 1);
            String path = file.getParentFile().getAbsolutePath();
            String hash = "";
            long size = file.length();
            int width = -1;
            int height = -1;
            int length = -1;
            
            String sql = "INSERT INTO media (name, type, path, hash, size, width, height, length) VALUES (" +
                    "\"" + name + "\", " +
                    "\"" + type + "\", " +
                    "\"" + path + "\", " +
                    "\"" + hash + "\", " +
                    size + ", " +
                    width + ", " +
                    height + ", " +
                    length + ");";
            progress.log(sql);
            sqlite.executeUpdate(sql);
        }
        
        progress.complete();
    }
    
    private static void ingestMedia() throws Exception {
        ingestMedia(new File("data/media/new"));
    }
    
    private static void fixVideos() throws Exception {
        ResultSet results = sqlite.executeQuery("SELECT id, file FROM media " +
                "WHERE (type = 'mp4') " +
                "AND ((size = -1) OR (length = -1) OR (width = -1) OR (height = -1));");
        
        Map<Integer, File> mediaResults = new LinkedHashMap<>();
        while (results.next()) {
            mediaResults.put(results.getInt("id"), new File(results.getString("file")));
        }
        results.close();
        
        ProgressBar progress = new ProgressBar("Processing Videos", mediaResults.size());
        progress.setAutoPrint(true);
        
        for (Map.Entry<Integer, File> mediaResult : mediaResults.entrySet()) {
            progress.addOne();
            
            int id = mediaResult.getKey();
            File file = mediaResult.getValue();
            if (!file.exists()) {
                continue;
            }
            
            long size = file.length();
            int length = -1;
            int width = -1;
            int height = -1;
            
            FFmpeg.MediaInfo med = FFmpeg.getMediaInfo(file);
            if (med != null) {
                length = (int) Math.ceil(med.getFormat().getDuration()); //ms
                
                FFmpeg.MediaInfo.Stream stream = med.getStream(FFmpeg.Identifier.Stream.ofFirst(FFmpeg.StreamType.VIDEO));
                if (stream != null) {
                    width = stream.getVideoInfo().getWidth();
                    height = stream.getVideoInfo().getHeight();
                }
            } else {
                continue;
            }
            
            String sql = "UPDATE media SET " +
                    "size = " + size + ", " +
                    "length = " + length + ", " +
                    "width = " + width + ", " +
                    "height = " + height + " " +
                    "WHERE (id = " + id + ");";
            progress.log(sql);
            sqlite.executeUpdate(sql);
        }
        
        progress.complete();
    }
    
    private static void fixImages() throws Exception {
        ResultSet results = sqlite.executeQuery("SELECT id, file FROM media " +
                "WHERE ((type = 'jpg') OR (type = 'gif')) " +
                "AND ((size = -1) OR (width = -1) OR (height = -1));");
        
        Map<Integer, File> mediaResults = new LinkedHashMap<>();
        while (results.next()) {
            mediaResults.put(results.getInt("id"), new File(results.getString("file")));
        }
        results.close();
        
        ProgressBar progress = new ProgressBar("Processing Images", mediaResults.size());
        progress.setAutoPrint(true);
        
        for (Map.Entry<Integer, File> mediaResult : mediaResults.entrySet()) {
            progress.addOne();
            
            int id = mediaResult.getKey();
            File file = mediaResult.getValue();
            if (!file.exists()) {
                continue;
            }
            
            long size = file.length();
            int width = -1;
            int height = -1;
            
            Dimension dim = ImageUtility.getDimensions(file);
            if (dim != null) {
                width = dim.width;
                height = dim.height;
            } else {
                continue;
            }
            
            String sql = "UPDATE media SET " +
                    "size = " + size + ", " +
                    "width = " + width + ", " +
                    "height = " + height + " " +
                    "WHERE (id = " + id + ");";
            progress.log(sql);
            sqlite.executeUpdate(sql);
        }
        
        progress.complete();
    }
    
    private static void fixHashes() throws Exception {
        ResultSet results = sqlite.executeQuery("SELECT id, file FROM media " +
                "WHERE (hash = '');");
        
        Map<Integer, File> mediaResults = new LinkedHashMap<>();
        while (results.next()) {
            mediaResults.put(results.getInt("id"), new File(results.getString("file")));
        }
        results.close();
        
        ProgressBar progress = new ProgressBar("Calculating Hashes", mediaResults.size());
        progress.setAutoPrint(true);
        
        for (Map.Entry<Integer, File> mediaResult : mediaResults.entrySet()) {
            progress.addOne();
            
            int id = mediaResult.getKey();
            File file = mediaResult.getValue();
            if (!file.exists()) {
                continue;
            }
            
            long size = file.length();
            String hash = "";
            
            try (InputStream is = Files.newInputStream(file.toPath())) {
                hash = DigestUtils.md5Hex(is);
            } catch (Exception e) {
                continue;
            }
            
            String sql = "UPDATE media SET " +
                    "hash = \"" + hash + "\", " +
                    "size = " + size + " " +
                    "WHERE (id = " + id + ");";
            progress.log(sql);
            sqlite.executeUpdate(sql);
        }
        
        progress.complete();
    }
    
    private static void moveMedia(File oldDir, File newDir) throws Exception {
        String oldPath = oldDir.getAbsolutePath();
        String newPath = newDir.getAbsolutePath();
        
        if (oldPath.isBlank() || newPath.isBlank()) {
            return;
        }
        
        String sql = "UPDATE media SET " +
                "path = replace(path,'" + oldPath + "','" + newPath + "') " +
                "WHERE (path = '" + oldPath + "') OR (path LIKE '" + oldPath + "\\%');";
        System.out.println(sql);
        sqlite.executeUpdate(sql);
    }
    
    private static void moveMedia() throws Exception {
        moveMedia(new File("data/media/a"), new File("data/media/b"));
    }
    
    private static void convertMedia() throws Exception {
        String oldType = "webm";
        String newType = "mp4";
        
        ResultSet results = sqlite.executeQuery("SELECT id, file FROM media " +
                "WHERE (type = '" + oldType + "');");
        
        Map<Integer, File> mediaResults = new LinkedHashMap<>();
        while (results.next()) {
            mediaResults.put(results.getInt("id"), new File(results.getString("file")));
        }
        results.close();
        
        ProgressBar progress = new ProgressBar("Converting Media", mediaResults.size());
        progress.setAutoPrint(true);
        
        for (Map.Entry<Integer, File> mediaResult : mediaResults.entrySet()) {
            progress.addOne();
            
            int id = mediaResult.getKey();
            File file = mediaResult.getValue();
            if (!file.exists()) {
                continue;
            }
            
            File tmpDir = Filesystem.createTemporaryDirectory();
            File tmpFile = new File(tmpDir, file.getName());
            if (!Filesystem.copyFile(file, tmpFile) || !tmpFile.exists()) {
                continue;
            }
            
            File outputFile = new File(tmpDir, file.getName().replaceAll("\\.[^.]+$", ("." + newType)));
            String cmd = "ffmpeg -hide_banner -i \"" + tmpFile.getAbsolutePath() + "\" " +
                    "-map 0:v -map 0:a? " +
                    "-vf \"scale=trunc(iw/2)*2:trunc(ih/2)*2\" " +
                    "-c:v libx264 -c:a copy " +
                    "-y \"" + outputFile.getAbsolutePath() + "\"";
            if (CmdLine.executeCmd(cmd) == null) {
                continue;
            }
            
            File newFile = new File(file.getParentFile(), outputFile.getName());
            if (Filesystem.isEmpty(outputFile) || !Filesystem.isEmpty(newFile) ||
                    !Filesystem.copyFile(outputFile, newFile) || Filesystem.isEmpty(newFile) ||
                    !Filesystem.deleteFile(file) || !Filesystem.deleteDirectory(tmpDir)) {
                continue;
            }
            
            String type = newType;
            String hash = "";
            long size = newFile.length();
            int width = -1;
            int height = -1;
            int length = -1;
            
            String sql = "UPDATE media SET " +
                    "type = \"" + type + "\", " +
                    "hash = \"" + hash + "\", " +
                    "size = " + size + ", " +
                    "width = " + width + ", " +
                    "height = " + height + ", " +
                    "length = " + length + " " +
                    "WHERE (id = " + id + ");";
            progress.log(sql);
            sqlite.executeUpdate(sql);
        }
        
        progress.complete();
    }
    
    private static void pruneMedia(File dir) throws Exception {
        ResultSet results = sqlite.executeQuery("SELECT id, file FROM media" +
                Optional.ofNullable(dir).map(File::getAbsolutePath)
                        .map(e -> (" WHERE (path = '" + e + "') OR (path LIKE '" + e + "\\%')"))
                        .orElse("") + ";");
        
        Map<Integer, File> mediaResults = new LinkedHashMap<>();
        while (results.next()) {
            mediaResults.put(results.getInt("id"), new File(results.getString("file")));
        }
        results.close();
        
        ProgressBar progress = new ProgressBar("Pruning Media", mediaResults.size());
        progress.setAutoPrint(true);
        
        for (Map.Entry<Integer, File> mediaResult : mediaResults.entrySet()) {
            progress.addOne();
            
            int id = mediaResult.getKey();
            File file = mediaResult.getValue();
            
            if (!file.exists() || EXCLUDE_TYPES.contains(Filesystem.getFileType(file).toLowerCase())) {
                String sql = "DELETE FROM media " +
                        "WHERE (id = " + id + ");";
                progress.log(sql);
                sqlite.executeUpdate(sql);
            }
        }
        
        progress.complete();
    }
    
    private static void pruneMedia() throws Exception {
        pruneMedia(null);
    }
    
    private static void syncMedia(File dir, boolean recursive) throws Exception {
        List<File> files = Filesystem.getFiles(dir,
                f -> !EXCLUDE_TYPES.contains(Filesystem.getFileType(f).toLowerCase()));
        
        ResultSet results = sqlite.executeQuery("SELECT id, file, size, hash FROM media " +
                "WHERE (path = '" + dir.getAbsolutePath() + "');");
        
        BiMap<Integer, File> mediaFileMap = new BiMap<>();
        BiMap<Integer, String> mediaFilePathMap = new BiMap<>();
        HashMap<Integer, Integer> mediaSizeMap = new HashMap<>();
        HashMap<Integer, String> mediaHashMap = new HashMap<>();
        while (results.next()) {
            Integer id = results.getInt("id");
            File file = new File(results.getString("file"));
            Integer size = results.getInt("size");
            String hash = results.getString("hash");
            
            mediaFileMap.put(id, file);
            mediaFilePathMap.put(id, file.getAbsolutePath());
            mediaSizeMap.put(id, size);
            mediaHashMap.put(id, hash);
        }
        results.close();
        
        ProgressBar progress = new ProgressBar("Syncing Media", (files.size() + mediaFileMap.size()));
        progress.setAutoPrint(true);
        
        for (Map.Entry<Integer, File> mediaFileEntry : mediaFileMap.entrySet()) {
            progress.addOne();
            
            Integer id = mediaFileEntry.getKey();
            File file = mediaFileEntry.getValue();
            
            //removals
            if (!file.exists() || EXCLUDE_TYPES.contains(Filesystem.getFileType(file).toLowerCase())) {
                String sql = "DELETE FROM media " +
                        "WHERE (id = " + id + ");";
                progress.log(sql);
                sqlite.executeUpdate(sql);
            }
        }
        
        for (File file : files) {
            progress.addOne();
            
            //additions
            if (!mediaFilePathMap.containsValue(file.getAbsolutePath())) {
                String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
                String type = file.getName().substring(file.getName().lastIndexOf('.') + 1);
                String path = file.getParentFile().getAbsolutePath();
                String hash = Filesystem.hash(file);
                long size = file.length();
                int width = -1;
                int height = -1;
                int length = -1;
                
                String sql = "INSERT INTO media (name, type, path, hash, size, width, height, length) VALUES (" +
                        "\"" + name + "\", " +
                        "\"" + type + "\", " +
                        "\"" + path + "\", " +
                        "\"" + hash + "\", " +
                        size + ", " +
                        width + ", " +
                        height + ", " +
                        length + ");";
                progress.log(sql);
                sqlite.executeUpdate(sql);
                
                continue;
            }
            
            //modifications
            Integer id = mediaFilePathMap.inverseGet(file.getAbsolutePath());
            if ((id != null) && file.exists()) {
                long size = file.length();
                if ((size == Optional.of(id).map(mediaSizeMap::get).orElse(0)) && !FORCE_CHECK_HASHES_ON_SYNC) {
                    continue;
                }
                
                String hash = Filesystem.hash(file);
                if ((hash == null) || hash.equals(Optional.of(id).map(mediaHashMap::get).orElse(""))) {
                    continue;
                }
                
                int width = -1;
                int height = -1;
                int length = -1;
                
                String sql = "UPDATE media SET " +
                        "hash = \"" + hash + "\", " +
                        "size = " + size + ", " +
                        "length = " + length + ", " +
                        "width = " + width + ", " +
                        "height = " + height + " " +
                        "WHERE (id = " + id + ");";
                progress.log(sql);
                sqlite.executeUpdate(sql);
            }
        }
        
        progress.complete();
        
        if (recursive) {
            for (File subDir : Filesystem.getDirs(dir)) {
                syncMedia(subDir, recursive);
            }
        }
    }
    
    private static void syncMedia(File dir) throws Exception {
        syncMedia(dir, true);
    }
    
    private static void syncMedia() throws Exception {
        syncMedia(new File("data/media"));
    }
    
}
