/*
 * File:    SQLiteDemo.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.sqlite.SQLiteConfig;

public class SQLiteDemo {
    
    //Constants
    
    public static final String SQLITE_PROTOCOL = "jdbc:sqlite";
    
    
    //Static Fields
    
    private static final File db = new File("data", "test.sqlite");
    
    
    //Main Methods
    
    public static void main(String[] args) throws Exception {
        String connectionString = SQLITE_PROTOCOL + ':' + db.getPath();
        
        SQLiteConfig config = new SQLiteConfig();
        
        try (Connection sqliteDb = DriverManager.getConnection(connectionString, new Properties());
             Statement sqlite = sqliteDb.createStatement()) {
            
            sqlite.setQueryTimeout(30);
            
            //create
            sqlite.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS record (" +
                            "id INTEGER PRIMARY KEY ASC, " +
                            "timestamp NOT NULL" +
                            ")"
            );
            
            //insert
            sqlite.executeUpdate(
                    "INSERT INTO record VALUES(NULL, datetime())"
            );
            
            //delete
            sqlite.executeUpdate(
                    "DELETE FROM record " +
                            "WHERE (timestamp > datetime('now','-5 minute')) " +
                            "AND (timestamp < datetime('now','-1 minute')) "
                    //"ORDER BY timestamp " +
                    //"LIMIT 1 OFFSET 0"
            );
            
            //select records
            ResultSet results = sqlite.executeQuery(
                    "SELECT * FROM record " +
                            "ORDER BY id DESC"
            );
            
            while (results.next()) {
                System.out.println("id        = " + results.getInt("id"));
                System.out.println("timestamp = " + results.getString("timestamp"));
                System.out.println();
            }
            results.close();
            
            //sqlite.close();
            //sqliteDb.close();
            
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }
    
}
