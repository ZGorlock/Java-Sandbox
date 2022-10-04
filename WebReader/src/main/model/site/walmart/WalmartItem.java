/*
 * File:    WalmartItem.java
 * Package: main.model.site.walmart
 * Author:  Zachary Gill
 */

package main.model.site.walmart;

import main.model.template.ItemForSale;

public class WalmartItem extends ItemForSale {
    
    //Constants
    
    public static final String BASE_URL = "https://www.walmart.com";
    
    public static final String ITEM_URL_KEY = "ip";
    
    
    //Constructors
    
    public WalmartItem(String urlOrId) {
        super(urlOrId, BASE_URL, ITEM_URL_KEY);
    }
    
    public WalmartItem() {
        super();
    }
    
    
    //Methods
    
    @Override
    protected void processPage() {
        super.processPage();
        
        this.item = page.getTextByClass("f3 b lh-copy dark-gray mt1 mb2");
        
        this.price = page.textToFloat(page.getTextByAttributeValue("itemprop", "price"));
        this.basePrice = page.textToFloat(page.getTextByClass("mr2 f6 gray strike"));
        
        this.rating = page.textToFloat(page.getTextByClass("rating-number"));
        this.category = page.textToList(page.getTextByAttributeValue("aria-label", "breadcrumb"), "/");
    }
    
    @Override
    protected String getItemUrlKey() {
        return ITEM_URL_KEY;
    }
    
}
