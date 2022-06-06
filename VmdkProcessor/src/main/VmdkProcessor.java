/*
 * File:    VmdkProcessor.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import common.CmdLine;
import common.Filesystem;

public class VmdkProcessor {
    
    public static void main(String[] args) {
//        osboxesConvertMonolithicDisks();

//        renameVmwareVm(new File("G:/Linux/CentOS/8/64bit"), "CentOS 8 - 64bit", "CentOS - 8 - 64bit");
//        renameVmwareVm(new File("G:/Linux/Debian/10/64bit"), "Debian 10.x - 64bit", "Debian - 10 - 64bit");
//        renameVmwareVm(new File("G:/Linux/Fedora/33/64bit"), "Fedora 33 - 64bit", "Fedora - 33 - 64bit");
//        renameVmwareVm(new File("G:/Linux/Kali Linux/2021/64bit"), "Kali Linux - 64bit", "Kali Linux - 2021 - 64bit");
//        renameVmwareVm(new File("G:/Linux/Linux Mint/20/64bit"), "Linux Mint 20.x - 64bit", "Linux Mint - 20 - 64bit");
//        renameVmwareVm(new File("G:/Linux/Raspbian/2021/64bit"), "Raspbian - 64bit", "Raspbian - 2021 - 64bit");
//        renameVmwareVm(new File("G:/Linux/Ubuntu/20/64bit"), "Ubuntu 20.x - 64bit", "Ubuntu - 20 - 64bit");

//        macosConvertMonolithicDisks();

//        renameVmwareVm(new File("G:/MacOS/Mojave/64bit"), "Mojave - 64bit", "macOS - Mojave - 64bit");
//        renameVmwareVm(new File("G:/MacOS/Sierra/64bit"), "Sierra - 64bit", "macOS - Sierra - 64bit");
//        renameVmwareVm(new File("G:/MacOS/High Sierra/64bit"), "High Sierra - 64bit", "macOS - High Sierra - 64bit");

//        convertMonolithicDisk(new File("G:/old/Windows XP Professional/Windows XP Professional-old.vmdk"), "Windows XP Professional");
//        renameVmwareVm(new File("G:/old/Windows XP Professional"), "Windows XP Professional", "Windows XP - Professional (SP3) - 32bit");
//        convertMonolithicDisk(new File("G:/old/Windows 7 Ultimate 64-bit/Windows 7 Ultimate 64-bit-old.vmdk"), "Windows 7 Ultimate 64-bit");
//        renameVmwareVm(new File("G:/old/Windows 7 Ultimate 64-bit"), "Windows 7 Ultimate 64-bit", "Windows 7 - Ultimate (SP1) - 64bit");

//        renameVmwareVm(new File("G:/Windows/Windows 10/Pro (Latest)/32bit"), "Windows 10 - Pro (20H2) - 32bit", "Windows 10 - Pro (Latest) - 32bit");
//        renameVmwareVm(new File("G:/Windows/Windows 10/Pro (Latest)/64bit"), "Windows 10 - Pro (20H2) - 64bit", "Windows 10 - Pro (Latest) - 64bit");
        
        countVms();
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
        String cmd = "\"" + vDiskManager + "\" -r \"" + vmdk.getAbsolutePath() + "\" -t 1 \"" + vmdk.getParentFile().getAbsolutePath() + "\\" + title + ".vmdk" + "\"" + "";
        System.out.println(cmd);
        String response = CmdLine.executeCmd(cmd, true);
        if (response.contains("Virtual disk conversion successful.")) {
            Filesystem.deleteFile(vmdk);
        }
    }
    
    private static void osboxesConvertMonolithicDisks() {
        File dir = new File("G:/Linux/OSBoxes");
        List<File> vmdks = Filesystem.getFilesRecursively(dir, ".*\\.vmdk");
        List<File> finalVmdks = vmdks.stream().filter(e -> !(e.getName().startsWith("{") || e.getName().matches("^.*-s\\d+\\.vmdk"))).collect(Collectors.toList());
        
        for (File vmdk : finalVmdks) {
            if (Filesystem.getFiles(vmdk.getParentFile(), ".*\\.vmdk").size() > 1) {
                continue;
            }
            String title = vmdk.getParentFile().getAbsolutePath().replace("G:\\Linux\\OSBoxes\\", "").replace("\\", " - ");
            convertMonolithicDisk(vmdk, title);
        }
    }
    
    private static void macosConvertMonolithicDisks() {
        File dir = new File("G:/MacOS");
        List<File> vmdks = Filesystem.getFilesRecursively(dir, ".*\\.vmdk");
        List<File> finalVmdks = vmdks.stream().filter(e -> !(e.getName().startsWith("{") || e.getName().matches("^.*-s\\d+\\.vmdk"))).collect(Collectors.toList());
        
        for (File vmdk : finalVmdks) {
            if (Filesystem.getFiles(vmdk.getParentFile(), ".*\\.vmdk").size() > 1) {
                continue;
            }
            String title = vmdk.getParentFile().getAbsolutePath().replace("G:\\MacOS\\", "").replace("\\", " - ");
            convertMonolithicDisk(vmdk, title);
        }
    }
    
    private static void countVms() {
        List<File> vmxs = Filesystem.getFilesRecursively(new File("G:"), ".*\\.vmx$");
        List<File> vboxs = Filesystem.getFilesRecursively(new File("G:"), ".*\\.vbox$");
        
        System.out.println();
        System.out.println("VMs            : " + (vmxs.size() + vboxs.size()));
        System.out.println("VMware VMs     : " + vmxs.size());
        System.out.println("Virtualbox VMs : " + vboxs.size());
        System.out.println();
        
        System.out.println("Windows        : " + vmxs.stream().filter(e -> e.getAbsolutePath().contains("Windows")).count());
        System.out.println("MacOS          : " + vmxs.stream().filter(e -> e.getAbsolutePath().contains("MacOS")).count());
        System.out.println("Linux          : " + vmxs.stream().filter(e -> e.getAbsolutePath().contains("Linux")).count());
        System.out.println("Solaris        : " + vmxs.stream().filter(e -> e.getAbsolutePath().contains("Solaris")).count());
        System.out.println("OSBoxes        : " + vboxs.size());
        System.out.println();
        
        System.out.println("Windows:");
        vmxs.stream().filter(e -> e.getAbsolutePath().contains("Windows")).map(e -> "    " + e.getName().replace(".vmx", "")).forEach(System.out::println);
        System.out.println("MacOS:");
        vmxs.stream().filter(e -> e.getAbsolutePath().contains("MacOS")).map(e -> "    " + e.getName().replace(".vmx", "")).forEach(System.out::println);
        System.out.println("Linux:");
        vmxs.stream().filter(e -> e.getAbsolutePath().contains("Linux")).map(e -> "    " + e.getName().replace(".vmx", "")).forEach(System.out::println);
        System.out.println("Solaris:");
        vmxs.stream().filter(e -> e.getAbsolutePath().contains("Solaris")).map(e -> "    " + e.getName().replace(".vmx", "")).forEach(System.out::println);
        System.out.println("OSBoxes:");
        vboxs.stream().map(e -> "    " + e.getName().replace(".vbox", "")).forEach(System.out::println);
    }
    
}
