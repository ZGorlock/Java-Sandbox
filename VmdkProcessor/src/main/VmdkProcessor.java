/*
 * File:    VmdkProcessor.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import common.CmdLine;
import common.Filesystem;

public class VmdkProcessor {
    
    private static final File VM_DIR = new File("G:/Virtual Machines");
    
    public static void main(String[] args) {
        //backupVmConfigurations();
        //relocateVms();
        
        //osboxesConvertMonolithicDisks();
        
        //renameVmwareVm(new File(VM_DIR, "Linux/CentOS/8/64bit"), "CentOS 8 - 64bit", "CentOS - 8 - 64bit");
        //renameVmwareVm(new File(VM_DIR, "Linux/Debian/10/64bit"), "Debian 10.x - 64bit", "Debian - 10 - 64bit");
        //renameVmwareVm(new File(VM_DIR, "Linux/Fedora/33/64bit"), "Fedora 33 - 64bit", "Fedora - 33 - 64bit");
        //renameVmwareVm(new File(VM_DIR, "Linux/Kali Linux/2021/64bit"), "Kali Linux - 64bit", "Kali Linux - 2021 - 64bit");
        //renameVmwareVm(new File(VM_DIR, "Linux/Linux Mint/20/64bit"), "Linux Mint 20.x - 64bit", "Linux Mint - 20 - 64bit");
        //renameVmwareVm(new File(VM_DIR, "Linux/Raspbian/2021/64bit"), "Raspbian - 64bit", "Raspbian - 2021 - 64bit");
        //renameVmwareVm(new File(VM_DIR, "Linux/Ubuntu/20/64bit"), "Ubuntu 20.x - 64bit", "Ubuntu - 20 - 64bit");
        
        //macosConvertMonolithicDisks();
        
        //renameVmwareVm(new File(VM_DIR, "MacOS/Big Sur/64bit"), "Big Sur - 64bit", "MacOS - Big Sur - 64bit");
        //renameVmwareVm(new File(VM_DIR, "MacOS/Catalina/64bit"), "Catalina - 64bit", "MacOS - Catalina - 64bit");
        //renameVmwareVm(new File(VM_DIR, "MacOS/High Sierra/64bit"), "High Sierra - 64bit", "MacOS - High Sierra - 64bit");
        //renameVmwareVm(new File(VM_DIR, "MacOS/Mojave/64bit"), "Mojave - 64bit", "MacOS - Mojave - 64bit");
        //renameVmwareVm(new File(VM_DIR, "MacOS/Sierra/64bit"), "Sierra - 64bit", "MacOS - Sierra - 64bit");
        
        //convertMonolithicDisk(new File(VM_DIR, "old/Windows XP Professional/Windows XP Professional-old.vmdk"), "Windows XP Professional");
        //renameVmwareVm(new File(VM_DIR, "old/Windows XP Professional"), "Windows XP Professional", "Windows XP - Professional (SP3) - 32bit");
        //convertMonolithicDisk(new File(VM_DIR, "old/Windows 7 Ultimate 64-bit/Windows 7 Ultimate 64-bit-old.vmdk"), "Windows 7 Ultimate 64-bit");
        //renameVmwareVm(new File(VM_DIR, "old/Windows 7 Ultimate 64-bit"), "Windows 7 Ultimate 64-bit", "Windows 7 - Ultimate (SP1) - 64bit");
        
        //renameVmwareVm(new File(VM_DIR, "Windows/Windows 10/Pro (Latest)/32bit"), "Windows 10 - Pro (20H2) - 32bit", "Windows 10 - Pro (Latest) - 32bit");
        //renameVmwareVm(new File(VM_DIR, "Windows/Windows 10/Pro (Latest)/64bit"), "Windows 10 - Pro (20H2) - 64bit", "Windows 10 - Pro (Latest) - 64bit");
        
        //countVms();
    }
    
    private static void renameVmwareVm(File dir, String oldName, String newName) {
        List<File> files = Filesystem.getFiles(dir);
        for (File file : files) {
            if (file.getName().contains(oldName)) {
                Filesystem.renameFile(file, new File(file.getParentFile(), file.getName().replace(oldName, newName)));
            }
        }
        if (Filesystem.getFiles(dir, ".*\\.vmdk").size() > 1) {
            File vmdk = new File(dir, newName + ".vmdk");
            if (vmdk.exists()) {
                List<String> vmdkLines = new ArrayList<>();
                for (String vmdkLine : Filesystem.readLines(vmdk)) {
                    vmdkLines.add(vmdkLine.replace(oldName, newName));
                }
                Filesystem.writeLines(vmdk, vmdkLines);
            }
        }
        File vmx = new File(dir, newName + ".vmx");
        if (vmx.exists()) {
            List<String> vmxLines = new ArrayList<>();
            for (String vmxLine : Filesystem.readLines(vmx)) {
                vmxLines.add(vmxLine.replace(oldName, newName));
            }
            Filesystem.writeLines(vmx, vmxLines);
        }
        File vmxf = new File(dir, newName + ".vmxf");
        if (vmx.exists()) {
            List<String> vmxfLines = new ArrayList<>();
            for (String vmxfLine : Filesystem.readLines(vmxf)) {
                vmxfLines.add(vmxfLine.replace(oldName, newName));
            }
            Filesystem.writeLines(vmxf, vmxfLines);
        }
    }
    
    private static void convertMonolithicDisk(File vmdk, String title) {
        String vDiskManager = "C:\\Program Files (x86)\\VMware\\VMware Player\\vmware-vdiskmanager.exe";
        String cmd = "\"" + vDiskManager + "\" -r \"" + vmdk.getAbsolutePath() + "\" -t 1 \"" + vmdk.getParentFile().getAbsolutePath() + "\\" + title + ".vmdk" + "\"";
        System.out.println(cmd);
        String response = CmdLine.executeCmd(cmd, true);
        if (response.contains("Virtual disk conversion successful.")) {
            Filesystem.deleteFile(vmdk);
        }
    }
    
    private static void osboxesConvertMonolithicDisks() {
        File dir = new File(VM_DIR, "Linux/OSBoxes");
        List<File> vmdks = Filesystem.getFilesRecursively(dir, ".*\\.vmdk");
        List<File> finalVmdks = vmdks.stream().filter(e -> !(e.getName().startsWith("{") || e.getName().matches("^.*-s\\d+\\.vmdk"))).collect(Collectors.toList());
        
        for (File vmdk : finalVmdks) {
            if (Filesystem.getFiles(vmdk.getParentFile(), ".*\\.vmdk").size() > 1) {
                continue;
            }
            String title = vmdk.getParentFile().getAbsolutePath().replace((dir.getAbsolutePath() + "\\"), "").replace("\\", " - ");
            convertMonolithicDisk(vmdk, title);
        }
    }
    
    private static void macosConvertMonolithicDisks() {
        File dir = new File(VM_DIR, "MacOS");
        List<File> vmdks = Filesystem.getFilesRecursively(dir, ".*\\.vmdk");
        List<File> finalVmdks = vmdks.stream().filter(e -> !(e.getName().startsWith("{") || e.getName().matches("^.*-s\\d+\\.vmdk"))).collect(Collectors.toList());
        
        for (File vmdk : finalVmdks) {
            if (Filesystem.getFiles(vmdk.getParentFile(), ".*\\.vmdk").size() > 1) {
                continue;
            }
            String title = vmdk.getParentFile().getAbsolutePath().replace((dir.getAbsolutePath() + "\\"), "").replace("\\", " - ");
            convertMonolithicDisk(vmdk, title);
        }
    }
    
    private static void backupVmConfigurations() {
        final File dir = VM_DIR;
        final File backup = new File("E:/Downloads/VM Configs");
        
        List<String> backupExts = List.of("vmx", "vmxf", "vmdk", "vmsd", "vbox");
        backupExts.stream()
                .map(ext -> Filesystem.getFilesRecursively(dir, ("(?i).*\\." + ext + "$")))
                .flatMap(Collection::stream).distinct()
                .filter(f -> !f.getName().matches("(?i).*-s\\d+\\..*"))
                .forEach(f -> {
                    File o = new File(f.getAbsolutePath().replace(dir.getAbsolutePath(), backup.getAbsolutePath()));
                    Filesystem.copyFile(f, o);
                });
    }
    
    private static void relocateVms() {
        final File oldDir = new File(VM_DIR.getParentFile(), "Windows");
        final File newDir = new File(VM_DIR, "Windows");
        
        final List<String> skipExts = List.of("gz", "7z", "zip", "rar", "iso", "log", "dmp", "vmdk", "vmem", "vmss");
        
        List<File> allFiles = Filesystem.getFilesRecursively(newDir);
        List<File> testFiles = allFiles.stream()
                .filter(f -> !skipExts.contains(f.getName().replaceAll(".*\\.([^.]+)$", "$1").toLowerCase()))
                .filter(f -> !f.getName().matches("(?i).*-s\\d+\\.vmdk$"))
                .filter(f -> !f.getName().matches("(?i)\\.log\\.\\d+$"))
                .collect(Collectors.toList());
        
        for (File f : testFiles) {
            if (f.length() > 10000000) {
                System.err.println(f.getAbsolutePath());
                continue;
            }
            
            String content = Filesystem.readFileToString(f);
            if (content.contains(oldDir.getAbsolutePath())) {
                System.out.println();
                System.out.println(f.getAbsolutePath());
                
                Pattern p = Pattern.compile("(?s)[^\r\n]+" + Pattern.quote(oldDir.getAbsolutePath()) + "[^\r\n]+");
                Matcher m = p.matcher(content);
                while (m.find()) {
                    System.out.println("     " + m.group().strip());
                }
                
                String newContent = content.replace(oldDir.getAbsolutePath(), newDir.getAbsolutePath());
                Filesystem.writeStringToFile(f, newContent);
                //break;
            }
        }
    }
    
    private static void countVms() {
        List<File> vmx = Filesystem.getFilesRecursively(VM_DIR, ".*\\.vmx$");
        List<File> vbox = Filesystem.getFilesRecursively(VM_DIR, ".*\\.vbox$");
        
        System.out.println();
        System.out.println("VMs            : " + (vmx.size() + vbox.size()));
        System.out.println("VMware VMs     : " + vmx.size());
        System.out.println("Virtualbox VMs : " + vbox.size());
        System.out.println();
        
        System.out.println("Windows        : " + vmx.stream().filter(e -> e.getAbsolutePath().contains("Windows")).count());
        System.out.println("MacOS          : " + vmx.stream().filter(e -> e.getAbsolutePath().contains("MacOS")).count());
        System.out.println("Linux          : " + vmx.stream().filter(e -> e.getAbsolutePath().contains("Linux")).count());
        System.out.println("Solaris        : " + vmx.stream().filter(e -> e.getAbsolutePath().contains("Solaris")).count());
        System.out.println("OSBoxes        : " + vbox.size());
        System.out.println();
        
        System.out.println("Windows:");
        vmx.stream().filter(e -> e.getAbsolutePath().contains("Windows")).map(e -> "    " + e.getName().replace(".vmx", "")).forEach(System.out::println);
        System.out.println("MacOS:");
        vmx.stream().filter(e -> e.getAbsolutePath().contains("MacOS")).map(e -> "    " + e.getName().replace(".vmx", "")).forEach(System.out::println);
        System.out.println("Linux:");
        vmx.stream().filter(e -> e.getAbsolutePath().contains("Linux")).map(e -> "    " + e.getName().replace(".vmx", "")).forEach(System.out::println);
        System.out.println("Solaris:");
        vmx.stream().filter(e -> e.getAbsolutePath().contains("Solaris")).map(e -> "    " + e.getName().replace(".vmx", "")).forEach(System.out::println);
        System.out.println("OSBoxes:");
        vbox.stream().map(e -> "    " + e.getName().replace(".vbox", "")).forEach(System.out::println);
    }
    
}
