/*
 * File:    RarUtil.java
 * Package: main.util
 * Author:  Zachary Gill
 */

package main.util;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import commons.access.CmdLine;
import commons.access.Filesystem;
import commons.io.console.ProgressBar;
import commons.math.number.BoundUtility;
import commons.object.string.StringUtility;

public final class RarUtil {
    
    //Static Methods
    
    public static void archiveFile(File file, boolean openDir, File archive, boolean slow, String password, boolean deleteAfter) {
        final File log = Filesystem.createTemporaryFile("log");
        final File tmpArchive = new File((openDir ? file : file.getParentFile()), archive.getName());
        final List<File> files = openDir ? Filesystem.getFilesAndDirs(file) : List.of(file);
        
        final String cmd = rarCmd(files, tmpArchive, null, log,
                "a", "rar", "f",
                5, 8, (slow ? 1 : -1), (slow ? 100 : 0), password,
                true, deleteAfter, true, true);
        final ProgressBar progressBar = new ArchivingProgressBar(file, archive, log);
        
        try {
            CmdLine.executeCmd(cmd, true, progressBar);
        } finally {
            if (!archive.getAbsolutePath().equalsIgnoreCase(tmpArchive.getAbsolutePath())) {
                if (!Filesystem.moveFile(tmpArchive, archive)) {
                    System.out.println(BackupUtil.ERROR + StringUtility.format("Failed to move: {} to: {}", StringUtility.quote(tmpArchive.getAbsolutePath(), true), StringUtility.quote(archive.getAbsolutePath(), true)));
                }
            }
            progressBar.complete();
        }
    }
    
    public static void archiveFile(File file, boolean openDir, File archive, boolean slow, String password) {
        archiveFile(file, openDir, archive, slow, password, false);
    }
    
    public static void archiveFile(File file, File archive, boolean slow, String password) {
        archiveFile(file, false, archive, slow, password);
    }
    
    public static void archiveFile(File file, File archive, boolean slow) {
        archiveFile(file, archive, slow, "");
    }
    
    public static void archiveFile(File file, File archive) {
        archiveFile(file, archive, false);
    }
    
    private static String rarCmd(List<File> files, File archive, File outputDir, File log, String mode, String type, String logMode, int quality, int threads, int priority, int sleepTime, String password, boolean recursive, boolean deleteFiles, boolean inBackground, boolean assumeYes) {
        final boolean filesLocalized = Optional.ofNullable(files).map(e -> e.stream().map(File::getParentFile).distinct().count() <= 1).orElse(true);
        final Function<File, String> fileNameMapper = filesLocalized ? File::getName : File::getAbsolutePath;
        
        return String.join(" ",
                ((archive == null) ? "" : String.join(" ",
                        StringUtility.lSnip(archive.getParentFile().getAbsolutePath(), 2), "&&",
                        "cd " + StringUtility.quote(archive.getParentFile().getAbsolutePath()), "&&")),
                "winrar",
                (!StringUtility.isNullOrBlank(mode) ? mode : ""),
                (!StringUtility.isNullOrBlank(type) ? ("-af" + type) : ""),
                (BoundUtility.inBounds(quality, 0, 5) ? ("-m" + quality) : ""),
                ((threads > 0) ? ("-mt" + threads) : ""),
                ((BoundUtility.inBounds(priority, 0, 15) && BoundUtility.inBounds(sleepTime, 0, 1000)) ? ("-ri" + priority + ':' + sleepTime) : ""),
                (StringUtility.isNullOrBlank(password) ? "" : ("-p" + StringUtility.quote(password))),
                (recursive ? "-r" : ""),
                (deleteFiles ? "-df" : ""),
                (inBackground ? "-ibck" : ""),
                (assumeYes ? "-y" : ""),
                (((log != null) && !StringUtility.isNullOrBlank(logMode)) ? ("-log" + logMode + '=' + StringUtility.quote(log.getAbsolutePath())) : ""),
                (!StringUtility.isNullOrBlank(logMode) ? "-inull" : ""),
                ((archive != null) ? StringUtility.quote(archive.getName()) : ""),
                ((files != null) ? files.stream().filter(Objects::nonNull).map(fileNameMapper).map(StringUtility::quote).collect(Collectors.joining(" ")) : ""),
                ((outputDir != null) ? StringUtility.quote(outputDir.getAbsolutePath() + "\\") : "")
        );
    }
    
    
    //Inner Classes
    
    private static class ArchivingProgressBar extends ProgressBar {
        
        //Fields
        
        private final File file;
        
        private final File archive;
        
        private final File logFile;
        
        private final String lastEntry;
        
        private final ScheduledExecutorService thread;
        
        
        //Constructors
        
        public ArchivingProgressBar(File file, File archive, File logFile) {
            super(archive.getName(), (1 + (file.isDirectory() ? Filesystem.getFilesAndDirsRecursively(file).size() : 0)));
            
            this.file = file;
            this.archive = archive;
            this.logFile = logFile;
            this.lastEntry = file.getAbsolutePath()
                    .replace(archive.getParentFile().getAbsolutePath(), "")
                    .replaceAll("^[\\\\/]", "");
            
            this.thread = Executors.newSingleThreadScheduledExecutor(task -> {
                final Thread thread = Executors.defaultThreadFactory().newThread(task);
                thread.setName("ArchiveLogReader");
                thread.setDaemon(true);
                return thread;
            });
            this.thread.scheduleAtFixedRate(() ->
                    processLog(""), 20, 20, TimeUnit.MILLISECONDS);
        }
        
        
        //Methods
        
        @Override
        public synchronized boolean processLog(String log, boolean error) {
            if (!logFile.exists()) {
                return false;
            }
            
            final List<String> logLines = Filesystem.readLines(logFile);
            update(logLines.size() - 1);
            
            if (logLines.contains(lastEntry)) {
                complete();
            } else if (error && log.startsWith("'winrar' is not recognized")) {
                fail(false, log);
            }
            return true;
        }
        
        @Override
        public synchronized void complete(boolean printTime, String additionalInfo) {
            super.complete(printTime, additionalInfo);
            thread.shutdownNow();
        }
        
        @Override
        public synchronized void fail(boolean printTime, String additionalInfo) {
            super.fail(printTime, additionalInfo);
            thread.shutdownNow();
        }
        
    }
    
}
