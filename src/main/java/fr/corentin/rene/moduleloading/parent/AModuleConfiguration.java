package fr.corentin.rene.moduleloading.parent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class AModuleConfiguration {
    public static final String FILENAME = "config";
    private File dataFolder;

    /**
     * Each config file will have an "enabled" field
     * this value will be set to "true" by default
     * You can add more fields to your config file by adding
     * them to your class with the @Expose annotation
     */
    @Expose
    protected boolean enabled = true;

    public void setDataFolder(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    /**
     * Returns the value corresponding to the one in the json config file
     *
     * @return true is enabled, else false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Saves the config to the config file
     */
    public void save() {
        try {
            String jsonFilePath = dataFolder.getCanonicalFile() + File.separator + FILENAME + ".json";
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
            Writer writer = Files.newBufferedWriter(Paths.get(jsonFilePath));
            gson.toJson(this, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}