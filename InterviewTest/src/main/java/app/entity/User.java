package app.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Defines a user entity.
 */
@Entity
public class User {
    
    //Fields
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String userName;
    
    private String password;
    
    
    //Constructors
    
    /**
     * The protected constructor for a user entity for use by JPA.
     */
    protected User() {
    }
    
    /**
     * Creates a new user.
     *
     * @param userName The username of the user.
     * @param password The password of the user.
     */
    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
    
    
    //Getters / Setters
    
    /**
     * Returns the id of the user.
     *
     * @return The id of the user.
     */
    public Long getId() {
        return id;
    }
    
    /**
     * Sets the id of the user.
     *
     * @param id The id of the user.
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Returns the username of the user.
     *
     * @return The username of the user.
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * Sets the username of the user.
     *
     * @param userName The username of the user.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    /**
     * Returns the password of the user.
     *
     * @return The password of the user.
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Sets the password of the user.
     *
     * @param password The password of the user.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
