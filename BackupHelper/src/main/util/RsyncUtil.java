/*
 * File:    RsyncUtil.java
 * Package: main.util
 * Author:  Zachary Gill
 */

package main.util;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.github.fracpete.processoutput4j.core.StreamingProcessOutputType;
import com.github.fracpete.processoutput4j.core.StreamingProcessOwner;
import com.github.fracpete.processoutput4j.output.StreamingProcessOutput;
import com.github.fracpete.rsync4j.RSync;
import commons.io.console.ProgressBar;
import commons.object.string.StringUtility;

public final class RsyncUtil {
    
    //Static Methods
    
    public static boolean rsync(File sourceDir, File targetDir) {
        final Function<File, String> fileArgumentFormatter = (File fileArgument) ->
                StringUtility.fixFileSeparators(fileArgument.getAbsolutePath()) + "/.";
        
        final RSync rsync = new RSync()
                .source(fileArgumentFormatter.apply(sourceDir))
                .destination(fileArgumentFormatter.apply(targetDir))
                .recursive(true).archive(false)
                .delete(true).deleteAfter(true).force(true)
                .perms(true).acls(true).owner(true).group(true).times(true).xattrs(true)
                .progress(false).itemizeChanges(true).verbose(false);
        
        final RsyncProgressBar progressBar = new RsyncProgressBar(sourceDir, targetDir);
        final StreamingProcessOutput processOutput = new StreamingProcessOutput(progressBar);
        
        try {
            rsync.dryRun(true);
            processOutput.monitor(rsync.builder());
            
            progressBar.beginProcessing();
            
            rsync.dryRun(false);
            processOutput.monitor(rsync.builder());
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (processOutput.getExitCode() > 0) {
                progressBar.fail();
            } else {
                progressBar.complete();
            }
        }
        return (processOutput.getExitCode() == 0);
    }
    
    
    //Inner Classes
    
    private static class RsyncProgressBar extends ProgressBar implements StreamingProcessOwner {
        
        //Fields
        
        private final AtomicInteger actions = new AtomicInteger(0);
        
        private final AtomicBoolean processing = new AtomicBoolean(false);
        
        
        //Constructors
        
        public RsyncProgressBar(File source, File target) {
            super(target.getName(), 0);
        }
        
        
        //Methods
        
        @Override
        public synchronized boolean processLog(String log, boolean error) {
//            System.out.println(log);
            if (!processing.get()) {
                actions.incrementAndGet();
            } else if (!error) {
                return addOne();
            }
            return false;
        }
        
        public synchronized void beginProcessing() {
            updateTotal(actions.get());
            processing.set(true);
        }
        
        @Override
        public StreamingProcessOutputType getOutputType() {
            return StreamingProcessOutputType.BOTH;
        }
        
        @Override
        public void processOutput(String line, boolean stdout) {
            processLog(line, !stdout);
        }
        
    }
    
}
