package fr.corentin.rene.moduleloading.parent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class AModuleConfiguration {
    public static final String FILENAME = "config";
    private static final Logger logger = LoggerFactory.getLogger(AModuleConfiguration.class);
    private File dataFolder;

    @Expose
    protected boolean enabled = true;

    public void setDataFolder(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void save() {
        try {
            String jsonFilePath = dataFolder.getCanonicalFile() + File.separator + FILENAME + ".json";
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
            try (Writer writer = Files.newBufferedWriter(Paths.get(jsonFilePath))) {
                gson.toJson(this, writer);
            }
        } catch (IOException e) {
            logger.error("Failed to save module configuration", e);
        }
    }
}
