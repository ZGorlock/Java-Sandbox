/*
 * File:    Drive.java
 * Package: main.util
 * Author:  Zachary Gill
 */

package main.util;

import java.io.File;

public enum Drive {
    
    //Values
    
    BOOT,
    GAMES,
    STORAGE,
    CODING,
    VIRTUAL_MACHINES,
    WORK,
    BACKUP,
    RECOVERY,
    EXTERNAL_BACKUP;
    
    
    //Fields
    
    public final char driveLetter;
    
    public final File drive;
    
    
    //Constructors
    
    Drive() {
        this.driveLetter = (char) (ordinal() + 'C');
        this.drive = new File(driveLetter + ":/");
    }
    
    
    //Methods
    
    public boolean available() {
        return drive.exists();
    }
    
}
