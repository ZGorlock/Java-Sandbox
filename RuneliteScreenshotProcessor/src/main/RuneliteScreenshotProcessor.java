/*
 * File:    RuneliteScreenshotProcessor.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import commons.access.Filesystem;
import commons.io.console.Console;

public class RuneliteScreenshotProcessor {
    
    private static final File RUNELITE = new File("D:/RuneScape/RuneLite/.runelite");
    
    private static final File PICTURES = new File("E:/Pictures/Runescape");
    
    public static void main(String[] args) {
        for (File characterDir : Filesystem.getDirs(new File(RUNELITE, "screenshots"))) {
            process(characterDir);
        }
    }
    
    private static void process(File sourceDir) {
        processRandom(sourceDir);
        processRandom(sourceDir, "Random Screenshots");
        
        processBank(sourceDir, "bank");
        
        processLevels(sourceDir, "Levels");
        
        processQuests(sourceDir, "Quests");
        processCombatAchievements(sourceDir, "Combat Achievements");
        
        processBossKills(sourceDir, "Boss Kills");
        processChestLoot(sourceDir, "Chest Loot");
        
        processValuableDrops(sourceDir, "Valuable Drops");
        processUntradeableDrops(sourceDir, "Untradeable Drops");
        
        processPets(sourceDir, "Pets");
        
        processClueScrollRewards(sourceDir, "Clue Scroll Rewards");
        processCollectionLog(sourceDir, "Collection Log");
        
        processWildernessLootChest(sourceDir, "Wilderness Loot Chest");
        processKingdomRewards(sourceDir, "Kingdom Rewards");
        processBaHighGambles(sourceDir, "BA High Gambles");
        
        processFriendsChatKicks(sourceDir, "Friends Chat Kicks");
        
        processDuels(sourceDir, "Duels");
        processPvpKills(sourceDir, "PvP Kills");
        
        processDeaths(sourceDir, "Deaths");
    }
    
    private static boolean process(File screenshotDir, Predicate<List<File>> processor) {
        return Optional.ofNullable(screenshotDir)
                .filter(sourceDir -> Optional.of(sourceDir)
                        .map(Filesystem::getFiles)
                        .map(processor::test)
                        .orElse(false))
                .filter(Filesystem::directoryIsEmpty)
                .map(Filesystem::deleteDirectory)
                .orElse(false);
    }
    
    private static boolean processRandom(File screenshotDir) {
        return process(Filesystem.getFiles(screenshotDir), "Random");
    }
    
    private static boolean processRandom(File screenshotRootDir, String screenshotCategory) {
        return processRandom(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processBank(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "Bank",
                "bankscreenshot",
                "Bank"));
    }
    
    private static boolean processBank(File screenshotRootDir, String screenshotCategory) {
        return processBank(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processLevels(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "Levels",
                "(?<skill>\\w+)\\s*\\((?<level>\\d+)\\)",
                "${skill} (${level})"));
    }
    
    private static boolean processLevels(File screenshotRootDir, String screenshotCategory) {
        return processLevels(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processQuests(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "Quests",
                "Quest\\s*\\((?<quest>.*)\\)",
                "Quest (${quest})"));
    }
    
    private static boolean processQuests(File screenshotRootDir, String screenshotCategory) {
        return processQuests(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processCombatAchievements(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "Combat Achievements",
                "Combat\\s+task\\s*\\((?<task>.*)\\)",
                "Combat Task (${task})"));
    }
    
    private static boolean processCombatAchievements(File screenshotRootDir, String screenshotCategory) {
        return processBank(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processBossKills(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "Boss Kills",
                "(?<boss>.*)\\s*\\((?<kc>\\d+)\\)",
                "${boss} (${kc})"));
    }
    
    private static boolean processBossKills(File screenshotRootDir, String screenshotCategory) {
        return processBossKills(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processChestLoot(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "Chest Loot"));
    }
    
    private static boolean processChestLoot(File screenshotRootDir, String screenshotCategory) {
        return processChestLoot(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processValuableDrops(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "Drops - Valuable",
                "Valuable\\s+drop\\s+(?<drop>.*)",
                "Valuable Drop (${drop})"));
    }
    
    private static boolean processValuableDrops(File screenshotRootDir, String screenshotCategory) {
        return processValuableDrops(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processUntradeableDrops(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "Drops - Untradeable",
                "Untradeable\\s+drop\\s+(?<drop>.*)",
                "Untradeable Drop (${drop})"));
    }
    
    private static boolean processUntradeableDrops(File screenshotRootDir, String screenshotCategory) {
        return processUntradeableDrops(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processPets(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "Pets"));
    }
    
    private static boolean processPets(File screenshotRootDir, String screenshotCategory) {
        return processPets(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processClueScrollRewards(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "Clue Scroll Rewards",
                "(?<type>\\w*)\\s*\\((?<count>\\d+)\\)",
                "${type} (${count})"));
    }
    
    private static boolean processClueScrollRewards(File screenshotRootDir, String screenshotCategory) {
        return processClueScrollRewards(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processCollectionLog(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "Collection Log",
                "Collection\\s+log\\s*\\((?<item>.*)\\)",
                "Collection Log (${item})"));
    }
    
    private static boolean processCollectionLog(File screenshotRootDir, String screenshotCategory) {
        return processCollectionLog(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processWildernessLootChest(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "Wilderness Loot Chest",
                "Loot\\s+key",
                "Loot Key"));
    }
    
    private static boolean processWildernessLootChest(File screenshotRootDir, String screenshotCategory) {
        return processWildernessLootChest(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processKingdomRewards(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "Kingdom Rewards",
                "Kingdom\\s+(?<date>[\\d\\-]+)",
                "Kingdom (${date})"));
    }
    
    private static boolean processKingdomRewards(File screenshotRootDir, String screenshotCategory) {
        return processKingdomRewards(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processBaHighGambles(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "BA High Gambles",
                "High\\s*Gamble\\s*\\((?<count>\\d+|count\\snot\\found)\\)",
                "High Gamble (${count})"));
    }
    
    private static boolean processBaHighGambles(File screenshotRootDir, String screenshotCategory) {
        return processBaHighGambles(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processFriendsChatKicks(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "Friends Chat Kicks",
                "Kick\\s+(?<player>.*)",
                "Kick (${player})"));
    }
    
    private static boolean processFriendsChatKicks(File screenshotRootDir, String screenshotCategory) {
        return processFriendsChatKicks(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processDuels(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "Duels",
                "Duel\\s+(?<result>won|lost)\\s+\\((?<count>\\d+)\\)",
                "Duel ${result} (${count})"));
    }
    
    private static boolean processDuels(File screenshotRootDir, String screenshotCategory) {
        return processDuels(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processPvpKills(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "PvP Kills",
                "Kill\\s*(?<character>.+)",
                "Kill (${character})"));
    }
    
    private static boolean processPvpKills(File screenshotRootDir, String screenshotCategory) {
        return processPvpKills(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static boolean processDeaths(File screenshotDir) {
        return process(screenshotDir, screenshots -> process(screenshots, "Deaths",
                "Death\\s*(?<character>.+)",
                "Death (${character})"));
    }
    
    private static boolean processDeaths(File screenshotRootDir, String screenshotCategory) {
        return processDeaths(new File(screenshotRootDir, screenshotCategory));
    }
    
    private static String formatTitle(File source, String pattern, String format) {
        return Optional.of(source).map(File::getName)
                .map(RuneliteScreenshotProcessor::fixTimestamp)
                .map(title -> cleanTitle(title, pattern, format))
                .map(RuneliteScreenshotProcessor::fixWhitespace)
                .orElseThrow();
    }
    
    private static String cleanTitle(String title, String pattern, String format) {
        return title.replaceAll(
                ("(?i)^" + pattern + "\\s+\\D*(?=\\d)"),
                (format + " - "));
    }
    
    private static String fixTimestamp(String title) {
        return title.replaceFirst(
                "(?<year>\\d{4})-(?<month>\\d{2})-(?<day>\\d{2})_(?<hour>\\d{2})-(?<minute>\\d{2})-(?<second>\\d{2})\\s*",
                "${year}${month}${day}_${hour}${minute}${second}");
    }
    
    private static String fixWhitespace(String title) {
        return title.replaceAll("\\s+", " ")
                .replaceAll("\\s+\\.(?<ext>[^.]+)$", ".${ext}")
                .replaceAll("(?<=\\s)\\({2,}", "(")
                .replaceAll("(?:\\)\\s-){2,}", ") -")
                .strip();
    }
    
    private static boolean store(File sourceFile, File targetFile) {
        int dirWidth = Math.max(sourceFile.getParentFile().getName().length(), targetFile.getParentFile().getName().length());
        
        System.out.println();
        System.out.printf("Moving:  %-" + dirWidth + "s / %s%n", sourceFile.getParentFile().getName(), sourceFile.getName());
        System.out.printf("    to:  %-" + dirWidth + "s / %s%n", targetFile.getParentFile().getName(), targetFile.getName());
        //        System.out.println("Moving: " + sourceFile.getAbsolutePath());
        //        System.out.println("    to: " + targetFile.getAbsolutePath());
        
        if (targetFile.exists()) {
            System.out.println(Console.ConsoleEffect.RED.apply("... Already exists"));
            return false;
        }
        
        boolean store = Filesystem.moveFile(sourceFile, targetFile);
        if (!store) {
            System.out.println(Console.ConsoleEffect.RED.apply("... Failed"));
            return false;
        }
        
        System.out.println(Console.ConsoleEffect.GREEN.apply("... Done"));
        return true;
    }
    
    private static boolean process(File sourceFile, File targetDir, String targetName) {
        return store(sourceFile, new File(targetDir, targetName));
    }
    
    private static boolean process(File sourceFile, String targetDirName, String targetName) {
        return process(sourceFile, new File(PICTURES, targetDirName), targetName);
    }
    
    private static boolean process(File sourceFile, File targetDir, String sourcePattern, String targetFormat) {
        return process(sourceFile, targetDir, formatTitle(sourceFile, sourcePattern, targetFormat));
    }
    
    private static boolean process(File sourceFile, String targetDirName, String sourcePattern, String targetFormat) {
        return process(sourceFile, new File(PICTURES, targetDirName), sourcePattern, targetFormat);
    }
    
    private static boolean process(File sourceFile, File targetDir) {
        return process(sourceFile, targetDir, "", "");
    }
    
    private static boolean process(File sourceFile, String targetDirName) {
        return process(sourceFile, targetDirName, "", "");
    }
    
    private static boolean process(List<File> sourceFiles, File targetDir, String sourcePattern, String targetFormat) {
        return sourceFiles.stream()
                .map(sourceFile -> process(sourceFile, targetDir, sourcePattern, targetFormat))
                .reduce(true, Boolean::logicalAnd);
    }
    
    private static boolean process(List<File> sourceFiles, String targetDirName, String sourcePattern, String targetFormat) {
        return process(sourceFiles, new File(PICTURES, targetDirName), sourcePattern, targetFormat);
    }
    
    private static boolean process(List<File> sourceFiles, File targetDir) {
        return process(sourceFiles, targetDir, "", "");
    }
    
    private static boolean process(List<File> sourceFiles, String targetDirName) {
        return process(sourceFiles, targetDirName, "", "");
    }
    
}
