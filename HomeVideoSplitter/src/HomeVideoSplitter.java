/*
 * File:    HomeVideoSplitter.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class HomeVideoSplitter {
    
    //Constants
    
    private static final File segmentFile = new File("todo.csv");
    
    private static final File videoDir = new File("todo");
    
    private static final File outputDir = new File("output");
    
    
    //Static Fields
    
    private static final List<Segment> segments = new ArrayList<>();
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        try {
            loadSegments();
            prepareVideo();
            processSegments();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    
    //Methods
    
    private static void loadSegments() throws Exception {
        if (!segmentFile.exists()) {
            System.err.println("Cannot find segment file: " + segmentFile.getAbsolutePath());
            return;
        }
        
        List<String> lines = Files.readAllLines(segmentFile.toPath());
        boolean headers = true;
        
        for (String line : lines) {
            if (headers) {
                headers = false;
                continue;
            }
            
            List<String> lineParts = new ArrayList<>();
            String[] linePartsTmp = line.split(",");
            StringBuilder linePart = new StringBuilder();
            for (int i = 0; i < linePartsTmp.length; i++) {
                if (linePartsTmp[i].startsWith("\"")) {
                    linePart.append(linePartsTmp[i].substring(1));
                    for (i = i + 1; i < linePartsTmp.length; i++) {
                        if (linePartsTmp[i].endsWith("\"")) {
                            linePart.append(linePartsTmp[i], 0, linePartsTmp[i].length() - 1);
                            break;
                        } else {
                            linePart.append(linePartsTmp[i]);
                        }
                    }
                } else {
                    linePart.append(linePartsTmp[i]);
                }
                lineParts.add(linePart.toString());
                linePart = new StringBuilder();
            }
            
            if (lineParts.size() != 7) {
                throw new Exception("Line: \"" + line + "\" does not have the correct number of fields");
            }
            
            Segment segment = new Segment();
            segment.family = lineParts.get(0);
            segment.disk = Integer.parseInt(lineParts.get(1));
            segment.title = Integer.parseInt(lineParts.get(2));
            segment.date = lineParts.get(3);
            segment.startTime = Long.parseLong(lineParts.get(4).replace(",", ""));
            segment.endTime = Long.parseLong(lineParts.get(5).replace(",", ""));
            segment.name = lineParts.get(6);
            segments.add(segment);
        }
        
        boolean error = false;
        for (Segment segment : segments) {
            if (segment.startTime == segment.endTime) {
                System.err.println("The start time and end time are the same for: \"" + segment.name + "\"");
                error = true;
            }
            if (segment.startTime > segment.endTime) {
                System.err.println("The start time is after the end time for: \"" + segment.name + "\"");
                error = true;
            }
            if (segment.name.matches(".*[\\\\/:*?\"<>|,.].*")) {
                System.err.println("The name: \"" + segment.name + "\" must not contain any of the following symbols: \\/:*?\"<>|,.");
                error = true;
            }
        }
        if (error) {
            throw new Exception("");
        }
    }
    
    private static void prepareVideo() throws Exception {
        if (!videoDir.exists()) {
            throw new Exception("Cannot find video folder: " + videoDir.getAbsolutePath());
        }
        
        for (int i = 1; i < 99; i++) {
            String title = (i < 9 ? "0" : "") + i;
            File[] files = videoDir.listFiles(e -> e.getName().matches("VTS_" + title + "_\\d\\.VOB"));
            if (files == null || files.length == 0) {
                break;
            }
            File output = new File(videoDir, "title_" + title + ".vob");
            if (output.exists()) {
                continue;
            }
            
            StringBuilder cmd = new StringBuilder();
            for (File f : files) {
                cmd.append((cmd.length() == 0) ? "" : "|").append("todo/").append(f.getName());
            }
            cmd = new StringBuilder("-y -i \"concat:" + cmd + "\" -c copy todo/" + output.getName());
            ffmpeg(cmd.toString());
        }
        
    }
    
    private static void processSegments() throws Exception {
        if (!outputDir.exists() && !outputDir.mkdir()) {
            throw new Exception("Could not create output folder: " + outputDir.getAbsolutePath());
        }
        
        for (Segment segment : segments) {
            File output = new File(outputDir, segment.getName());
            if (output.exists()) {
                continue;
            }
            
            String input = "todo/title_" + (segment.title < 9 ? "0" : "") + segment.title + ".vob";
            String cmd = "-y -i " + input + " -c:v libx264 -c:a aac -strict experimental -b:a 192k -map 0 " +
                    "-ss " + segment.getStart() + " -t " + segment.getLength() + " \"" + output.getAbsolutePath() + "\"";
            ffmpeg(cmd);
        }
    }
    
    
    //Functions
    
    private static String ffmpeg(String cmd) throws Exception {
        cmd = "ffmpeg -hide_banner " + cmd;
        System.out.println(cmd);
        return executeCmd(cmd, true);
    }
    
    /**
     * Executes a command on the system command line.
     *
     * @param cmd  The command to execute.
     * @param wait Whether to wait for the execution to finish or not.
     * @return The output.
     */
    public static String executeCmd(String cmd, boolean wait) throws Exception {
        ProcessBuilder builder = buildProcess(cmd);
        
        Process process = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
        
        StringBuilder response = new StringBuilder();
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) {
                break;
            }
            System.out.println(line);
            response.append(line).append(System.lineSeparator());
        }
        
        if (wait) {
            process.waitFor();
        }
        r.close();
        process.destroy();
        
        return response.toString();
    }
    
    /**
     * Builds a process from a command.
     *
     * @param cmd The command to build a process for.
     * @return The process that was built or null if it was not built.
     * @throws Exception When there is an unknown operating system.
     */
    private static ProcessBuilder buildProcess(String cmd) throws Exception {
        ProcessBuilder builder;
        builder = new ProcessBuilder("cmd.exe", "/c", cmd);
        builder.redirectErrorStream(true);
        return builder;
    }
    
    private static String timeToTimeStamp(long time) {
        long hour = time / 3600000;
        long minute = (time / 60000) - (hour * 60);
        long second = (time / 1000) - (minute * 60) - (hour * 3600);
        long millisecond = time % 1000;
        return (hour < 10 ? "0" : "") + hour + ":" +
                (minute < 10 ? "0" : "") + minute + ":" +
                (second < 10 ? "0" : "") + second + "." +
                (millisecond < 100 ? (millisecond < 10 ? "00" : "0") : "") + millisecond;
    }
    
    
    //Inner Classes
    
    private static class Segment {
        
        //Fields
        
        String family;
        
        int disk;
        
        int title;
        
        String date;
        
        long startTime;
        
        long endTime;
        
        String name;
        
        
        //Getters
        
        public String getName() {
            return "Disk " + disk + " - Title " + title +
                    " - " + getDate() + " - " + name + ".mp4";
        }
        
        public String getStart() {
            return timeToTimeStamp(startTime);
        }
        
        public String getLength() {
            return timeToTimeStamp(endTime - startTime);
        }
        
        public String getDate() {
            String[] parts = date.split("/");
            if (parts.length == 2) {
                return parts[1] + "-" +
                        (parts[0].length() < 2 ? "0" : "") + parts[0] +
                        "   ";
            } else {
                return parts[2] + "-" +
                        (parts[0].length() < 2 ? "0" : "") + parts[0] + "-" +
                        (parts[1].length() < 2 ? "0" : "") + parts[1];
            }
        }
        
    }
    
}
