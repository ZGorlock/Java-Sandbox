/*
 * File:    Project.java
 * Package: commons.access
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.access;

import java.io.File;
import java.util.stream.Stream;

import commons.object.string.EntityStringUtility;
import commons.object.string.StringUtility;

/**
 * Defines directories for the project.
 */
public final class Project {
    
    //Constants
    
    /**
     * The project directory.
     */
    public static final File PROJECT_DIR = new File("").getAbsoluteFile();
    
    /**
     * The local project source directory.
     */
    public static final File SOURCE = new File("src");
    
    /**
     * The project source directory.
     */
    public static final File SOURCE_DIR = new File(PROJECT_DIR, SOURCE.getPath());
    
    /**
     * The local project test directory.
     */
    public static final File TEST = new File("test");
    
    /**
     * The project test directory.
     */
    public static final File TEST_DIR = new File(PROJECT_DIR, TEST.getPath());
    
    /**
     * The project data directory.
     */
    public static final File DATA_DIR = new File(PROJECT_DIR, "data");
    
    /**
     * The project resources directory.
     */
    public static final File RESOURCES_DIR = new File(PROJECT_DIR, "resources");
    
    /**
     * The project test resources directory.
     */
    public static final File TEST_RESOURCES_DIR = new File(PROJECT_DIR, "test-resources");
    
    /**
     * The project output directory.
     */
    public static final File OUTPUT_DIR = new File(PROJECT_DIR, "bin");
    
    /**
     * The project source classes directory.
     */
    public static final File SOURCE_CLASSES_DIR = new File(OUTPUT_DIR, "classes");
    
    /**
     * The project test classes directory.
     */
    public static final File TEST_CLASSES_DIR = new File(OUTPUT_DIR, "test-classes");
    
    /**
     * The project log directory.
     */
    public static final File LOG_DIR = new File(PROJECT_DIR, "log");
    
    /**
     * The project temporary directory.
     */
    public static final File TMP_DIR = new File(PROJECT_DIR, "tmp");
    
    
    //Static Methods
    
    /**
     * Ensures the project directories exist and creates them if needed.
     *
     * @return Whether the project directories are successfully initialized.
     */
    public static boolean initializeProjectDirectories() {
        return Filesystem.clearDirectory(TMP_DIR) &&
                Stream.of(
                                SOURCE, TEST,
                                DATA_DIR, RESOURCES_DIR, TEST_RESOURCES_DIR,
                                OUTPUT_DIR, SOURCE_CLASSES_DIR, TEST_CLASSES_DIR,
                                LOG_DIR, TMP_DIR)
                        .map(Filesystem::createDirectory)
                        .reduce(true, Boolean::logicalAnd);
    }
    
    /**
     * Returns the source directory for a particular class.
     *
     * @param clazz The class.
     * @return The source directory for the specified class.
     * @see #classDir(File, String, Class, boolean)
     */
    public static File sourceDir(Class<?> clazz) {
        return classDir(SOURCE, "", clazz, false);
    }
    
    /**
     * Returns the test directory for a particular class.
     *
     * @param clazz The class.
     * @return The test directory for the specified class.
     * @see #classDir(File, String, Class, boolean)
     */
    public static File testDir(Class<?> clazz) {
        return classDir(TEST, "", clazz, false);
    }
    
    /**
     * Returns the data directory for a particular class.
     *
     * @param clazz  The class.
     * @param prefix The prefix within the project data directory.
     * @return The data directory for the specified class.
     * @see #classDir(File, String, Class, boolean)
     */
    public static File dataDir(Class<?> clazz, String prefix) {
        return classDir(DATA_DIR, prefix, clazz, true);
    }
    
    /**
     * Returns the data directory for a particular class.
     *
     * @param clazz The class.
     * @return The data directory for the specified class.
     * @see #dataDir(Class, String)
     */
    public static File dataDir(Class<?> clazz) {
        return dataDir(clazz, "");
    }
    
    /**
     * Returns the resources directory for a particular class.
     *
     * @param clazz  The class.
     * @param prefix The prefix within the project resources directory.
     * @return The resources directory for the specified class.
     * @see #classDir(File, String, Class, boolean)
     */
    public static File resourcesDir(Class<?> clazz, String prefix) {
        return classDir(RESOURCES_DIR, prefix, clazz, true);
    }
    
    /**
     * Returns the resources directory for a particular class.
     *
     * @param clazz The class.
     * @return The resources directory for the specified class.
     * @see #resourcesDir(Class, String)
     */
    public static File resourcesDir(Class<?> clazz) {
        return resourcesDir(clazz, "");
    }
    
    /**
     * Returns the test resources directory for a particular class.
     *
     * @param clazz  The class.
     * @param prefix The prefix within the project test resources directory.
     * @return The test resources directory for the specified class.
     * @see #classDir(File, String, Class, boolean)
     */
    public static File testResourcesDir(Class<?> clazz, String prefix) {
        return classDir(TEST_RESOURCES_DIR, prefix, clazz, true);
    }
    
    /**
     * Returns the test resources directory for a particular class.
     *
     * @param clazz The class.
     * @return The test resources directory for the specified class.
     * @see #testResourcesDir(Class, String)
     */
    public static File testResourcesDir(Class<?> clazz) {
        return testResourcesDir(clazz, "");
    }
    
    /**
     * Returns the source classes directory for a particular class.
     *
     * @param clazz The class.
     * @return The source classes directory for the specified class.
     * @see #classDir(File, String, Class, boolean)
     */
    public static File sourceClassesDir(Class<?> clazz) {
        return classDir(SOURCE_CLASSES_DIR, "", clazz, false);
    }
    
    /**
     * Returns the test classes directory for a particular class.
     *
     * @param clazz The class.
     * @return The test classes directory for the specified class.
     * @see #classDir(File, String, Class, boolean)
     */
    public static File testClassesDir(Class<?> clazz) {
        return classDir(TEST_CLASSES_DIR, "", clazz, false);
    }
    
    /**
     * Returns the log directory for a particular class.
     *
     * @param clazz  The class.
     * @param prefix The prefix within the project log directory.
     * @return The log directory for the specified class.
     * @see #classDir(File, String, Class, boolean)
     */
    public static File logDir(Class<?> clazz, String prefix) {
        return classDir(LOG_DIR, prefix, clazz, true);
    }
    
    /**
     * Returns the log directory for a particular class.
     *
     * @param clazz The class.
     * @return The log directory for the specified class.
     * @see #logDir(Class, String)
     */
    public static File logDir(Class<?> clazz) {
        return logDir(clazz, "");
    }
    
    /**
     * Returns the directory for a particular class.
     *
     * @param baseDir    The base directory.
     * @param prefix     The prefix within the base directory
     * @param clazz      The class.
     * @param classOwned Whether the class directory is private to the class or not.
     * @return The directory for the specified class.
     */
    private static File classDir(File baseDir, String prefix, Class<?> clazz, boolean classOwned) {
        return new File(Filesystem.generatePath(
                StringUtility.fileString(baseDir),
                StringUtility.fixFileSeparators(prefix),
                Filesystem.generatePath(clazz.getPackage().getName().split("\\.")),
                (classOwned ? EntityStringUtility.simpleClassString(clazz) : "")));
    }
    
}
