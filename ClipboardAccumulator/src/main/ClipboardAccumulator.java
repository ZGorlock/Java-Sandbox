/*
 * File:    ClipboardAccumulator.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.access.Clipboard;
import commons.access.CmdLine;
import commons.access.Filesystem;
import commons.access.Project;
import commons.lambda.stream.mapper.Mappers;
import commons.object.string.StringUtility;

public class ClipboardAccumulator {
    
    //Constants
    
    private static final boolean LOG = true;
    
    private static final boolean FILE = true;
    
    private static final boolean CMD = false;
    
    private static final boolean FILTER = true;
    
    private static final boolean FORMAT = true;
    
    private static final boolean TRANSFORM = true;
    
    private static final Pattern PATTERN = Pattern.compile("^\\s*https?://.*$");
    
    private static final String PREPEND = "";
    
    private static final String APPEND = "";
    
    private static final UnaryOperator<String> TRANSFORMER = (String data) ->
            Optional.ofNullable(data)
                    .map(String::strip)
                    .map(e -> e.replaceAll("(\r?\n)+,", ","))
                    .orElse(null);
    
    private static final int DELAY = 200;
    
    private static final File SAVE_FILE = new File(Project.DATA_DIR,
            ("clipboard-" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".txt"));
    
    
    //Static Fields
    
    private static final AtomicReference<String> clipboard = new AtomicReference<>(null);
    
    private static final List<String> history = new ArrayList<>();
    
    private static final List<String> queue = new ArrayList<>();
    
    private static final AtomicBoolean busy = new AtomicBoolean(false);
    
    private static final ExecutorService executor = Executors.newFixedThreadPool(1, task -> {
        final Thread thread = Executors.defaultThreadFactory().newThread(task);
        thread.setDaemon(true);
        return thread;
    });
    
    
    //Main Methods
    
    @SuppressWarnings({"BusyWait", "InfiniteLoopStatement"})
    public static void main(String[] args) throws Exception {
        System.setErr(new PrintStream("log/error.log"));
        
        while (true) {
            Thread.sleep(DELAY);
            cmdDequeue();
            checkClipboard();
        }
    }
    
    
    //Static Methods
    
    private static void checkClipboard() {
        Optional.ofNullable(Clipboard.getClipboard())
                .filter(e -> !StringUtility.isNullOrEmpty(e))
                .filter(e -> !e.equals(clipboard.get()))
                .map(e -> Mappers.perform(e, clipboard::set))
                .ifPresent(ClipboardAccumulator::processClipboard);
    }
    
    private static void processClipboard(String clipboard) {
        Optional.ofNullable(clipboard)
                .filter(ClipboardAccumulator::acceptClipboard)
                .map(ClipboardAccumulator::formatClipboard)
                .ifPresent(ClipboardAccumulator::accumulateClipboard);
    }
    
    private static boolean acceptClipboard(String clipboard) {
        return !FILTER || Optional.ofNullable(clipboard)
                .map(PATTERN::matcher).map(Matcher::matches)
                .orElse(false);
    }
    
    private static String formatClipboard(String clipboard) {
        return Optional.ofNullable(clipboard)
                .map(e -> (!TRANSFORM ? e : TRANSFORMER.apply(e)))
                .map(e -> (!FORMAT ? e : (PREPEND + e + APPEND)))
                .orElse(null);
    }
    
    private static void accumulateClipboard(String clipboard) {
        history.add(clipboard);
        
        if (LOG) {
            System.out.println(clipboard);
        }
        if (FILE) {
            Filesystem.writeLines(SAVE_FILE, history);
        }
        if (CMD) {
            queue.add(clipboard);
        }
    }
    
    private static void cmdDequeue() {
        if (!CMD || (queue.isEmpty() && busy.get())) {
            return;
        }
        executor.execute(ClipboardAccumulator::executeCmd);
    }
    
    private static void executeCmd() {
        if (busy.compareAndSet(false, true)) {
            try {
                final String working = queue.get(0);
                queue.remove(0);
                
                CmdLine.executeCmd(working);
                
            } catch (Exception ignored) {
            } finally {
                busy.set(false);
            }
        }
    }
    
}
