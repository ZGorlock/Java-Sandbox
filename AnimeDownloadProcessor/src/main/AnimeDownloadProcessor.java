/*
 * File:    AnimeDownloadProcessor.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;

import common.CmdLine;
import common.Filesystem;

public class AnimeDownloadProcessor {
    
    public static final File videoDir = new File("E:\\Downloads\\Videos");
    
    public static void main(String[] args) {
        File source = new File(videoDir, "old");
        File dest = new File(videoDir.getAbsolutePath());
        
        for (File f : Filesystem.getFilesRecursively(source)) {
            if (f.getAbsolutePath().contains("\\old\\old\\") || !f.getName().endsWith("mp4")) {
                continue;
            }
            File output = new File(f.getAbsolutePath().replace("old\\", ""));
            String cmd = "-y -i \"" + f.getAbsolutePath() + "\" -map_metadata -1 -map_chapters -1 -map 0 -c copy -c:s mov_text \"" + output.getAbsolutePath() + "\"";
            ffmpeg(cmd, true);
        }
    }
    
    private static String ffmpeg(String cmd, boolean printOutput) {
        cmd = "ffmpeg -hide_banner " + cmd;
        if (printOutput) {
            System.out.println(cmd);
        }
        CmdLine.printOutput = printOutput;
        String cmdLog = CmdLine.executeCmd(cmd);
        CmdLine.printOutput = false;
        if (cmdLog.contains("Error") && !cmd.contains("Error")) {
            System.err.println("Error in ffmpeg: " + cmd);
        }
//        Filesystem.writeStringToFile(log, cmdLog + "\n" + StringUtility.fillStringOfLength('-', 120) + "\n\n", true);
        return cmdLog;
    }
    
}
