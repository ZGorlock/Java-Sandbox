/*
 * File:    AmazonItem.java
 * Package: main.model.site.amazon
 * Author:  Zachary Gill
 */

package main.model.site.amazon;

import java.util.List;

import main.model.template.ItemForSale;

public class AmazonItem extends ItemForSale {
    
    //Constants
    
    public static final String BASE_URL = "https://www.amazon.com";
    
    public static final String ITEM_URL_KEY = "dp";
    
    
    //Constructors
    
    public AmazonItem(String urlOrId) {
        super(urlOrId, BASE_URL, ITEM_URL_KEY);
    }
    
    public AmazonItem() {
        super();
    }
    
    
    //Methods
    
    @Override
    protected void processPage() {
        super.processPage();
        
        this.item = page.getTextById("productTitle");
        
        this.price = page.textToFloat(page.getTextByClass(List.of("reinventPricePriceToPayMargin", "apexPriceToPay"), "a-offscreen"));
        this.basePrice = page.textToFloat(page.getTextByClass("basisPrice", "a-offscreen"));
        
        this.rating = page.textToFloat(page.getTextByClass("a-icon-star", "a-icon-alt"));
        this.category = page.textToList(page.getTextById("wayfinding-breadcrumbs_feature_div"), "›");
    }
    
    @Override
    protected String getItemUrlKey() {
        return ITEM_URL_KEY;
    }
    
}
