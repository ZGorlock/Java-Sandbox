/*
 * File:    TargetItem.java
 * Package: main.model.site.target
 * Author:  Zachary Gill
 */

package main.model.site.target;

import main.model.template.ItemForSale;

public class TargetItem extends ItemForSale {
    
    //Constants
    
    public static final String BASE_URL = "https://www.target.com";
    
    public static final String ITEM_URL_KEY = "p";
    
    
    //Constructors
    
    public TargetItem(String urlOrId) {
        super(urlOrId, BASE_URL, ITEM_URL_KEY);
    }
    
    public TargetItem() {
        super();
    }
    
    
    //Methods
    
    @Override
    protected void processPage() {
        super.processPage();
        
        this.item = page.getTextByAttributeValue("data-test", "product-title");
        
        this.price = page.textToFloat(page.getTextByAttributeValue("data-test", "product-price"));
        this.basePrice = page.textToFloat(page.getTextByAttributeValue("data-test", "product-regular-price"));
        
        this.rating = page.textToFloat(page.getTextByAttributeValue("data-test", "ratingCountText"));
        this.category = page.textToList(page.getTextByAttributeValue("data-test", "@web/Breadcrumb/Container"), "/");
    }
    
    @Override
    protected String getItemUrlKey() {
        return ITEM_URL_KEY;
    }
    
}
