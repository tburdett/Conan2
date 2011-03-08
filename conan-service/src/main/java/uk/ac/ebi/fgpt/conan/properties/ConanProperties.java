package uk.ac.ebi.fgpt.conan.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * A singleton class that acts as a wrapper around a {@link java.util.Properties} object that contains all system-wide
 * conan properties.  All method calls are delegated to an internal Properties object.
 *
 * @author Tony Burdett
 * @date 04-Nov-2010
 */
public class ConanProperties {
    private static ConanProperties conanProperties = new ConanProperties();
    private static boolean initialized = false;

    public static ConanProperties getConanProperties() {
        return conanProperties;
    }

    public static int size() {
        init();
        return conanProperties.getProperties().size();
    }

    public static boolean isEmpty() {
        init();
        return conanProperties.getProperties().isEmpty();
    }

    public static Enumeration<Object> keys() {
        init();
        return conanProperties.getProperties().keys();
    }

    public static Enumeration<Object> elements() {
        init();
        return conanProperties.getProperties().elements();
    }

    public static boolean contains(Object value) {
        init();
        return conanProperties.getProperties().contains(value);
    }

    public static boolean containsValue(Object value) {
        init();
        return conanProperties.getProperties().containsValue(value);
    }

    public static boolean containsKey(Object key) {
        init();
        return conanProperties.getProperties().containsKey(key);
    }

    public static Object get(Object key) {
        init();
        return conanProperties.getProperties().get(key);
    }

    public static Object put(Object key, Object value) {
        init();
        return conanProperties.getProperties().put(key, value);
    }

    public static Object remove(Object key) {
        init();
        return conanProperties.getProperties().remove(key);
    }

    public static void putAll(Map<?, ?> t) {
        init();
        conanProperties.getProperties().putAll(t);
    }

    public static void clear() {
        init();
        conanProperties.getProperties().clear();
    }

    public static Set<Object> keySet() {
        init();
        return conanProperties.getProperties().keySet();
    }

    public static Set<Map.Entry<Object, Object>> entrySet() {
        init();
        return conanProperties.getProperties().entrySet();
    }

    public static Collection<Object> values() {
        init();
        return conanProperties.getProperties().values();
    }

    public static Enumeration<?> propertyNames() {
        init();
        return conanProperties.getProperties().propertyNames();
    }

    public static String getProperty(String key, String defaultValue) {
        init();
        return conanProperties.getProperties().getProperty(key, defaultValue);
    }

    public static String getProperty(String key) {
        init();
        return conanProperties.getProperties().getProperty(key);
    }

    public static Set<String> stringPropertyNames() {
        init();
        return conanProperties.getProperties().stringPropertyNames();
    }

    private static void init() {
        if (!initialized) {
            conanProperties.loadProperties();
            initialized = true;
        }
    }

    private File conanPropertiesFile;
    private Properties properties;
    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public void loadProperties() {
        this.properties = new Properties();
        try {
            properties.load(new BufferedInputStream(new FileInputStream(conanPropertiesFile)));
            getLog().info("Loaded Conan properties from " + conanPropertiesFile.getAbsolutePath());
        }
        catch (IOException e) {
            getLog().error("Could not read from file " + conanPropertiesFile.getAbsolutePath() + ": " +
                    "properties will not be loaded");
            throw new RuntimeException("Could not read from file " + conanPropertiesFile.getAbsolutePath() + ": " +
                    "properties will not be loaded", e);
        }
    }

    public void setPropertiesFile(File conanPropertiesFile) {
        this.conanPropertiesFile = conanPropertiesFile;
    }

    private Properties getProperties() {
        return properties;
    }
}
