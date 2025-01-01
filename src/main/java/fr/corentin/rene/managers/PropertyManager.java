package fr.corentin.rene.managers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyManager {
    private final Properties properties = new Properties();

    public void loadProperties(String filename) throws IOException {
        try (FileInputStream input = new FileInputStream(filename)) {
            properties.load(input);
        }
    }

    public void saveProperties(String filename) throws IOException {
        try (FileOutputStream output = new FileOutputStream(filename)) {
            properties.store(output, null);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}
