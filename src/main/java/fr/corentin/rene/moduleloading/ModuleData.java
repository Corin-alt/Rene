package fr.corentin.rene.moduleloading;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.corentin.rene.Rene;
import fr.corentin.rene.moduleloading.parent.AModule;
import fr.corentin.rene.moduleloading.parent.AModuleConfiguration;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ModuleData {
    private AModuleConfiguration configuration;
    private final AModule module;
    private final Logger logger;

    public ModuleData(AModule module, AModuleConfiguration moduleConfiguration) {
        this.module = module;
        this.configuration = moduleConfiguration;
        this.logger = Rene.getInstance().getLogger();
        getOrCreateDataFolder();
    }

    public boolean isEnabled() {
        return configuration.isEnabled();
    }

    public AModuleConfiguration getConfiguration() {
        return configuration;
    }

    public AModule getModule() {
        return module;
    }

    public void getOrCreateDataFolder() {
        String dataFolderPath = Rene.getInstance().getDataFolder().getPath() + File.separator + module.getClass().getSimpleName();
        File dataFolder = new File(dataFolderPath);
        if (!dataFolder.exists()) {
            if (dataFolder.mkdirs()) {
                logger.info("Data folder created for module {}", module.getClass().getSimpleName());
                generateJsonConfigFile(dataFolder);
            } else {
                logger.warn("Can't create {} data folder", module.getClass().getSimpleName());
            }
        } else {
            generateJsonConfigFile(dataFolder);
        }
    }

    private void generateJsonConfigFile(File dataFolder) {
        try {
            File jsonConfigFile = new File(dataFolder.getCanonicalPath() + File.separator + AModuleConfiguration.FILENAME + ".json");
            if (jsonConfigFile.exists()) {
                Reader reader = Files.newBufferedReader(Paths.get(jsonConfigFile.getCanonicalPath()));
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                configuration = gson.fromJson(reader, configuration.getClass());
            }
            configuration.setDataFolder(dataFolder);
            configuration.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
