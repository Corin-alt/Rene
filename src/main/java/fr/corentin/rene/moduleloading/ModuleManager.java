package fr.corentin.rene.moduleloading;

import fr.corentin.rene.Rene;
import fr.corentin.rene.moduleloading.parent.AModule;
import fr.corentin.rene.moduleloading.parent.AModuleConfiguration;
import org.reflections.Reflections;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ModuleManager {
    private static ModuleManager instance;
    private final Map<Class<? extends AModule>, ModuleData> modulesDatas;

    private final ArrayList<Class<? extends AModule>> loadedModules;

    private final Logger logger;

    private ModuleManager() {
        logger = Rene.getInstance().getLogger();

        loadedModules = new ArrayList<>();
        modulesDatas = new HashMap<>();
    }

    private AModule instantiateModule(Class<? extends AModule> clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return clazz.getDeclaredConstructor().newInstance();
    }

    private boolean enableModule(AModule module) {
        AModuleConfiguration moduleConfiguration;
        try {
            moduleConfiguration = module.getConfigClass().getDeclaredConstructor().newInstance();
            ModuleData moduleData = new ModuleData(module, moduleConfiguration);
            modulesDatas.put(module.getClass(), moduleData);
            if (moduleData.isEnabled()) {
                module.enable();
                return true;
            }
            return false;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            logger.error("Could not instantiate module's {} configuration", module.getClass().getSimpleName());
        } catch (Throwable throwable) {
            logger.error("Could not enable {}", module.getClass().getSimpleName(), throwable);
        }
        return false;
    }

    public void enableModules() {
        Reflections reflections = new Reflections("fr.corentin.rene.modules");
        Set<Class<? extends AModule>> classes = reflections.getSubTypesOf(AModule.class);

        Map<Class<? extends AModule>, AModule> modulePerClasses = new HashMap<>();

        classes.forEach(clazz -> {
            try {
                AModule module = instantiateModule(clazz);
                modulePerClasses.put(clazz, module);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                logger.error("Could not instantiate module from class {}", clazz.getSimpleName());
            }
        });

        modulePerClasses.forEach((aClass, aModule) -> {
            if (!enableModule(aModule)) {
                logger.error("{} could not be enabled", aModule.getClass().getSimpleName());
            } else {
                logger.info("{} enabled!", aModule.getClass().getSimpleName());
                loadedModules.add(aClass);
            }
        });
    }

    public Map<Class<? extends AModule>, ModuleData> getModulesDatas() {
        return modulesDatas;
    }

    public static ModuleManager getInstance() {
        if (instance == null) {
            instance = new ModuleManager();
        }
        return instance;
    }
}
