/*
 * File:    DatabaseManager.java
 * Package: dla.manager
 * Author:  Zachary Gill
 */

package main.java;

import java.lang.reflect.InvocationTargetException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Properties;

/**
 * Manages database access within the system.
 */
public class DatabaseManager {
    
    //Constants
    
    /**
     * The framework to use for database access.
     */
    public static final String FRAMEWORK = "embedded";
    
    /**
     * The driver to use for database access.
     */
    public static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    
    /**
     * The protocol to use for database access.
     */
    public static final String PROTOCOL = "jdbc:derby:";
    
    
    //Constructors
    
    /**
     * The private constructor for a DatabaseManager.
     */
    public DatabaseManager() {
    }
    
    
    //Methods
    
    public final void setup() {
        try {
            Class.forName(DRIVER).getConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public final void shutdown() {
        if (Objects.equals("embedded", FRAMEWORK)) {
            try {
                DriverManager.getConnection(PROTOCOL + ";shutdown=true");
            } catch (SQLException ignored) {
            }
        }
    }
    
    
    //Functions
    
    public static Connection connect(String db, String user, String pass, boolean autoCommit) {
        Properties props = new Properties();
        props.put("user", user);
        props.put("password", pass);
        
        Connection conn;
        try {
            conn = DriverManager.getConnection(DatabaseManager.PROTOCOL +
                    db + ";create=true", props);
            conn.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        
        return conn;
    }
    
    public static Connection connect(String db, String user, String pass) {
        return connect(db, user, pass, true);
    }
    
    public static Connection connect(String db, boolean autoCommit) {
        return connect(db, "admin", "admin", autoCommit);
    }
    
    public static Connection connect(String db) {
        return connect(db, "admin", "admin");
    }
    
    public static boolean disconnect(Connection conn) {
        try {
            conn.close();
        } catch (SQLException ignored) {
            return true; //this should always throw an exception
        }
        return false;
    }
    
    public static boolean isConnected(Connection conn) {
        try {
            return !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean commitChanges(Connection conn) {
        try {
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public static boolean rollbackChanges(Connection conn) {
        try {
            if (!conn.getAutoCommit()) {
                conn.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public static boolean setReadOnly(Connection conn, boolean readOnly) {
        try {
            conn.setReadOnly(readOnly);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public static boolean isReadOnly(Connection conn) {
        try {
            return conn.isReadOnly();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static Statement getStatement(Connection conn) {
        Statement s;
        try {
            s = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return s;
    }
    
    public static PreparedStatement getPreparedStatement(Connection conn, String sql) {
        PreparedStatement ps;
        try {
            ps = conn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return ps;
    }
    
    public static CallableStatement getCallableStatement(Connection conn, String sql) {
        CallableStatement cs;
        try {
            cs = conn.prepareCall(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return cs;
    }
    
    public static boolean executeSql(Statement s, String sql) {
        try {
            s.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public static ResultSet querySql(Statement s, String sql) {
        ResultSet rs;
        try {
            rs = s.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return rs;
    }
    
    public static int updateSql(Statement s, String sql) {
        try {
            return s.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    public static boolean closeStatement(Statement s) {
        try {
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
}
