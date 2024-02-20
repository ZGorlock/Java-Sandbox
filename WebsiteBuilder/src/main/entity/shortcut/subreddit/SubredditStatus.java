/*
 * File:    SubredditStatus.java
 * Package: main.entity.shortcut.subreddit
 * Author:  Zachary Gill
 */

package main.entity.shortcut.subreddit;

public class SubredditStatus {
    
    //Enums
    
    public enum Status {
        BANNED,
        PRIVATE,
        ALIVE,
        UNKNOWN
    }
    
    
    //Fields
    
    protected Status status;
    
    
    //Constructors
    
    protected SubredditStatus(Subreddit subredditEntity) {
        this.status = subredditEntity.getAttributes().isBanned() ? Status.BANNED :
                      subredditEntity.getAttributes().isPrivate() ? Status.PRIVATE :
                      subredditEntity.getAttributes().isAlive() ? Status.ALIVE :
                      Status.UNKNOWN;
    }
    
    
    //Getters
    
    public Status getStatus() {
        return status;
    }
    
    public boolean isBanned() {
        return (status == Status.BANNED);
    }
    
    public boolean isPrivate() {
        return (status == Status.PRIVATE);
    }
    
    public boolean isAlive() {
        return (status == Status.ALIVE);
    }
    
    public boolean isUnknown() {
        return (status == Status.UNKNOWN);
    }
    
}
