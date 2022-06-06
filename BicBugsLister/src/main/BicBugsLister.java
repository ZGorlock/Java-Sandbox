/*
 * File:    BicBugsLister.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import commons.access.Filesystem;
import commons.access.Internet;
import commons.string.StringUtility;
import org.jsoup.nodes.Document;

public class BicBugsLister {
    
    private static final File DATA_DIR = new File("data");
    
    private static final String SHOP_BUTTERFLIES = "https://bicbugs.com/shop/butterflies/";
    
    private static final String SHOP_MOTHS = "https://bicbugs.com/shop/moths/";
    
    private static final String SHOP_BEETLES = "https://bicbugs.com/shop/beetles/";
    
    private static final String SHOP_DRAGONFLIES_CICADAS = "https://bicbugs.com/shop/dragonflies-and-cicadas/";
    
    private static final String SHOP_BEES_WASPS_ANTS = "https://bicbugs.com/shop/bees-wasps-and-ants-2/";
    
    private static final String SHOP_SCORPIONS_SPIDERS_CENTIPEDES = "https://bicbugs.com/shop/scorpions-spiders-and-centipedes/";
    
    private static final String SHOP_GRASSHOPPERS_PRAYING_MANTIS_STICK_BUGS = "https://bicbugs.com/shop/scorpions-spiders-and-centipedes/";
    
    private static final List<String> SHOPS = new ArrayList<>();
    
    static {
        SHOPS.add(SHOP_BEETLES);
        SHOPS.add(SHOP_BEES_WASPS_ANTS);
        SHOPS.add(SHOP_GRASSHOPPERS_PRAYING_MANTIS_STICK_BUGS);
        SHOPS.add(SHOP_SCORPIONS_SPIDERS_CENTIPEDES);
        SHOPS.add(SHOP_DRAGONFLIES_CICADAS);
        SHOPS.add(SHOP_BUTTERFLIES);
        SHOPS.add(SHOP_MOTHS);
    }
    
    private static final List<Listing> listings = new ArrayList<>();
    
    private static final List<String> owned = new ArrayList<>();
    
    private static final Map<String, List<String>> shopData = new LinkedHashMap<>();
    
    public static void main(String[] args) throws Exception {
        try {
            getShops();
            getOwned();
            parseShops();
            produceListings();
            
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    private static void getShops() throws Exception {
        for (String shop : SHOPS) {
            Document shopHtml = Internet.getHtml(shop);
            if (shopHtml == null) {
                throw new Exception("Failed to download shop: " + shop);
            }
            
            List<String> data = StringUtility.splitLines(shopHtml.toString());
            shopData.put(shop, data);
        }
    }
    
    private static void parseShops() throws Exception {
        List<String> ownedList = new ArrayList<>(owned);
        
        Pattern productPattern = Pattern.compile("^\\s*<span class=\"gtm4wp_productdata\" style=\"display:none; visibility:hidden;\" data-gtm4wp_product_id=\"(?<productId>[^\"]+)\" data-gtm4wp_product_name=\"(?<name>[^\"]+)\" data-gtm4wp_product_price=\"(?<price>[^\"]+)\" data-gtm4wp_product_cat=\"(?<category>[^\"]+)\" data-gtm4wp_product_url=\"(?<url>[^\"]+)\".+$");
        for (String shop : SHOPS) {
            List<String> data = shopData.get(shop);
            
            for (String line : data) {
                Matcher productMatcher = productPattern.matcher(line);
                if (productMatcher.matches()) {
                    
                    Listing listing = new Listing();
                    listing.category = productMatcher.group("category");
                    if (listing.category.toUpperCase().contains("COLLECTION") ||
                            listing.category.toUpperCase().contains("JEWELRY") ||
                            listing.category.toUpperCase().contains("SPECIMENS") ||
                            listing.category.toUpperCase().contains("ORNAMENT")) {
                        continue;
                    }
                    
                    listing.category = StringUtility.toTitleCase(StringUtility.trim(shop.replace("https://bicbugs.com/shop/", "")
                            .replace("/", "").replace("-", " ").replaceAll("\\d", "").replaceAll("\\s+", " ")));
                    listing.name = productMatcher.group("name");
                    listing.price = "$" + productMatcher.group("price") + ".00";
                    listing.url = productMatcher.group("url");
                    listing.productId = productMatcher.group("productId");
                    listings.add(listing);
                    
                    if (ownedList.contains(listing.productId)) {
                        listing.owned = true;
                        ownedList.remove(listing.productId);
                    }
                }
            }
        }
        
        if (!ownedList.isEmpty()) {
            throw new Exception("Owned but does not exist: " + ownedList.stream().collect(Collectors.joining(", ", "(", ")")));
        }
    }
    
    private static void getOwned() throws Exception {
        Set<String> uniqueOwned = new HashSet<>(Filesystem.readLines(new File(DATA_DIR, "owned.txt")));
        
        File listingSheet = new File(DATA_DIR, "BicBugs.csv");
        if (listingSheet.exists()) {
            List<Listing> listings = readListings();
            for (Listing listing : listings) {
                if (listing.owned) {
                    uniqueOwned.add(listing.productId);
                }
            }
        }
        
        owned.clear();
        owned.addAll(uniqueOwned);
    }
    
    private static List<Listing> readListings() throws Exception {
        List<Listing> listings = new ArrayList<>();
        
        File listingSheet = new File(DATA_DIR, "BicBugs.csv");
        if (listingSheet.exists()) {
            List<String> lines = Filesystem.readLines(listingSheet);
            for (String line : lines) {
                String[] lineParts = line.split(",", -1);
                Listing listing = new Listing();
                listing.category = lineParts[0];
                listing.name = lineParts[1];
                listing.price = lineParts[2];
                listing.owned = lineParts[3].equalsIgnoreCase("TRUE") || lineParts[3].equalsIgnoreCase("YES");
                listing.url = lineParts[4];
                listing.productId = lineParts[5];
                listings.add(listing);
            }
        }
        
        return listings;
    }
    
    private static void produceListings() throws Exception {
        List<String> lines = new ArrayList<>();
        lines.add("Category,Name,Price,Owned,Url,Product Id");
        
        for (Listing listing : listings) {
            lines.add(listing.category.replace(",", "-") +
                    "," + listing.name.replace(",", "-") +
                    "," + listing.price.replace(",", "-") +
                    "," + (listing.owned ? "Yes" : "No") +
                    "," + listing.url.replace(",", "-") +
                    "," + listing.productId.replace(",", "-")
            );
        }
        
        Filesystem.writeLines(new File(DATA_DIR, "BigBugs.csv"), lines);
    }
    
    private static final class Listing {
        
        String category;
        
        String name;
        
        String price;
        
        boolean owned;
        
        String url;
        
        String productId;
    }
    
}
