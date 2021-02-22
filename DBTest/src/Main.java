/*
 * File:    Main.java
 * Package:
 * Author:  Zachary Gill
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    
    public static void main(String args[]) {
        
        
        DatabaseManager dbm = new DatabaseManager();
        dbm.setup();
        
        Connection c = DatabaseManager.connect("resources/DLA/Module1/testdb");
        Statement s = DatabaseManager.getStatement(c);
        DatabaseManager.executeSql(s, "DROP TABLE x");
        DatabaseManager.executeSql(s, "CREATE TABLE x (id INTEGER NOT NULL PRIMARY KEY, name VARCHAR(32))");
        DatabaseManager.updateSql(s, "INSERT INTO x VALUES (1, 'Zack')");
        DatabaseManager.updateSql(s, "INSERT INTO x VALUES (2, 'Levi')");
        DatabaseManager.updateSql(s, "INSERT INTO x VALUES (3, 'Dalton')");
        DatabaseManager.updateSql(s, "INSERT INTO x VALUES (4, 'Holly')");
        
        ResultSet r = DatabaseManager.querySql(s, "SELECT * FROM x");
        
        try {
            while (r.next()) {
                String name = r.getString("id");
                System.out.println(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        DatabaseManager.closeStatement(s);
        DatabaseManager.disconnect(c);
        dbm.shutdown();
    }
    
}
