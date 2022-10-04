/*
 * File:    ItemForSale.java
 * Package: main.model.template
 * Author:  Zachary Gill
 */

package main.model.template;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

import commons.object.collection.ListUtility;
import main.model.BaseModel;

public abstract class ItemForSale extends BaseModel {
    
    //Fields
    
    public String item;
    
    public Float price;
    
    public Float basePrice;
    
    public Float rating;
    
    public List<String> category;
    
    public String id;
    
    
    //Constructors
    
    protected ItemForSale(String urlOrId, String baseUrl, String itemUrlKey) {
        super(buildUrl(baseUrl, itemUrlKey, urlOrId));
    }
    
    protected ItemForSale() {
        super();
    }
    
    
    //Methods
    
    @Override
    protected void processPage() {
        super.processPage();
        
        this.id = Optional.of(getUrlPattern(getItemUrlKey(), true, "(?:-/)?(?:[A-Z]-)?[^/#?\\-]+").matcher(url)).filter(Matcher::matches)
                .map(e -> e.group("id")).orElse(null);
    }
    
    @Override
    protected List<List<Map.Entry<String, Optional<String>>>> getDisplayFields() {
        return ListUtility.addAndGet(super.getDisplayFields(),
                List.of(
                        Map.entry("Item", Optional.ofNullable(item)),
                        Map.entry("Price", Optional.ofNullable(price).map(e -> String.format("$%.2f", e))),
                        Map.entry("Base Price", Optional.ofNullable(basePrice).map(e -> String.format("$%.2f", e))),
                        Map.entry("Rating", Optional.ofNullable(rating).map(e -> String.format("%.1f / 5.0", e))),
                        Map.entry("Category", Optional.ofNullable(category).map(e -> String.join(" | ", e))),
                        Map.entry("Item ID", Optional.ofNullable(id))
                ));
    }
    
    protected abstract String getItemUrlKey();
    
    
    //Static Methods
    
    public static String buildUrl(String baseUrl, String itemUrlKey, String urlOrId) {
        return urlOrId.startsWith(baseUrl) ? urlOrId :
                String.join("/", baseUrl, itemUrlKey, urlOrId);
    }
    
}
