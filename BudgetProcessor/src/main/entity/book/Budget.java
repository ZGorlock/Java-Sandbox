/*
 * File:    Budget.java
 * Package: main.entity.book
 * Author:  Zachary Gill
 */

package main.entity.book;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import main.entity.base.Book;
import main.entity.page.Month;
import main.entity.page.Overview;
import main.entity.page.Totals;
import main.entity.page.Transactions;

public class Budget extends Book {
    
    //Enums
    
    public enum InfoPage {
        BUDGET,
        OVERVIEW,
        TOTALS,
        TRANSACTIONS
    }
    
    //Constants
    
    public static final int FIRST_MONTH_PAGE_INDEX = 4;
    
    
    //Fields
    
    public main.entity.page.Budget budget;
    
    public Overview overview;
    
    public Totals totals;
    
    public Transactions transactions;
    
    public List<Month> months;
    
    
    //Constructors
    
    public Budget(Book book) {
        super(book);
    }
    
    public Budget(File workbookFile) {
        super(workbookFile);
    }
    
    
    //Methods
    
    @Override
    protected void parse() {
        super.parse();
        
        title = "Budget";
        budget = new main.entity.page.Budget(pages.get(InfoPage.BUDGET.ordinal()));
        overview = new Overview(pages.get(InfoPage.OVERVIEW.ordinal()));
        totals = new Totals(pages.get(InfoPage.TOTALS.ordinal()));
        transactions = new Transactions(pages.get(InfoPage.TRANSACTIONS.ordinal()));
        
        months = new ArrayList<>();
        for (int i = FIRST_MONTH_PAGE_INDEX; i < pages.size(); i++) {
            months.add(new Month(pages.get(i)));
        }
    }
    
}
