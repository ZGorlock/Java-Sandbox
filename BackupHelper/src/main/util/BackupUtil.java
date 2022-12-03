/*
 * File:    BackupUtil.java
 * Package: main.util
 * Author:  Zachary Gill
 */

package main.util;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.access.Filesystem;
import commons.access.Project;
import commons.lambda.stream.collector.MapCollectors;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BackupUtil {
    
    //Logger
    
    private static final Logger logger = LoggerFactory.getLogger(BackupUtil.class);
    
    
    //Constants
    
    public static final boolean TEST_MODE = false; //prevent filesystem changes
    
    public static final boolean SAFE_MODE = false; //prevent file deletion
    
    public static final boolean CHECK_RECENT = true; //scan for existing monthly backup before reprocessing
    
    public static final boolean ASSUME_RECENT_EXISTS = false; //skip scanning for existing monthly backups, and assume one is present
    
    public static final boolean SKIP_RSYNC = false; //prevents rsync operations
    
    public static final String INDENT = StringUtility.spaces(4);
    
    public static final String ERROR = StringUtility.fillStringOfLength('*', INDENT.length() / 2) + StringUtility.spaces(INDENT.length() / 2);
    
    public static final File BLACKLIST_FILE = new File(Project.DATA_DIR, "blacklist.txt");
    
    public static final List<String> BLACKLIST = Stream.concat(
            Stream.of(
                    "$RECYCLE.BIN", "System Volume Information",
                    "desktop.ini", "Thumbs.db"
            ),
            Filesystem.readLines(BLACKLIST_FILE).stream()
    ).filter(e -> !StringUtility.isNullOrBlank(e)).distinct().collect(Collectors.toList());
    
    static {
        if (!BLACKLIST_FILE.exists()) {
            Filesystem.writeLines(BLACKLIST_FILE, BLACKLIST);
        }
    }
    
    
    //Static Methods
    
    /**
     * Creates a backup cache directory.
     */
    public static File makeBackupCache(File backupCache) {
        final File backupCacheDir = backupCache.getAbsoluteFile();
        
        logger.debug(StringUtility.format("Creating backup cache: {}", Log.logFile(backupCache)));
        
        Action.mkdir(backupCacheDir);
        
        return backupCacheDir;
    }
    
    public static File makeBackupCache(File backupCacheLocation, String backupCacheName) {
        return makeBackupCache(new File(backupCacheLocation, backupCacheName));
    }
    
    /**
     * Adds files to a backup cache.
     */
    public static void addToBackupCache(File backupCache, List<File> files) {
        logger.trace(StringUtility.format("Adding: {} to: {}", Log.logFile(files), Log.logFile(backupCache)));
        
        for (File file : files) {
            if (ListUtility.containsIgnoreCase(BLACKLIST, file.getName())) {
                continue;
            }
            
            final File backupFile = new File(backupCache, file.getName()).getAbsoluteFile();
            Action.copy(file, backupFile);
        }
    }
    
    public static void addToBackupCache(File backupCache, File dir, boolean openDir, FileFilter filter) {
        addToBackupCache((openDir ? backupCache : new File(backupCache, dir.getName())), Filesystem.getFilesAndDirs(dir, filter, filter));
    }
    
    public static void addToBackupCache(File backupCache, File dir, FileFilter filter) {
        addToBackupCache(backupCache, dir, false, filter);
    }
    
    public static void addToBackupCache(File backupCache, File dir, boolean openDir, List<String> fileList, boolean exclude) {
        addToBackupCache(backupCache, dir, openDir, f -> (fileList.contains(f.getName()) == !exclude));
    }
    
    public static void addToBackupCache(File backupCache, File dir, List<String> fileList, boolean exclude) {
        addToBackupCache(backupCache, dir, false, fileList, exclude);
    }
    
    public static void addToBackupCache(File backupCache, File dir, boolean openDir, List<String> fileList) {
        addToBackupCache(backupCache, dir, openDir, fileList, false);
    }
    
    public static void addToBackupCache(File backupCache, File dir, List<String> fileList) {
        addToBackupCache(backupCache, dir, false, fileList);
    }
    
    public static void addToBackupCache(File backupCache, File file, boolean openDir) {
        if (openDir) {
            addToBackupCache(backupCache, file, true, f -> true);
        } else {
            addToBackupCache(backupCache, List.of(file));
        }
    }
    
    public static void addToBackupCache(File backupCache, File file) {
        addToBackupCache(backupCache, file, false);
    }
    
    /**
     * Compresses a backup cache.
     */
    public static File compressBackupCache(File backupCache, boolean openDir, String archiveName, boolean slow, String password) {
        final File archive = new File(backupCache.getParentFile(), (archiveName + ".rar")).getAbsoluteFile();
        final boolean deleteAfter = backupCache.getAbsolutePath().replace("\\", "/").matches("^.*/Sandbox/BackupHelper/tmp/.*$");
        
        logger.debug(StringUtility.format("Compressing: {} to: {}", Log.logFile(backupCache), Log.logFile(archive)));
        
        Action.compress(backupCache, openDir, archive, slow, password, deleteAfter);
        
        return archive;
    }
    
    public static File compressBackupCache(File backupCache, String archiveName, boolean slow, String password) {
        return compressBackupCache(backupCache, false, archiveName, slow, password);
    }
    
    public static File compressBackupCache(File backupCache, boolean openDir, String archiveName, boolean slow) {
        return compressBackupCache(backupCache, openDir, archiveName, slow, null);
    }
    
    public static File compressBackupCache(File backupCache, boolean openDir, String archiveName) {
        return compressBackupCache(backupCache, openDir, archiveName, false);
    }
    
    public static File compressBackupCache(File backupCache, String archiveName, boolean slow) {
        return compressBackupCache(backupCache, false, archiveName, slow);
    }
    
    public static File compressBackupCache(File backupCache, String archiveName) {
        return compressBackupCache(backupCache, archiveName, false);
    }
    
    public static File compressBackupCache(File backupCache) {
        return compressBackupCache(backupCache, backupCache.getName().replaceAll("\\..+$", ""));
    }
    
    /**
     * Copies or moves a backup to a backup location.
     */
    public static void commitBackup(File backupDir, File backup, boolean saveCopy) {
        logger.debug(StringUtility.format("Committing: {} to: {}", Log.logFile(backup), Log.logFile(backupDir)));
        
        final File backupSave = new File(backupDir, backup.getName()).getAbsoluteFile();
        if (saveCopy) {
            Action.copy(backup, backupSave);
        } else {
            Action.move(backup, backupSave);
        }
    }
    
    public static void commitBackup(File backupDir, File backup) {
        commitBackup(backupDir, backup, false);
    }
    
    /**
     * Cleans old backups from a backup directory.
     */
    public static void cleanBackupDir(File backupDir, String baseName, int numberToKeep) {
        logger.debug(StringUtility.format("Cleaning{} backups in: {}", Log.logBaseName(baseName), Log.logFile(backupDir)));
        
        final List<File> existingBackups = Search.list(backupDir, baseName);
        if (existingBackups.size() > numberToKeep) {
            logger.debug(INDENT + StringUtility.format("More than {}{} backups found in: {}", numberToKeep, Log.logBaseName(baseName), Log.logFile(backupDir)));
            
            for (int i = 0; i < (existingBackups.size() - numberToKeep); i++) {
                final File extraBackup = existingBackups.get(i);
                Action.delete(extraBackup);
            }
        }
    }
    
    public static void cleanBackupDir(File backupDir, int numberToKeep) {
        cleanBackupDir(backupDir, null, numberToKeep);
    }
    
    /**
     * Synchronizes a source backup directory to a target backup directory.
     */
    public static void syncBackupDir(File sourceBackupDir, File targetBackupDir, String baseName) {
        logger.debug(StringUtility.format("Synchronizing: {} to: {}", Log.logFile(sourceBackupDir), Log.logFile(targetBackupDir)));
        
        for (File source : Filesystem.getFilesRecursively(sourceBackupDir, f -> (StringUtility.isNullOrBlank(baseName) || Stamper.baseName(f).equals(baseName)))) {
            final File backup = new File(source.getAbsolutePath().replace(sourceBackupDir.getAbsolutePath(), targetBackupDir.getAbsolutePath())).getAbsoluteFile();
            if (!backup.exists()) {
                Action.copy(source, backup);
            } else if (source.isFile() && (Filesystem.dateCompare(backup, source) < 0)) {
                Action.copy(source, backup);
            }
        }
        
        for (File backup : Filesystem.getFilesAndDirsRecursively(targetBackupDir, f -> (StringUtility.isNullOrBlank(baseName) || Stamper.baseName(f).equals(baseName)))) {
            final File source = new File(backup.getAbsolutePath().replace(targetBackupDir.getAbsolutePath(), sourceBackupDir.getAbsolutePath())).getAbsoluteFile();
            if (!source.exists()) {
                Action.delete(backup);
            }
        }
    }
    
    public static void syncBackupDir(File sourceBackupDir, File targetBackupDir) {
        syncBackupDir(sourceBackupDir, targetBackupDir, null);
    }
    
    /**
     * Rsyncs a source backup directory to a target backup directory.
     */
    public static boolean rsyncBackupDir(File sourceBackupDir, File targetBackupDir) {
        if (!SKIP_RSYNC) {
            logger.debug(StringUtility.format("Rsyncing: {} to: {}", Log.logFile(sourceBackupDir), Log.logFile(targetBackupDir)));
            
            return Action.rsync(sourceBackupDir, targetBackupDir);
        }
        return false;
    }
    
    /**
     * Checks if a monthly backup already exists.
     */
    public static boolean monthlyBackupExists(File backupDir, String baseName) {
        if (CHECK_RECENT) {
            logger.debug(StringUtility.format("Checking for existing monthly{} backup in: {}", Log.logBaseName(baseName), Log.logFile(backupDir)));
            
            if (ASSUME_RECENT_EXISTS) {
                logger.warn(ERROR + "Assuming existing backup was found");
                return true;
                
            } else if (!Search.getForMonth(backupDir, baseName).isEmpty()) {
                logger.debug(INDENT + StringUtility.format("Found existing backup from: {}", Log.logStamp(Search.getNewestDate(backupDir, baseName))));
                return true;
            }
            
            logger.debug(INDENT + "No existing backup found");
        }
        return false;
    }
    
    public static boolean monthlyBackupExists(File backupDir) {
        return monthlyBackupExists(backupDir, null);
    }
    
    public static boolean clearTmpDir() {
        return Action.delete(Project.TMP_DIR, false) && Action.mkdir(Project.TMP_DIR, false);
    }
    
    
    //Inner Classes
    
    public static final class Action {
        
        //Static Methods
        
        public static boolean copy(File file, File target, boolean log) {
            final boolean update = target.exists();
            if (SAFE_MODE && target.exists()) {
                if (log) {
                    logger.warn(ERROR + StringUtility.format("Already exists: {}; skipping in safe mode", Log.logFile(target)));
                }
            } else {
                if (log) {
                    logger.trace(INDENT + StringUtility.format("{}: {}", (update ? "Updating" : "Copying"), Log.logFile(file, false)));
                }
                if (!TEST_MODE) {
                    if (!Filesystem.copy(file, target, true)) {
                        if (log) {
                            logger.error(INDENT + ERROR + StringUtility.format("Failed to {}: {}", (update ? "update" : "copy"), Log.logFile(file)));
                        }
                        return false;
                    }
//                    return doCopy(file, target, true);
                }
            }
            return true;
        }
        
        public static boolean copy(File file, File target) {
            return copy(file, target, true);
        }
        
        private static boolean doCopy(File source, File target, boolean log) {
            final boolean update = target.exists();
            boolean success = true;
            try {
                Files.copy(source.toPath(), target.toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                if (log) {
                    logger.error(INDENT + ERROR + StringUtility.format("Failed to {}: {}", (update ? "update" : "copy"), Log.logFile(source)));
                }
                success = false;
            }
            if (success && source.isDirectory()) {
                for (File sourceFile : Optional.ofNullable(source.listFiles()).orElse(new File[] {})) {
                    success &= doCopy(sourceFile, new File(target, sourceFile.getName()));
                }
            }
            return success;
        }
        
        private static boolean doCopy(File source, File target) {
            return doCopy(source, target, true);
        }
        
        public static boolean move(File file, File target, boolean log) {
            if (SAFE_MODE && target.exists()) {
                if (log) {
                    logger.warn(ERROR + StringUtility.format("Already exists: {}; skipping in safe mode", Log.logFile(target)));
                }
            } else {
                if (log) {
                    logger.trace(INDENT + StringUtility.format("Moving: {}", Log.logFile(file, false)));
                }
                if (!TEST_MODE) {
                    if (!Filesystem.move(file, target, true)) {
                        if (log) {
                            logger.error(INDENT + ERROR + StringUtility.format("Failed to move: {}", Log.logFile(file)));
                        }
                        return false;
                    }
                }
            }
            return true;
        }
        
        public static boolean move(File file, File target) {
            return move(file, target, true);
        }
        
        public static boolean delete(File file, boolean log) {
            if (file.exists()) {
                if (SAFE_MODE) {
                    if (log) {
                        logger.warn(ERROR + StringUtility.format("Deleting: {}; skipping in safe mode", Log.logFile(file, false)));
                    }
                } else {
                    if (log) {
                        logger.trace(INDENT + StringUtility.format("Deleting: {}", Log.logFile(file, false)));
                    }
                    if (!TEST_MODE) {
                        return doDelete(file, false);
                    }
                }
            }
            return true;
        }
        
        public static boolean delete(File file) {
            return delete(file, true);
        }
        
        private static boolean doDelete(File file, boolean log) {
            boolean success = true;
            if (file.isDirectory()) {
                try {
                    Files.setAttribute(file.toPath(), "dos:readonly", false);
                } catch (Exception ignored) {
                }
                File[] f = Optional.ofNullable(file.listFiles()).orElse(new File[] {});
                for (File sourceFile : f) {
                    success &= doDelete(sourceFile, log);
                }
            }
            try {
                Files.delete(file.toPath());
            } catch (Exception e) {
                try {
                    Files.setAttribute(file.toPath(), "dos:readonly", false);
                    Files.delete(file.toPath());
                } catch (Exception e2) {
                    if (log) {
                        logger.error(INDENT + ERROR + StringUtility.format("Failed to delete: {}", Log.logFile(file)));
                    }
                    success = false;
                }
            }
            return success;
        }
        
        private static boolean doDelete(File file) {
            return doDelete(file, true);
        }
        
        public static boolean mkdir(File file, boolean log) {
            if (!file.exists()) {
                if (log) {
                    logger.trace(INDENT + StringUtility.format("Creating: {}", Log.logFile(file, false)));
                }
                if (!TEST_MODE) {
                    if (!Filesystem.createDirectory(file)) {
                        if (log) {
                            logger.error(ERROR + StringUtility.format("Failed to create: {}", Log.logFile(file)));
                        }
                        return false;
                    }
                }
            }
            return true;
        }
        
        public static boolean mkdir(File file) {
            return mkdir(file, true);
        }
        
        public static boolean compress(File file, boolean openDir, File target, boolean slow, String password, boolean deleteAfter, boolean log) {
            if (SAFE_MODE && target.exists()) {
                if (log) {
                    logger.warn(ERROR + StringUtility.format("Already exists: {}; skipping in safe mode", Log.logFile(target)));
                }
            } else {
                if (log) {
                    logger.trace(INDENT + StringUtility.format("Compressing: {}", Log.logFile(target, false)));
                }
                if (!TEST_MODE) {
                    RarUtil.archiveFile(file, openDir, target, slow, password, deleteAfter);
                }
            }
            return true;
        }
        
        public static boolean compress(File file, boolean openDir, File target, boolean slow, String password, boolean deleteAfter) {
            return compress(file, openDir, target, slow, password, deleteAfter, true);
        }
        
        public static boolean rsync(File sourceDir, File targetDir, boolean log) {
            if (!sourceDir.exists()) {
                if (log) {
                    logger.error(INDENT + "Source directory: " + Log.logFile(sourceDir) + " could not be found");
                }
                return false;
            }
            if (!targetDir.exists()) {
                if (log) {
                    logger.error(INDENT + "Target directory: " + Log.logFile(targetDir) + " could not be found");
                }
                return false;
            }
            
            if (SAFE_MODE) {
                if (log) {
                    logger.warn(ERROR + StringUtility.format("Rsyncing: {}; skipping in safe mode", Log.logFile(targetDir)));
                }
            } else {
                if (log) {
                    logger.trace(INDENT + StringUtility.format("Rsyncing: {}", Log.logFile(targetDir)));
                }
                if (!TEST_MODE) {
                    return RsyncUtil.rsync(sourceDir, targetDir);
                }
            }
            return true;
        }
        
        public static boolean rsync(File sourceDir, File targetDir) {
            return rsync(sourceDir, targetDir, true);
        }
        
        @SuppressWarnings("BusyWait")
        public static void wait(File file) {
            long size;
            do {
                size = file.length();
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException ignored) {
                }
            } while (size != file.length());
        }
        
    }
    
    public static final class Stamper {
        
        //Constants
        
        public static final String DATE_STAMP_PATTERN = "yyyyMMdd";
        
        public static final SimpleDateFormat DATE_STAMP_FORMAT = new SimpleDateFormat(DATE_STAMP_PATTERN);
        
        public static final Date DATE = new Date();
        
        public static final String DATE_STAMP = DATE_STAMP_FORMAT.format(DATE);
        
        public static final Date ONE_WEEK_AGO = Date.from(Stamper.DATE.toInstant().minus(7, ChronoUnit.DAYS));
        
        public static final Date ONE_MONTH_AGO = Date.from(Stamper.DATE.toInstant().minus(30, ChronoUnit.DAYS));
        
        public static final Date ONE_YEAR_AGO = Date.from(Stamper.DATE.toInstant().minus(365, ChronoUnit.DAYS));
        
        
        //Static Methods
        
        public static String formatDate(Date date) {
            return new SimpleDateFormat(DATE_STAMP_PATTERN).format(date);
        }
        
        public static Date parseDate(String date) {
            try {
                return new SimpleDateFormat(DATE_STAMP_PATTERN).parse(date);
            } catch (Exception ignored) {
                return null;
            }
        }
        
        public static Date extract(String backupName) {
            return parseDate(backupName
                    .replaceAll("^.+(?:\\s*-\\s*)?(?<dateStamp>\\d{8})\\.[^.]+$", "$1"));
        }
        
        public static Date extract(File backupFile) {
            return extract(backupFile.getName());
        }
        
        public static String stamp(String backupName) {
            return baseName(backupName) +
                    ((backupName.isEmpty() || backupName.startsWith(".")) ? "" : "-") + DATE_STAMP +
                    (backupName.contains(".") ? backupName.substring(backupName.indexOf('.')) : "");
        }
        
        public static File stamp(File backupFile) {
            return new File(backupFile.getParentFile(), stamp(backupFile.getName()));
        }
        
        public static String unstamp(String backupName) {
            return backupName.replaceAll("(^|\\s*-\\s*)\\d{8}", "");
        }
        
        public static File unstamp(File backupFile) {
            return new File(backupFile.getParentFile(), unstamp(backupFile.getName()));
        }
        
        public static String baseName(String backupName) {
            return unstamp(backupName).replaceAll("\\..+$", "");
        }
        
        public static String baseName(File backupFile) {
            return baseName(backupFile.getName());
        }
        
    }
    
    public static final class Search {
        
        //Static Methods
        
        public static Map<Date, File> map(File backupDir, String baseName) {
            return Filesystem.getFilesAndDirs(backupDir,
                            f -> (StringUtility.isNullOrBlank(baseName) || Stamper.baseName(f).equals(baseName)),
                            d -> (StringUtility.isNullOrBlank(baseName) || Stamper.baseName(d).equals(baseName)))
                    .stream()
                    .map(e -> Optional.ofNullable(Stamper.extract(e))
                            .map(e2 -> Map.entry(e2, e)).orElse(null))
                    .filter(Objects::nonNull)
                    .sorted(Map.Entry.comparingByKey())
                    .collect(MapCollectors.toLinkedHashMap());
        }
        
        public static Map<Date, File> map(File backupDir) {
            return map(backupDir, null);
        }
        
        public static List<File> list(File backupDir, String baseName) {
            return new ArrayList<>(map(backupDir, baseName).values());
        }
        
        public static List<File> list(File backupDir) {
            return list(backupDir, null);
        }
        
        public static List<Date> listDates(File backupDir, String baseName) {
            return new ArrayList<>(map(backupDir, baseName).keySet());
        }
        
        public static List<Date> listDates(File backupDir) {
            return listDates(backupDir, null);
        }
        
        public static File getOldest(File backupDir, String baseName) {
            return list(backupDir, baseName).stream()
                    .sorted(Comparator.naturalOrder())
                    .limit(1).findFirst().orElse(null);
        }
        
        public static File getOldest(File backupDir) {
            return getOldest(backupDir, null);
        }
        
        public static Date getOldestDate(File backupDir, String baseName) {
            return listDates(backupDir, baseName).stream()
                    .sorted(Comparator.naturalOrder())
                    .limit(1).findFirst().orElse(null);
        }
        
        public static Date getOldestDate(File backupDir) {
            return getOldestDate(backupDir, null);
        }
        
        public static File getNewest(File backupDir, String baseName) {
            return list(backupDir, baseName).stream()
                    .sorted(Comparator.reverseOrder())
                    .limit(1).findFirst().orElse(null);
        }
        
        public static File getNewest(File backupDir) {
            return getNewest(backupDir, null);
        }
        
        public static Date getNewestDate(File backupDir, String baseName) {
            return listDates(backupDir, baseName).stream()
                    .sorted(Comparator.reverseOrder())
                    .limit(1).findFirst().orElse(null);
        }
        
        public static Date getNewestDate(File backupDir) {
            return getNewestDate(backupDir, null);
        }
        
        public static List<File> search(File backupDir, String baseName, Date startDate, Date endDate) {
            return map(backupDir, baseName).entrySet().stream()
                    .filter(e -> e.getKey().compareTo(startDate) >= 0)
                    .filter(e -> e.getKey().compareTo(endDate) <= 0)
                    .map(Map.Entry::getValue).collect(Collectors.toList());
        }
        
        public static List<File> search(File backupDir, Date startDate, Date endDate) {
            return search(backupDir, null, startDate, endDate);
        }
        
        public static File find(File backupDir, String baseName, Date date) {
            return map(backupDir, baseName).entrySet().stream()
                    .filter(e -> Stamper.formatDate(e.getKey()).equals(Stamper.formatDate(date)))
                    .findFirst().map(Map.Entry::getValue).orElse(null);
        }
        
        public static File find(File backupDir, Date date) {
            return find(backupDir, null, date);
        }
        
        public static File getForToday(File backupDir, String baseName) {
            return find(backupDir, baseName, Stamper.DATE);
        }
        
        public static File getForToday(File backupDir) {
            return getForToday(backupDir, null);
        }
        
        public static List<File> getForWeek(File backupDir, String baseName) {
            return search(backupDir, baseName, Stamper.ONE_WEEK_AGO, Stamper.DATE);
        }
        
        public static List<File> getForWeek(File backupDir) {
            return getForWeek(backupDir, null);
        }
        
        public static List<File> getForMonth(File backupDir, String baseName) {
            return search(backupDir, baseName, Stamper.ONE_MONTH_AGO, Stamper.DATE);
        }
        
        public static List<File> getForMonth(File backupDir) {
            return getForMonth(backupDir, null);
        }
        
        public static List<File> getForYear(File backupDir, String baseName) {
            return search(backupDir, baseName, Stamper.ONE_YEAR_AGO, Stamper.DATE);
        }
        
        public static List<File> getForYear(File backupDir) {
            return getForYear(backupDir, null);
        }
        
    }
    
    public static final class Log {
        
        //Static Methods
        
        public static String logFile(File file, boolean path) {
            return StringUtility.quote((path ? file.getAbsolutePath().replace(Project.TMP_DIR.getAbsolutePath(), Project.TMP_DIR.getName()) : file.getName()), true);
        }
        
        public static String logFile(File file) {
            return logFile(file, true);
        }
        
        public static String logFile(List<File> files) {
            return files.stream().map(Log::logFile)
                    .collect(Collectors.joining(", ", ((files.size() == 1) ? "" : "[ "), ((files.size() == 1) ? "" : " ]")));
        }
        
        public static String logBaseName(String baseName, boolean leadingSpace) {
            return StringUtility.isNullOrBlank(baseName) ? "" :
                    ((leadingSpace ? " " : "") + StringUtility.quote(baseName, true));
        }
        
        public static String logBaseName(String baseName) {
            return logBaseName(baseName, true);
        }
        
        public static String logStamp(Date date) {
            return Stamper.formatDate(date);
        }
        
    }
    
}
