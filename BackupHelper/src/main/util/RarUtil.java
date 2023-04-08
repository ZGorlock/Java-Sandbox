/*
 * File:    RarUtil.java
 * Package: main.util
 * Author:  Zachary Gill
 */

package main.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.access.CmdLine;
import commons.access.Filesystem;
import commons.io.console.ProgressBar;
import commons.math.number.BoundUtility;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RarUtil {
    
    //Logger
    
    private static final Logger logger = LoggerFactory.getLogger(RarUtil.class);
    
    
    //Enums
    
    public enum Mode {
        
        //Values
        
        ADD_FILES("a"),
        ADD_COMMENT("c"),
        CHANGE_PARAMETERS("ch"),
        CONVERT_ARCHIVE("cv"),
        WRITE_COMMENT_TO_FILE("cw"),
        DELETE_FILES("d"),
        EXTRACT_FILES_IGNORE_PATHS("e"),
        FRESHEN_FILES("f"),
        SEARCH_ARCHIVE("i"),
        LOCK_ARCHIVE("k"),
        MOVE_FILES("m"),
        REPAIR_ARCHIVE("r"),
        RECONSTRUCT_VOLUMES("rc"),
        RENAME_FILES("rn"),
        ADD_DATA_RECOVERY_RECORD("rr"),
        CREATE_RECOVERY_VOLUMES("rv"),
        ADD_SFX_MODULE("s"),
        REMOVE_SFX_MODULE("s-"),
        TEST_FILES("t"),
        UPDATE_FILES("u"),
        EXTRACT_FILES("x");
        
        
        //Fields
        
        private final String flag;
        
        
        //Constructors
        
        Mode(String flag) {
            this.flag = flag;
        }
        
        
        //Getters
        
        public String getFlag() {
            return flag;
        }
        
    }
    
    public enum CompressionMethod {
        
        //Values
        
        STORE(0),
        FASTEST(1),
        FAST(2),
        NORMAL(3),
        GOOD(4),
        BEST(5);
        
        
        //Fields
        
        private final int compression;
        
        
        //Constructors
        
        CompressionMethod(int compression) {
            this.compression = compression;
        }
        
        
        //Getters
        
        public int getMethod() {
            return compression;
        }
        
    }
    
    public enum DictionarySize {
        
        //Values
        
        K_64("64k", 64000),
        K_128("128k", 128000),
        K_256("256k", 256000),
        K_512("512k", 512000),
        M_1("1m", 1000000),
        M_2("2m", 2000000),
        M_4("4m", 4000000),
        M_8("8m", 8000000),
        M_16("16m", 16000000),
        M_32("32m", 32000000),
        M_64("64m", 64000000),
        M_128("128m", 128000000),
        M_256("256m", 256000000),
        M_512("512m", 512000000),
        G_1("1g", 1000000000);
        
        
        //Fields
        
        private final String size;
        
        private final int sizeInBytes;
        
        
        //Constructors
        
        DictionarySize(String size, int sizeInBytes) {
            this.size = size;
            this.sizeInBytes = sizeInBytes;
        }
        
        
        //Getters
        
        public String getSize() {
            return size;
        }
        
        public int getSizeInBytes() {
            return sizeInBytes;
        }
        
    }
    
    public enum HashType {
        
        //Values
        
        CRC32("c"),
        BLAKE2("b");
        
        
        //Fields
        
        private final String flag;
        
        
        //Constructors
        
        HashType(String flag) {
            this.flag = flag;
        }
        
        
        //Getters
        
        public String getFlag() {
            return flag;
        }
        
    }
    
    public enum ArchiveFormat {
        
        //Values
        
        RAR("rar"),
        ZIP("zip");
        
        
        //Fields
        
        private final String format;
        
        
        //Constructors
        
        ArchiveFormat(String format) {
            this.format = format;
        }
        
        
        //Getters
        
        public String getFormat() {
            return format;
        }
        
    }
    
    public enum ArchivingVersion {
        
        //Values
        
        RAR_4_X(4),
        RAR_5_0(5);
        
        
        //Fields
        
        private final int version;
        
        
        //Constructors
        
        ArchivingVersion(int version) {
            this.version = version;
        }
        
        
        //Getters
        
        public int getVersion() {
            return version;
        }
        
    }
    
    public enum LogMode {
        
        //Values
        
        ARCHIVES("a"),
        FILES("f"),
        APPEND("p"),
        UNICODE("u");
        
        
        //Fields
        
        private final String flag;
        
        
        //Constructors
        
        LogMode(String flag) {
            this.flag = flag;
        }
        
        
        //Getters
        
        public String getFlag() {
            return flag;
        }
        
    }
    
    public enum CharsetSpec {
        
        //Values
        
        UNICODE("u"),
        NATIVE("a"),
        OEM("o"),
        LOG_FILES("g"),
        LIST_FILES("l"),
        COMMENT_FILES("c");
        
        
        //Fields
        
        private final String flag;
        
        
        //Constructors
        
        CharsetSpec(String flag) {
            this.flag = flag;
        }
        
        
        //Getters
        
        public String getFlag() {
            return flag;
        }
        
    }
    
    
    //Static Methods
    
    public static boolean archiveFile(File source, boolean openDir, File archive, boolean slow, String password, boolean deleteAfter) {
        final List<File> sourceFiles = (source.isDirectory() && openDir) ? Filesystem.getFilesAndDirs(source) : List.of(source);
        final File archiveFile = Optional.ofNullable(archive).orElseGet(() -> new File(source.getParentFile(), source.getName().replaceAll("(?<=\\.)[^.]+$", ArchiveFormat.RAR.getFormat())));
        final File outputDir = Optional.of(archiveFile).map(File::getParentFile).filter(Filesystem::createDirectory).orElse(null);
        final File workDir = Filesystem.createTemporaryDirectory(archiveFile.getName());
        final File logFile = Filesystem.createTemporaryFile("log", archiveFile.getName());
        
        final String cmd = rarCmd(sourceFiles, archiveFile, null, workDir, logFile,
                Mode.ADD_FILES.getFlag(), ArchiveFormat.RAR.getFormat(), ArchivingVersion.RAR_5_0.getVersion(),
                CompressionMethod.BEST.getMethod(), DictionarySize.M_64.getSize(), HashType.CRC32.getFlag(), null,
                8, (slow ? 1 : -1), (slow ? 100 : 0), true, true,
                false, true, null,
                false, true, true, -1,
                true, true, true,
                deleteAfter, true, false,
                password, true,
                LogMode.FILES.getFlag(), true, true);
        
        final ProgressBar progressBar = new ArchivingProgressBar(source, archiveFile, logFile);
        try {
            logger.trace(cmd);
            final String response = CmdLine.executeCmd(cmd, true, progressBar);
            if (!StringUtility.isNullOrBlank(response)) {
                logger.debug(response);
            }
        } catch (Exception e) {
            progressBar.fail(false, e.getMessage());
        } finally {
            progressBar.complete(true);
        }
        return !progressBar.isFailed();
    }
    
    public static void archiveFile(File source, boolean openDir, File archive, boolean slow, String password) {
        archiveFile(source, openDir, archive, slow, password, false);
    }
    
    public static void archiveFile(File source, boolean openDir, File archive, boolean slow) {
        archiveFile(source, openDir, archive, slow, "");
    }
    
    public static void archiveFile(File source, boolean openDir, File archive) {
        archiveFile(source, openDir, archive, false);
    }
    
    public static void archiveFile(File source, File archive, boolean slow, String password) {
        archiveFile(source, false, archive, slow, password);
    }
    
    public static void archiveFile(File source, File archive, boolean slow) {
        archiveFile(source, archive, slow, "");
    }
    
    public static void archiveFile(File source, File archive) {
        archiveFile(source, archive, false);
    }
    
    private static String rarCmd(List<File> sourceFiles, File archiveFile, File outputDir, File workDir, File logFile,
            String mode, String format, int version,
            int method, String dictionarySize, String hashType, String profile,
            int threads, int priority, int sleepTime, boolean inBackground, boolean assumeYes,
            boolean recursive, boolean excludeBasePath, String internalPath,
            boolean preserveHardLinks, boolean preserveSoftLinks, boolean identicalFileReferences, int fileReferenceMinSize,
            boolean saveFileSecurityInfo, boolean saveNtfsStreams, boolean setNtfsCompressedAttribute,
            boolean deleteFiles, boolean deleteToRecycleBin, boolean wipeDeletedFiles,
            String password, boolean encryptFileNames,
            String logMode, boolean logErrors, boolean useUnicode) {
        return Stream.of(
                        "winrar",
                        (!StringUtility.isNullOrBlank(mode) ? mode : ""),
                        (!StringUtility.isNullOrBlank(format) ? ("-af" + format) : ""),
                        (BoundUtility.inBounds(version, 4, 5) ? ("-ma" + version) : ""),
                        (BoundUtility.inBounds(method, 0, 5) ? ("-m" + method) : ""),
                        (!StringUtility.isNullOrBlank(dictionarySize) ? ("-md" + dictionarySize) : ""),
                        (!StringUtility.isNullOrBlank(hashType) ? ("-ht" + ((version >= ArchivingVersion.RAR_5_0.getVersion()) ? hashType : HashType.CRC32.getFlag())) : ""),
                        (!StringUtility.isNullOrBlank(profile) ? ("-cp" + StringUtility.quote(profile)) : "-cfg-"),
                        ((threads > 0) ? ("-mt" + threads) : ""),
                        ((BoundUtility.inBounds(priority, 0, 15) && BoundUtility.inBounds(sleepTime, 0, 1000)) ? ("-ri" + priority + ':' + sleepTime) : ""),
                        (recursive ? "-r" : "-r0"),
                        (excludeBasePath ? "-ep1" : ""),
                        (!StringUtility.isNullOrBlank(internalPath) ? ("-ap" + StringUtility.quote(internalPath)) : ""),
                        ((preserveHardLinks && (version >= ArchivingVersion.RAR_5_0.getVersion())) ? "-oh" : ""),
                        ((preserveSoftLinks && (version >= ArchivingVersion.RAR_5_0.getVersion())) ? "-ol" : ""),
                        ((identicalFileReferences && (version >= ArchivingVersion.RAR_5_0.getVersion())) ? ("-oi" + ((fileReferenceMinSize > 0) ? (":" + fileReferenceMinSize) : "")) : "-oi-"),
                        (saveFileSecurityInfo ? "-ow" : ""),
                        (saveNtfsStreams ? "-os" : ""),
                        (setNtfsCompressedAttribute ? "-oc" : ""),
                        (deleteFiles ? (deleteToRecycleBin ? "-dr" : (wipeDeletedFiles ? "-dw" : "-df")) : ""),
                        (!StringUtility.isNullOrBlank(password) ? ((encryptFileNames ? "-hp" : "-p") + StringUtility.quote(password)) : ""),
                        ((logErrors) ? ("-ilog") : "-inul"),
                        (inBackground ? "-ibck" : ""),
                        (useUnicode ? "-scuglc" : ""),
                        (assumeYes ? "-y" : ""),
                        ((logFile != null) ? ("-log" + (!StringUtility.isNullOrBlank(logMode) ? (logMode + (useUnicode ? LogMode.UNICODE.getFlag() : "")) : "") + "=" + StringUtility.quote(logFile.getAbsolutePath())) : ""),
                        ((workDir != null) ? ("-w" + StringUtility.quote(workDir.getAbsolutePath())) : ""),
                        ((archiveFile != null) ? (StringUtility.quote(archiveFile.getAbsolutePath())) : ""),
                        ((sourceFiles != null) ? (rarCmdEncodeFileList(sourceFiles, useUnicode)) : "")
//                        ((outputDir != null) ? (StringUtility.quote(outputDir.getAbsolutePath()) + "\\") : "")
                )
                .filter(e -> !StringUtility.isNullOrBlank(e))
                .collect(Collectors.joining(" "));
    }
    
    private static String rarCmdEncodeFileList(List<File> files, boolean forceExternal) {
        final List<String> fileList = files.stream().filter(Objects::nonNull).map(File::getAbsolutePath).collect(Collectors.toList());
        final boolean containsUnicode = fileList.stream().flatMap(StringUtility::charStream).map(Character.UnicodeBlock::of).anyMatch(e -> !e.equals(Character.UnicodeBlock.BASIC_LATIN));
        
        return (forceExternal || containsUnicode) ?
                Optional.of(Filesystem.createTemporaryFile("lst"))
                        .filter(externalFile -> Filesystem.writeByteArrayToFile(externalFile,
                                StringUtility.unsplitLines(fileList).getBytes(StandardCharsets.UTF_16)))
//                        .filter(externalFile -> Filesystem.writeLines(externalFile, fileList, StandardCharsets.UTF_16))
//                        .filter(externalFile -> Optional.of(fileList)
//                                .map(StringUtility::unsplitLines)
//                                .map(StandardCharsets.UTF_16::encode).map(ByteBuffer::array)
//                                .map(e -> Filesystem.writeByteArrayToFile(externalFile, e))
//                                .orElse(false))
                        //fileList.stream().collect(Collectors.joining(System.lineSeparator(), "\ufeff", "")), StandardCharsets.UTF_16BE))
                        .map(File::getAbsolutePath).map(StringUtility::quote).map(e -> ("@" + e))
                        .orElse("") :
                fileList.stream().map(StringUtility::quote).collect(Collectors.joining(" "));
    }
    
    private static String rarCmdEncodeFileList(List<File> files, int directListMaxSize) {
        return rarCmdEncodeFileList(files, (files.size() > directListMaxSize));
    }
    
    private static String rarCmdEncodeFileList(List<File> files) {
        return rarCmdEncodeFileList(files, 10);
    }
    
    
    //Inner Classes
    
    private static class ArchivingProgressBar extends ProgressBar {
        
        //Constants
        
        private static final boolean TRACK_FILE_SIZES = false;
        
        
        //Fields
        
        private final File source;
        
        private final File archive;
        
        private final Contents contents;
        
        private final File logFile;
        
        private final List<String> logCache;
        
        private final ScheduledExecutorService thread;
        
        
        //Constructors
        
        public ArchivingProgressBar(File source, File archive, File logFile) {
            super(archive.getName(), 1);
            
            this.source = source;
            this.archive = archive;
            this.contents = calculateContents(source, archive);
            this.logFile = logFile;
            this.logCache = new ArrayList<>();
            
            updateTotal(TRACK_FILE_SIZES ? contents.getTotalSizeInKb() : contents.getTotalCount());
            updateUnits(TRACK_FILE_SIZES ? "KB" : "");
            setUseCommas(true);
            
            this.thread = Executors.newSingleThreadScheduledExecutor(task -> {
                final Thread thread = Executors.defaultThreadFactory().newThread(task);
                thread.setName("ArchiveLogReader");
                thread.setDaemon(true);
                return thread;
            });
            this.thread.scheduleAtFixedRate(() ->
                    processLog(""), 0, 100, TimeUnit.MILLISECONDS);
        }
        
        
        //Methods
        
        @Override
        public synchronized boolean processLog(String log, boolean error) {
            if (!logFile.exists()) {
                return false;
            }
            
            if (error && (log.startsWith("--------") || log.startsWith("'winrar' is not recognized"))) {
                fail(false, log);
            }
            
            final List<String> logLines = Filesystem.readLines(logFile, StandardCharsets.UTF_16LE);
            if (logLines.size() > logCache.size()) {
                logLines.stream()
                        .filter(e -> !logCache.contains(e))
                        .peek(contents::setProcessed)
                        .forEachOrdered(logCache::add);
                update(TRACK_FILE_SIZES ? contents.getProcessedTotalSizeInKb() : contents.getProcessedCount());
            }
            
            if (contents.isComplete()) {
                complete(true);
            }
            return true;
        }
        
        @Override
        public synchronized void complete(boolean printTime, String additionalInfo) {
            if (!archive.exists()) {
                fail(printTime, additionalInfo);
            } else {
                super.complete(printTime, additionalInfo);
                thread.shutdownNow();
            }
        }
        
        @Override
        public synchronized void fail(boolean printTime, String additionalInfo) {
            super.fail(printTime, additionalInfo);
            thread.shutdownNow();
        }
        
        
        //Static Methods
        
        private static Contents calculateContents(File source, File archive) {
            final List<File> fileContents = source.isDirectory() ? Filesystem.getFilesAndDirsRecursively(source) : List.of(source);
            
            final Contents contents = new Contents(source, archive);
            fileContents.stream()
                    .map(Contents.Content::new)
                    .forEachOrdered(contents::add);
            return contents;
        }
        
        
        //Inner Classes
        
        private static class Contents extends ArrayList<Contents.Content> {
            
            //Fields
            
            private final Map<File, Content> fileMap;
            
            private final Map<String, Content> pathMap;
            
            
            //Constructors
            
            public Contents(File source, File archive) {
                super();
                
                this.fileMap = new LinkedHashMap<>();
                this.pathMap = new LinkedHashMap<>();
            }
            
            
            //Methods
            
            @Override
            public synchronized boolean add(Content content) {
                if (super.add(content)) {
                    fileMap.put(content.getFile(), content);
                    pathMap.put(content.getPath(), content);
                    return true;
                }
                return false;
            }
            
            @Override
            public synchronized void add(int index, Content content) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public synchronized boolean addAll(Collection<? extends Content> content) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public synchronized boolean addAll(int index, Collection<? extends Content> content) {
                throw new UnsupportedOperationException();
            }
            
            public synchronized Content get(File file) {
                return Optional.ofNullable(file).map(fileMap::get).orElse(null);
            }
            
            public synchronized Content get(String path) {
                return Optional.ofNullable(path).map(pathMap::get).orElse(null);
            }
            
            
            //Getters
            
            public synchronized int getTotalCount() {
                return size();
            }
            
            public synchronized long getTotalSize() {
                return stream().mapToLong(Content::getSize).sum();
            }
            
            public synchronized long getTotalSizeInKb() {
                return (getTotalSize() / 1024L);
            }
            
            public synchronized List<Content> getDirectories() {
                return stream().filter(Content::isDirectory).collect(Collectors.toList());
            }
            
            public synchronized List<Content> getFiles() {
                return stream().filter(Content::isFile).collect(Collectors.toList());
            }
            
            public synchronized List<Content> getProcessed() {
                return stream().filter(Content::isProcessed).collect(Collectors.toList());
            }
            
            public synchronized List<Content> getUnprocessed() {
                return stream().filter(Content::isUnprocessed).collect(Collectors.toList());
            }
            
            public synchronized int getProcessedCount() {
                return (int) stream().filter(Content::isProcessed).count();
            }
            
            public synchronized int getUnprocessedCount() {
                return (int) stream().filter(Content::isUnprocessed).count();
            }
            
            public synchronized long getProcessedTotalSize() {
                return stream().filter(Content::isProcessed).mapToLong(Content::getSize).sum();
            }
            
            public synchronized long getProcessedTotalSizeInKb() {
                return (getProcessedTotalSize() / 1024L);
            }
            
            public synchronized long getUnprocessedTotalSize() {
                return stream().filter(Content::isUnprocessed).mapToLong(Content::getSize).sum();
            }
            
            public synchronized long getUnprocessedTotalSizeInKb() {
                return (getUnprocessedTotalSize() / 1024L);
            }
            
            public synchronized boolean isComplete() {
                return ((getUnprocessedCount() == 0) && (getProcessedCount() > 0) && (getProcessedCount() == getTotalCount()));
            }
            
            
            //Setters
            
            public synchronized void setProcessed(File file, boolean processed) {
                Optional.ofNullable(file).map(this::get).ifPresent(e -> e.setProcessed(processed));
            }
            
            public synchronized void setProcessed(File file) {
                setProcessed(file, true);
            }
            
            public synchronized void setProcessed(String path, boolean processed) {
                Optional.ofNullable(path).map(this::get).ifPresent(e -> e.setProcessed(processed));
            }
            
            public synchronized void setProcessed(String path) {
                setProcessed(path, true);
            }
            
            
            //Inner Classes
            
            private static class Content {
                
                //Fields
                
                public File file;
                
                public String path;
                
                public long size;
                
                public boolean directory;
                
                public boolean processed;
                
                
                //Constructors
                
                public Content(File file) {
                    this.file = file;
                    this.path = this.file.getAbsolutePath();
                    this.size = (this.file.isDirectory() ? 0L : this.file.length());
                    this.directory = this.file.isDirectory();
                    this.processed = false;
                }
                
                
                //Getters
                
                public File getFile() {
                    return file;
                }
                
                public String getPath() {
                    return path;
                }
                
                public long getSize() {
                    return size;
                }
                
                public boolean isDirectory() {
                    return directory;
                }
                
                public boolean isFile() {
                    return !isDirectory();
                }
                
                public boolean isProcessed() {
                    return processed;
                }
                
                public boolean isUnprocessed() {
                    return !isProcessed();
                }
                
                
                //Setters
                
                public void setProcessed(boolean processed) {
                    this.processed = processed;
                }
                
                public void setProcessed() {
                    setProcessed(true);
                }
                
                public void setUnprocessed() {
                    setProcessed(false);
                }
                
            }
            
        }
        
    }
    
}
