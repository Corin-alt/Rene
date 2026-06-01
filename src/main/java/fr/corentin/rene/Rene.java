package fr.corentin.rene;

import fr.corentin.rene.managers.CommandManager;
import fr.corentin.rene.managers.DatabaseManager;
import fr.corentin.rene.managers.EventManager;
import fr.corentin.rene.managers.PropertyManager;
import fr.corentin.rene.moduleloading.ModuleManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Rene {
    public static final String DATA_FOLDER_NAME = "rene-data";
    private static final String DEFAULT_PREFIX = "?";
    private static Rene instance;
    private static final Logger logger = LoggerFactory.getLogger(Rene.class);

    private File dataFolder;
    private ModuleManager moduleManager;
    private final PropertyManager propertyManager;
    private DatabaseManager databaseManager;
    private CommandManager commandManager;
    private EventManager eventManager;
    private Instant startTime;

    private final Map<String, String> prefixCache;

    private JDA jda;

    public Rene() {
        setupDataFolder();
        this.propertyManager = new PropertyManager();
        prefixCache = new ConcurrentHashMap<>();
    }

    private void setupDataFolder() {
        String currentFolderPath = System.getProperty("user.dir");

        this.dataFolder = new File(currentFolderPath + File.separator + DATA_FOLDER_NAME);
        if (!dataFolder.exists() && !dataFolder.mkdir()) {
            logger.error("Could not create bot data folder.");
        }
    }


    private String loadToken() {
        // D'abord, essayer de charger le token depuis la variable d'environnement
        String token = System.getenv("BOT_TOKEN");
        if (token != null && !token.trim().isEmpty()) {
            logger.info("Using bot token from environment variable");
            return token;
        }

        // Si pas de variable d'environnement, essayer de charger depuis le fichier de propriétés
        logger.info("Environment variable BOT_TOKEN not found, trying properties file");
        try {
            propertyManager.loadProperties(dataFolder.getPath() + File.separator + "bot.properties");
            token = propertyManager.getProperty("token");
        } catch (Exception e) {
            logger.error("Could not load bot properties from file.", e);
        }

        if (token == null || token.trim().isEmpty()) {
            logger.error("Bot token is not set! Please either:");
            logger.error("1. Set the BOT_TOKEN environment variable, or");
            logger.error("2. Add your token to the bot.properties file");
            System.exit(1);
        }

        return token;
    }


    private void createJda() throws InterruptedException {
        String token = loadToken();

        try {
            // Initialize JDA
            this.jda = JDABuilder.createDefault(token)
                    .disableCache(CacheFlag.CLIENT_STATUS, CacheFlag.ACTIVITY)
                    .enableIntents(List.of(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS))
                    .build().awaitStatus(JDA.Status.CONNECTED);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Disallowed intent")) {
                logger.error("ERROR: You need to enable privileged intents in the Discord Developer Portal!");
                logger.error("Visit https://discord.com/developers/applications");
                logger.error("Select your bot, go to the Bot tab, and enable MESSAGE CONTENT INTENT and SERVER MEMBERS INTENT");
                System.exit(1);
            } else {
                throw e;
            }
        }
    }

    private void start() {
        registerManagers();

        moduleManager.enableModules();

        logger.info("Rene is running!");
        jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB,
                Activity.playing("rien"), false);
        startTime = Instant.now();
    }

    private void registerManagers() {
        logger.info("Registering managers..");
        this.moduleManager = ModuleManager.getInstance();
        this.databaseManager = DatabaseManager.getInstance();
        this.commandManager = CommandManager.getInstance();
        this.eventManager = EventManager.getInstance();
        this.eventManager.registerListeners();
    }

    public String getCommandPrefix(String guildId) {
        if (!prefixCache.containsKey(guildId)) {
            String prefix = databaseManager.getCommandPrefix(guildId);
            if (prefix == null || prefix.isEmpty()) {
                prefix = DEFAULT_PREFIX;
                setCommandPrefix(guildId, prefix);
            }
            prefixCache.put(guildId, prefix);
        }
        return prefixCache.get(guildId);
    }

    public void setCommandPrefix(String guildId, String prefix) {
        databaseManager.setCommandPrefix(guildId, prefix);
        prefixCache.put(guildId, prefix);
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public Logger getLogger() {
        return logger;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public JDA getJda() {
        return this.jda;
    }

    public static Rene getInstance() {
        if (instance == null) {
            instance = new Rene();
        }
        return instance;
    }

    public static void main(String[] args) {
        try {
            Rene rene = Rene.getInstance();
            rene.createJda();
            rene.start();
        } catch (InterruptedException ignored) {
            logger.error("Could not start bot!");
            Thread.currentThread().interrupt();
        }
    }
}