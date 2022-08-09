/*
 * File:    BudgetProcessor.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import main.entity.book.Budget;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class BudgetProcessor {
    
    //Constants
    
    private static File BUDGET = new File("E:\\Documents\\Money\\Budget.xlsx");
    
    private static File BUDGET_TEST = new File("data\\Budget.xlsx");
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        loadBudget();
//        formatSheets();
    }
    
    
    //Static Methods
    
    private static Budget loadBudget() {
        try {
            Budget budget = new Budget(BUDGET_TEST);
            return budget;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    private static void formatSheets() {
        int test = 5;
        File read = new File(BUDGET_TEST.getAbsolutePath().replaceAll("\\.xlsx$", (test + ".xlsx")));
        File write = new File(BUDGET_TEST.getAbsolutePath().replaceAll("\\.xlsx$", ((test + 1) + ".xlsx")));
        
        try {
            
            FileInputStream excelFile = new FileInputStream(read);
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet sheet;
            
            String maxi = "";
            String max = "";

//            sheet = workbook.getSheetAt(4);
//            List<Integer> cWidths = new ArrayList<>();
//            int cols = sheet.getRow(0).getLastCellNum();
//            for (int c = 0; c <= cols; c++) {
//                cWidths.add(sheet.getColumnWidth(c));
//            }
//            short height = sheet.getDefaultRowHeight();
//            int width = sheet.getDefaultColumnWidth();
            
            
            for (int i = 2; i <= 51; i++) {
//                System.out.println(i);
                sheet = workbook.getSheetAt(i);
                String name = sheet.getSheetName();
                
                Row row1 = sheet.getRow(0);
                if (row1.getPhysicalNumberOfCells() == 15) {
                    row1.createCell(3);
                    row1.createCell(9);
                }
                if (row1.getCell(3).getStringCellValue().isEmpty() && row1.getCell(9).getStringCellValue().isEmpty()) {
                    row1.getCell(3).setCellValue(" ");
                    row1.getCell(9).setCellValue(" ");
                } else if (!row1.getCell(3).getStringCellValue().equals(" ") || !row1.getCell(9).getStringCellValue().equals(" ")) {
                    int df = 3;
                }
                if (row1.getCell(1).getStringCellValue().isEmpty()) {
                    row1.getCell(1).setCellValue(" ");
                }
                
                Row row2 = sheet.getRow(1);
                if (row2 == null) {
                    sheet.createRow(1);
                }

//                sheet.setDefaultRowHeight(height);
//                sheet.setDefaultColumnWidth(width * 256);
                System.out.println(sheet.getSheetName() + " = " + row1.getPhysicalNumberOfCells());
                for (int c = 0; c < row1.getPhysicalNumberOfCells(); c++) {
                    sheet.setColumnWidth(c, 2973);
//                    sheet.autoSizeColumn(c, false);
                }
                if (row1.getCell(1).getStringCellValue().equals("Purchase")) {
                    sheet.setColumnWidth(1, 7598);
                }
                
                Iterator<Row> rows = sheet.rowIterator();
                while (rows.hasNext()) {
                    rows.next().setHeight((short) 292);
                }

//                Iterator<Row> iterator = sheet.iterator();
//                while (iterator.hasNext()) {
//
//                    Row currentRow = iterator.next();
//                    currentRow.setHeight(height);
//                    Cell cell = currentRow.getCell(1);
//                    if (cell == null || cell.getStringCellValue() == null) {
//                        continue;
//                    }
//                    
//                    String str = cell.getStringCellValue();
//                    if (str.length() > max.length()) {
//                        max = str;
//                        maxi = sheet.getSheetName();
//                    }
//                    
//                    String t = cell.getStringCellValue()
//                            .replace("’", "'")
//                            .replace("”", "\"")
//                            .replace("“", "\"");
//                    if (!cell.getStringCellValue().equals(t)) {
//                        cell.setCellValue(t);
//                        System.out.println(t);
//                    }
//                }
            }

//            System.out.println(max);
//            System.out.println(maxi);
            
            
            FileOutputStream out = new FileOutputStream(write);
            workbook.write(out);
            workbook.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
