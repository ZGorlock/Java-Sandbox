package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main application.
 */
@SpringBootApplication
public class Application {
    
    //Logger
    
    //Set logback configuration file
    static {
        System.setProperty("logback.configurationFile", "resources/logback.xml");
    }
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    
    
    //Methods
    
    /**
     * The main method.
     *
     * @param args Arguments to the main method.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
}
