/*
 * File:    PerceptualImageMatcher.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

import dev.brachtendorf.jimagehash.datastructures.tree.Result;
import dev.brachtendorf.jimagehash.datastructures.tree.binaryTree.BinaryTree;
import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.AverageHash;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;
import dev.brachtendorf.jimagehash.matcher.exotic.SingleImageMatcher;
import org.apache.commons.io.FileUtils;

public class PerceptualImageMatcher {
    
    //Static Fields
    
    private static File imageDir = new File("resources");
    
    private static File cacheDir = new File("tmp");
    
    private static List<String> extensions = List.of("jpg", "jpeg", "png", "bmp", "tif", "tiff");
    
    private static boolean useCache = true;
    
    private static boolean printProgress = true;
    
    private static boolean previewMatches = true;
    
    private static boolean printMatches = true;
    
    private static boolean relocateMatches = true;
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        List<File> images = new ArrayList<>(FileUtils.listFiles(imageDir, extensions.toArray(String[]::new), false));

//        testHasher(images);
//        testMatcher(images);
        testMatchingTree(images);
    }
    
    
    //Static Methods
    
    private static void testHasher(List<File> images) throws Exception {
        Map<File, Hash> hashes = new LinkedHashMap<>();
        
        HashingAlgorithm hasher = new PerceptiveHash(32);
        
        for (File image : images) {
            try {
                hashes.put(image, hasher.hash(image));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        double[][] comparisons = new double[images.size()][images.size()];
        
        for (int i = 0; i < images.size(); i++) {
            for (int j = 0; j < images.size(); j++) {
                if ((i == j) || (comparisons[i][j] != 0)) {
                    continue;
                }
                //System.out.println(hashes.get(images.get(i)).normalizedHammingDistance(hashes.get(images.get(j))) + " ::: " + hashes.get(images.get(j)).normalizedHammingDistance(hashes.get(images.get(i))));
                
                double similarityScore = hashes.get(images.get(i)).normalizedHammingDistance(hashes.get(images.get(j)));
                comparisons[i][j] = similarityScore;
                
                if (similarityScore >= 0.2) {
                    System.out.printf("s%-10f%24s%24s%n", similarityScore, images.get(i).getName(), images.get(j).getName());
                }
            }
        }
    }
    
    private static void testMatcher(List<File> images) throws Exception {
        SingleImageMatcher matcher = new SingleImageMatcher();
        matcher.addHashingAlgorithm(new AverageHash(64), .3);
        matcher.addHashingAlgorithm(new PerceptiveHash(32), .2);
        
        Boolean[][] comparisons = new Boolean[images.size()][images.size()];
        
        for (int i = 0; i < images.size(); i++) {
            for (int j = 0; j < images.size(); j++) {
                if ((i == j) || (comparisons[i][j] != null)) {
                    continue;
                }
                
                boolean match = matcher.checkSimilarity(images.get(i), images.get(j));
                comparisons[i][j] = match;
                
                if (!match) {
                    System.out.printf("%24s%24s%n", images.get(i).getName(), images.get(j).getName());
                }
            }
        }
    }
    
    private static void testMatchingTree(List<File> images) throws Exception {
        BinaryTree<File> tree = new BinaryTree<>(true);
        Map<File, Hash> hashes = new LinkedHashMap<>();
        
        HashingAlgorithm hasher = new PerceptiveHash(64);
        hasher.setOpaqueHandling(253);
        
        for (File image : images) {
            if (printProgress && (images.indexOf(image) % 100 == 0) || (images.indexOf(image) == images.size() - 1)) {
                System.out.print("\rHashed " + images.indexOf(image) + " / " + images.size());
            }
            
            Hash hash;
            if (useCache) {
                File cache = new File(cacheDir, image.getName() + ".hash");
                if (cache.exists()) {
                    hash = Hash.fromFile(cache);
                } else {
                    hash = hasher.hash(image);
                    hash.toFile(cache);
                }
            } else {
                hash = hasher.hash(image);
            }
            
            hashes.put(image, hash);
            tree.addHash(hash, image);
        }
        System.out.println("\n\n");
        
        Set<File> found = new HashSet<>();
        
        for (File image : images) {
            if (printProgress && (images.indexOf(image) % 100 == 0) || (images.indexOf(image) == images.size() - 1)) {
                System.out.print("\rSearched " + images.indexOf(image) + " / " + images.size());
            }
            if (!found.add(image)) {
                continue;
            }
            
            PriorityQueue<Result<File>> matches = tree.getElementsWithinHammingDistance(hashes.get(image), 10);
            
            if (matches.size() > 1) {
                List<File> matchingImages = new ArrayList<>();
                matchingImages.add(image);
                
                matches.forEach(match -> {
                    if (!match.value.getName().equals(image.getName()) && (!relocateMatches || !found.contains(match.value))) {
                        found.add(match.value);
                        matchingImages.add(match.value);
                    }
                });
                if (matchingImages.size() <= 1) {
                    continue;
                }
                
                
                if (printMatches) {
                    if (printProgress) {
                        System.out.println();
                    }
                    System.out.println(image.getName());
                    matchingImages.subList(1, matchingImages.size()).forEach(e -> System.out.println("    " + e.getName()));
                    System.out.println();
                }
                
                if (previewMatches) {
                    List<BufferedImage> loadedPreviews = matchingImages.stream().map(e -> {
                        try {
                            return ImageIO.read(e);
                        } catch (Exception ignored) {
                            return null;
                        }
                    }).collect(Collectors.toList());
                    boolean breakpoint = true;
                }
                
                if (relocateMatches) {
                    File newDir = new File(imageDir, image.getName().replaceAll("\\.[^.]+$", ""));
                    matchingImages.forEach(e -> {
                        try {
                            FileUtils.moveFileToDirectory(e, newDir, true);
                        } catch (IOException ignored) {
                        }
                    });
                }
            }
        }
        System.out.println();
    }
    
}
